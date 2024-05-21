package com.hcmute.instagram.Stories;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//import jp.shts.android.storiesprogressview.StoriesProgressView;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.hcmute.instagram.Profile.FollowersFollowing;
import com.hcmute.instagram.R;
import com.hcmute.instagram.models.Story;
import com.hcmute.instagram.models.Users;

import jp.shts.android.storiesprogressview.StoriesProgressView;

public class StoryActivity extends AppCompatActivity implements StoriesProgressView.StoriesListener {

    private static final String TAG = "StoryActivity";
    int counter = 0;
    long pressTime = 0L;
    long limit = 500L;

    private StoriesProgressView storiesProgressView;
    ImageView image, story_photo;
    TextView story_username;

    LinearLayout r_seen;
    TextView seen_number;
    ImageView story_delete;

    List<String> images;
    List<String> storyids;
    String userid;
    String viewcount;

        private View.OnTouchListener onTouchListener = new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        pressTime = System.currentTimeMillis();
                        storiesProgressView.pause();
                        return false;
                    case MotionEvent.ACTION_UP:
                        long now = System.currentTimeMillis();
                        storiesProgressView.resume();
                        return limit < now - pressTime;
                }
                return false;
            }
        };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_story);

        storiesProgressView = findViewById(R.id.ActivityStory_storiesProgrsssView);
        image = findViewById(R.id.ActivityStory_image);
        story_photo = findViewById(R.id.ActivityStory_story_photo);
        story_username = findViewById(R.id.ActivityStory_story_username);

        r_seen = findViewById(R.id.ActivityStory_r_seen);
        seen_number = findViewById(R.id.ActivityStory_seen_number);
        story_delete = findViewById(R.id.ActivityStory_story_delete);

        r_seen.setVisibility(View.GONE);
        story_delete.setVisibility(View.GONE);

        userid = getIntent().getStringExtra("userid");

        if (userid.equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
            r_seen.setVisibility(View.VISIBLE);
            story_delete.setVisibility(View.VISIBLE);
        }

        View reverse = findViewById(R.id.ActivityStory_reverse);
        reverse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                storiesProgressView.reverse();
            }
        });
        reverse.setOnTouchListener(onTouchListener);

        getStories(userid);
        userInfo(userid);

        View skip = findViewById(R.id.ActivityStory_skip);
        skip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                storiesProgressView.skip();
            }
        });
        skip.setOnTouchListener(onTouchListener);

        r_seen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(StoryActivity.this, FollowersFollowing.class);
                intent.putExtra("id", userid);
                intent.putExtra("storyid", storyids.get(counter));
                intent.putExtra("title", "Views");
                intent.putExtra("number", viewcount);
                startActivity(intent);
            }
        });

        story_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                storiesProgressView.pause();
                new AlertDialog.Builder(StoryActivity.this)
                        .setMessage("Are you sure you want to Delete?")
                        .setCancelable(false)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {

                                FirebaseFirestore db = FirebaseFirestore.getInstance();
                                db.collection("Story").document(storyids.get(counter))
                                        .delete()
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Toast.makeText(StoryActivity.this, "Deleted!", Toast.LENGTH_SHORT).show();
                                                finish();
                                            }
                                        });
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                storiesProgressView.resume();
                            }
                        })
                        .show();
            }
        });

    }

//    @Override
    public void onNext() {

        Glide.with(getApplicationContext()).load(images.get(++counter)).into(image);

        addView(storyids.get(counter));
        seenNumber(storyids.get(counter));
    }

//    @Override
    public void onPrev() {

        if ((counter - 1) < 0) return;
        Glide.with(getApplicationContext()).load(images.get(--counter)).into(image);

        seenNumber(storyids.get(counter));

    }

//    @Override
    public void onComplete() {
        finish();
    }

    @Override
    protected void onDestroy() {
        storiesProgressView.destroy();
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        storiesProgressView.pause();
        super.onPause();
    }

    @Override
    protected void onResume() {
        storiesProgressView.resume();
        super.onResume();
    }

    private void getStories(String userid) {
        images = new ArrayList<>();
        storyids = new ArrayList<>();

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("Story").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                if (!queryDocumentSnapshots.isEmpty()) {
                    images.clear();
                    storyids.clear();

                    for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                        if (documentSnapshot.get("userid").equals(userid)) {
                            Story story = documentSnapshot.toObject(Story.class); // Chuyển đổi DocumentSnapshot thành đối tượng Story
                            long timecurrent = System.currentTimeMillis();
                            if (timecurrent > story.getTimestart() && timecurrent < story.getTimeend()) {
                                images.add(story.getImageurl());
                                storyids.add(story.getStoryid());
                            }
                        }
                    }

                    storiesProgressView.setStoriesCount(images.size()); // <- set stories
                    storiesProgressView.setStoryDuration(5000L); // <- set a story duration
                    storiesProgressView.setStoriesListener(StoryActivity.this); // <- set listener
                    storiesProgressView.startStories(counter); // <- start progress

                    Glide.with(getApplicationContext()).load(images.get(counter)).into(image);

                    addView(storyids.get(counter));
                    seenNumber(storyids.get(counter));
                } else {
                    // Collection is empty
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                // Handle failure
            }
        });
    }

    private void userInfo(String userid) {

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("Users").document(userid).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            Users user = documentSnapshot.toObject(Users.class);
                            Glide.with(getApplicationContext()).load(user.getProfilePhoto()).into(story_photo);
                            story_username.setText(user.getUsername());
                        } else {
                            // Document does not exist
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Handle failure
                    }
                });
    }

    //
    private void addView(String storyid) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        DocumentReference docRef = db.collection("Story").document(storyid);
        docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()) {
                    if (!documentSnapshot.get("userid").equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                        Map<String, Object> data = new HashMap<>();
                        data.put("userId", FirebaseAuth.getInstance().getCurrentUser().getUid());

                        db.collection("Story").document(storyid).collection("views")
                                .document(FirebaseAuth.getInstance().getCurrentUser().getUid()).set(data);
                    }
                }
            }
        });


    }

    private void seenNumber(String storyid) {

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference viewsRef = db.collection("Story").document(storyid)
                .collection("views");
        viewsRef.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot snapshot,
                                @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w(TAG, "Listen failed.", e);
                    return;
                }

                if (snapshot != null) {
                    int viewCount = snapshot.size();
                    viewcount = String.valueOf(viewCount);
                    seen_number.setText(viewcount);
                } else {
                    Log.d(TAG, "Current data: null");
                }
            }
        });
    }
}