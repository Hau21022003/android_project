package com.hcmute.instagram.Utils;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
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
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

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
import com.hcmute.instagram.Retrofit.APIService;
import com.hcmute.instagram.Retrofit.RetrofitClient;
import com.hcmute.instagram.Search.UserSearchProfileActivity;
import com.hcmute.instagram.models.Notification;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

import com.hcmute.instagram.Profile.ViewComments;
import com.hcmute.instagram.R;
import com.hcmute.instagram.models.Comments;
import com.hcmute.instagram.models.Likes;
import com.hcmute.instagram.models.Photo;
import com.hcmute.instagram.models.Users;

public class HomeFragmentPostViewListAdapter extends ArrayAdapter<Photo> {

    public interface OnLoadMoreItemsListener{
        void onLoadMoreItems();
    }
    OnLoadMoreItemsListener mOnLoadMoreItemsListener;

    private static final String TAG = "HomePostViewListAdapter";
    private FirebaseFirestore firestore;

    private LayoutInflater mInflater;
    private int mLayoutResource;
    private Context mContext;
    private DatabaseReference mReference;
    private String currentUsername = "";
    private ProgressBar mProgressBar;


    public HomeFragmentPostViewListAdapter(@NonNull Context context, @LayoutRes int resource, @NonNull List<Photo> objects) {
        super(context, resource, objects);
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mLayoutResource = resource;
        this.mContext = context;
        mReference = FirebaseDatabase.getInstance().getReference();
    }
    static class ViewHolder{
        CircleImageView mprofileImage;
        String likesString = "";
        TextView username, timeDetla, caption, likes, comments,mTags;
        SquareImageView image;
        ImageView heartRed, heartWhite, comment;

        Users settings = new Users();
//        privatedetails user  = new privatedetails();
        StringBuilder users;
//        String mLikesString;
        boolean likeByCurrentUser;
        Heart heart;
        GestureDetector detector;
        Photo photo;

        LinearLayout toxicContentLayout;

        TextView txtShowToxicContent;
    }

    @NonNull
    @Override
    public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        final ViewHolder holder;
        firestore = FirebaseFirestore.getInstance();

        if (convertView == null) {
            convertView = mInflater.inflate(mLayoutResource, parent, false);
            holder = new ViewHolder();

            holder.username = convertView.findViewById(R.id.fragment_home_post_viewer_username);
            holder.image = convertView.findViewById(R.id.fragment_home_post_viewer_post_image);
            holder.heartRed = convertView.findViewById(R.id.fragment_home_post_viewer_img_heart_red);
            holder.heartWhite = convertView.findViewById(R.id.fragment_home_post_viewer_img_heart);
            holder.comment = convertView.findViewById(R.id.fragment_home_post_viewer_img_comments);
            holder.likes = convertView.findViewById(R.id.fragment_home_post_viewer_txt_likes);
            holder.comments = convertView.findViewById(R.id.fragment_home_post_viewer_txt_commments);
            holder.caption = convertView.findViewById(R.id.fragment_home_post_viewer_txt_caption);
            holder.timeDetla = convertView.findViewById(R.id.fragment_home_post_viewer_txt_timePosted);
            holder.mprofileImage = convertView.findViewById(R.id.fragment_home_post_viewer_user_img);
            holder.mTags = convertView.findViewById(R.id.fragment_home_post_viewer_txt_tags);

            holder.toxicContentLayout = convertView.findViewById(R.id.fragment_home_post_viewer_toxic_layout);
            holder.txtShowToxicContent = convertView.findViewById(R.id.fragment_home_post_viewer_txt_show_toxic_content);

            holder.heart = new Heart(holder.heartWhite, holder.heartRed);
            holder.photo = getItem(position);
//            holder.detector = new GestureDetector(mContext, new GestureListener(holder));
            holder.users = new StringBuilder();
            setupLikesString(holder, holder.likesString);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        // Get the current user's username
        getCurrentUsername();

        // Get likes string
        getLikesString(holder);

        // Set the caption
        holder.caption.setText(getItem(position).getCaption());

        //goi api
        RetrofitClient.getAPIService().postPredictContent(getItem(position).getCaption()).enqueue(new Callback<Boolean>() {
            @Override
            public void onResponse(Call<Boolean> call, Response<Boolean> response) {
                Boolean isToxicContent = response.body();
                if(isToxicContent){
                    holder.caption.setVisibility(View.GONE);
                    holder.toxicContentLayout.setVisibility(View.VISIBLE);
                    holder.txtShowToxicContent.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            holder.toxicContentLayout.setVisibility(View.GONE);
                            holder.caption.setVisibility(View.VISIBLE);
                        }
                    });
                }
            }

            @Override
            public void onFailure(Call<Boolean> call, Throwable t) {
                Log.d(TAG, "onFailure: " + t.getMessage());
            }
        });

        // Set Tags
        holder.mTags.setText(getItem(position).getTags());

        // Set the comment
        final List<Comments> comments = getItem(position).getComments();
        checkIfUserLikedPost(holder, FirebaseAuth.getInstance().getCurrentUser().getUid());

        holder.comments.setText("View all " +  "comments");
        holder.comments.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: loading comment thread for " + getItem(position).getPhoto_id());
                Intent b = new Intent(mContext, ViewComments.class);
                Bundle bundle = new Bundle();
                bundle.putParcelable("posts", getItem(position));
                b.putExtra("commentcount", comments.size());
                b.putExtras(bundle);
                mContext.startActivity(b);
            }
        });

        // Set the time it was posted
        String timestampDifference = getTimestampDifference(getItem(position));
        if (!timestampDifference.equals("0")) {
            holder.timeDetla.setText(timestampDifference + " DAYS AGO");
        } else {
            holder.timeDetla.setText("TODAY");
        }

        // Set the profile image
        final ImageLoader imageLoader = ImageLoader.getInstance();
        imageLoader.displayImage(getItem(position).getImage_Path(), holder.image);

        // Get the profile image and username from Firestore
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("Users")
                .whereEqualTo("user_id", getItem(position).getUser_id())
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
                                        Intent intent=new Intent(mContext, UserSearchProfileActivity.class);
                                        intent.putExtra("SearchedUserid",user.getUser_id());
                                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                        mContext.startActivity(intent);
                                    }
                                });

                                holder.mprofileImage.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        Intent intent=new Intent(mContext, UserSearchProfileActivity.class);
                                        intent.putExtra("SearchedUserid",user.getUser_id());
                                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                        mContext.startActivity(intent);
                                    }
                                });

                                holder.settings = user;
                                holder.comment.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        Intent b = new Intent(mContext, ViewComments.class);
                                        Bundle bundle = new Bundle();
                                        bundle.putParcelable("posts", getItem(position));
                                        b.putExtra("commentcount", comments.size());
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

        if (reachedEndOfList(position)) {
            loadMoreData();
        }


        return convertView;
    }

    private boolean reachedEndOfList(int position){
        return position == getCount() - 1;
    }
    private void loadMoreData(){

        try{
            mOnLoadMoreItemsListener = (OnLoadMoreItemsListener) getContext();
        }catch (ClassCastException e){
            Log.e(TAG, "loadMoreData: ClassCastException: " +e.getMessage() );
        }

        try{
            mOnLoadMoreItemsListener.onLoadMoreItems();
        }catch (NullPointerException e){
            Log.e(TAG, "loadMoreData: ClassCastException: " +e.getMessage() );
        }
    }
    public class GestureListener extends GestureDetector.SimpleOnGestureListener{

        ViewHolder mHolder;
        public GestureListener(ViewHolder holder) {
            mHolder = holder;
        }

        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            Log.d(TAG, "onSingleTapConfirmed: Singletap detected.");

            DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
            Query query = reference
                    .child("Photo")
                    .child(mHolder.photo.getPhoto_id())
                    .child("likes");
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for(DataSnapshot singleSnapshot : dataSnapshot.getChildren()){

                        String keyID = singleSnapshot.getKey();

                        //case1: Then user already liked the photo
                        if(mHolder.likeByCurrentUser &&
                                singleSnapshot.getValue(Likes.class).getUser_id()
                                        .equals(FirebaseAuth.getInstance().getCurrentUser().getUid())){

                            mReference.child("Photo")
                                    .child(mHolder.photo.getPhoto_id())
                                    .child("likes")
                                    .child(keyID)
                                    .removeValue();
///
                            mReference.child("User_Photo")
                                    .child(mHolder.photo.getUser_id())
                                    .child(mHolder.photo.getPhoto_id())
                                    .child("likes")
                                    .child(keyID)
                                    .removeValue();

                            mHolder.heart.toggleLike();
                        }
                        //case2: The user has not liked the photo
                        else if(!mHolder.likeByCurrentUser){
                            //add new like
                            addNewLike(mHolder);
                            break;
                        }
                    }
                    if(!dataSnapshot.exists()){
                        //add new like
                        addNewLike(mHolder);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

            return true;
        }
    }
    private void checkIfUserLikedPost(final ViewHolder holder, final String userId) {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        firestore.collection("posts").document(holder.photo.getPhoto_id())
                .collection("likes").whereEqualTo("user_id", userId)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        holder.likeByCurrentUser = !queryDocumentSnapshots.isEmpty();
                        setupLikesString(holder, holder.likesString);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Error checking if user liked post: " + e.getMessage());
                    }
                });
    }

    private void addNewLike(final ViewHolder holder) {
        DocumentReference newLikeRef = firestore.collection("posts").document(holder.photo.getPhoto_id()).collection("likes").document();
        Likes like = new Likes();
        like.setUser_id(FirebaseAuth.getInstance().getCurrentUser().getUid());
        newLikeRef.set(like)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
//                        holder.heart.toggleLike();
                        getLikesString(holder);
                        addLikeNotification(holder.photo.getUser_id(), holder.photo.getPhoto_id());
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Error adding like: " + e.getMessage());
                    }
                });
    }

    private void removeLike(final ViewHolder holder) {
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        firestore.collection("posts").document(holder.photo.getPhoto_id())
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
                                            getLikesString(holder);
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


    private void getCurrentUsername() {
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
                            currentUsername = snapshot.toObject(Users.class).getUsername();
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

    private void getLikesString(final ViewHolder holder) {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        firestore.collection("posts").document(holder.photo.getPhoto_id()).collection("likes")
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        holder.users = new StringBuilder();
                        int likeCount = queryDocumentSnapshots.size();
                        if (likeCount == 0) {
                            holder.likesString = "No likes yet";
                            setupLikesString(holder, holder.likesString);
                        } else {
                            for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                                String userId = documentSnapshot.getString("user_id");
                                fetchUsername(holder, userId);
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

    private void fetchUsername(final ViewHolder holder, String userId) {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        firestore.collection("Users").document(userId)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        String username = documentSnapshot.getString("username");
                        holder.users.append(username).append(",");
                        checkIfCurrentUserLiked(holder, username);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Error fetching username: " + e.getMessage());
                    }
                });
    }

    private void checkIfCurrentUserLiked(final ViewHolder holder, String username) {
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
        setupLikesString(holder, holder.likesString);

    }


    private void setupLikesString(final ViewHolder holder, String likesString) {
        Log.d(TAG, "setupLikesString: likes string:" + holder.likesString);
        if (holder.likeByCurrentUser) {
            holder.heartWhite.setVisibility(View.GONE);
            holder.heartRed.setVisibility(View.VISIBLE);
            holder.heartRed.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Remove like when heart icon is tapped
                    removeLike(holder);
                    holder.likeByCurrentUser = false; // Update liked status
                    animateHeart(holder.heartRed);
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
                    // Add like when heart icon is tapped
                    addNewLike(holder);
                    holder.likeByCurrentUser = true; // Update liked status
                    animateHeart(holder.heartWhite);
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
    private String getTimestampDifference(Photo photo){
        Log.d(TAG, "getTimestampDifference: getting timestamp difference.");

        String difference = "";
        Calendar c = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date today = c.getTime();
        sdf.format(today);
        Date timestamp;
        final String photoTimestamp = photo.getDate_Created();
        try{
            timestamp = sdf.parse(photoTimestamp);
            difference = String.valueOf(Math.round(((today.getTime() - timestamp.getTime()) / 1000 / 60 / 60 / 24 )));
        }catch (ParseException e){
            Log.e(TAG, "getTimestampDifference: ParseException: " + e.getMessage() );
            difference = "0";
        }
        return difference;
    }
    private String getTimestamp() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(new Date());
    }
    private void addLikeNotification(String userid,String postid){
        if (userid.equals(FirebaseAuth.getInstance().getCurrentUser().getUid())){
            return;
        }
        Notification notification = new Notification(userid,FirebaseAuth.getInstance().getCurrentUser().getUid(), " liked your post ", postid, true, false,getTimestamp());

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
