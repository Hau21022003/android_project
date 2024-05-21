package com.hcmute.instagram.Profile;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.hcmute.instagram.R;
import com.hcmute.instagram.Utils.GridImageAdapter;
import com.hcmute.instagram.models.Comments;
import com.hcmute.instagram.models.Likes;
import com.hcmute.instagram.models.Photo;
import com.hcmute.instagram.models.Users;
import com.hcmute.instagram.models.Video;
import com.hcmute.instagram.models.privatedetails;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ProfileFragment extends Fragment {

    private static final int NUM_GRID_COLUMNS = 3;
    private static final String TAG = "ProfileFragment";

    ImageView account_setting_menu;
    Button editProfile;
    ImageView profilePhoto;
    GridView gridView, gridView2;
    TextView posts, followers, followings, name, description, website, username;
    LinearLayout follower, following;
    String noFollowers, noFollowings;
    CollectionReference usersCollectionRef;
    private ProgressBar mProgressBar;
    private TabHost tabHost;
    private TabWidget tabWidget;

    @Nullable
    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_profile, container, false);

        account_setting_menu = v.findViewById(R.id.account_settingMenu);
        editProfile = v.findViewById(R.id.edit_profile);
        profilePhoto = v.findViewById(R.id.user_img);
        gridView = v.findViewById(R.id.gridview1);
        gridView2 = v.findViewById(R.id.gridview2);
        posts = v.findViewById(R.id.txtPosts);
        followers = v.findViewById(R.id.txtFollowers);
        followings = v.findViewById(R.id.txtFollowing);
        name = v.findViewById(R.id.display_name);
        description = v.findViewById(R.id.description);
        website = v.findViewById(R.id.website);
        username = v.findViewById(R.id.profileName);
        follower = v.findViewById(R.id.FragmentProfile_followerLinearLayout);
        following = v.findViewById(R.id.FragmentProfile_followingLinearLayout);
        mProgressBar = v.findViewById(R.id.profileProgressBar);
        tabHost = v.findViewById(R.id.tabhost);
        tabWidget = v.findViewById(android.R.id.tabs);

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

        account_setting_menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), Account_Settings.class);
                startActivity(intent);
            }
        });

        editProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), EditProfile.class);
                startActivity(intent);
            }
        });

        follower.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), FollowersFollowing.class);
                intent.putExtra("id", FirebaseAuth.getInstance().getCurrentUser().getUid());
                intent.putExtra("title", "Followers");
                intent.putExtra("number", noFollowers);
                startActivity(intent);
            }
        });

        following.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), FollowersFollowing.class);
                intent.putExtra("id", FirebaseAuth.getInstance().getCurrentUser().getUid());
                intent.putExtra("title", "Following");
                intent.putExtra("number", noFollowings);
                startActivity(intent);
            }
        });

        return v;
    }

    private void loadTab1Data() {
        tempGridSetup();
    }

    private void loadTab2Data() {
        videoGridSetup();
    }

    private void tempGridSetup() {
        Log.d(TAG, "setupGridView: Setting up image grid.");
        final ArrayList<Photo> photos = new ArrayList<>();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        reference.keepSynced(true);
        FirebaseFirestore firestoreDB = FirebaseFirestore.getInstance();
        firestoreDB.collection("posts")
                .whereEqualTo("userId", FirebaseAuth.getInstance().getCurrentUser().getUid())
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
                                    .whereEqualTo("userId", FirebaseAuth.getInstance().getCurrentUser().getUid())
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
                        gridView.setColumnWidth(imageWidth);
                        ArrayList<String> imgUrls = new ArrayList<>();
                        for (int i = 0; i < photos.size(); i++) {
                            imgUrls.add(photos.get(i).getImage_Path());
                        }
                        GridImageAdapter adapter = new GridImageAdapter(getActivity(), R.layout.layout_grid_imageview,
                                "", imgUrls);
                        gridView.setAdapter(adapter);
                        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                ViewPostFragment fragment = new ViewPostFragment();
                                Bundle args = new Bundle();
                                args.putParcelable("posts", photos.get(position));
                                fragment.setArguments(args);
                                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                                fragmentTransaction.replace(ProfileFragment.this.getId(), fragment);
                                fragmentTransaction.addToBackStack(null);
                                fragmentTransaction.commit();
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
    private void videoGridSetup() {
        Log.d(TAG, "setupGridView: Setting up image grid.");
        final ArrayList<Video> photos = new ArrayList<>();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        reference.keepSynced(true);
        FirebaseFirestore firestoreDB = FirebaseFirestore.getInstance();
        firestoreDB.collection("videos")
                .whereEqualTo("userId", FirebaseAuth.getInstance().getCurrentUser().getUid())
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
                                    .whereEqualTo("userId", FirebaseAuth.getInstance().getCurrentUser().getUid())
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
                        GridImageAdapter adapter = new GridImageAdapter(getActivity(), R.layout.layout_grid_imageview,
                                "", imgUrls);
                        gridView2.setAdapter(adapter);
                        gridView2.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                ViewReelFragment fragment = new ViewReelFragment();
                                Bundle args = new Bundle();
                                args.putParcelable("videos", photos.get(position));
                                fragment.setArguments(args);
                                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                                fragmentTransaction.replace(ProfileFragment.this.getId(), fragment);
                                fragmentTransaction.addToBackStack(null);
                                fragmentTransaction.commit();
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
    private void RetrivingGeneralData() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        Log.i("userid", "user id: " + userId);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        usersCollectionRef = db.collection("Users");
        usersCollectionRef.document(userId).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            Users user = documentSnapshot.toObject(Users.class);

                            // Update UI with user data
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
                            Glide.with(ProfileFragment.this)
                                    .load(user.getProfilePhoto())
                                    .into(profilePhoto);
                            mProgressBar.setVisibility(View.GONE);
                        } else {
                            Log.d("TAG", "No such document");
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("TAG", "Error getting documents: " + e);
                    }
                });
    }

    @Override
    public void onResume() {
        super.onResume();
        RetrivingGeneralData();
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
}
