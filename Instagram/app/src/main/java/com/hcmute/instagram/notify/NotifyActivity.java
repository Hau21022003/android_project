package com.hcmute.instagram.notify;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ListView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.hcmute.instagram.Home;
import com.hcmute.instagram.Messages.ChatActivity;
import com.hcmute.instagram.Messages.MessageActivity;
import com.hcmute.instagram.R;
import com.hcmute.instagram.Utils.LikeNotificationAdapter;
import com.hcmute.instagram.Utils.SearchUsersAdapter;
import com.hcmute.instagram.models.Notification;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class NotifyActivity extends AppCompatActivity {

    private static final String TAG = "LikeFragment";
    private RecyclerView notifyRecycle;
    private LikeNotificationAdapter likeNotificationAdapter;
    private List<Notification> notificationList;
    private FrameLayout frameLayout;
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notify);
        toolbar = findViewById(R.id.NotifyActivity_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Notification");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        frameLayout = findViewById(R.id.notify_frameLayout);
        replaceFragment(new NotifyFragment());
//        notifyRecycle = findViewById(R.id.notify_recycleview);
//        notifyRecycle.setHasFixedSize(true);
//        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
//        notifyRecycle.setLayoutManager(linearLayoutManager);
//        readNotifications();
    }


    public void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.notify_frameLayout, fragment);
        fragmentTransaction.commit();
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            // Handle the click on the Up button (the back button in the toolbar)
            // Here you can perform any action you want, such as navigating back or executing a custom action.
            // For example, you can start the Home activity:
            startActivity(new Intent(NotifyActivity.this, Home.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void readNotifications() {

        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("Notifications")
                .document(firebaseUser.getUid())
                .collection("user_notification")
                .orderBy("postAt", Query.Direction.DESCENDING)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot snapshot, @Nullable FirebaseFirestoreException error) {
                        if (error != null) {
                            Log.w("FAIL", "Listen failed.", error);
                            return;
                        }
                        if (snapshot != null) {
                            notificationList = new ArrayList<>();
                            for (DocumentSnapshot document : snapshot.getDocuments()) {
                                Notification notification = document.toObject(Notification.class);
                                notification.setId(document.getId());
                                notificationList.add(notification);
                            }
                            likeNotificationAdapter = new LikeNotificationAdapter(NotifyActivity.this, notificationList);
                            notifyRecycle.setAdapter(likeNotificationAdapter);
                        }
                    }
                });

//        db.collection("Notifications")
//                .document(firebaseUser.getUid())
//                .collection("user_notification")
//                .orderBy("postAt", Query.Direction.DESCENDING)
//                .addSnapshotListener((value, error) -> {
//                    if (error != null) {
//                        // Handle error
//                        Log.e("NotificationActivity", "Listen failed.", error);
//                        return;
//                    }
//
//                    if (value != null && !value.isEmpty()) {
//                        List<Notification> notificationList = new ArrayList<>();
//                        for (DocumentChange dc : value.getDocumentChanges()) {
//                            switch (dc.getType()) {
//                                case ADDED:
//                                    Notification notification = dc.getDocument().toObject(Notification.class);
//                                    notificationList.add(notification);
//                                    break;
//                                case MODIFIED:
//                                    // Handle modified notification if needed
//                                    break;
//                                case REMOVED:
//                                    // Handle removed notification if needed
//                                    break;
//                            }
//                        }
//                        // Log notifications
//                        for (Notification noti : notificationList){
//                            Log.i("NotificationActivity", noti.getText() + " " + noti.getPostAt());
//                        }
//                        // Reverse the list if needed
////                        Collections.reverse(notificationList);
//                        // Update your adapter with the new data
////                        likeNotificationAdapter.setData(notificationList);
//                    } else {
//                        Log.d("NotificationActivity", "No notifications found");
//                    }
//                });

    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        boolean returnSuperKeyDown = true;

        if (keyCode == KeyEvent.KEYCODE_BACK) {
            startActivity(new Intent(NotifyActivity.this, Home.class));

        }
        return true;

    }
}