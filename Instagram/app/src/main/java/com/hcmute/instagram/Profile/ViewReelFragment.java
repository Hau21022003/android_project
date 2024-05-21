package com.hcmute.instagram.Profile;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

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
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.hcmute.instagram.R;
import com.hcmute.instagram.Utils.Heart;
import com.hcmute.instagram.Utils.SquareImageView;
import com.hcmute.instagram.Utils.SquareVideoView;
import com.hcmute.instagram.Utils.UniversalImageLoader;
import com.hcmute.instagram.models.Likes;
import com.hcmute.instagram.models.Users;
import com.hcmute.instagram.models.Video;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class ViewReelFragment extends Fragment {

    private static final String TAG = "ViewVideoFragment";

    public ViewReelFragment(){
        super();
        setArguments(new Bundle());
    }

    // Firestore instance
    private FirebaseFirestore firestore;

    //widgets
    private SquareVideoView mVideo;
    private TextView mCaption, mUsername, mTimestamp, mTags, mLikes, mtotalComments;
    private ImageView mBackArrow, mComments, mHeartRed, mHeart, mProfileImage, moption, msend;
    String lcaption, ltags, lusername;
    private ProgressBar mProgressBar;

    //vars
    Video mPhoto;
    private Heart mheart;
    Boolean mLikedByCurrentUser=false;
    StringBuilder mUsers;
    Users user;
    String mLikesString = "";

    private GestureDetector mGestureDetector;

    DatabaseReference databaseReference, ref;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_reel_viewer, container, false);

        firestore = FirebaseFirestore.getInstance();

        mVideo = view.findViewById(R.id.post_imagee);
        mBackArrow = view.findViewById(R.id.back_from_post_viewer);
        mCaption = view.findViewById(R.id.txt_caption);
        mTags = view.findViewById(R.id.txt_tags);
        mUsername = view.findViewById(R.id.username);
        mTimestamp = view.findViewById(R.id.txt_timePosted);
        mtotalComments = view.findViewById(R.id.txt_commments);
        mLikes = view.findViewById(R.id.txt_likes);
        mComments = view.findViewById(R.id.img_comments);
        mHeartRed = view.findViewById(R.id.img_heart_red);
        mHeart = view.findViewById(R.id.img_heart);
        mProfileImage = view.findViewById(R.id.user_img);
        moption = view.findViewById(R.id.option);
        msend = view.findViewById(R.id.img_send);
        mProgressBar = view.findViewById(R.id.viewpostProgressBar);

        mheart = new Heart(mHeart, mHeartRed);
        mGestureDetector = new GestureDetector(getActivity(), new GestureListener());

        try {
            mPhoto = getVideofromBundle();
            System.out.println("09123" + mPhoto.getVideo_id());
            UniversalImageLoader.setVideo(mPhoto.getVideo_path(), mVideo, null, "");
            retrievingData();
            getLikesString();

        } catch (NullPointerException e) {
            Log.e(TAG, "onCreateView: NullPointerException: " + e.getMessage());
        }

        return view;
    }


    private Video getVideofromBundle(){
        Bundle bundle = this.getArguments();
        if(bundle != null) {
            return bundle.getParcelable("videos");
        }else{
            return null;
        }
    }

    private void retrievingData() {
        String userid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DocumentReference userRef = firestore.collection("Users").document(userid);
        userRef.get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        user = documentSnapshot.toObject(Users.class);
                        if (user != null) {
                            Glide.with(ViewReelFragment.this)
                                    .load(user.getProfilePhoto())
                                    .into(mProfileImage);
                            lcaption = mPhoto.getCaption();
                            ltags = mPhoto.getTags();
                            lusername = user.getUsername();

                            mTags.setText(ltags);
                            mUsername.setText(lusername);
                            mProgressBar.setVisibility(View.GONE);

                            mUsername.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Fragment fragment = new ProfileFragment();
                                    FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                                    fragmentTransaction.replace(R.id.frameMainLayout, fragment);
                                    fragmentTransaction.addToBackStack(null);
                                    fragmentTransaction.commit();
                                }
                            });

                            mProfileImage.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Fragment fragment = new ProfileFragment();
                                    FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                                    fragmentTransaction.replace(R.id.frameMainLayout, fragment);
                                    fragmentTransaction.addToBackStack(null);
                                    fragmentTransaction.commit();
                                }
                            });
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Error retrieving user data: " + e.getMessage());
                    }
                });
    }

    private void getLikesString() {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        firestore.collection("videos").document(mPhoto.getVideo_id()).collection("likes")
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        mUsers = new StringBuilder();
                        System.out.println(queryDocumentSnapshots.size());
                        int likeCount = queryDocumentSnapshots.size();
                        if (likeCount == 0) {
                            mLikesString = "No likes yet";
                            setupWidgets();

                        } else {
                            for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                                String userId = documentSnapshot.getString("user_id");
                                fetchUsername(userId);
                            }
                        }
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
                        System.out.println('1' + username);
                        mUsers.append(username).append(",");
                        System.out.println('2' + username);
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

    private void checkIfCurrentUserLiked(String username) {
        String[] splitUsers = mUsers.toString().split(",");
        int length = splitUsers.length;
        for (String user : splitUsers) {
            if (user.trim().equals(username)) {
                mLikedByCurrentUser = true;
                break;
            }
        }
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

        setupWidgets();
    }



    private void setupWidgets() {
        String timestampDiff = getTimestampDifference();
        if (!timestampDiff.equals("0")) {
            mTimestamp.setText(timestampDiff + " days ago");
        } else {
            mTimestamp.setText("Today");
        }
        System.out.println(mPhoto.getComments());

        mLikes.setText(mLikesString);
        mCaption.setText(mPhoto.getCaption());

        if (mPhoto.getComments().size() > 0) {
            mtotalComments.setText("View all " +" comments");
        } else {
            mtotalComments.setText("");
        }
        if (mLikedByCurrentUser) {
            mHeart.setVisibility(View.GONE);
            mHeartRed.setVisibility(View.VISIBLE);
            mHeartRed.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Remove like when heart icon is tapped
                    removeLike();
                    mLikedByCurrentUser = false; // Update liked status
                    mHeart.setVisibility(View.VISIBLE);
                    mHeartRed.setVisibility(View.GONE);
                }
            });
        } else {
            mHeart.setVisibility(View.VISIBLE);
            mHeartRed.setVisibility(View.GONE);
            mHeart.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Add like when heart icon is tapped
                    addNewLike();
                    mLikedByCurrentUser = true; // Update liked status
                    mHeart.setVisibility(View.GONE);
                    mHeartRed.setVisibility(View.VISIBLE);

                }
            });
        }

        mtotalComments.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent b = new Intent(getActivity(), ViewVideoComments.class);
                Bundle bundle = new Bundle();
                bundle.putParcelable("videos", mPhoto);
                System.out.println("333"+ mPhoto);
                b.putExtra("commentcount", mPhoto.getComments().size());
                b.putExtras(bundle);
                startActivity(b);
            }
        });
        // Inside the setupWidgets() method



        mBackArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });

        mComments.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent b = new Intent(getActivity(), ViewVideoComments.class);
                Bundle bundle = new Bundle();
                bundle.putParcelable("videos", mPhoto);
                b.putExtras(bundle);
                System.out.println("333"+ mPhoto);

                b.putExtra("commentcount", mPhoto.getComments().size());
                startActivity(b);
            }
        });


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
            Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
            difference = "0";
        }
        return difference;
    }

    public class GestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            // Handle like action
            return true;
        }
    }

    private void addNewLike() {
        DocumentReference newLikeRef = firestore.collection("videos").document(mPhoto.getVideo_id()).collection("likes").document();
        Likes like = new Likes();
        like.setUser_id(FirebaseAuth.getInstance().getCurrentUser().getUid());
        newLikeRef.set(like)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        mheart.toggleLike();
                        getLikesString();
                        addLikeNotification(mPhoto.getUser_id(), mPhoto.getVideo_id());
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Error adding like: " + e.getMessage());
                    }
                });
    }
    private void removeLike() {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        firestore.collection("videos").document(mPhoto.getVideo_id())
                .collection("likes").whereEqualTo("user_id", currentUserId)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        for (DocumentSnapshot snapshot : queryDocumentSnapshots) {
                            snapshot.getReference().delete()
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            // Update UI after removing like
                                            getLikesString();
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


    private void addLikeNotification(String userid, String postid) {
    }

    public void onResume() {
        super.onResume();
        this.getView().setFocusableInTouchMode(true);
        this.getView().requestFocus();
        this.getView().setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    getActivity().getSupportFragmentManager().popBackStack();
                }
                return true;
            }
        });
    }
}
