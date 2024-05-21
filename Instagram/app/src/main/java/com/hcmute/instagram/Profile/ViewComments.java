package com.hcmute.instagram.Profile;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.ListenerRegistration;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.hcmute.instagram.R;
import com.hcmute.instagram.Utils.CommentListAdapter;
import com.hcmute.instagram.models.Comments;
import com.hcmute.instagram.models.Notification;
import com.hcmute.instagram.models.Photo;
import com.hcmute.instagram.models.Users;


public class ViewComments extends AppCompatActivity {

    private static final String TAG = "ViewComments";

    private ImageView mBackArrow;
    private EditText mComment;
    private ListView mListView;
    private TextView mpost;
    ImageView profileImage;

    private Photo mphoto;
    private ArrayList<Comments> mComments;

    private FirebaseAuth mAuth;
    private FirebaseFirestore mFirestore;

    private ListenerRegistration commentListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_comments);
        mBackArrow = findViewById(R.id.back_from_view_comment);
        mComment = findViewById(R.id.comment);
        mListView = findViewById(R.id.listView);
        mpost = findViewById(R.id.post_comment);
        profileImage = findViewById(R.id.user_img);
        mFirestore = FirebaseFirestore.getInstance();
        mComments = new ArrayList<>();

        try {
            mphoto = getPhotoFromBundle();
            Log.d(TAG, "getPhotoFromBundle: arguments: " + mphoto);
            getCommentList();

        } catch (NullPointerException e) {
            Log.e(TAG, "onCreateView: NullPointerException: " + e.getMessage());
        }

        // Initialize Firebase Auth and Firestore
        mAuth = FirebaseAuth.getInstance();
        mFirestore = FirebaseFirestore.getInstance();

        // Load user profile photo
        String userId = mAuth.getCurrentUser().getUid();
        CollectionReference usersRef = mFirestore.collection("Users");
        usersRef.document(userId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    Users user = document.toObject(Users.class);
                    Glide.with(ViewComments.this)
                            .load(user.getProfilePhoto())
                            .into(profileImage);
                }
            } else {
                Log.e(TAG, "Error getting user document", task.getException());
            }
        });

        // Post comment button click listener
        mpost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mComment.getText().toString().isEmpty()) {
                    Log.d(TAG, "onClick: attempting to submit new comment.");
                    addNewComment(mComment.getText().toString());

                    mComment.setText("");
                    closeKeyboard();
                } else {
                    Toast.makeText(ViewComments.this, "You can't post a blank comment", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Remove the comment listener when the activity is destroyed
        if (commentListener != null) {
            commentListener.remove();
        }
    }

    private Photo getPhotoFromBundle() {
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            return bundle.getParcelable("posts");
        } else {
            return null;
        }
    }

    private void closeKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void addNewComment(String newComment) {
        Log.d(TAG, "addNewComment: adding new comment: " + newComment);

        String commentID = mFirestore.collection("posts")
                .document(mphoto.getPhoto_id())
                .collection("comments")
                .document().getId();

        Comments comment = new Comments();
        comment.setComment_id(commentID); // Set comment_id
        comment.setComment(newComment);
        comment.setDate_created(getTimestamp());
        comment.setUser_id(mAuth.getCurrentUser().getUid());
        mFirestore.collection("posts")
                .document(mphoto.getPhoto_id())
                .collection("comments")
                .document(commentID)
                .set(comment);

        addCommentNotification(newComment, mphoto.getUser_id(), mphoto.getPhoto_id());
    }


    private String getTimestamp() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(new Date());
    }

    private void getCommentList() {
        commentListener = mFirestore.collection("posts")
                .document(mphoto.getPhoto_id())
                .collection("comments")
                .orderBy("date_created", Query.Direction.DESCENDING)
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
                                Log.d(TAG, "Comment: " + comment.getComment());
                            } else {
                                Log.e(TAG, "Failed to parse comment");
                            }
                        }
                        setupWidgets();
                    } else {
                        Log.d(TAG, "Current data: null");
                    }
                });
    }

    private void setupWidgets() {
        CommentListAdapter adapter = new CommentListAdapter(this, R.layout.layout_each_comment, mComments);
        mListView.setAdapter(adapter);
    }

    private void addCommentNotification(String comment, String userId, String photoId) {
        if (userId.equals(mAuth.getCurrentUser().getUid())){
            return;
        }
        Notification notification = new Notification(userId, mAuth.getCurrentUser().getUid(), " commented on your post : " + comment, photoId, true, false,getTimestamp());

        mFirestore.collection("Notifications")
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
