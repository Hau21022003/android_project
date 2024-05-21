package com.hcmute.instagram.Post;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.hcmute.instagram.Home;
import com.hcmute.instagram.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class PostActivity extends AppCompatActivity {

    private ImageView postNow, backFromPost, addedImage;
    private EditText addedCaption, addedTag;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private StorageReference storageReference;

    private ProgressDialog progressDialog;

    private final int PICK_IMAGE_REQUEST = 1;

    private ArrayList<Uri> imageUris = new ArrayList<>();
    private int selectedThumbnailPosition = -1;

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

        openFileChooser(); // Mở luôn chức năng upload ảnh khi màn hình được mở

        backFromPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(PostActivity.this, Home.class));
                finish();
            }
        });

        postNow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadImages();
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
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true); // Cho phép chọn nhiều hình ảnh
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            if (data.getClipData() != null) {
                // Nếu người dùng chọn nhiều hình ảnh
                int count = data.getClipData().getItemCount();
                for (int i = 0; i < count; i++) {
                    Uri imageUri = data.getClipData().getItemAt(i).getUri();
                    imageUris.add(imageUri);
                }
            } else if (data.getData() != null) {
                // Nếu người dùng chỉ chọn một hình ảnh
                Uri imageUri = data.getData();
                imageUris.add(imageUri);
            }
            // Hiển thị hình ảnh đầu tiên trong danh sách làm thumbnail
            selectedThumbnailPosition = 0;
            addedImage.setImageURI(imageUris.get(selectedThumbnailPosition));
        }
    }

    private void uploadImages() {
        if (!imageUris.isEmpty()) {
            progressDialog.setMessage("Uploading...");
            progressDialog.show();

            final String caption = addedCaption.getText().toString().trim();
            final String tags = addedTag.getText().toString().trim();
            final String userId = mAuth.getCurrentUser().getUid();
            final String randomUID = UUID.randomUUID().toString();
            final StorageReference thumbnailRef = storageReference.child("photos/users/" + userId + "/thumbnail" + randomUID);
            thumbnailRef.putFile(imageUris.get(selectedThumbnailPosition)).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    thumbnailRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri thumbnailUri) {
                            uploadRemainingImages(thumbnailUri.toString(), randomUID, caption, tags, userId);
                        }
                    });
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    progressDialog.dismiss();
                    Toast.makeText(PostActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void uploadRemainingImages(final String thumbnailUrl,final String randomUID , final String caption, final String tags, final String userId) {
        final CollectionReference postsCollection = db.collection("posts");
        final List<String> imageUrls = new ArrayList<>();
        final String currentDate = getTimeStamp(); // Get current timestamp

        for (int i = 0; i < imageUris.size(); i++) {
            final StorageReference imageRef = storageReference.child("photos/users/" + userId + "/photo" + UUID.randomUUID().toString());
            final int finalI = i;
            imageRef.putFile(imageUris.get(i)).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    imageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri imageUrl) {
                            imageUrls.add(imageUrl.toString());
                            if (finalI == imageUris.size() - 1) {
                                // If all images are uploaded, add post to Firestore
                                addPostToFirestore(caption, tags, thumbnailUrl,randomUID , imageUrls, userId);
                                updatePostCount(userId);
                            }
                        }
                    });
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    progressDialog.dismiss();
                    Toast.makeText(PostActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private String  getTimeStamp() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(new Date());
    }

    private void addPostToFirestore(String caption, String tags, String thumbnailUrl,String randomUID ,  List<String> imageUrls, String userId) {
        final String currentDate = getTimeStamp(); // Assuming you have a method to get current timestamp

        // Use the thumbnailUrl as photo_id
        final String photoId = thumbnailUrl; // Use thumbnailUrl as photo_id

        // Add post data to Firestore
        DocumentReference newPostRef = db.collection("posts").document(randomUID);
        Map<String, Object> postData = new HashMap<>();
        postData.put("caption", caption);
        postData.put("tags", tags);
        postData.put("thumbnailUrl", thumbnailUrl);
        postData.put("photo_id", randomUID);
        postData.put("imageUrls", imageUrls);
        postData.put("userId", userId);
        postData.put("date_Created", currentDate); // Add date created

        newPostRef.set(postData)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        progressDialog.dismiss();
                        Toast.makeText(PostActivity.this, "Post added successfully", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(PostActivity.this, Home.class));
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Toast.makeText(PostActivity.this, "Failed to add post to Firestore: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
    private void updatePostCount(final String userId) {
        db.collection("posts")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        int postCount = queryDocumentSnapshots.size();
                        String postCountString = String.valueOf(postCount); // Convert post count to string
                        // Update the 'posts' field of the user document with the post count string
                        DocumentReference userRef = db.collection("Users").document(userId);
                        userRef.update("posts", postCountString)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        // Post count updated successfully
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        // Failed to update post count
                                        Toast.makeText(PostActivity.this, "Failed to update post count: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Failed to retrieve post count
                        Toast.makeText(PostActivity.this, "Failed to retrieve post count: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }







    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
