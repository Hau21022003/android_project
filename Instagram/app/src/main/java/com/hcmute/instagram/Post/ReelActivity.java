package com.hcmute.instagram.Post;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.media.MediaMetadataRetriever;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.hcmute.instagram.Home;
import com.hcmute.instagram.R;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ReelActivity extends AppCompatActivity {

    private ImageView postNow, backFromPost, addedImage;
    private EditText addedCaption, addedTag;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private StorageReference storageReference;

    private ProgressDialog progressDialog;

    private final int PICK_VIDEO_REQUEST = 1;

    private Uri videoUri;
    private Uri thumbnailUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        postNow = findViewById(R.id.post_now);
        backFromPost = findViewById(R.id.back_from_post);
        addedImage = findViewById(R.id.added_image);
        addedCaption = findViewById(R.id.added_caption);
        addedTag = findViewById(R.id.added_tags);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();

        progressDialog = new ProgressDialog(this);

        openFileChooser(); // Open file chooser to select a video when the screen is opened

        backFromPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ReelActivity.this, Home.class));
                finish();
            }
        });

        postNow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadVideo();
            }
        });

        addedImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFileChooser();
            }
        });
    }

    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("video/*"); // Allow only video selection
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Video"), PICK_VIDEO_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_VIDEO_REQUEST && resultCode == RESULT_OK && data != null) {
            videoUri = data.getData();
            if (videoUri != null) {
                MediaMetadataRetriever retriever = new MediaMetadataRetriever();
                try {
                    retriever.setDataSource(getApplicationContext(), videoUri);
                    Bitmap thumbnail = retriever.getFrameAtTime(); // Retrieve the first frame of the video as a thumbnail
                    // Convert Bitmap to Uri
                    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                    thumbnail.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
                    String path = MediaStore.Images.Media.insertImage(getContentResolver(), thumbnail, "Thumbnail", null);
                    thumbnailUri = Uri.parse(path);
                    addedImage.setImageBitmap(thumbnail); // Display the thumbnail in the ImageView
                } catch (Exception e) {
                    e.printStackTrace();
                    // Handle any errors that may occur during thumbnail extraction
                } finally {
                    try {
                        retriever.release(); // Release the MediaMetadataRetriever to free up resources
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
    }

    private void uploadVideo() {
        if (videoUri != null && thumbnailUri != null) { // Ensure both video and thumbnail URIs are not null
            progressDialog.setMessage("Uploading...");
            progressDialog.show();

            final String caption = addedCaption.getText().toString().trim();
            final String tags = addedTag.getText().toString().trim();
            final String userId = mAuth.getCurrentUser().getUid();

            final StorageReference videoRef = storageReference.child("videos/users/" + userId + "/video" + UUID.randomUUID().toString());
            final StorageReference thumbnailRef = storageReference.child("thumbnails/users/" + userId + "/thumbnail" + UUID.randomUUID().toString()); // Reference for thumbnail

            // Upload video
            videoRef.putFile(videoUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    videoRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri videoUrl) {
                            // Upload thumbnail
                            thumbnailRef.putFile(thumbnailUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                    thumbnailRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                        @Override
                                        public void onSuccess(Uri thumbnailUrl) {
                                            // String caption, String tags, String videoUrl, String thumbnailUrl, String userId
                                            addPostToFirestore(caption, tags, videoUrl.toString(), thumbnailUrl.toString(), userId);
                                        }
                                    });
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    progressDialog.dismiss();
                                    Toast.makeText(ReelActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    });
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    progressDialog.dismiss();
                    Toast.makeText(ReelActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void addPostToFirestore(String caption, String tags, String videoUrl, String thumbnailUrl, String userId) {
        // Generate a random UUID for the video post
        String videoId = UUID.randomUUID().toString();

        // Get current timestamp as dateCreated
        String dateCreated = getCurrentTimestamp();

        DocumentReference newPostRef = db.collection("videos").document(videoId); // Use the videoId as the document ID

        Map<String, Object> postData = new HashMap<>();
        postData.put("videoId", videoId);
        postData.put("caption", caption);
        postData.put("tags", tags);
        postData.put("videoUrl", videoUrl);
        postData.put("thumbnailUrl", thumbnailUrl);
        postData.put("userId", userId);
        postData.put("dateCreated", dateCreated);

        newPostRef.set(postData)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        progressDialog.dismiss();
                        Toast.makeText(ReelActivity.this, "Posted successfully", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(ReelActivity.this, Home.class));
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Toast.makeText(ReelActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // Method to get current timestamp
    private String getCurrentTimestamp() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(new Date());
    }



    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
