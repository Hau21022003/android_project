package com.hcmute.instagram.Messages.Fragments;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
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

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.auth.User;
import com.google.firebase.installations.FirebaseInstallations;
import com.google.firebase.installations.InstallationTokenResult;
import com.hcmute.instagram.Messages.Adapter.FriendsAdapter;
import com.hcmute.instagram.Messages.ConvertObjectTime;
import com.hcmute.instagram.Messages.Model.Chat;
import com.hcmute.instagram.Messages.Notification.Token;
import com.hcmute.instagram.R;
import com.hcmute.instagram.models.Users;


public class ChatsFragment extends Fragment {

    private RecyclerView recyclerView;

    private FriendsAdapter userAdapter;
    private List<Users> mUsers;

    FirebaseUser fuser;
    DatabaseReference reference;

    private List<String> usersList;
    private ArrayList<Chat> lastMessageList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chats, container, false);

        recyclerView = view.findViewById(R.id.ChatsFragment_recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        usersList = new ArrayList<>();
        lastMessageList = new ArrayList<Chat>();

        fuser = FirebaseAuth.getInstance().getCurrentUser();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference chatsRef = db.collection("Chats");
        Query query = chatsRef.orderBy("timestamp", Query.Direction.DESCENDING);
//        query.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
//            @Override
//            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
//                usersList.clear();
//                for (DocumentSnapshot snapshot : queryDocumentSnapshots) {
//                    Chat chat = snapshot.toObject(Chat.class);
//                    if (chat != null) {
//                        if (chat.getSender().equals(fuser.getUid())) {
//                            usersList.add(chat.getReceiver());
//                        }
//                        if (chat.getReceiver().equals(fuser.getUid())) {
//                            usersList.add(chat.getSender());
//                        }
//                    }
//                }
//                Set<String> hashSet = new HashSet<>(usersList);
//                usersList.clear();
//                usersList.addAll(hashSet);
//
//                readChats();
//            }
//        });

        query.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    // Handle error if any
                    return;
                }
                if (queryDocumentSnapshots != null) {
                    usersList.clear();
                    for (DocumentSnapshot snapshot : queryDocumentSnapshots) {
                        Chat chat = snapshot.toObject(Chat.class);

                        if (chat != null) {
                            if (chat.getSender().equals(fuser.getUid()) && chat.getReceiver().equals(fuser.getUid())) {
                            } else {
                                if (chat.getSender().equals(fuser.getUid()) && !usersList.contains(chat.getReceiver())) {
                                    usersList.add(chat.getReceiver());

                                }
                                if (chat.getReceiver().equals(fuser.getUid()) && !usersList.contains(chat.getSender())) {
                                    usersList.add(chat.getSender());

                                }
                            }

                        }
                    }
                    Set<String> hashSet = new HashSet<>(usersList);
                    usersList.clear();
                    usersList.addAll(hashSet);
                    readChats();
                }
            }
        });

        UpdateToken();
        return view;
    }

    public void readChats() {
        mUsers = new ArrayList<>();
        lastMessageList = new ArrayList<>();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference usersRef = db.collection("Users");
        for (String userId : usersList) {
            Log.i("USERID", userId);
            usersRef.whereEqualTo("user_id", userId)
                    .get()
                    .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @SuppressLint("NotifyDataSetChanged")
                        @Override
                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                            // Iterate through the query result for each user
                            for (DocumentSnapshot snapshot : queryDocumentSnapshots) {
                                // Convert the document snapshot to a Users object
                                Users user = snapshot.toObject(Users.class);
                                if (user != null) {
                                    // Add the user to the mUsers list

                                    lastMessage(user.getUser_id(), user);
                                }
                            }

                        }
                    });
        }
    }

    public void lastMessage(String userid, Users user) {
        mUsers.add(user);

        // Call lastMessage for the user
        Log.i("Get user", user.getUser_id());
        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference chatsRef = db.collection("Chats");

        // Query for the last message between the current user and the specified user ID
        chatsRef.orderBy("timestamp", Query.Direction.DESCENDING).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                for (DocumentSnapshot snapshot : queryDocumentSnapshots) {
                    Chat chat = snapshot.toObject(Chat.class);

                    if (firebaseUser != null && chat != null) {
                        if ((chat.getReceiver().equals(firebaseUser.getUid()) && chat.getSender().equals(userid)) ||
                                (chat.getReceiver().equals(userid) && chat.getSender().equals(firebaseUser.getUid()))) {
                            lastMessageList.add(chat);
                            break; // Once we find the last message, we can stop iterating
                        }
                    }
                }

                userAdapter = new FriendsAdapter(getContext(), mUsers, true);
                recyclerView.setAdapter(userAdapter);

            }
        });
    }


    public void sortChatlist() {
//        for (Chat chat : lastMessageList)   Log.i("lastmessage", chat.getMessage() );
        if (lastMessageList.size() != mUsers.size())
            return;
        if (lastMessageList == null) return;
        for (int i = 0; i < lastMessageList.size() - 1; i++) {
            for (int j = i + 1; j < lastMessageList.size(); j++) {
                Object timestampObj1 = lastMessageList.get(i).getTimestamp();
                Object timestampObj2 = lastMessageList.get(j).getTimestamp();

                // Convert Object to Date for comparison
                Date timestamp1 = ConvertObjectToTimestamp(timestampObj1);
                Date timestamp2 = ConvertObjectToTimestamp(timestampObj2);

                if (timestamp1 != null && timestamp2 != null && timestamp1.before(timestamp2)) {
                    // Swap lastMessageList elements
                    Chat tmpChat = lastMessageList.get(i);
                    lastMessageList.set(i, lastMessageList.get(j));
                    lastMessageList.set(j, tmpChat);

                    // Swap muser elements accordingly
                    Users tmpUser = mUsers.get(i);
                    mUsers.set(i, mUsers.get(j));
                    mUsers.set(j, tmpUser);
                }
            }
        }

    }

    private Date ConvertObjectToTimestamp(Object timestampObj) {
        if (timestampObj instanceof Date) {
            return (Date) timestampObj;
        } else if (timestampObj instanceof Timestamp) {
            return new Date(((Timestamp) timestampObj).toDate().getTime());
        } else {
            // Handle other cases or return null if conversion is not possible
            return null;
        }
    }

    private void UpdateToken() {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        FirebaseInstallations.getInstance().getToken(/* forceRefresh */ false)
                .addOnCompleteListener(new OnCompleteListener<InstallationTokenResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstallationTokenResult> task) {
                        if (task.isSuccessful()) {
                            String refreshToken = task.getResult().getToken();
                            Token token = new Token(refreshToken);
                            FirebaseDatabase.getInstance().getReference("Tokens")
                                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                    .setValue(token);
                        } else {
                            // Handle error
                        }
                    }
                });
    }


}