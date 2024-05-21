package com.hcmute.instagram.home;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.auth.User;
import com.hcmute.instagram.Messages.Model.Chat;
import com.hcmute.instagram.Stories.StoryAdapter;
import com.hcmute.instagram.models.Likes;
import com.hcmute.instagram.models.Notification;
import com.hcmute.instagram.models.Photo;
import com.hcmute.instagram.models.Users;
import com.hcmute.instagram.notify.NotifyActivity;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.hcmute.instagram.Messages.ChatActivity;
import com.hcmute.instagram.R;
import com.hcmute.instagram.Stories.StoryAdapter;
import com.hcmute.instagram.Utils.HomeFragmentPostViewListAdapter;
import com.hcmute.instagram.Utils.UniversalImageLoader;
import com.hcmute.instagram.models.Comments;
import com.hcmute.instagram.models.Photo;
import com.hcmute.instagram.models.Story;

public class HomeFragment extends Fragment {

    private static final String TAG = "HomeFragment";
    private FirebaseFirestore db;
    private String userId;
    //vars
    private ArrayList<Photo> mPhotos;
    private ArrayList<Photo> mPaginatedPhotos;
    private ArrayList<String> mFollowing;
    private ListView mListView;
    private HomeFragmentPostViewListAdapter mAdapter;
    private int mResults;
    ImageView message, notify;
    private List<Notification> notificationList;


    private RecyclerView recyclerView_story;
    private StoryAdapter storyAdapter;
    private List<Story> storyList;
    private TextView badge, notifyBadge;
    FirebaseUser fuser;

    private Users currentUser;
    private ArrayList<Chat> listChat;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_home, null);
        mListView = v.findViewById(R.id.FragmentHome_postListView);
        mFollowing = new ArrayList<>();
        mPhotos = new ArrayList<>();
        mPaginatedPhotos = new ArrayList<>();
        notificationList = new ArrayList<>();
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        db = FirebaseFirestore.getInstance();
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
        recyclerView_story = v.findViewById(R.id.FragmentHome_story_recyclerView);
        recyclerView_story.setHasFixedSize(true);
        LinearLayoutManager linearlayoutManager = new LinearLayoutManager(getContext(),
                LinearLayoutManager.HORIZONTAL, false);
        recyclerView_story.setLayoutManager(linearlayoutManager);
        storyList = new ArrayList<>();
        storyAdapter = new StoryAdapter(getContext(), storyList);
        recyclerView_story.setAdapter(storyAdapter);

        getFollowing();
        initImageLoader();
        displayMorePhotos();
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
                Log.d(TAG, "getFollowing: searching for following" + mFollowing);
                readStory();
                getPhotos();
            }
        });

    }

    private void initImageLoader() {
        UniversalImageLoader universalImageLoader = new UniversalImageLoader(getActivity());
        ImageLoader.getInstance().init(universalImageLoader.getConfig());
    }

    private void getPhotos() {
        Log.d(TAG, "getPhotos: getting photos");

        db.collection("posts").whereIn("userId", mFollowing).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                    Photo photo = new Photo();
                    Map<String, Object> objectMap = documentSnapshot.getData();
                    Log.d(TAG, "setupGridView(objectMap)" + objectMap.get("caption"));

                    photo.setCaption(objectMap.get("caption") != null ? objectMap.get("caption").toString() : "");
                    photo.setTags(objectMap.get("tags") != null ? objectMap.get("tags").toString() : "");
                    photo.setPhoto_id(objectMap.get("photo_id") != null ? objectMap.get("photo_id").toString() : "");
                    photo.setUser_id(objectMap.get("userId") != null ? objectMap.get("userId").toString() : "");
                    photo.setDate_Created(objectMap.get("date_Created") != null ? objectMap.get("date_Created").toString() : "");
                    photo.setImage_Path(objectMap.get("thumbnailUrl") != null ? objectMap.get("thumbnailUrl").toString() : "");


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
                displayPhotos();

            }
        });

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
    }

    private void displayPhotos() {
        if (mPhotos != null) {
            try {
                Collections.sort(mPhotos, new Comparator<Photo>() {
                    @Override
                    public int compare(Photo o1, Photo o2) {
                        return o2.getDate_Created().compareTo(o1.getDate_Created());
                    }
                });

                int iterations = mPhotos.size();

                if (iterations > 10) {
                    iterations = 10;
                }

                mResults = 10;
                for (int i = 0; i < iterations; i++) {
                    mPaginatedPhotos.add(mPhotos.get(i));
                }
                for (Photo post : mPaginatedPhotos) {
                    Log.i("HomeFragment", post.getCaption());
                }
                mAdapter = new HomeFragmentPostViewListAdapter(getActivity(), R.layout.fragment_home_post_viewer, mPaginatedPhotos);
                mListView.setAdapter(mAdapter);

            } catch (NullPointerException e) {
                Log.e(TAG, "displayPhotos: NullPointerException: " + e.getMessage());
            } catch (IndexOutOfBoundsException e) {
                Log.e(TAG, "displayPhotos: IndexOutOfBoundsException: " + e.getMessage());
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

    private void readStory() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        CollectionReference storyRef = db.collection("Story");
        storyRef.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot snapshot, @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w(TAG, "Listen failed.", e);
                    return;
                }

                long timecurrent = System.currentTimeMillis();
                storyList.clear();
                storyList.add(new Story(null, null,
                        FirebaseAuth.getInstance().getCurrentUser().getUid(), 0, 0));
                mFollowing.remove(currentUser.getUser_id());
                for (String id : mFollowing) {
                    int countStory = 0;
                    Story story = null;
                    for (DocumentSnapshot document : snapshot.getDocuments()) {
                        if (document != null && document.get("userid").equals(id)) {
                            story = document.toObject(Story.class);
                            if (timecurrent > story.getTimestart() && timecurrent < story.getTimeend()) {
                                countStory++;
                            }
                        }
                    }
                    if (countStory > 0) {
                        storyList.add(story);
                    }
                }
                storyAdapter.notifyDataSetChanged();
            }
        });


    }

}
