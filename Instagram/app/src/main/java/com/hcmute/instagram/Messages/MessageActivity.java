package com.hcmute.instagram.Messages;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.hcmute.instagram.Home;
import com.hcmute.instagram.Messages.Adapter.FriendsAdapter;
import com.hcmute.instagram.Messages.Adapter.MessageAdapter;
import com.hcmute.instagram.Messages.Adapter.PageAdapter;
import com.hcmute.instagram.Messages.Floatingview.ChatFloatingWindow;
import com.hcmute.instagram.Messages.Fragments.ChatsFragment;
import com.hcmute.instagram.Messages.Fragments.FriendsFragment;
import com.hcmute.instagram.Messages.Model.Chat;
import com.hcmute.instagram.Messages.Notification.APIService;
import com.hcmute.instagram.Messages.Notification.Client;
import com.hcmute.instagram.Messages.Notification.Data;
import com.hcmute.instagram.Messages.Notification.MyResponse;
import com.hcmute.instagram.Messages.Notification.NotificationSender;
import com.hcmute.instagram.Messages.Notification.Token;
import com.hcmute.instagram.R;
import com.hcmute.instagram.Search.UserSearchProfileActivity;
import com.hcmute.instagram.Search.UserSearchViewPost;
import com.hcmute.instagram.models.Users;
import com.zegocloud.uikit.prebuilt.call.invite.widget.ZegoSendCallInvitationButton;
import com.zegocloud.uikit.service.defines.ZegoUIKitUser;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MessageActivity extends AppCompatActivity {

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
    private ImageView imgChatBuble;
    ChatFloatingWindow chatFloatingWindow;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        toolbar = findViewById(R.id.MessageActivity_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        username = (TextView) findViewById(R.id.MessageActivity_userName);
        fullname = (TextView) findViewById(R.id.MessageActivity_fullname);
        profile_image = (CircleImageView) findViewById(R.id.MessageActivity_user_img);
        btn_send = findViewById(R.id.MessageActivity_btn_send);
        text_send = findViewById(R.id.MessageActivity_text_send);
        btn_like = findViewById(R.id.MessageActivity_likeBtn);
        videoCallBtn = findViewById(R.id.video_call_btn);
        audioCallBtn = findViewById(R.id.audio_call_btn);
        imgChatBuble = findViewById(R.id.imgChatBuble);

        //chỉnh
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

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

        recyclerView = findViewById(R.id.MessageActivity_recyclerView);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        intent = getIntent();
        userid = intent.getStringExtra("userid");
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
                        Glide.with(MessageActivity.this)
                                .load(user.getProfilePhoto())
                                .into(profile_image);

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
                    Toast.makeText(MessageActivity.this, "You can't send empty message", Toast.LENGTH_SHORT).show();
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

        imgChatBuble.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chatFloatingWindow = new ChatFloatingWindow(getApplicationContext(), userid);
                //chatFloatingWindow = new ChatFloatingWindow(getApplicationContext());
                if (canDrawOverlays()) {
                    Log.d("OK2", "onClick: OK");
                    chatFloatingWindow.show();
                } else {
                    startManageDrawOverlaysPermission();
                }
            }
        });
    }

    private void accessUserProfile(String id) {
        Intent intent=new Intent(MessageActivity.this, UserSearchProfileActivity.class);
        intent.putExtra("SearchedUserid",id);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
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
                                    Toast.makeText(MessageActivity.this, "Failed", Toast.LENGTH_SHORT).show();
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
//                        com.google.firebase.firestore.Query query = chatsRef.whereEqualTo("sender", userid)
//                                .whereEqualTo("receiver", myid)
//                                .whereEqualTo("isseen", false);
//                        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
//                            @Override
//                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
//                                if (task.isSuccessful()) {
//                                    for (QueryDocumentSnapshot document : task.getResult()) {
//                                        // Update each message to mark it as seen
//                                        String messageId = document.getId();
//                                        chatsRef.document(messageId)
//                                                .update("isseen", true)
//                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
//                                                    @Override
//                                                    public void onSuccess(Void aVoid) {
//                                                        Log.i("update", messageId);
//                                                        // Message marked as seen successfully
//                                                        Log.d("setSeenMessage", "Message marked as seen");
//                                                    }
//                                                })
//                                                .addOnFailureListener(new OnFailureListener() {
//                                                    @Override
//                                                    public void onFailure(@NonNull Exception e) {
//                                                        // Failed to mark message as seen
//                                                        Log.e("setSeenMessage", "Error marking message as seen", e);
//                                                    }
//                                                });
//                                    }
//                                } else {
//                                    Log.e("setSeenMessage", "Error getting documents: ", task.getException());
//                                }
//                            }
//                        });

                    }
                    // Update the RecyclerView with the new data
                    messageAdapter = new MessageAdapter(MessageActivity.this, mchat, imageurl);
                    recyclerView.setAdapter(messageAdapter);
                } else {
                    Log.d("ERROR", "Current data: null");
                }
            }
        });
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        boolean returnSuperKeyDown = true;

        if(keyCode == KeyEvent.KEYCODE_BACK){
           startActivity(new Intent(MessageActivity.this, ChatActivity.class));

        }
        return true;

    }


    private void closeKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            // Handle the click on the Up button (the back button in the toolbar)
            // Here you can perform any action you want, such as navigating back or executing a custom action.
            // For example, you can start the Home activity:
            startActivity(new Intent(MessageActivity.this, ChatActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_DRAW_OVERLAY_PERMISSION) {
            if (canDrawOverlays()) {
                chatFloatingWindow.show();
            } else {
                showToast("Permission is not granted!");
            }
        }
    }

    private void startManageDrawOverlaysPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getApplicationContext().getPackageName()));
            startActivityForResult(intent, REQUEST_CODE_DRAW_OVERLAY_PERMISSION);
        }
    }

    private boolean canDrawOverlays() {
        // Implement the logic to check if the app has the "Draw over other apps" permission
        return Settings.canDrawOverlays(getApplicationContext());
        //return true;
    }

    private void showToast(String message) {
        // Implement the logic to show a toast message
    }

    private static final int REQUEST_CODE_DRAW_OVERLAY_PERMISSION = 5;
}