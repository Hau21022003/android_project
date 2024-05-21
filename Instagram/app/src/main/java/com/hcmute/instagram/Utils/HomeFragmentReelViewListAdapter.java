    package com.hcmute.instagram.Utils;

    import android.animation.AnimatorSet;
    import android.animation.ObjectAnimator;
    import android.content.Context;
    import android.content.Intent;
    import android.media.MediaPlayer;
    import android.os.Bundle;
    import android.util.Log;
    import android.view.GestureDetector;
    import android.view.LayoutInflater;
    import android.view.MotionEvent;
    import android.view.View;
    import android.view.ViewGroup;
    import android.view.animation.OvershootInterpolator;
    import android.widget.ArrayAdapter;
    import android.widget.ImageView;
    import android.widget.LinearLayout;
    import android.widget.MediaController;
    import android.widget.ProgressBar;
    import android.widget.TextView;
    import android.widget.VideoView;

    import androidx.annotation.LayoutRes;
    import androidx.annotation.NonNull;
    import androidx.annotation.Nullable;
    import androidx.recyclerview.widget.RecyclerView;

    import com.firebase.ui.database.FirebaseRecyclerAdapter;
    import com.firebase.ui.database.FirebaseRecyclerOptions;
    import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
    import com.firebase.ui.firestore.FirestoreRecyclerOptions;
    import com.google.android.gms.tasks.OnFailureListener;
    import com.google.android.gms.tasks.OnSuccessListener;
    import com.google.firebase.auth.FirebaseAuth;
    import com.google.firebase.database.DataSnapshot;
    import com.google.firebase.database.DatabaseError;
    import com.google.firebase.database.DatabaseReference;
    import com.google.firebase.database.FirebaseDatabase;
    import com.google.firebase.database.Query;
    import com.google.firebase.database.ValueEventListener;
    import com.google.firebase.firestore.DocumentReference;
    import com.google.firebase.firestore.DocumentSnapshot;
    import com.google.firebase.firestore.FirebaseFirestore;
    import com.google.firebase.firestore.QuerySnapshot;
    import com.hcmute.instagram.Profile.ViewComments;
    import com.hcmute.instagram.Profile.ViewReelComments;
    import com.hcmute.instagram.Profile.ViewVideoComments;
    import com.hcmute.instagram.R;
    import com.hcmute.instagram.Retrofit.RetrofitClient;
    import com.hcmute.instagram.Search.UserSearchProfileActivity;
    import com.hcmute.instagram.models.Comments;
    import com.hcmute.instagram.models.Likes;
    import com.hcmute.instagram.models.Notification;
    import com.hcmute.instagram.models.Photo;
    import com.hcmute.instagram.models.Reel;
    import com.hcmute.instagram.models.Users;
    import com.hcmute.instagram.models.Video;
    import com.nostra13.universalimageloader.core.ImageLoader;

    import java.text.ParseException;
    import java.text.SimpleDateFormat;
    import java.util.Calendar;
    import java.util.Date;
    import java.util.HashMap;
    import java.util.List;
    import java.util.Objects;

    import com.google.firebase.firestore.FirebaseFirestore;
    import de.hdodenhof.circleimageview.CircleImageView;
    import retrofit2.Call;
    import retrofit2.Callback;
    import retrofit2.Response;

    public class HomeFragmentReelViewListAdapter extends FirestoreRecyclerAdapter<Reel, HomeFragmentReelViewListAdapter.MyHolder> {



        public HomeFragmentReelViewListAdapter(@NonNull FirestoreRecyclerOptions<Reel> options, Context context) {
            super(options);
            mContext = context;
        }
        @NonNull
        @Override
        public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_home_reel_viewer, parent, false);
            return new MyHolder(view);
        }
        public interface OnLoadMoreItemsListener{
            void onLoadMoreItems();
        }
        OnLoadMoreItemsListener mOnLoadMoreItemsListener;

        private static final String TAG = "HomeReelViewListAdapter";
        private Context mContext;
        Reel photos;


        public class MyHolder extends RecyclerView.ViewHolder {
            private FirebaseFirestore firestore;

            private LayoutInflater mInflater;
            private int mLayoutResource;
            private DatabaseReference mReference;
            private String currentUsername = "";
            private ProgressBar mProgressBar;
            private String UserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

            CircleImageView mprofileImage;
            String likesString = "";
            TextView username, timeDetla, caption, likes, comments,mTags, showToxicContent;
            LinearLayout toxicContentLayout;

            SquareVideoView video, videosss;
            SquareImageView image;
            ImageView heartRed, heartWhite, comment;

            Users settings = new Users();
            //        privatedetails user  = new privatedetails();
            StringBuilder users;
            //        String mLikesString;
            boolean likeByCurrentUser;
            Heart heart;
            GestureDetector detector;
            public MyHolder(@NonNull View convertView) {
                super(convertView);
                firestore = FirebaseFirestore.getInstance();
                username = convertView.findViewById(R.id.fragment_home_post_viewer_username);
                video = convertView.findViewById(R.id.fragment_home_post_viewer_video_video_video);
                heartRed = convertView.findViewById(R.id.fragment_home_post_viewer_img_heart_red);
                heartWhite = convertView.findViewById(R.id.fragment_home_post_viewer_img_heart);
                comment = convertView.findViewById(R.id.fragment_home_post_viewer_img_comments);
                likes = convertView.findViewById(R.id.fragment_home_post_viewer_txt_likes);
                comments = convertView.findViewById(R.id.fragment_home_post_viewer_txt_commments);
                caption = convertView.findViewById(R.id.fragment_home_post_viewer_txt_caption);
                timeDetla = convertView.findViewById(R.id.fragment_home_post_viewer_txt_timePosted);
                mprofileImage = convertView.findViewById(R.id.fragment_home_post_viewer_user_img);
                mTags = convertView.findViewById(R.id.fragment_home_post_viewer_txt_tags);
                toxicContentLayout = convertView.findViewById(R.id.fragment_home_real_viewer_toxic_layout);
                showToxicContent = convertView.findViewById(R.id.fragment_home_real_viewer_txt_show_toxic_content);
                heart = new Heart(heartWhite, heartRed);
                mProgressBar = convertView.findViewById(R.id.viewpostProgressBar);
                users = new StringBuilder();


            }
        }




        protected void onBindViewHolder(@NonNull MyHolder holder, int position, @NonNull Reel model) {
            System.out.println("SSSS" + model.getVideoId()); // Use model.getVideoId() instead of photos.getVideoId()
            System.out.println(model);
            getCurrentUsername(holder);
            getLikesString(holder,model);
            setupLikesString(holder,holder.likesString, model);

            String caption = model.getCaption();
            RetrofitClient.getAPIService().postPredictContent(caption).enqueue(new Callback<Boolean>() {
                @Override
                public void onResponse(Call<Boolean> call, Response<Boolean> response) {
                    Boolean isToxicContent = response.body();
                    if(isToxicContent) {
                        holder.caption.setVisibility(View.GONE);
                        holder.toxicContentLayout.setVisibility(View.VISIBLE);
                        holder.showToxicContent.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                holder.caption.setVisibility(View.VISIBLE);
                                holder.toxicContentLayout.setVisibility(View.GONE);
                            }
                        });
                    }
                }

                @Override
                public void onFailure(Call<Boolean> call, Throwable t) {
                    Log.d(TAG, "onFailure: " + t.getMessage());
                }
            });
            // Set the caption
            holder.caption.setText(model.getCaption());
            System.out.println("dda" + model.getCaption());
            // Set Tags
            holder.mTags.setText(model.getTags());
            String videoPath = model.getVideoUrl();
            System.out.println("DAAA " + videoPath);

            // Set the comment
            checkIfUserLikedPost(holder, FirebaseAuth.getInstance().getCurrentUser().getUid(), model);

            holder.comments.setText("View all " + " comments"); // Assuming comments is the list of comments
            holder.comments.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d(TAG, "onClick: loading comment thread for " + model.getVideoUrl());
                    Intent b = new Intent(mContext, ViewReelComments.class);
                    Bundle bundle = new Bundle();
                    bundle.putParcelable("videos", model);
                    b.putExtras(bundle);
                    mContext.startActivity(b);
                }
            });

            // Set the time it was posted
            String timestampDifference = getTimestampDifference(model);
            if (!timestampDifference.equals("0")) {
                holder.timeDetla.setText(timestampDifference + " DAYS AGO");
            } else {
                holder.timeDetla.setText("TODAY");
            }
            final ImageLoader imageLoader = ImageLoader.getInstance();
            holder.video.setVideoPath(videoPath);
//            holder.video.requestFocus();
            String vd = model.getVideoUrl();
            if (vd != null && !vd.isEmpty()) {

                try {
                    UniversalImageLoader.setVideo(model.getVideoUrl(), holder.video, null, "");
                    MediaController mediaController = new MediaController(mContext);
                    mediaController.setAnchorView(holder.video);
                    holder.video.setMediaController(mediaController);
                } catch (NullPointerException e) {
                    Log.e(TAG, "onCreateView: NullPointerException: " + e.getMessage());
                }
            }
            // Set the profile image and username from Firestore
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("Users")
                    .whereEqualTo("user_id", model.getUserId())
                    .limit(1)
                    .get()
                    .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                            for (DocumentSnapshot snapshot : queryDocumentSnapshots) {
                                Users user = snapshot.toObject(Users.class);
                                if (user != null) {
                                    holder.username.setText(user.getUsername());
                                    imageLoader.displayImage(user.getProfilePhoto(), holder.mprofileImage);
                                    holder.username.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            Intent intent = new Intent(mContext, UserSearchProfileActivity.class);
                                            intent.putExtra("SearchedUserid", user.getUser_id());
                                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                            mContext.startActivity(intent);
                                        }
                                    });

                                    holder.mprofileImage.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            Intent intent = new Intent(mContext, UserSearchProfileActivity.class);
                                            intent.putExtra("SearchedUserid", user.getUser_id());
                                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                            mContext.startActivity(intent);
                                        }
                                    });

                                    holder.settings = user;
                                    holder.comment.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            Intent b = new Intent(mContext, ViewReelComments.class);
                                            Bundle bundle = new Bundle();
                                            bundle.putParcelable("videos", model);
                                            b.putExtras(bundle);
                                            mContext.startActivity(b);
                                        }
                                    });
                                }
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.e(TAG, "Error getting user data from Firestore", e);
                        }
                    });
        }




        private void checkIfUserLikedPost( MyHolder holder,final String userId, Reel photo) {
            FirebaseFirestore firestore = FirebaseFirestore.getInstance();
            firestore.collection("videos").document(photo.getVideoId())
                    .collection("likes").whereEqualTo("user_id", userId)
                    .get()
                    .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                            holder.likeByCurrentUser = !queryDocumentSnapshots.isEmpty();
                            setupLikesString(holder, holder.likesString, photo);
                            System.out.println("locdz" + holder.likeByCurrentUser);
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.e(TAG, "Error checking if user liked post: " + e.getMessage());
                        }
                    });
        }

        private void addNewLike(MyHolder holder,Reel photo) {
            DocumentReference newLikeRef = holder.firestore.collection("videos").document(photo.getVideoId()).collection("likes").document();
            Likes like = new Likes();
            like.setUser_id(FirebaseAuth.getInstance().getCurrentUser().getUid());
            newLikeRef.set(like)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
    //                        holder.heart.toggleLike();
                            getLikesString(holder,photo);
                            addLikeNotification(photo.getUserId(), photo.getVideoId(), photo);
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.e(TAG, "Error adding like: " + e.getMessage());
                        }
                    });

        }

        private void removeLike(MyHolder holder, Reel photo) {
            String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            holder.firestore.collection("videos").document(photo.getVideoId())
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
                                                getLikesString(holder,photo);
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


        private void getCurrentUsername(MyHolder holder) {
            Log.d(TAG, "getCurrentUsername: retrieving user");
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("Users")
                    .whereEqualTo("user_id", FirebaseAuth.getInstance().getCurrentUser().getUid())
                    .limit(1)
                    .get()
                    .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                            for (DocumentSnapshot snapshot : queryDocumentSnapshots) {
                                holder.currentUsername = snapshot.toObject(Users.class).getUsername();
                                System.out.println("CAs" + holder.currentUsername);
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.e(TAG, "getCurrentUsername: Error retrieving current user", e);
                        }
                    });
        }

        private void getLikesString(MyHolder holder,Reel photo) {
            FirebaseFirestore firestore = FirebaseFirestore.getInstance();
            firestore.collection("videos").document(photo.getVideoId()).collection("likes")
                    .get()
                    .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                            holder.users = new StringBuilder();
                            int likeCount = queryDocumentSnapshots.size();
                            if (likeCount == 0) {
                                holder.likesString = "No likes yet";
                                setupLikesString(holder, holder.likesString, photo);
                            } else {
                                for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                                    String userId = documentSnapshot.getString("user_id");
                                    fetchUsername(holder, userId, photo);
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

        private void fetchUsername(MyHolder holder, String userId, Reel photo) {
            FirebaseFirestore firestore = FirebaseFirestore.getInstance();
            firestore.collection("Users").document(userId)
                    .get()
                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            String username = documentSnapshot.getString("username");
                            holder.users.append(username).append(",");
                            checkIfCurrentUserLiked(holder, username, photo);
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.e(TAG, "Error fetching username: " + e.getMessage());
                        }
                    });
        }

        private void checkIfCurrentUserLiked(MyHolder holder, String username, Reel photo) {
            String[] splitUsers = holder.users.toString().split(",");
            int length = splitUsers.length;

            if (length == 0) {
                holder.likesString = "No likes yet";
            } else if (length == 1) {
                holder.likesString = "Liked by " + splitUsers[0];
            } else if (length == 2) {
                holder.likesString = "Liked by " + splitUsers[0] + " and " + splitUsers[1];
            } else if (length == 3) {
                holder.likesString = "Liked by " + splitUsers[0] + ", " + splitUsers[1] + " and " + splitUsers[2];
            } else if (length == 4) {
                holder.likesString = "Liked by " + splitUsers[0] + ", " + splitUsers[1] + ", " + splitUsers[2] + " and " + splitUsers[3];
            } else if (length > 4) {
                holder.likesString = "Liked by " + splitUsers[0] + ", " + splitUsers[1] + ", " + splitUsers[2] + " and " + (splitUsers.length - 3) + " others";
            }
            setupLikesString(holder, holder.likesString, photo);

        }


        private void setupLikesString(MyHolder holder, String likesString, Reel photo) {
            Log.d(TAG, "setupLikesString: likes string:" + likesString);
            if (holder.likeByCurrentUser) {
                holder.heartWhite.setVisibility(View.GONE);
                holder.heartRed.setVisibility(View.VISIBLE);
                holder.heartRed.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        System.out.println("wasssa");
                        // Remove like when heart icon is tapped
                        removeLike(holder, photo);
                        holder.likeByCurrentUser = false; // Update liked status
                        System.out.println("wasssa" + holder.likeByCurrentUser);
                        holder.heartWhite.setVisibility(View.VISIBLE);
                        holder.heartRed.setVisibility(View.GONE);
                    }
                });
            } else {
                holder.heartWhite.setVisibility(View.VISIBLE);
                holder.heartRed.setVisibility(View.GONE);
                holder.heartWhite.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        System.out.println("wsasssa");

                        // Add like when heart icon is tapped
                        addNewLike(holder, photo);
                        holder.likeByCurrentUser = true; // Update liked status
                        System.out.println("wsasssa" + holder.likeByCurrentUser);

                        holder.heartWhite.setVisibility(View.GONE);
                        holder.heartRed.setVisibility(View.VISIBLE);

                    }
                });
            }

            // Hiển thị số lượt thích
            if (likesString.isEmpty()) {
                holder.likes.setText("No likes yet");
            } else {
                holder.likes.setText(likesString);
            }
        }

        /**
         * Returns a string representing the number of days ago the post was made
         * @return
         */
        private void animateHeart(final ImageView likeButton) {
            Log.i("ANIMATE", "animateHeart: ");
            // Use ObjectAnimator to animate the scale of the heart icon
            ObjectAnimator scaleX = ObjectAnimator.ofFloat(likeButton, "scaleX", 1f, 1.2f, 1f);
            ObjectAnimator scaleY = ObjectAnimator.ofFloat(likeButton, "scaleY", 1f, 1.2f, 1f);

            // Set duration and interpolator for the animation
            scaleX.setDuration(300);
            scaleY.setDuration(300);
            scaleX.setInterpolator(new OvershootInterpolator());
            scaleY.setInterpolator(new OvershootInterpolator());

            // Create AnimatorSet and play the animations together
            AnimatorSet animatorSet = new AnimatorSet();
            animatorSet.playTogether(scaleX, scaleY);
            animatorSet.start();
        }
        private String getTimestampDifference(Reel photo){
            Log.d(TAG, "getTimestampDifference: getting timestamp difference.");

            String difference = "";
            Calendar c = Calendar.getInstance();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date today = c.getTime();
            sdf.format(today);
            Date timestamp;
            final String photoTimestamp = photo.getDateCreated();
            try{
                timestamp = sdf.parse(photoTimestamp);
                difference = String.valueOf(Math.round(((today.getTime() - timestamp.getTime()) / 1000 / 60 / 60 / 24 )));
            }catch (ParseException e){
                Log.e(TAG, "getTimestampDifference: ParseException: " + e.getMessage() );
                difference = "0";
            }
            return difference;
        }
        private void addLikeNotification(String userid,String postid, Reel photo){
            if (userid.equals(FirebaseAuth.getInstance().getCurrentUser().getUid())){
                return;
            }
            Notification notification = new Notification(userid,FirebaseAuth.getInstance().getCurrentUser().getUid(), " liked your reel ", postid, true, false,getTimestampDifference(photo));

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



    }
