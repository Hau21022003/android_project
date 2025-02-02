package com.hcmute.instagram.Search;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.hcmute.instagram.R;
import com.hcmute.instagram.Utils.SearchUsersAdapter;
import com.hcmute.instagram.models.Users;

public class SearchFragment extends Fragment {

    private static final String TAG ="SearchFragment" ;
    private RecyclerView recyclerView;
    private SearchUsersAdapter searchUsersAdapter;
    private List<Users> mUser;
    EditText search;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_search,null);

        recyclerView = (RecyclerView)v.findViewById(R.id.recycler_view_search);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        search = (EditText)v.findViewById(R.id.search_user);

        mUser = new ArrayList<>();
        readUsers();

        search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                searchUsers(s.toString().trim().toLowerCase());

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        return v;
    }

    private void searchUsers(String s){

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference usersRef = db.collection("Users");

        usersRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                mUser.clear();
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        Users user = document.toObject(Users.class);
                        String username = user.getUsername().toLowerCase();
                        String fullname = user.getFullName().toLowerCase().trim();
                        if (username != null && username.contains(String.valueOf(s))) {
                            mUser.add(user);
                        }
                        else if (fullname != null && fullname.contains(String.valueOf(s))) {
                            mUser.add(user);
                        }
                    }
                    updateSearchList();
                } else {
                    Log.e(TAG, "Error getting usernames: ", task.getException());
                }
            }
        });

    }

    private void readUsers(){

        List<Users> users2 = new ArrayList<>();
        FirebaseFirestore.getInstance().collection("Users").get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (search.getText().toString().equals("")) {
                            if (task.isSuccessful()) {
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    Users users = document.toObject(Users.class);
                                    users2.add(users);
                                }
                            } else {
                                Log.d(TAG, "Error getting documents: ", task.getException());
                            }
                        }
                    }
                });




//        FirebaseFirestore.getInstance().collection("Users").get()
//                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
//                    @Override
//                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
//                        if (search.getText().toString().equals("")) {
//                            mUser.clear();
//                            if (task.isSuccessful()) {
//                                for (QueryDocumentSnapshot document : task.getResult()) {
//                                    Users users = document.toObject(Users.class);
//                                    mUser.add(users);
//                                }
//                                updateSearchList();
//                            } else {
//                                Log.d(TAG, "Error getting documents: ", task.getException());
//                            }
//                        }
//                    }
//                });
    }

    public void onResume() {

        super.onResume();
        this.getView().setFocusableInTouchMode(true);
        this.getView().requestFocus();
        this.getView().setOnKeyListener(new View.OnKeyListener() {

            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {

                if (keyCode == KeyEvent.KEYCODE_BACK) {

                    return true;
                }
                return false;
            }
        });
    }

    private void updateSearchList(){

        Log.d(TAG,"updateSearchList : Updating Search List");

        searchUsersAdapter = new SearchUsersAdapter(getContext(),mUser, "search");
        recyclerView.setAdapter(searchUsersAdapter);


    }

}
