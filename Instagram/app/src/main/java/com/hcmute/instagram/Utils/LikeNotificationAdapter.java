package com.hcmute.instagram.Utils;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.hcmute.instagram.Profile.ProfileFragment;
import com.hcmute.instagram.Profile.ViewPostFragment;
import com.hcmute.instagram.R;
import com.hcmute.instagram.Search.UserSearchProfileActivity;
import com.hcmute.instagram.models.Comments;
import com.hcmute.instagram.models.Likes;
import com.hcmute.instagram.models.Notification;
import com.hcmute.instagram.models.Photo;
import com.hcmute.instagram.models.Users;
import com.hcmute.instagram.notify.NotifyFragment;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class LikeNotificationAdapter extends RecyclerView.Adapter<LikeNotificationAdapter.ViewHolder> {

    private static final String TAG = "LikeNotificationAdapter";

    private Context mContext;
    private List<Notification> mNotification;

    public LikeNotificationAdapter(Context context, List<Notification> notificationList) {
        mContext = context;
        mNotification = notificationList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.like_each_item_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final Notification notification = mNotification.get(position);
        holder.text.setText(notification.getText().trim());
        getUserInfo(holder.profileImage, holder.usernamee, notification.getUserid());
        holder.timestamp.setText(getTimestampDifference(notification.getPostAt()));
        if (!notification.isIsseen()) {
            holder.notify_item_container.setBackgroundColor(Color.LTGRAY);
        }
        if (notification.isIspost()) {
            Log.d(TAG, "onBindViewHolder: Notification for Post");
            holder.postImage.setVisibility(View.VISIBLE);
            getPostImage(holder.postImage, notification.getPostid());
            holder.layoutButton.setVisibility(View.GONE);

        } else {
            holder.layoutButton.setVisibility(View.VISIBLE);
            holder.postImage.setVisibility(View.GONE);
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            DocumentReference userRef = db.collection("Users").document(FirebaseAuth.getInstance().getCurrentUser().getUid());
            userRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    if (documentSnapshot.exists()) {
                        ArrayList<String> idListCurrentUser = (ArrayList<String>) documentSnapshot.get("following");
                        if (idListCurrentUser.contains(String.valueOf(notification.getUserid()))) {
                            setBtnFollowing(holder);
                        } else {
                            setBtnFollow(holder);
                        }
                    }
                }
            });

        }
        holder.follow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!notification.isIsseen()) {
                    setSeenNotify(notification);
                }
                FirebaseFirestore db = FirebaseFirestore.getInstance();
                // Thêm ID của người dùng khác vào trường "following" của người dùng hiện tại
                DocumentReference currentUserRef = db.collection("Users").document(FirebaseAuth.getInstance().getCurrentUser().getUid());
                currentUserRef.update("following", FieldValue.arrayUnion(notification.getUserid()));

                // Thêm ID của người dùng hiện tại vào trường "followers" của người dùng khác
                DocumentReference otherUserRef = db.collection("Users").document(notification.getUserid());
                ;
                otherUserRef.update("followers", FieldValue.arrayUnion(FirebaseAuth.getInstance().getCurrentUser().getUid()));

                setBtnFollowing(holder);
                addFollowNotification(notification.getUserid());
            }
        });
        holder.following.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!notification.isIsseen()) {
                    setSeenNotify(notification);
                }
                FirebaseFirestore db = FirebaseFirestore.getInstance();

                // Xóa ID của người dùng khác khỏi trường "following" của người dùng hiện tại
                DocumentReference currentUserRef = db.collection("Users").document(FirebaseAuth.getInstance().getCurrentUser().getUid());
                currentUserRef.update("following", FieldValue.arrayRemove(notification.getUserid()));

                // Xóa ID của người dùng hiện tại khỏi trường "followers" của người dùng khác
                DocumentReference otherUserRef = db.collection("Users").document(notification.getUserid());
                otherUserRef.update("followers", FieldValue.arrayRemove(FirebaseAuth.getInstance().getCurrentUser().getUid()));

                setBtnFollow(holder);

            }
        });
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                FirebaseFirestore db = FirebaseFirestore.getInstance();
                String notificationId = notification.getId();
                if (!notification.isIsseen()) {
                    setSeenNotify(notification);
                }
                if (notification.isIspost()) {
                    Log.d(TAG, "onClick: Notification Item Clicked: Redirecting to ViewPost page");
                    Log.i(TAG, notification.getPostid());
                    // Fetch post document
                    db.collection("posts").document(notification.getPostid()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot postSnapshot) {
                            if (postSnapshot.exists()) {
                                // Convert post snapshot to Photo object
                                Photo post = postSnapshot.toObject(Photo.class);
                                Map<String, Object> objectMap = postSnapshot.getData();

                                post.setCaption(objectMap.get("caption") != null ? objectMap.get("caption").toString() : "");
                                post.setTags(objectMap.get("tags") != null ? objectMap.get("tags").toString() : "");
                                post.setPhoto_id(objectMap.get("photo_id") != null ? objectMap.get("photo_id").toString() : "");
                                post.setUser_id(objectMap.get("userId") != null ? objectMap.get("userId").toString() : "");
                                post.setDate_Created(objectMap.get("date_Created") != null ? objectMap.get("date_Created").toString() : "");
                                post.setImage_Path(objectMap.get("thumbnailUrl") != null ? objectMap.get("thumbnailUrl").toString() : "");

                                // Fetch comments for this post
                                postSnapshot.getReference().collection("comments").get()
                                        .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                            @Override
                                            public void onSuccess(QuerySnapshot commentSnapshots) {
                                                List<Comments> comments = new ArrayList<>();
                                                for (DocumentSnapshot commentSnapshot : commentSnapshots) {
                                                    Comments comment = commentSnapshot.toObject(Comments.class);
                                                    comments.add(comment);
                                                }
                                                post.setComments(comments);
                                                // Fetch likes for this post
                                                postSnapshot.getReference().collection("likes").get()
                                                        .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                                            @Override
                                                            public void onSuccess(QuerySnapshot likeSnapshots) {
                                                                List<Likes> likesList = new ArrayList<>();
                                                                for (DocumentSnapshot likeSnapshot : likeSnapshots) {
                                                                    Likes like = likeSnapshot.toObject(Likes.class);
                                                                    likesList.add(like);
                                                                }
                                                                post.setLikes(likesList);
                                                                // Pass the post data to the ViewPostFragment
                                                                ViewPostFragment fragment = new ViewPostFragment();
                                                                Bundle args = new Bundle();
                                                                args.putParcelable("posts", post);
                                                                fragment.setArguments(args);
                                                                FragmentManager fragmentManager = ((FragmentActivity) mContext).getSupportFragmentManager();
                                                                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                                                                fragmentTransaction.replace(R.id.notify_frameLayout, fragment);
                                                                fragmentTransaction.addToBackStack(null);
                                                                fragmentTransaction.commit();
                                                            }
                                                        })
                                                        .addOnFailureListener(new OnFailureListener() {
                                                            @Override
                                                            public void onFailure(@NonNull Exception e) {
                                                                Log.e(TAG, "Error getting likes for post: " + e.getMessage());
                                                            }
                                                        });
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Log.e(TAG, "Error getting comments for post: " + e.getMessage());
                                            }
                                        });
                            }
                        }
                    });
                } else {
                    Intent intent = new Intent(mContext, UserSearchProfileActivity.class);
                    intent.putExtra("SearchedUserid", notification.getUserid());
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    mContext.startActivity(intent);
                }
            }

        });

    }

    private void setSeenNotify(Notification notification) {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        notification.setIsseen(true);
        // Update isSeen field in Firestore
        db.collection("Notifications")
                .document(notification.getId())
                .set(notification)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("NotificationActivity", "Notification isSeen field updated successfully");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("NotificationActivity", "Error updating notification isSeen field", e);
                    }
                });
    }

    private String getTimestamp() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(new Date());
    }

    private void addFollowNotification(String userid) {
        Notification notification = new Notification(userid,FirebaseAuth.getInstance().getCurrentUser().getUid(), " started following you ", "", false, false, getTimestamp());
        FirebaseFirestore mFirestore = FirebaseFirestore.getInstance();
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

    private void setBtnFollow(LikeNotificationAdapter.ViewHolder holder) {
        holder.follow.setVisibility(View.VISIBLE);
        holder.following.setVisibility(View.GONE);
    }

    private void setBtnFollowing(LikeNotificationAdapter.ViewHolder holder) {
        holder.follow.setVisibility(View.GONE);
        holder.following.setVisibility(View.VISIBLE);
    }

    private String getTimestampDifference(String time) {
        String difference = "";
        Calendar c = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        Date today = c.getTime();
        Date timestamp;
        final String photoTimestamp = time;
        try {
            timestamp = sdf.parse(photoTimestamp);
            long diffInMillis = Math.abs(today.getTime() - timestamp.getTime());
            long seconds = TimeUnit.MILLISECONDS.toSeconds(diffInMillis);
            long minutes = TimeUnit.MILLISECONDS.toMinutes(diffInMillis);
            long hours = TimeUnit.MILLISECONDS.toHours(diffInMillis);
            long days = TimeUnit.MILLISECONDS.toDays(diffInMillis);
            long weeks = days / 7;
            long months = days / 30;
            long years = days / 365;
            if (seconds == 0) {
                difference = "Just finished";
            } else if (seconds < 60) {
                difference = seconds + " seconds ago";
            } else if (minutes < 60) {
                difference = minutes + " minutes ago";
            } else if (hours < 24) {
                difference = hours + " hours ago";
            } else if (days < 7) {
                difference = days + " days ago";
            } else if (weeks < 4) {
                difference = weeks + " weeks ago";
            } else if (months < 12) {
                difference = months + " months ago";
            } else {
                difference = years + " years ago";
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return difference;
    }

    @Override
    public int getItemCount() {
        return mNotification.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView profileImage;
        public ImageView postImage;
        RelativeLayout layoutButton;
        LinearLayout notify_item_container;
        public TextView usernamee, text, timestamp;
        public Button follow, following;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            profileImage = itemView.findViewById(R.id.like_each_item_user_img);
            postImage = itemView.findViewById(R.id.like_each_item_post_image);
            usernamee = itemView.findViewById(R.id.like_each_item_username);
            text = itemView.findViewById(R.id.like_each_item_comment);
            timestamp = itemView.findViewById(R.id.notify_timestamp);
            layoutButton = itemView.findViewById(R.id.layoutButton);
            notify_item_container = itemView.findViewById(R.id.notify_item_container);
            follow = itemView.findViewById(R.id.notify_followBtn);
            following = itemView.findViewById(R.id.notify_followingBtn);
        }

    }

    private void getUserInfo(final ImageView profimage, final TextView username, String publisherId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("Users").document(publisherId).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@javax.annotation.Nullable DocumentSnapshot snapshot, @javax.annotation.Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    Log.e(TAG, "Error getting user information: " + error.getMessage());
                    return;
                }

                if (snapshot != null && snapshot.exists()) {
                    Users user = snapshot.toObject(Users.class);
                    if (user != null) {
                        Glide.with(mContext).load(user.getProfilePhoto()).into(profimage);
                        username.setText(user.getUsername());
                    }
                }
            }
        });
    }

    private void getPostImage(final ImageView postimg, String postid) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("posts").document(postid).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@javax.annotation.Nullable DocumentSnapshot snapshot, @javax.annotation.Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    Log.e(TAG, "Error getting post image: " + error.getMessage());
                    return;
                }

                if (snapshot != null && snapshot.exists()) {
                    String imagePath = snapshot.getString("thumbnailUrl");
                    if (imagePath != null) {
                        Glide.with(mContext).load(imagePath).into(postimg);
                    }
                }
            }
        });
    }
}
