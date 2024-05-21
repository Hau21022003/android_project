package com.hcmute.instagram.Messages.Floatingview;


import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Build;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.hcmute.instagram.Messages.Adapter.MessageAdapter;
import com.hcmute.instagram.Messages.ChatActivity;
import com.hcmute.instagram.Messages.MessageActivity;
import com.hcmute.instagram.Messages.Model.Chat;
import com.hcmute.instagram.Messages.Notification.APIService;
import com.hcmute.instagram.Messages.Notification.Client;
import com.hcmute.instagram.Messages.Notification.Data;
import com.hcmute.instagram.Messages.Notification.MyResponse;
import com.hcmute.instagram.Messages.Notification.NotificationSender;
import com.hcmute.instagram.Messages.Notification.Token;
import com.hcmute.instagram.R;
import com.hcmute.instagram.Search.UserSearchProfileActivity;
import com.hcmute.instagram.models.Users;
import com.zegocloud.uikit.prebuilt.call.invite.widget.ZegoSendCallInvitationButton;
import com.zegocloud.uikit.service.defines.ZegoUIKitUser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatFloatingWindow {
    private final static String TAG = "ChatFloatingWindow";
    CircleImageView profile_image;
    TextView username, fullname;

    FirebaseUser fuser;
    DatabaseReference reference;

    private Toolbar toolbar;
    Intent intent;
    boolean notify = false;

    TextView btn_send;
    ImageView btn_like;
    EditText text_send;

    MessageAdapter messageAdapter;
    List<Chat> mchat;

    RecyclerView recyclerView;
    String userid;
    private String userUsername;

    private APIService apiService;
    private ZegoSendCallInvitationButton videoCallBtn;
    private ZegoSendCallInvitationButton audioCallBtn;


    private Context context;
    private WindowManager windowManager;
    private View floatView;
    private WindowManager.LayoutParams layoutParams;
    private int lastX;
    private int lastY;
    private int firstX;
    private int firstY;
    private boolean isShowing;
    private boolean touchConsumedByMove;

    private ImageView imgFloatingAvatar;
    private RelativeLayout floatingChatLayout;
    private ImageView imageCloseFloatingView;

    public ChatFloatingWindow(Context context, String userid) {
        this.context = context;
        this.userid = userid;

        floatView = LayoutInflater.from(context).inflate(R.layout.fragment_floating_chat, null);
        imgFloatingAvatar = floatView.findViewById(R.id.imgFloatingAvatar);
        floatingChatLayout = floatView.findViewById(R.id.floatingChatLayout);
        imageCloseFloatingView = floatView.findViewById(R.id.closeFloatingView);


        layoutParams = new WindowManager.LayoutParams();
        layoutParams.format = PixelFormat.TRANSLUCENT;
        layoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            layoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            //noinspection deprecation
            layoutParams.type = WindowManager.LayoutParams.TYPE_TOAST;
        }
        layoutParams.gravity = Gravity.CENTER;
        layoutParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
        layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;

        floatView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                int totalDeltaX = lastX - firstX;
                int totalDeltaY = lastY - firstY;

                switch (event.getActionMasked()) {
                    case MotionEvent.ACTION_DOWN:
                        lastX = (int) event.getRawX();
                        lastY = (int) event.getRawY();
                        firstX = lastX;
                        firstY = lastY;
                        break;
                    case MotionEvent.ACTION_UP:
                        if(event.getEventTime() - event.getDownTime() <= 200) { // case or when statement of action Touch listener
                            view.performClick();
                            if (floatingChatLayout.getVisibility() == View.GONE) {
                                layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
                                layoutParams.height = WindowManager.LayoutParams.MATCH_PARENT;
                                getWindowManager().updateViewLayout(floatView, layoutParams);
                                floatingChatLayout.setVisibility(View.VISIBLE);
                            } else {
                                layoutParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
                                layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
                                getWindowManager().updateViewLayout(floatView, layoutParams);
                                floatingChatLayout.setVisibility(View.GONE);
                            }
                        }
                        break;
                    case MotionEvent.ACTION_MOVE:
                        int deltaX = (int) event.getRawX() - lastX;
                        int deltaY = (int) event.getRawY() - lastY;
                        lastX = (int) event.getRawX();
                        lastY = (int) event.getRawY();
                        if (Math.abs(totalDeltaX) >= 5 || Math.abs(totalDeltaY) >= 5) {
                            if (event.getPointerCount() == 1) {
                                layoutParams.x += deltaX;
                                layoutParams.y += deltaY;
                                touchConsumedByMove = true;
                                getWindowManager().updateViewLayout(floatView, layoutParams);
                            } else {
                                touchConsumedByMove = false;
                            }
                        } else {
                            touchConsumedByMove = false;
                        }
                        break;
                    default:
                        break;
                }
                return touchConsumedByMove;
            }
        });

        //chat activity

        username = floatView.findViewById(R.id.MessageActivity_userName);
        fullname = floatView.findViewById(R.id.MessageActivity_fullname);
        profile_image = (CircleImageView) floatView.findViewById(R.id.MessageActivity_user_img);
        btn_send = floatView.findViewById(R.id.MessageActivity_btn_send);
        text_send = floatView.findViewById(R.id.MessageActivity_text_send);
        btn_like = floatView.findViewById(R.id.MessageActivity_likeBtn);
        videoCallBtn = floatView.findViewById(R.id.video_call_btn);
        audioCallBtn = floatView.findViewById(R.id.audio_call_btn);


        text_send.requestFocus();
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(text_send, InputMethodManager.SHOW_IMPLICIT);

        videoCallBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startVideoCall(userid, userUsername);
            }
        });
        audioCallBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startAudioCall(userid, userUsername);
            }
        });

        apiService = Client.getClient("https://fcm.googleapis.com/").create(APIService.class);

        recyclerView = floatView.findViewById(R.id.MessageActivity_recyclerView);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        fuser = FirebaseAuth.getInstance().getCurrentUser();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        DocumentReference docRef = db.collection("Users").document(userid);

        docRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot snapshot,
                                @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w("Fail", "Listen failed.", e);
                    return;
                }

                if (snapshot != null && snapshot.exists()) {
                    // Convert the document snapshot to a Users object
                    Users user = snapshot.toObject(Users.class);
                    if (user != null) {
                        // Set UI elements with user data
                        username.setText(user.getUsername());
                        fullname.setText(user.getFullName());

                        userUsername = user.getUsername();
                        // Use Glide or another library to load profile image from URL
                        Glide.with(context)
                                .load(user.getProfilePhoto())
                                .into(profile_image);

                        Glide.with(context)
                                .load(user.getProfilePhoto())
                                .into(imgFloatingAvatar);

                        username.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                accessUserProfile(user.getUser_id());
                            }
                        });

                        fullname.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                accessUserProfile(user.getUser_id());
                            }
                        });


                        profile_image.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                accessUserProfile(user.getUser_id());
                            }
                        });


                        startAudioCall(userid, user.getFullName());
                        startVideoCall(userid, user.getFullName());


                        // Call method to read messages (assuming fuser is accessible)
                        readMessages(fuser.getUid(), userid, user.getProfilePhoto());
                    }
                } else {
                    Log.d("OK", "Current data: null");
                }
            }
        });
        text_send.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (text_send.getText().toString().trim().isEmpty()){
                    btn_like.setVisibility(View.VISIBLE);
                    btn_send.setVisibility(View.GONE);
                }else {
                    btn_send.setVisibility(View.VISIBLE);
                    btn_like.setVisibility(View.GONE);
                }
            }
        });

        btn_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                notify = true;
                String msg = text_send.getText().toString().trim();
                if (!msg.equals("")) {
                    sendMessage(fuser.getUid(), userid, msg);

                } else {
                    Toast.makeText(context, "You can't send empty message", Toast.LENGTH_SHORT).show();
                }
                text_send.setText("");
                closeKeyboard();
            }
        });
        btn_like.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage(fuser.getUid(), userid, "\uD83D\uDC4D");
            }
        });

        imageCloseFloatingView.setOnClickListener(v -> dismiss());
        //floatView.findViewById(R.id.closeImageButton).setOnClickListener(v -> dismiss());
    }

    private void accessUserProfile(String id) {
        Intent intent=new Intent((MessageActivity)context, UserSearchProfileActivity.class);
        intent.putExtra("SearchedUserid",id);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    private void sendMessage(String sender, final String receiver, String message) {
        // Get a Firestore instance
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Create a new HashMap to store chat message data
        HashMap<String, Object> chatMessage = new HashMap<>();
        chatMessage.put("sender", sender);
        chatMessage.put("receiver", receiver);
        chatMessage.put("message", message);
        chatMessage.put("isseen", false);

        // Add a timestamp field with the current timestamp
        chatMessage.put("timestamp", FieldValue.serverTimestamp());
        Log.i("timee ", FieldValue.serverTimestamp().toString());
        // Reference to the "Chats" collection
        CollectionReference chatsRef = db.collection("Chats");

        // Add the chat message to the "Chats" collection
        chatsRef.add(chatMessage)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        // Successfully added chat message
                        Log.d("SENDed", "Chat message added with ID: " + documentReference.getId());

//                        ChatsFragment chatsFragment = new ChatsFragment();
//                        chatsFragment.readChats();
                        // Notify the receiver if needed
                        if (notify) {
                            // sendNotification(receiver, message);
                        }
                        notify = false;
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Failed to add chat message
                        Log.w("ERROR", "Error adding chat message", e);
                    }
                });
    }



    private void sendNotification(String receiver, final String username, final String message) {

        DatabaseReference tokens = FirebaseDatabase.getInstance().getReference("Tokens");
        Query query = tokens.orderByKey().equalTo(receiver);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Token token = snapshot.getValue(Token.class);
                    Data data = new Data(fuser.getUid(), R.mipmap.ic_launcher_instaclone, username + ":" + message, "New Message",
                            userid);

                    NotificationSender sender = new NotificationSender(data, token.getToken());

                    apiService.sendNotification(sender).enqueue(new Callback<MyResponse>() {
                        @Override
                        public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {
                            if (response.code() == 200) {
                                if (response.body().success != 1) {
                                    Log.d(TAG, "onResponse: ");
                                }
                            }
                        }

                        @Override
                        public void onFailure(Call<MyResponse> call, Throwable t) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void readMessages(final String myid, final String userid, final String imageurl) {
        mchat = new ArrayList<>();

        // Get a Firestore instance
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Reference to the "Chats" collection
        CollectionReference chatsRef = db.collection("Chats");

        // Query to get messages for the specified users ordered by timestamp
        com.google.firebase.firestore.Query query = chatsRef
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.ASCENDING);

        // Add a listener to listen for changes in the query results
        query.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot snapshot, @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w("FAIL", "Listen failed.", e);
                    return;
                }

                if (snapshot != null) {
                    mchat.clear();
                    for (DocumentSnapshot document : snapshot.getDocuments()) {
                        Chat chat = document.toObject(Chat.class);
                        String messageId = document.getId();
                        if (chat != null && (chat.getReceiver().equals(myid) && chat.getSender().equals(userid) ||
                                chat.getReceiver().equals(userid) && chat.getSender().equals(myid))) {
                            mchat.add(chat);
                        }
                    }
                    // Update the RecyclerView with the new data
                    messageAdapter = new MessageAdapter(context, mchat, imageurl);
                    recyclerView.setAdapter(messageAdapter);
                } else {
                    Log.d("ERROR", "Current data: null");
                }
            }
        });
    }

    private void closeKeyboard() {
        View view = ((MessageActivity)context).getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) ((MessageActivity)context).getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void startVideoCall(String receiverId, String fullname) {
        videoCallBtn.setIsVideoCall(true);
        videoCallBtn.setResourceID("zego_uikit_call");
        videoCallBtn.setInvitees(Collections.singletonList(new ZegoUIKitUser(receiverId, fullname)));
    }

    private void startAudioCall(String receiverId, String fullname) {
        audioCallBtn.setIsVideoCall(false);
        audioCallBtn.setResourceID("zego_uikit_call");
        audioCallBtn.setInvitees(Collections.singletonList(new ZegoUIKitUser(receiverId, fullname)));
    }

    private WindowManager getWindowManager() {
        if (windowManager == null) {
            windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        }
        return windowManager;
    }

    public void show() {
        if (Settings.canDrawOverlays(context)) {
            dismiss();
            isShowing = true;
            getWindowManager().addView(floatView, layoutParams);
        }
    }

    public void dismiss() {
        if (isShowing) {
            getWindowManager().removeView(floatView);
            isShowing = false;
        }
    }
}
