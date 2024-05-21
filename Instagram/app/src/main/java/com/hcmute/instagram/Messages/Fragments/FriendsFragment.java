package com.hcmute.instagram.Messages.Fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.hcmute.instagram.Messages.Adapter.FriendsAdapter;
import com.hcmute.instagram.Messages.Model.Chat;
import com.hcmute.instagram.R;
import com.hcmute.instagram.models.Users;


public class FriendsFragment extends Fragment {

    private static final String TAG = "FriendsFragment";

    private RecyclerView recyclerView;
    private FriendsAdapter friendsAdapter;
    private List<Users> mUser;
    private ArrayList<String> friends;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_friends, container, false);

        recyclerView = view.findViewById(R.id.FragmentFriends_userList);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        friends = new ArrayList<String>();
        mUser = new ArrayList<>();

//        readUsers();
        searchUsers();
        return view;
    }

    private void readUsers() {

        // Retriving all users except self

//        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
//        reference.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//
//                for(DataSnapshot snapshot1:snapshot.getChildren()){
//                    Users users = snapshot1.getValue(Users.class);
//                    if(!users.getUser_id().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())){
//                        Log.d(TAG, "onDataChange: userid:"+FirebaseAuth.getInstance().getCurrentUser().getUid());
//                        mUser.add(users);
//                    }
//                }
//                updateFriendList();
//            }
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//
//            }
//        });

    }


    private void searchUsers() {

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference usersRef = db.collection("Users");
        FirebaseUser fuser = FirebaseAuth.getInstance().getCurrentUser();
        usersRef.whereEqualTo("user_id", fuser.getUid()).limit(1).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    Users currentUser = new Users();
                    for (DocumentSnapshot document : task.getResult()) {
                        currentUser = document.toObject(Users.class);
                        break;
                    }
                    if (currentUser.getFollowers().size() < currentUser.getFollowing().size()) {
                        setFriendList(currentUser.getFollowers(), currentUser.getFollowing());
                    } else {
                        setFriendList(currentUser.getFollowing(), currentUser.getFollowers());
                    }
                }
                for (String friendId : friends) {
                    usersRef.whereEqualTo("user_id", friendId).limit(1).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            mUser.clear();
                            if (task.isSuccessful()) {
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    Users user = document.toObject(Users.class);
                                    mUser.add(user);
                                }
                                updateFriendList();
                            } else {
                                Log.d(TAG, "Error getting documents: ", task.getException());
                            }
                        }
                    });
                }
            }
        });
//        usersRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
//            @Override
//            public void onComplete(@NonNull Task<QuerySnapshot> task) {
//                mUser.clear();
//                if (task.isSuccessful()) {
//                    for (QueryDocumentSnapshot document : task.getResult()) {
//                        Users user = document.toObject(Users.class);
//                        if (!user.getUser_id().equals(fuser.getUid()))
//                            mUser.add(user);
//                    }
//                    updateFriendList();
//                } else {
//                    Log.d(TAG, "Error getting documents: ", task.getException());
//                }
//            }
//        });
    }

    private void setFriendList(ArrayList<String> list1, ArrayList<String> list2) {
        int length = list1.size();
        for (int i = 0; i < length; i++) {
            if (list2.contains(list1.get(i))) friends.add(list1.get(i));
        }
    }

    private void updateFriendList() {

        Log.d(TAG, "updateFriendList : Updating Friend List");

        friendsAdapter = new FriendsAdapter(getContext(), mUser, false);
        recyclerView.setAdapter(friendsAdapter);

    }
}