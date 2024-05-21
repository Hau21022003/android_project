package com.hcmute.instagram.home;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.hcmute.instagram.Messages.ChatActivity;
import com.hcmute.instagram.Messages.Model.Chat;
import com.hcmute.instagram.R;
import com.hcmute.instagram.Stories.StoryAdapter;
import com.hcmute.instagram.Utils.HomeFragmentReelViewListAdapter;
import com.hcmute.instagram.Utils.UniversalImageLoader;
import com.hcmute.instagram.models.Comments;
import com.hcmute.instagram.models.Likes;
import com.hcmute.instagram.models.Notification;
import com.hcmute.instagram.models.Reel;
import com.hcmute.instagram.models.Story;
import com.hcmute.instagram.models.Users;
import com.hcmute.instagram.models.Video;
import com.hcmute.instagram.notify.NotifyActivity;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class ReelFragment extends Fragment {

    private static final String TAG = "ReelFragment";
    private FirebaseFirestore db;
    FirebaseUser fuser;

    private String userId;
    private ArrayList<Chat> listChat;
    private List<Notification> notificationList;

    //vars
    private ArrayList<Reel> mPhotos;
    private ArrayList<Reel> mPaginatedPhotos;
    private ArrayList<String> mFollowing;
    private ViewPager2 mListView;
    private HomeFragmentReelViewListAdapter mAdapter;
    private int mResults;
    ImageView message, notify;


    private RecyclerView recyclerView_story;
    private StoryAdapter storyAdapter;
    private List<Story> storyList;
    private TextView badge, notifyBadge;

    private Users currentUser;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_reel, null);

        mListView = v.findViewById(R.id.vpager);
        mFollowing = new ArrayList<>();
        mPhotos = new ArrayList<>();
        mPaginatedPhotos = new ArrayList<>();
        message = v.findViewById(R.id.FragmentHome_msg);
        notify = v.findViewById(R.id.FragmentNotify);
        notifyBadge = v.findViewById(R.id.notifyBadge);
        badge = v.findViewById(R.id.badge);
        message.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), ChatActivity.class);
                startActivity(intent);
            }
        });
        notify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), NotifyActivity.class);
                startActivity(intent);
            }
        });
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        db = FirebaseFirestore.getInstance();
        getPhotos();
        getNumberMessages();
        readNotifications();
        return v;
    }
    private void readNotifications() {
        notificationList = new ArrayList<>();
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        fuser = FirebaseAuth.getInstance().getCurrentUser();

        CollectionReference notifyRef = db.collection("Notifications");
        Query query = notifyRef
                .orderBy("postAt", Query.Direction.DESCENDING);
        query.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot snapshot, @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    Log.w("FAIL", "Listen failed.", error);
                    return;
                }
                if (snapshot != null) {
                    notificationList.clear();
                    for (DocumentSnapshot document : snapshot.getDocuments()) {
                        Notification notification = document.toObject(Notification.class);
                        if (notification != null) {
                            if (notification.getReceiverId().equals(fuser.getUid()) && !notification.isIsseen())
                                notificationList.add(notification);
                        }

                    }
                    for (Notification noti : notificationList) {
                        Log.i("NotificationActivity", noti.getText() + " " + noti.getPostAt());
                    }
                    if (notificationList.size() == 0)
                        notifyBadge.setVisibility(View.GONE);
                    else {
                        notifyBadge.setText(String.valueOf(notificationList.size()));
                        notifyBadge.setVisibility(View.VISIBLE);
                    }
                }
            }
        });
    }
    private void getNumberMessages() {
        listChat = new ArrayList<Chat>();
        fuser = FirebaseAuth.getInstance().getCurrentUser();
        CollectionReference chatsRef = db.collection("Chats");
        Query query = chatsRef.orderBy("timestamp", Query.Direction.DESCENDING);
        query.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    // Handle error if any
                    return;
                }
                if (queryDocumentSnapshots != null) {
                    listChat.clear();
                    for (DocumentSnapshot snapshot : queryDocumentSnapshots) {
                        Chat chat = snapshot.toObject(Chat.class);
                        if (chat != null) {
                            if (chat.getReceiver().equals(fuser.getUid()) && !chat.isIsseen()) {
                                listChat.add(chat);
                            }
                        }
                    }

                    if (listChat.size() == 0) {
                        badge.setVisibility(View.GONE);
                    } else {
                        badge.setText(String.valueOf(listChat.size()));
                        badge.setVisibility(View.VISIBLE);
                    }
                }
            }

        });
    }


    private void getFollowing() {

        DocumentReference userDocRef = db.collection("Users").document(userId);
        Log.d(TAG, "getFollowing: searching for following");

        userDocRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                currentUser = documentSnapshot.toObject(Users.class);
                mFollowing = currentUser.getFollowing();
                mFollowing.add(currentUser.getUser_id());
//                readStory();
                getPhotos();
            }
        });

//        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
//        Query query = reference
//                .child("Following")
//                .child(FirebaseAuth.getInstance().getCurrentUser().getUid());
//        query.addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {
//                    Log.d(TAG, "onDataChange: found user: " +
//                            singleSnapshot.child("user_id").getValue());
//
//                    mFollowing.add(singleSnapshot.child("user_id").getValue().toString());
//                }
//                mFollowing.add(FirebaseAuth.getInstance().getCurrentUser().getUid());
//                //get the photos
//                readStory();
//                getPhotos();
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//
//            }
//        });
    }

    private void initImageLoader() {
        UniversalImageLoader universalImageLoader = new UniversalImageLoader(getActivity());
        ImageLoader.getInstance().init(universalImageLoader.getConfig());
    }

    private void getPhotos() {
        Log.d(TAG, "getPhotos: getting photos");
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        Query query = firestore.collection("videos");

        query.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.e(TAG, "Listen failed.", e);
                    return;
                }

                // Clear the existing photos list before adding new data
                mPhotos.clear();

                for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                    Reel photo = new Reel();
                    Map<String, Object> objectMap = documentSnapshot.getData();
                    Log.d(TAG, "setupGridView(objectMap)" + objectMap.get("caption"));

                    photo.setCaption(objectMap.get("caption") != null ? objectMap.get("caption").toString() : "");
                    photo.setTags(objectMap.get("tags") != null ? objectMap.get("tags").toString() : "");
                    photo.setVideoId(objectMap.get("videoId") != null ? objectMap.get("videoId").toString() : "");
                    photo.setUserId(objectMap.get("userId") != null ? objectMap.get("userId").toString() : "");
                    photo.setDateCreated(objectMap.get("dateCreated") != null ? objectMap.get("dateCreated").toString() : "");
                    photo.setThumbnailUrl(objectMap.get("thumbnailUrl") != null ? objectMap.get("thumbnailUrl").toString() : "");
                    photo.setVideoUrl(objectMap.get("videoUrl") != null ? objectMap.get("videoUrl").toString() : "");

                    List<Comments> comments = new ArrayList<>();
                    if (documentSnapshot.contains("comments")) {
                        List<DocumentSnapshot> commentSnapshots = (List<DocumentSnapshot>) documentSnapshot.get("comments");
                        for (DocumentSnapshot commentSnapshot : commentSnapshots) {
                            Comments comment = commentSnapshot.toObject(Comments.class);
                            comments.add(comment);
                        }
                    }

                    photo.setComments(comments);

                    List<Likes> likesList = new ArrayList<>();
                    if (documentSnapshot.contains("likes")) {
                        List<DocumentSnapshot> likeSnapshots = (List<DocumentSnapshot>) documentSnapshot.get("likes");
                        for (DocumentSnapshot likeSnapshot : likeSnapshots) {
                            Likes like = likeSnapshot.toObject(Likes.class);
                            likesList.add(like);
                        }
                    }
                    photo.setLikes(likesList);
                    mPhotos.add(photo);
                }

                // Call your method to display the photos after retrieving them
            }
        });
        mAdapter = new HomeFragmentReelViewListAdapter(new FirestoreRecyclerOptions.Builder<Reel>().setQuery(query, Reel.class).build(), getActivity());
        mListView.setOrientation(ViewPager2.ORIENTATION_VERTICAL);
        System.out.println("SAD" + mAdapter);
        mListView.setAdapter(mAdapter);

    }
    @Override
    public void onStart(){
        super.onStart();
        mAdapter.startListening();
    }
    public void onStop() {
        super.onStop();
        mAdapter.stopListening();
    }
    public void onResume() {
        super.onResume();
        mAdapter.notifyDataSetChanged();
    }


//        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
//        for (int i = 0; i < mFollowing.size(); i++) {
//            final int count = i;
//            Query query = reference
//                    .child("User_Photo")
//                    .child(mFollowing.get(i))
//                    .orderByChild("user_id")
//                    .equalTo(mFollowing.get(i));
//            query.addListenerForSingleValueEvent(new ValueEventListener() {
//                @Override
//                public void onDataChange(DataSnapshot dataSnapshot) {
//                    for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {
//
//                        Photo photo = new Photo();
//                        Map<String, Object> objectMap = (HashMap<String, Object>) singleSnapshot.getValue();
//
//                        photo.setCaption(objectMap.get("caption").toString());
//                        photo.setTags(objectMap.get("tags").toString());
//                        photo.setPhoto_id(objectMap.get("photo_id").toString());
//                        photo.setUser_id(objectMap.get("user_id").toString());
//                        photo.setDate_Created(objectMap.get("date_Created").toString());
//                        photo.setImage_Path(objectMap.get("image_Path").toString());
//
//
//                        ArrayList<Comments> comments = new ArrayList<Comments>();
//                        for (DataSnapshot dSnapshot : singleSnapshot
//                                .child("comments").getChildren()) {
//                            Comments comment = new Comments();
//                            comment.setUser_id(dSnapshot.getValue(Comments.class).getUser_id());
//                            comment.setComment(dSnapshot.getValue(Comments.class).getComment());
//                            comment.setDate_created(dSnapshot.getValue(Comments.class).getDate_created());
//                            comments.add(comment);
//                        }
//
//                        photo.setComments(comments);
//                        mPhotos.add(photo);
//                    }
//                    if (count >= mFollowing.size() - 1) {
//                        //display our photos
//                        displayPhotos();
//                    }
//                }
//
//                @Override
//                public void onCancelled(DatabaseError databaseError) {
//
//                }
//            });
//        }


    private void displayPhotos(Query query) {
        if (mPhotos != null && !mPhotos.isEmpty()) {
            try {
                mPaginatedPhotos.clear();
                int count = Math.min(mPhotos.size(), 10);
                mPaginatedPhotos.addAll(mPhotos.subList(0, count));
                if (mAdapter == null) {

                } else {
                    mAdapter.notifyDataSetChanged();
                }
            } catch (NullPointerException | IndexOutOfBoundsException e) {
                Log.e(TAG, "displayPhotos: Error: " + e.getMessage());
            }
        }
    }

    public void displayMorePhotos() {
        Log.d(TAG, "displayMorePhotos: displaying more photos");

        try {

            if (mPhotos.size() > mResults && mPhotos.size() > 0) {

                int iterations;
                if (mPhotos.size() > (mResults + 10)) {
                    Log.d(TAG, "displayMorePhotos: there are greater than 10 more photos");
                    iterations = 10;
                } else {
                    Log.d(TAG, "displayMorePhotos: there is less than 10 more photos");
                    iterations = mPhotos.size() - mResults;
                }

                //add the new photos to the paginated results
                for (int i = mResults; i < mResults + iterations; i++) {
                    mPaginatedPhotos.add(mPhotos.get(i));
                }
                mResults = mResults + iterations;
                mAdapter.notifyDataSetChanged();
            }
        } catch (NullPointerException e) {
            Log.e(TAG, "displayPhotos: NullPointerException: " + e.getMessage());
        } catch (IndexOutOfBoundsException e) {
            Log.e(TAG, "displayPhotos: IndexOutOfBoundsException: " + e.getMessage());
        }
    }


}
