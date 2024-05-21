package com.hcmute.instagram.Messages.Adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.hcmute.instagram.Messages.ConvertObjectTime;
import com.hcmute.instagram.Messages.MessageActivity;
import com.hcmute.instagram.Messages.Model.Chat;
import com.hcmute.instagram.R;
import com.hcmute.instagram.models.Users;

import org.w3c.dom.Text;

public class FriendsAdapter extends RecyclerView.Adapter<FriendsAdapter.ViewHolder> {

    String TAG = "FriendsAdapter";

    private Context mcontext;
    private List<Users> muser;
    private boolean ischat;

    private FirebaseUser firebaseUser;
    String theLastMessage;
    Object timeOfLastMessage;
    Users users;
    ArrayList<Chat> lastMessageList;

    public FriendsAdapter(Context mcontext, List<Users> muser, boolean ischat) {
        this.mcontext = mcontext;
        this.muser = muser;
        this.ischat = ischat;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mcontext).inflate(R.layout.friends_single_layout, parent, false);
        return new ViewHolder(view);

    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") int position) {

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        users = muser.get(position);
        holder.username.setText(users.getUsername());

//        holder.fullname.setText(users.getFullName());
        Glide.with(mcontext)
                .load(users.getProfilePhoto())
                .into(holder.profileimage);

        if (ischat) {
            lastMessageList = new ArrayList<Chat>();
            lastMessage(users.getUser_id(), holder.last_msg, holder.timestamp);
        } else {
            holder.last_msg.setText(users.getFullName());
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ischat) {
                    setSeenMessage(position);
                }

                Intent intent = new Intent(mcontext, MessageActivity.class);
                intent.putExtra("userid", muser.get(position).getUser_id());
                mcontext.startActivity(intent);

            }
        });

    }


    @Override
    public int getItemCount() {
        return muser.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public TextView username, last_msg, timestamp;
        public CircleImageView profileimage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            username = (TextView) itemView.findViewById(R.id.FriendSingle_userName);
            last_msg = (TextView) itemView.findViewById(R.id.FriendSingle_lastMsg);
            profileimage = (CircleImageView) itemView.findViewById(R.id.FriendSingle_user_img);
            timestamp = (TextView) itemView.findViewById(R.id.FriendSingle_timestamp);
        }
    }

    private void setSeenMessage(int position) {
        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        // Ensure lastMessage is not null
        if (lastMessageList.get(position) != null && !lastMessageList.get(position).isIsseen() && !lastMessageList.get(position).getSender().equals(firebaseUser.getUid())) {
            // Get the ID of the last message
            String senderId = lastMessageList.get(position).getSender();
            String receiverId = lastMessageList.get(position).getReceiver();

            FirebaseFirestore db = FirebaseFirestore.getInstance();
            CollectionReference chatsRef = db.collection("Chats");

// Query for messages with the given sender and receiver IDs
            Query query = chatsRef.whereEqualTo("sender", senderId)
                    .whereEqualTo("receiver", receiverId)
                    .whereEqualTo("isseen", false);

            query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            // Update each message to mark it as seen
                            String messageId = document.getId();
                            chatsRef.document(messageId)
                                    .update("isseen", true)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Log.i("update", messageId);
                                            // Message marked as seen successfully
                                            Log.d("setSeenMessage", "Message marked as seen");
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            // Failed to mark message as seen
                                            Log.e("setSeenMessage", "Error marking message as seen", e);
                                        }
                                    });
                        }
                    } else {
                        Log.e("setSeenMessage", "Error getting documents: ", task.getException());
                    }
                }
            });
        } else {
            // Handle case where lastMessage is null
            Log.e("setSeenMessage", "Last message is null");
        }
    }

    //check for last message
    public void lastMessage(final String userid, final TextView last_msg, TextView timestamp) {
        theLastMessage = "default";
        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference chatsRef = db.collection("Chats");

        chatsRef.orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        boolean isSeen = true;
                        String sender = "";
                        for (DocumentSnapshot snapshot : queryDocumentSnapshots) {
                            Chat chat = snapshot.toObject(Chat.class);

                            if (firebaseUser != null && chat != null) {
                                if ((chat.getReceiver().equals(firebaseUser.getUid()) && chat.getSender().equals(userid)) ||
                                        (chat.getReceiver().equals(userid) && chat.getSender().equals(firebaseUser.getUid()))) {
                                    if (chat.getSender().equals(firebaseUser.getUid())) {
                                        theLastMessage = "You: " + chat.getMessage();
                                    } else {
                                        theLastMessage = chat.getMessage();
                                    }
                                    isSeen = chat.isIsseen();
                                    sender = chat.getSender();
                                    chat.setId(snapshot.getId());
                                    lastMessageList.add(chat);
                                    timeOfLastMessage = chat.getTimestamp();
                                    break; // Once we find the last message, we can stop iterating
                                }
                            }
                        }
                        switch (theLastMessage) {
                            case "default":
                                last_msg.setText(users.getFullName());
                                timestamp.setText("");
                                break;
                            default:
                                last_msg.setText(theLastMessage);
                                if (timeOfLastMessage != null) {
                                    timestamp.setText(ConvertObjectTime.convertTimestampToString(timeOfLastMessage));
                                }
                                if (!isSeen && !sender.equals(firebaseUser.getUid())) {
                                    last_msg.setTypeface(null, Typeface.BOLD);
                                    last_msg.setTextColor(Color.BLACK);
                                    timestamp.setTypeface(null, Typeface.BOLD);
                                    timestamp.setTextColor(Color.BLACK);
                                }
                                break;
                        }

                    }
                });
    }


}
