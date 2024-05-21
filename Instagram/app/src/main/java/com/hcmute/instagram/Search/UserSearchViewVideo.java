package com.hcmute.instagram.Search;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.hcmute.instagram.Profile.ViewComments;
import com.hcmute.instagram.Profile.ViewVideoComments;
import com.hcmute.instagram.R;
import com.hcmute.instagram.Utils.Heart;
import com.hcmute.instagram.Utils.SquareImageView;
import com.hcmute.instagram.Utils.SquareVideoView;
import com.hcmute.instagram.Utils.UniversalImageLoader;
import com.hcmute.instagram.models.Likes;
import com.hcmute.instagram.models.Notification;
import com.hcmute.instagram.models.Photo;
import com.hcmute.instagram.models.Users;
import com.hcmute.instagram.models.Video;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;

public class UserSearchViewVideo extends AppCompatActivity {

    private static final String TAG = "UserSearchViewReel";

    private SquareVideoView mPostImage;
    private TextView mCaption, mUsername, mTimestamp, mTags, mLikes, mtotalComments;
    private ImageView mBackArrow, mComments, mHeartRed, mHeart, mProfileImage, moption, msend;
    private ProgressBar mProgressBar;

    private Video mPhoto;
    private Heart mheart;
    private boolean mLikedByCurrentUser;
    private StringBuilder mUsers;
    private Users user;
    private String mLikesString = "";
    private Integer Commentcount;
    private Users mCurrentUser;

    private GestureDetector mGestureDetector;

    private DatabaseReference databaseReference, ref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reel_viewer);

        mPostImage = findViewById(R.id.post_imagee);
        mBackArrow = findViewById(R.id.back_from_post_viewer);
        mCaption = findViewById(R.id.txt_caption);
        mTags = findViewById(R.id.txt_tags);
        mUsername = findViewById(R.id.username);
        mTimestamp = findViewById(R.id.txt_timePosted);
        mtotalComments = findViewById(R.id.txt_commments);
        mLikes = findViewById(R.id.txt_likes);
        mComments = findViewById(R.id.img_comments);
        mHeartRed = findViewById(R.id.img_heart_red);
        mHeart = findViewById(R.id.img_heart);
        mProfileImage = findViewById(R.id.user_img);
        moption = findViewById(R.id.option);
        msend = findViewById(R.id.img_send);
        mProgressBar = findViewById(R.id.viewpostProgressBar);

        mheart = new Heart(mHeart, mHeartRed);
//        mGestureDetector = new GestureDetector(UserSearchViewPost.this, new GestureListener());

        try {
            mPhoto = getPhotoFromBundle();
            System.out.println("das"+ mPhoto);
            checkIfUserLikedPost(mPhoto.getVideo_id());

            // Assuming UniversalImageLoader is your custom image loading library
            UniversalImageLoader.setVideo(mPhoto.getVideo_path(), mPostImage, null, "");
            Commentcount = getIntent().getIntExtra("Commentcount", 0);
            retrievingData();
            getCurrentUser();
            System.out.println("chacha" + mCurrentUser);
            getLikesString();
        } catch (NullPointerException e) {
            Log.e(TAG, "onCreate: NullPointerException: " + e.getMessage());
        }
    }

    private Video getPhotoFromBundle() {
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            return bundle.getParcelable("videos");
        } else {
            return null;
        }
    }

    private void getCurrentUser() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference usersRef = db.collection("Users");
        usersRef.whereEqualTo("user_id", FirebaseAuth.getInstance().getCurrentUser().getUid())
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                mCurrentUser = document.toObject(Users.class);
                                System.out.println("casd" + mCurrentUser);
                            }
                        } else {
                            Log.e(TAG, "Error getting current user: ", task.getException());
                        }
                    }
                });
    }


//    public class GestureListener extends GestureDetector.SimpleOnGestureListener {
//        @Override
//        public boolean onDown(MotionEvent e) {
//            return true;
//        }
//
//        @Override
//        public boolean onSingleTapConfirmed(MotionEvent e) {
//            FirebaseFirestore db = FirebaseFirestore.getInstance();
//            CollectionReference likesRef = db.collection("posts")
//                    .document(mPhoto.getPhoto_id())
//                    .collection("likes");
//
//            likesRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
//                @Override
//                public void onComplete(@NonNull Task<QuerySnapshot> task) {
//                    if (task.isSuccessful()) {
//                        QuerySnapshot querySnapshot = task.getResult();
//                        if (querySnapshot != null) {
//                            for (QueryDocumentSnapshot documentSnapshot : querySnapshot) {
//                                Likes like = documentSnapshot.toObject(Likes.class);
//                                String keyID = documentSnapshot.getId();
//                                if (mLikedByCurrentUser && like.getUser_id().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
//                                    likesRef.document(keyID)
//                                            .delete()
//                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
//                                                @Override
//                                                public void onSuccess(Void aVoid) {
//                                                    mheart.toggleLike();
//                                                    getLikesString();
//                                                }
//                                            })
//                                            .addOnFailureListener(new OnFailureListener() {
//                                                @Override
//                                                public void onFailure(@NonNull Exception e) {
//                                                    Log.e(TAG, "Error removing like: " + e.getMessage());
//                                                }
//                                            });
//                                } else if (!mLikedByCurrentUser) {
//                                    addNewLike();
//                                    break;
//                                }
//                            }
//                            if (querySnapshot.isEmpty()) {
//                                addNewLike();
//                            }
//                        }
//                    } else {
//                        Log.e(TAG, "Error getting likes: ", task.getException());
//                    }
//                }
//            });
//
//            return true;
//        }
//    }



    private void getLikesString() {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        firestore.collection("videos").document(mPhoto.getVideo_id()).collection("likes")
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        mUsers = new StringBuilder();
                        HashSet<String> userIdSet = new HashSet<>(); // HashSet để lưu trữ userId duy nhất

                        for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                            String userId = documentSnapshot.getString("user_id");
                            userIdSet.add(userId); // Thêm userId vào HashSet
                        }

                        for (String userId : userIdSet) {
                            System.out.println("123asd" + userId);
                            fetchUsername(userId);
                        }

                        if (queryDocumentSnapshots.isEmpty()) {
                            mLikesString = "No likes yet";
                        }
                        // Call setupWidgets() after retrieving likes
                        setupWidgets();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Error fetching likes: " + e.getMessage());
                    }
                });
    }




    private void fetchUsername(String userId) {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        firestore.collection("Users").document(userId)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        String username = documentSnapshot.getString("username");
                        mUsers.append(username).append(",");
                        checkIfCurrentUserLiked(username);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Error fetching username: " + e.getMessage());
                    }
                });
    }
    private void checkIfUserLikedPost(String postId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        db.collection("videos")
                .document(postId)
                .collection("likes")
                .whereEqualTo("user_id", currentUserId)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            mLikedByCurrentUser = true;
                        } else {
                            mLikedByCurrentUser = false;
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Error checking like: " + e.getMessage());
                    }
                });
    }


    private void checkIfCurrentUserLiked(String username) {
        String[] splitUsers = mUsers.toString().split(",");

        int length = splitUsers.length;

        if (length == 0) {
            mLikesString = "No likes yet";
        } else if (length == 1) {
            mLikesString = "Liked by " + splitUsers[0];
        } else if (length == 2) {
            mLikesString = "Liked by " + splitUsers[0] + " and " + splitUsers[1];
        } else if (length == 3) {
            mLikesString = "Liked by " + splitUsers[0] + ", " + splitUsers[1] + " and " + splitUsers[2];
        } else if (length == 4) {
            mLikesString = "Liked by " + splitUsers[0] + ", " + splitUsers[1] + ", " + splitUsers[2] + " and " + splitUsers[3];
        } else if (length > 4) {
            mLikesString = "Liked by " + splitUsers[0] + ", " + splitUsers[1] + ", " + splitUsers[2] + " and " + (splitUsers.length - 3) + " others";
        }
        System.out.println("asdasss" + mLikesString);

        mLikes.setText(mLikesString);
        setupWidgets();
    }




    private void addNewLike() {
        if (!mLikedByCurrentUser) {
            FirebaseFirestore db = FirebaseFirestore.getInstance();

            // Generate a new document reference within the "likes" collection
            DocumentReference newLikeRef = db.collection("videos")
                    .document(mPhoto.getVideo_id())
                    .collection("likes")
                    .document();

            Likes like = new Likes();
            like.setUser_id(FirebaseAuth.getInstance().getCurrentUser().getUid());

            newLikeRef.set(like)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            mheart.toggleLike();
                            getLikesString();
                            addLikeNotification(mPhoto.getUser_id(), mPhoto.getVideo_id());
                            mLikedByCurrentUser = true; // Update liked status
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.e(TAG, "Error adding like: " + e.getMessage());
                        }
                    });
        }
    }

    private void removeLike() {
        if (mLikedByCurrentUser) {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("videos").document(mPhoto.getVideo_id())
                    .collection("likes").whereEqualTo("user_id", FirebaseAuth.getInstance().getCurrentUser().getUid())
                    .get()
                    .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                            for (QueryDocumentSnapshot snapshot : queryDocumentSnapshots) {
                                snapshot.getReference().delete()
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                // Update UI after removing like
                                                getLikesString();
                                                mLikedByCurrentUser = false; // Update liked status
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Log.e(TAG, "Error removing like: " + e.getMessage());
                                            }
                                        });
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.e(TAG, "Error removing like: " + e.getMessage());
                        }
                    });
        }
    }


    private String getTimestampDifference() {
        String difference = "";
        Calendar c = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date today = c.getTime();
        sdf.format(today);
        Date timestamp;
        final String photoTimestamp = mPhoto.getDate_Created();
        try {
            timestamp = sdf.parse(photoTimestamp);
            difference = String.valueOf(Math.round(((today.getTime() - timestamp.getTime()) / 1000 / 60 / 60 / 24)));
        } catch (ParseException e) {
            Toast.makeText(UserSearchViewVideo.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            difference = "0";
        }
        return difference;
    }

    private void setupWidgets() {
        String timestampDiff = getTimestampDifference();
        if (!timestampDiff.equals("0")) {
            mTimestamp.setText(timestampDiff + " days ago");
        } else {
            mTimestamp.setText("Today");
        }
        checkIfUserLikedPost(mPhoto.getVideo_id());
        mLikes.setText(mLikesString);
        mCaption.setText(mPhoto.getCaption());
        if (mPhoto.getComments() != null && mPhoto.getComments().size() > 0) {
            mtotalComments.setText("View all " + mPhoto.getComments().size() + " comments");
        } else {
            mtotalComments.setText("View all comments   ");
        }
        mtotalComments.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent b = new Intent(UserSearchViewVideo.this, ViewVideoComments.class);
                Bundle bundle = new Bundle();
                bundle.putParcelable("videos", mPhoto);
                System.out.println("haha" + mPhoto);
                b.putExtras(bundle);
                startActivity(b);
            }
        });

        mComments.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent b = new Intent(UserSearchViewVideo.this, ViewVideoComments.class);
                Bundle bundle = new Bundle();
                bundle.putParcelable("videos", mPhoto);
                b.putExtras(bundle);
                startActivity(b);
            }
        });

        // Remove the click listener for mHeart if the user has already liked the post
        mHeart.setVisibility(View.VISIBLE);
        mHeartRed.setVisibility(View.GONE);
        // Remove the click listener for mHeart if the user has already liked the post
        if (mLikedByCurrentUser) {
            // User has already liked the post
            mHeart.setVisibility(View.GONE);
            mHeartRed.setVisibility(View.VISIBLE);
        } else {
            // User hasn't liked the post yet
            mHeart.setVisibility(View.VISIBLE);
            mHeartRed.setVisibility(View.GONE);
        }

        // Set click listeners for like buttons
        mHeart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Add like when gray heart icon is tapped
                addNewLike();
                mLikedByCurrentUser = true; // Update liked status
                mHeart.setVisibility(View.GONE);
                mHeartRed.setVisibility(View.VISIBLE);
            }
        });

        mHeartRed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Remove like when red heart icon is tapped
                removeLike();
                mLikedByCurrentUser = false; // Update liked status
                mHeart.setVisibility(View.VISIBLE);
                mHeartRed.setVisibility(View.GONE);
            }
        });
    }


    private void retrievingData() {
        String userId = mPhoto.getUser_id();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Get user data
        DocumentReference userRef = db.collection("Users").document(userId);
        userRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()) {
                    user = documentSnapshot.toObject(Users.class);
                    Glide.with(UserSearchViewVideo.this)
                            .load(user.getProfilePhoto())
                            .into(mProfileImage);
                    System.out.println("adasw" + mPhoto);
                    mTags.setText(mPhoto.getTags());
                    mCaption.setText(mPhoto.getCaption());
                    mUsername.setText(user.getUsername());
                    mProgressBar.setVisibility(View.GONE);

                    mUsername.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent=new Intent(UserSearchViewVideo.this, UserSearchProfileActivity.class);
                            intent.putExtra("SearchedUserid",user.getUser_id());
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                        }
                    });

                    mProfileImage.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent=new Intent(UserSearchViewVideo.this, UserSearchProfileActivity.class);
                            intent.putExtra("SearchedUserid",user.getUser_id());
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                        }
                    });

                } else {
                    Log.d(TAG, "No such user document");
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e(TAG, "Error retrieving user document: " + e.getMessage());
            }
        });
    }


    private void addLikeNotification(String userid, String postid) {
        if (userid.equals(FirebaseAuth.getInstance().getCurrentUser().getUid())){
            return;
        }
        Notification notification = new Notification(userid, FirebaseAuth.getInstance().getCurrentUser().getUid(), " liked your post ", postid, true, false,getTimestamp());

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
    private String getTimestamp() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(new Date());
    }
}
