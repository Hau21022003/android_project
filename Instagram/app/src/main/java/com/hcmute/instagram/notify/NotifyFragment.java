package com.hcmute.instagram.notify;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.hcmute.instagram.Utils.LikeNotificationAdapter;
import com.hcmute.instagram.models.Notification;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.hcmute.instagram.R;

public class NotifyFragment extends Fragment {

    private static final String TAG = "LikeFragment";
    private RecyclerView notifyRecycle;

    private LikeNotificationAdapter likeNotificationAdapter;
    private List<Notification> notificationList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_like, null);

        notifyRecycle = v.findViewById(R.id.notify_recycleview);
        notifyRecycle.setHasFixedSize(true);
        notifyRecycle.setLayoutManager(new LinearLayoutManager(getContext()));

        notificationList = new ArrayList<>();

        readNotifications();

        return v;
    }

    private void readNotifications() {

        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("Notifications")
                .orderBy("postAt", Query.Direction.DESCENDING)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @SuppressLint("NotifyDataSetChanged")
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
                                if (notification != null) {
                                    if (notification.getReceiverId().equals(firebaseUser.getUid())) {
                                        notification.setId(document.getId());
                                        notificationList.add(notification);
                                    }

                                }

                            }
                            likeNotificationAdapter = new LikeNotificationAdapter(getContext(), notificationList);
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
    public void onResume() {
        super.onResume();
        requireView().setFocusableInTouchMode(true);
        requireView().requestFocus();
        requireView().setOnKeyListener((v, keyCode, event) -> {
            if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP) {
                // Handle back button press here
                // For example, pop the current Fragment from the back stack
                requireActivity().getSupportFragmentManager().popBackStack();
                return true; // Consume the event
            }
            return false; // Let the event propagate if not handled
        });
    }

}
