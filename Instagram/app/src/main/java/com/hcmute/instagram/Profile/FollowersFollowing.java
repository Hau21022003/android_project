package com.hcmute.instagram.Profile;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.hcmute.instagram.R;
import com.hcmute.instagram.Utils.SearchUsersAdapter;
import com.hcmute.instagram.models.Users;

public class FollowersFollowing extends AppCompatActivity {

    String id,title,number,storyid;

    List<String> idList;
    RecyclerView recyclerView;
    TextView Title,Number;
    SearchUsersAdapter usersAdapter;
    List<Users> usersList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_followers_following);

        Title = (TextView) findViewById(R.id.FollowFollowing_txt);
        //Number = (TextView) findViewById(R.id.FollowFollowing_number);
        recyclerView = (RecyclerView) findViewById(R.id.FollowFollowing_recyclerView);

        Intent intent = getIntent();
        id = intent.getStringExtra("id");
        storyid = intent.getStringExtra("storyid");
        title = intent.getStringExtra("title");
        number = intent.getStringExtra("number");

        Title.setText(title);
        //Number.setText(number);

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        usersList = new ArrayList<>();
        usersAdapter = new SearchUsersAdapter(this,usersList, "follow");
        recyclerView.setAdapter(usersAdapter);

        idList = new ArrayList<>();

        switch (title){

            case ("Followers"):
                getFollowers();
                break;
            case ("Following"):
                getFollowings();
                break;
            case "Views":
                getViews();
                break;
        }

    }

    private void getFollowings() {

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference userRef = db.collection("Users").document(id);
        userRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()) {
                    idList = (ArrayList<String>) documentSnapshot.get("following");
                }
                showUsers();
            }
        });

    }

    private void getFollowers() {

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        DocumentReference userRef = db.collection("Users").document(id);
        userRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()) {
                    idList = (ArrayList<String>) documentSnapshot.get("followers");
                }
                if (id.equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                    ShowUserOfCurrentUser();
                } else {
                    showUsers();
                }
            }
        });

    }

    private void ShowUserOfCurrentUser() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        for (String checkID : idList) {

            DocumentReference userRef = db.collection("Users").document(checkID);
            userRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    if (documentSnapshot.exists()) {
                        Users user = documentSnapshot.toObject(Users.class);
                        if (user!=null)
                            usersList.add(user);
                    }
                    usersAdapter = new SearchUsersAdapter(FollowersFollowing.this,usersList, "remove");
                    recyclerView.setAdapter(usersAdapter);
                }
            });

        }
    }

    private void showUsers() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        for (String checkID : idList) {

            DocumentReference userRef = db.collection("Users").document(checkID);
            userRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    if (documentSnapshot.exists()) {
                        Users user = documentSnapshot.toObject(Users.class);
                        usersList.add(user);
                        usersAdapter.notifyDataSetChanged();
                    }
                }
            });

        }

    }
    private void getViews(){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference viewsRef = db.collection("Story").document(storyid)
                .collection("views");
        viewsRef.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot snapshot,
                                @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w(TAG, "Listen failed.", e);
                    return;
                }
                idList.clear();
                for (DocumentSnapshot document : snapshot) {
                    idList.add(document.getId());
                }
                showUsers();
            }
        });
    }
    public void onResume() {
        super.onResume();
        usersAdapter.notifyDataSetChanged();

    }
}