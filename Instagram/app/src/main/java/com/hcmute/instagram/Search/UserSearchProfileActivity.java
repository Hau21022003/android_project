package com.hcmute.instagram.Search;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TabHost;
import android.widget.TabWidget;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.hcmute.instagram.Home;
import com.hcmute.instagram.Messages.ChatActivity;
import com.hcmute.instagram.Messages.MessageActivity;
import com.hcmute.instagram.Profile.FollowersFollowing;
import com.hcmute.instagram.Profile.ProfileFragment;
import com.hcmute.instagram.Profile.ViewPostFragment;
import com.hcmute.instagram.Profile.ViewReelFragment;
import com.hcmute.instagram.R;
import com.hcmute.instagram.Utils.GridImageAdapter;
import com.hcmute.instagram.models.Comments;
import com.hcmute.instagram.models.Likes;
import com.hcmute.instagram.models.Notification;
import com.hcmute.instagram.models.Photo;
import com.hcmute.instagram.models.Users;
import com.hcmute.instagram.models.Video;

public class UserSearchProfileActivity extends AppCompatActivity {

    private static final String TAG ="UserSearchActivity" ;
    private static final int NUM_GRID_COLUMNS = 3;

    String searchedUserId;
    Button Follow,Following,FollowBack,Message;
    ImageView profilePhoto;
    GridView gridView1, gridView2;
    TextView posts,followers,followings,name, description,website,username;
    LinearLayout follower,following;
    String noFollowers,noFollowings;
    private ProgressBar mProgressBar;
    private TabHost tabHost;
    Toolbar toolbar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_search_profile);

        searchedUserId = getIntent().getStringExtra("SearchedUserid");
        Log.d(TAG, "Item Clicked Getting UID "+searchedUserId);
        toolbar = findViewById(R.id.UserSearchProfile_ToolBar);
//        account_setting_menu = (ImageView)findViewById(R.id.account_settingMenu);
        Follow = (Button)findViewById(R.id.UserSearchProfile_Followbtn);
        FollowBack = (Button) findViewById(R.id.UserSearchProfile_FollowBackbtn);
        Following = (Button)findViewById(R.id.UserSearchProfile_Followingbtn);
        Message = (Button)findViewById(R.id.UserSearchProfile_messages);
        profilePhoto = (ImageView)findViewById(R.id.UserSearchProfile_user_img);
        gridView1 = (GridView)findViewById(R.id.UserSearchProfile_gridview1);
        gridView2 = (GridView)findViewById(R.id.UserSearchProfile_gridview2);
        posts = (TextView)findViewById(R.id.UserSearchProfile_txtPosts);
        followers = (TextView)findViewById(R.id.UserSearchProfile_txtFollowers);
        followings = (TextView)findViewById(R.id.UserSearchProfile_txtFollowing);
        name = (TextView)findViewById(R.id.UserSearchProfile_display_name);
        description = (TextView)findViewById(R.id.UserSearchProfile_description);
        website = (TextView)findViewById(R.id.UserSearchProfile_website);
        username = (TextView)findViewById(R.id.UserSearchProfile_profileName);
        follower = (LinearLayout)findViewById(R.id.UserSearchProfile_noFollowers);
        following = (LinearLayout)findViewById(R.id.UserSearchProfile_noFollowing);

        mProgressBar = (ProgressBar)findViewById(R.id.UserSearchProfile_ProgressBar);

        tabHost = (TabHost) findViewById(R.id.UserSearchProfile_tabhost);

        RetrivingGeneralData();

        // Initialize the TabHost
        tabHost.setup();

        // Add tabs to the TabHost
        TabHost.TabSpec tab1Spec = tabHost.newTabSpec("Tab 1");
        tab1Spec.setContent(R.id.tab1);
        tab1Spec.setIndicator("",getResources().getDrawable(R.drawable.square_plus_icon));
        tabHost.addTab(tab1Spec);

        TabHost.TabSpec tab2Spec = tabHost.newTabSpec("Tab 2");
        tab2Spec.setContent(R.id.tab2);
        tab2Spec.setIndicator("",getResources().getDrawable(R.drawable.video_icon));
        tabHost.addTab(tab2Spec);

        // Set a listener to handle tab changes
        tabHost.setOnTabChangedListener(new TabHost.OnTabChangeListener() {
            @Override
            public void onTabChanged(String tabId) {
                // Handle tab changes here
                switch (tabId) {
                    case "Tab 1":
                        loadTab1Data();
                        break;
                    case "Tab 2":
                        loadTab2Data();
                        break;
                    default:
                        break;
                }
            }
        });

        loadTab1Data();

//        isFollowing();

        Follow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Log.d(TAG, "onClick: Follow: " + searchedUserId);

                addUserFollow();
            }
        });

        FollowBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Log.d(TAG, "onClick: Follow back: " + searchedUserId);

                addUserFollow();
            }
        });
        Following.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Log.d(TAG, "onClick: Following: " + searchedUserId);

                FirebaseFirestore db = FirebaseFirestore.getInstance();

                // Xóa ID của người dùng khác khỏi trường "following" của người dùng hiện tại
                DocumentReference currentUserRef = db.collection("Users").document(FirebaseAuth.getInstance().getCurrentUser().getUid());
                currentUserRef.update("following", FieldValue.arrayRemove(searchedUserId));

                // Xóa ID của người dùng hiện tại khỏi trường "followers" của người dùng khác
                DocumentReference otherUserRef = db.collection("Users").document(searchedUserId);
                otherUserRef.update("followers", FieldValue.arrayRemove(FirebaseAuth.getInstance().getCurrentUser().getUid()));

                isFollowing();

                updateFollowOtherUser();
            }
        });

        Message.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(UserSearchProfileActivity.this, MessageActivity.class);
                intent.putExtra("userid", searchedUserId);
                startActivity(intent);
            }
        });
    }

    private void loadTab2Data() {
        videoGridSetup();
    }

    private void loadTab1Data() {
        tempGridSetup();
    }

    private void addUserFollow() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        // Thêm ID của người dùng khác vào trường "following" của người dùng hiện tại
        DocumentReference currentUserRef = db.collection("Users").document(FirebaseAuth.getInstance().getCurrentUser().getUid());
        currentUserRef.update("following", FieldValue.arrayUnion(searchedUserId));

        // Thêm ID của người dùng hiện tại vào trường "followers" của người dùng khác
        DocumentReference otherUserRef = db.collection("Users").document(searchedUserId);
        otherUserRef.update("followers", FieldValue.arrayUnion(FirebaseAuth.getInstance().getCurrentUser().getUid()));

        setFollowing();
        updateFollowOtherUser();

        addFollowNotification(searchedUserId);
    }

    private void RetrivingGeneralData(){

        if(searchedUserId.equals(FirebaseAuth.getInstance().getCurrentUser().getUid())){
            // Current User
            Follow.setVisibility(View.GONE);
            Message.setVisibility(View.GONE);
            FollowBack.setVisibility(View.GONE);
            Following.setVisibility(View.GONE);
            dataretrive();

        }else{
            // Other User
            dataretrive();
            isFollowing();
        }


    }


    private void dataretrive(){

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference userDocRef = db.collection("Users").document(searchedUserId);
        userDocRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()) {
                    Users user = documentSnapshot.toObject(Users.class);
                    if (user != null) {
                        posts.setText(user.getPosts());
                        ArrayList<String> followersList = user.getFollowers();
                        noFollowers = String.valueOf(followersList.size());
                        ArrayList<String> followingsList = user.getFollowing();
                        noFollowings = String.valueOf(followingsList.size());
                        followers.setText(noFollowers);
                        followings.setText(noFollowings);
                        name.setText(user.getFullName());
                        description.setText(user.getDescription());
                        website.setText(user.getWebsite());
                        username.setText(user.getUsername());
                        Glide.with(UserSearchProfileActivity.this)
                                .load(user.getProfilePhoto())
                                .into(profilePhoto);
                    }
                } else {
                }
                mProgressBar.setVisibility(View.GONE);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                // Xử lý khi có lỗi xảy ra trong quá trình lấy dữ liệu
            }
        });


        follower.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(UserSearchProfileActivity.this, FollowersFollowing.class);
                intent.putExtra("id",searchedUserId);
                intent.putExtra("title","Followers");
                intent.putExtra("number",noFollowers);
                startActivity(intent);

            }
        });

        following.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(UserSearchProfileActivity.this,FollowersFollowing.class);
                intent.putExtra("id",searchedUserId);
                intent.putExtra("title","Following");
                intent.putExtra("number",noFollowings);
                startActivity(intent);

            }
        });

    }

    private void videoGridSetup() {
        Log.d(TAG, "setupGridView: Setting up image grid.");
        final ArrayList<Video> photos = new ArrayList<>();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        reference.keepSynced(true);
        FirebaseFirestore firestoreDB = FirebaseFirestore.getInstance();
        firestoreDB.collection("videos")
                .whereEqualTo("userId", searchedUserId)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                            Video photo = new Video();
                            Map<String, Object> objectMap = documentSnapshot.getData();
                            photo.setCaption(objectMap.get("caption") != null ? objectMap.get("caption").toString() : "");
                            photo.setTags(objectMap.get("tags") != null ? objectMap.get("tags").toString() : "");
                            photo.setVideo_id(objectMap.get("videoId") != null ? objectMap.get("videoId").toString() : "");
                            photo.setUser_id(objectMap.get("userId") != null ? objectMap.get("userId").toString() : "");
                            photo.setDate_Created(objectMap.get("dateCreated") != null ? objectMap.get("dateCreated").toString() : "");
                            photo.setThumbnail_Path(objectMap.get("thumbnailUrl") != null ? objectMap.get("thumbnailUrl").toString() : "");
                            photo.setVideo_path(objectMap.get("videoUrl") != null ? objectMap.get("videoUrl").toString() : "");


                            List<Comments> comments = new ArrayList<>();
                            firestoreDB.collection("videos")
                                    .whereEqualTo("userId", searchedUserId)
                                    .get()
                                    .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                        @Override
                                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                            for (DocumentSnapshot postSnapshot : queryDocumentSnapshots) {
                                                String postId = postSnapshot.getId();
                                                CollectionReference commentsRef = postSnapshot.getReference().collection("comments");
                                                commentsRef.get()
                                                        .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                                            @Override
                                                            public void onSuccess(QuerySnapshot commentSnapshots) {
                                                                for (DocumentSnapshot commentSnapshot : commentSnapshots) {
                                                                    // Convert each comment snapshot to a Comments object
                                                                    Comments comment = commentSnapshot.toObject(Comments.class);
                                                                    comments.add(comment);
                                                                }
                                                                // Do something with the comments list
                                                            }
                                                        })
                                                        .addOnFailureListener(new OnFailureListener() {
                                                            @Override
                                                            public void onFailure(@NonNull Exception e) {
                                                                Log.e(TAG, "Error getting comments for videos " + postId + ": " + e.getMessage());
                                                            }
                                                        });
                                            }
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Log.e(TAG, "Error getting posts: " + e.getMessage());
                                        }
                                    });
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
                            photos.add(photo);
                        }
                        System.out.println(photos);
                        //setup our image grid
                        int gridWidth = getResources().getDisplayMetrics().widthPixels;
                        int imageWidth = gridWidth / NUM_GRID_COLUMNS;
                        gridView2.setColumnWidth(imageWidth);
                        ArrayList<String> imgUrls = new ArrayList<>();
                        for (int i = 0; i < photos.size(); i++) {
                            imgUrls.add(photos.get(i).getThumbnail_Path());
                        }
                        GridImageAdapter adapter = new GridImageAdapter(UserSearchProfileActivity.this, R.layout.layout_grid_imageview,
                                "", imgUrls);
                        gridView2.setAdapter(adapter);
                        gridView2.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                Log.d(TAG, "Item Clicked Getting Bundle "+photos.get(position));
                                Intent intent=new Intent(UserSearchProfileActivity.this, UserSearchViewVideo.class);
                                intent.putExtra("videos",photos.get(position));
                                intent.putExtra("Commentcount",photos.get(position).getComments().size());

                                startActivity(intent);
                            }
                        });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "onFailure: query failed. " + e.getMessage());
                    }
                });

    }
    private void tempGridSetup(){

        Log.d(TAG, "setupGridView: Setting up image grid.");
        final ArrayList<Photo> photos = new ArrayList<>();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        reference.keepSynced(true);
        FirebaseFirestore firestoreDB = FirebaseFirestore.getInstance();
        firestoreDB.collection("posts")
                .whereEqualTo("userId", searchedUserId)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                            Photo photo = new Photo();
                            Map<String, Object> objectMap = documentSnapshot.getData();

                            photo.setCaption(objectMap.get("caption") != null ? objectMap.get("caption").toString() : "");
                            photo.setTags(objectMap.get("tags") != null ? objectMap.get("tags").toString() : "");
                            photo.setPhoto_id(objectMap.get("photo_id") != null ? objectMap.get("photo_id").toString() : "");
                            photo.setUser_id(objectMap.get("userId") != null ? objectMap.get("userId").toString() : "");
                            photo.setDate_Created(objectMap.get("date_Created") != null ? objectMap.get("date_Created").toString() : "");
                            photo.setImage_Path(objectMap.get("thumbnailUrl") != null ? objectMap.get("thumbnailUrl").toString() : "");

                            List<Comments> comments = new ArrayList<>();
                            firestoreDB.collection("posts")
                                    .whereEqualTo("userId", searchedUserId)
                                    .get()
                                    .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                        @Override
                                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                            for (DocumentSnapshot postSnapshot : queryDocumentSnapshots) {
                                                // Get the post ID to fetch comments
                                                String postId = postSnapshot.getId();
                                                // Reference the comments subcollection of this post
                                                CollectionReference commentsRef = postSnapshot.getReference().collection("comments");
                                                // Fetch comments for this post
                                                commentsRef.get()
                                                        .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                                            @Override
                                                            public void onSuccess(QuerySnapshot commentSnapshots) {
                                                                for (DocumentSnapshot commentSnapshot : commentSnapshots) {
                                                                    // Convert each comment snapshot to a Comments object
                                                                    Comments comment = commentSnapshot.toObject(Comments.class);
                                                                    comments.add(comment);
                                                                }
                                                                // Do something with the comments list
                                                            }
                                                        })
                                                        .addOnFailureListener(new OnFailureListener() {
                                                            @Override
                                                            public void onFailure(@NonNull Exception e) {
                                                                Log.e(TAG, "Error getting comments for post " + postId + ": " + e.getMessage());
                                                            }
                                                        });
                                            }
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Log.e(TAG, "Error getting posts: " + e.getMessage());
                                        }
                                    });
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
                            photos.add(photo);
                        }
                        //setup our image grid
                        int gridWidth = getResources().getDisplayMetrics().widthPixels;
                        int imageWidth = gridWidth / NUM_GRID_COLUMNS;
                        gridView1.setColumnWidth(imageWidth);
                        ArrayList<String> imgUrls = new ArrayList<>();
                        for (int i = 0; i < photos.size(); i++) {
                            imgUrls.add(photos.get(i).getImage_Path());
                        }
                        GridImageAdapter adapter = new GridImageAdapter(UserSearchProfileActivity.this, R.layout.layout_grid_imageview,
                                "", imgUrls);
                        gridView1.setAdapter(adapter);
                        gridView1.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                Log.d(TAG, "Item Clicked Getting Bundle "+photos.get(position));
                                Intent intent=new Intent(UserSearchProfileActivity.this, UserSearchViewPost.class);
                                intent.putExtra("posts",photos.get(position));
                                intent.putExtra("Commentcount",photos.get(position).getComments().size());

                                startActivity(intent);
                            }
                        });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "onFailure: query failed. " + e.getMessage());
                    }
                });


    }

    private void setFollowing(){
        Log.d(TAG, "setFollowing: updating UI for following this user");
        Follow.setVisibility(View.GONE);
        FollowBack.setVisibility(View.GONE);
        Following.setVisibility(View.VISIBLE);
    }

    private void setUnfollowing(){
        Log.d(TAG, "setFollowing: updating UI for unfollowing this user");
        Follow.setVisibility(View.VISIBLE);
        FollowBack.setVisibility(View.GONE);
        Following.setVisibility(View.GONE);
    }

    private void setFollowBack() {
        Follow.setVisibility(View.GONE);
        FollowBack.setVisibility(View.VISIBLE);
        Following.setVisibility(View.GONE);
    }

    private void isFollowing(){
        Log.d(TAG, "isFollowing: checking if following this users.");


        FirebaseFirestore db = FirebaseFirestore.getInstance();
        // set follow back
        DocumentReference userRef1 = db.collection("Users").document(FirebaseAuth.getInstance().getCurrentUser().getUid());
        userRef1.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()) {
                    ArrayList<String> followers = (ArrayList<String>) documentSnapshot.get("followers");
                    ArrayList<String> following = (ArrayList<String>) documentSnapshot.get("following");
                    if (followers != null && following.contains(searchedUserId)) {
                        setFollowing();
                    }
                    else if (followers != null && followers.contains(searchedUserId)) {
                        // searchedUserId đang theo dõi người dùng hiện tại
                        Log.d(TAG, "onSuccess: found user:" + documentSnapshot.getData());
                        setFollowBack();
                    }
                    else {
                        Log.d(TAG, "onSuccess: unfollowing:" + documentSnapshot.getData());
                        setUnfollowing();
                    }
                }
            }
        });
    }

    public void updateFollowOtherUser() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference userRef = db.collection("Users").document(searchedUserId);

        userRef.get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            ArrayList<String> followersList = (ArrayList<String>) documentSnapshot.get("followers");
                            ArrayList<String> followingList = (ArrayList<String>) documentSnapshot.get("following");

                            noFollowers = String.valueOf(followersList.size());
                            noFollowings = String.valueOf(followingList.size());

                            followers.setText(noFollowers);
                            followings.setText(noFollowings);
                        } else {
                            Log.d(TAG, "onSuccess: Document does not exist");
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Error getting document: ", e);
                    }
                });
    }
    private String getTimestamp() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(new Date());
    }

    private void addFollowNotification(String userid){
        Notification notification = new Notification(userid,FirebaseAuth.getInstance().getCurrentUser().getUid(), " started following you ", "", false, false,getTimestamp());
        FirebaseFirestore mFirestore =  FirebaseFirestore.getInstance();
        mFirestore.collection("Notifications")
                .add(notification)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d("Home FragPost", "Notification added successfully");
                    } else {
                        Log.e(TAG, "Error adding notification", task.getException());
                    }
                });
    }
    public void onResume() {
        super.onResume();
        RetrivingGeneralData();
    }
}