package com.hcmute.instagram.Profile;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.hcmute.instagram.R;
import com.hcmute.instagram.Utils.CommentListAdapter;
import com.hcmute.instagram.models.Comments;
import com.hcmute.instagram.models.Reel;
import com.hcmute.instagram.models.Video;
import com.hcmute.instagram.models.Users;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ViewReelComments extends AppCompatActivity {

    private static final String TAG = "ViewVideosComments";

    private ImageView mBackArrow;
    private EditText mComment;
    private ListView mListView;
    private TextView mPost;
    private ImageView profileImage;

    private Reel mVideo;
    private ArrayList<Comments> mComments;

    private FirebaseAuth mAuth;
    private FirebaseFirestore mFirestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_comments);

        mBackArrow = findViewById(R.id.back_from_view_comment);
        mComment = findViewById(R.id.comment);
        mListView = findViewById(R.id.listView);
        mPost = findViewById(R.id.post_comment);
        profileImage = findViewById(R.id.user_img);

        mFirestore = FirebaseFirestore.getInstance();
        mComments = new ArrayList<>();

        mAuth = FirebaseAuth.getInstance();

        try {
            mVideo = getVideoFromBundle();
            System.out.println("asdasd" + mVideo);
            getCommentList();
        } catch (NullPointerException e) {
            Log.e(TAG, "onCreate: NullPointerException: " + e.getMessage());
        }

        String userId = mAuth.getCurrentUser().getUid();
        CollectionReference usersRef = mFirestore.collection("Users");
        usersRef.document(userId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    Users user = document.toObject(Users.class);
                    if (user != null) {
                        Glide.with(this).load(user.getProfilePhoto()).into(profileImage);
                    }
                }
            } else {
                Log.e(TAG, "Error getting user document", task.getException());
            }
        });

        mPost.setOnClickListener(v -> {
            if (!mComment.getText().toString().isEmpty()) {
                addNewComment(mComment.getText().toString());
                mComment.setText("");
                closeKeyboard();
            } else {
                Toast.makeText(ViewReelComments.this, "You can't post a blank comment", Toast.LENGTH_SHORT).show();
            }
        });

        mBackArrow.setOnClickListener(v -> onBackPressed());
    }

    private Reel getVideoFromBundle() {
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            return bundle.getParcelable("videos");
        } else {
            return null;
        }
    }

    private void closeKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void addNewComment(String newComment) {
        String commentID = mFirestore.collection("videos")
                .document(mVideo.getVideoId())
                .collection("comments")
                .document().getId();
        Comments comment = new Comments();
        comment.setComment(newComment);
        comment.setDate_created(getTimestamp());
        comment.setUser_id(mAuth.getCurrentUser().getUid());

        mFirestore.collection("videos")
                .document(mVideo.getVideoId())
                .collection("comments")
                .document(commentID)
                .set(comment);

        addCommentNotification(newComment, mVideo.getUserId(), mVideo.getVideoId());
    }

    private String getTimestamp() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(new Date());
    }

    private void getCommentList() {
        if (mVideo != null) {
            mFirestore.collection("videos")
                    .document(mVideo.getVideoId())
                    .collection("comments")
                    .addSnapshotListener((value, error) -> {
                        if (error != null) {
                            Log.e(TAG, "Listen failed", error);
                            return;
                        }

                        if (value != null) {
                            mComments.clear();
                            for (DocumentSnapshot document : value) {
                                Comments comment = document.toObject(Comments.class);
                                if (comment != null) {
                                    mComments.add(comment);
                                } else {
                                    Log.e(TAG, "Failed to parse comment");
                                }
                            }
                            setupWidgets();
                        } else {
                            Log.d(TAG, "Current data: null");
                        }
                    });
        } else {
            Log.e(TAG, "Video object is null");
        }
    }


    private void setupWidgets() {
        CommentListAdapter adapter = new CommentListAdapter(this, R.layout.layout_each_comment, mComments);
        mListView.setAdapter(adapter);
    }

    private void addCommentNotification(String comment, String userId, String videoId) {
        Map<String, Object> notification = new HashMap<>();
        notification.put("userid", mAuth.getCurrentUser().getUid());
        notification.put("text", "Commented! " + comment);
        notification.put("videoId", videoId);
        notification.put("ispost", true);

        mFirestore.collection("Notifications")
                .document(userId)
                .collection("user_notifications")
                .add(notification)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "Notification added successfully");
                    } else {
                        Log.e(TAG, "Error adding notification", task.getException());
                    }
                });
    }
}
