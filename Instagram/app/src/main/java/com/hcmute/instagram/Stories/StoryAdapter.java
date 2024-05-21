package com.hcmute.instagram.Stories;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.hcmute.instagram.R;
import com.hcmute.instagram.models.Story;
import com.hcmute.instagram.models.Users;

public class StoryAdapter extends RecyclerView.Adapter<StoryAdapter.ViewHolder> {

    private Context mcontext;
    private List<Story> mstory;

    public StoryAdapter(Context mcontext, List<Story> mstory) {
        this.mcontext = mcontext;
        this.mstory = mstory;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        if(viewType==0){
            View view = LayoutInflater.from(mcontext)
                    .inflate(R.layout.add_story_items,parent,false);
            return new ViewHolder(view);
        }else {
            View view = LayoutInflater.from(mcontext)
                    .inflate(R.layout.story_items,parent,false);
            return new ViewHolder(view);

        }
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder viewHolder, final int i) {

        final Story story = mstory.get(i);

        userInfo(viewHolder, story.getUserid(), i);

        if (viewHolder.getAdapterPosition() != 0) {
            seenStory(viewHolder, story.getUserid());
        }

        if (viewHolder.getAdapterPosition() == 0){
            myStory(viewHolder.storyAddText, viewHolder.storyAdd, false);
        }

        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (viewHolder.getAdapterPosition() == 0){
                    myStory(viewHolder.storyAddText, viewHolder.storyAdd, true);
                } else {
                    // TODO: go to story
                    Intent intent = new Intent(mcontext, StoryActivity.class);
                    intent.putExtra("userid", story.getUserid());
                    mcontext.startActivity(intent);
                }
            }
        });

    }

    @Override
    public int getItemCount() {
        return mstory.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        public ImageView storyPhoto,storyAdd,storyPhotoSeen;
        public TextView storyUsername,storyAddText;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            storyPhoto = itemView.findViewById(R.id.addStoryItems_storyPhoto);
            storyAdd = itemView.findViewById(R.id.addStoryItems_addstory);
            storyPhotoSeen = itemView.findViewById(R.id.storyItems_storyPhotoSeen);
            storyUsername = itemView.findViewById(R.id.storyItems_storyUsername);
            storyAddText = itemView.findViewById(R.id.addStoryItems_addstorytxt);
        }
    }

    @Override
    public int getItemViewType(int position) {
        if(position ==0){
            return 0;
        }
        return 1;
    }

    private void userInfo(final ViewHolder viewHolder, String userid, final int pos){

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference userRef = db.collection("Users").document(userid);
        userRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()) {
                    Users user = documentSnapshot.toObject(Users.class);
                    Glide.with(mcontext).load(user.getProfilePhoto()).into(viewHolder.storyPhoto);
                    if (pos != 0) {
                        Glide.with(mcontext).load(user.getProfilePhoto()).into(viewHolder.storyPhotoSeen);
                        viewHolder.storyUsername.setText(user.getUsername());
                    }
                } else {
                    Log.d(TAG, "No such document");
                    // Handle the case where the document does not exist
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, "get failed with ", e);
                // Handle failures
            }
        });

    }

    private void myStory(final TextView textView, final ImageView imageView, final boolean click){

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser != null) {
            db.collection("Story").whereEqualTo("userid", currentUser.getUid()).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                @Override
                public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                    int count = 0;
                    long currentTime = System.currentTimeMillis();

                    for (QueryDocumentSnapshot snapshot : queryDocumentSnapshots) {
                        Story story = snapshot.toObject(Story.class);
                        if (currentTime > story.getTimestart() && currentTime < story.getTimeend()) {
                            count++;
                        }
                    }

                    if (click) {
                        if (count > 0) {
                            AlertDialog alertDialog = new AlertDialog.Builder(mcontext).create();
                            alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "View Story",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            //TODO: go to story
                                            Intent intent = new Intent(mcontext, StoryActivity.class);
                                            intent.putExtra("userid", currentUser.getUid());
                                            mcontext.startActivity(intent);
                                            dialog.dismiss();
                                        }
                                    });
                            alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Add Story",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            Intent intent = new Intent(mcontext, AddStoryActivity.class);
                                            mcontext.startActivity(intent);
                                            dialog.dismiss();
                                        }
                                    });
                            alertDialog.show();
                        } else {
                            Intent intent = new Intent(mcontext, AddStoryActivity.class);
                            mcontext.startActivity(intent);
                        }
                    } else {
                        if (count > 0) {
                            textView.setText("My story");
                            imageView.setVisibility(View.GONE);
                        } else {
                            textView.setText("Add story");
                            imageView.setVisibility(View.VISIBLE);
                        }
                    }
                }
            });
        }
    }

    private void seenStory(ViewHolder viewHolder, String userid){
        int[] count = new int[1];

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("Story").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot snapshots, @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    Log.w(TAG, "Listen failed.", error);
                    return;
                }
                for (DocumentSnapshot document : snapshots) {
                    if (document.get("userid").equals(userid)) {
                        Story story = document.toObject(Story.class); // Chuyển đổi DocumentSnapshot thành đối tượng Story
                        long timecurrent = System.currentTimeMillis();
                        if (timecurrent > story.getTimestart() && timecurrent < story.getTimeend()) {
                            count[0]++; //
                            Log.d(TAG, "check count tang:" + count[0]);
                            CollectionReference collectionRef = document.getReference().collection("views");
                            collectionRef.addSnapshotListener(new EventListener<QuerySnapshot>() {
                                @Override
                                public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                                    if (error != null) {
                                        Log.w(TAG, "Listen failed.", error);
                                        return;
                                    }
                                    for (DocumentSnapshot doc : value) {
                                        if (doc.get("userId").equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                                            count[0]--;
                                            Log.d(TAG, "check count giam:" + count[0]);
                                        }
                                    }
                                    checkSeenStory(viewHolder,count[0]);
                                }
                            });
                            Log.d(TAG, "check list story:" + story.getStoryid());
                        }
                        else {
                            Log.d(TAG, "khong co");
                        }
                    }
                }
            }
        });
    }

    private void checkSeenStory(ViewHolder viewHolder,int count) {
        if (count == 0) {
            Log.d(TAG, "check kiem tra da xem chua");
            viewHolder.storyPhoto.setVisibility(View.GONE);
            viewHolder.storyPhotoSeen.setVisibility(View.VISIBLE);
        } else {
            Log.d(TAG, "chua xem");
            viewHolder.storyPhoto.setVisibility(View.VISIBLE);
            viewHolder.storyPhotoSeen.setVisibility(View.GONE);
        }
    }
}
