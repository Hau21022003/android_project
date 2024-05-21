package com.hcmute.instagram;

import android.app.Application;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.hcmute.instagram.Messages.MessageActivity;
import com.hcmute.instagram.Post.PostActivity;
import com.hcmute.instagram.Post.ReelActivity;
import com.hcmute.instagram.Profile.ProfileFragment;
import com.hcmute.instagram.Search.SearchFragment;
import com.hcmute.instagram.home.HomeFragment;
import com.hcmute.instagram.R;
import com.hcmute.instagram.home.ReelFragment;
import com.hcmute.instagram.models.Users;
import com.zegocloud.uikit.components.audiovideo.ZegoAvatarViewProvider;
import com.zegocloud.uikit.prebuilt.call.ZegoUIKitPrebuiltCallConfig;
import com.zegocloud.uikit.prebuilt.call.ZegoUIKitPrebuiltCallFragment;
import com.zegocloud.uikit.prebuilt.call.ZegoUIKitPrebuiltCallService;
import com.zegocloud.uikit.prebuilt.call.config.ZegoNotificationConfig;
import com.zegocloud.uikit.prebuilt.call.invite.ZegoUIKitPrebuiltCallInvitationConfig;
import com.zegocloud.uikit.prebuilt.call.invite.ZegoUIKitPrebuiltCallInvitationService;
import com.zegocloud.uikit.prebuilt.call.invite.internal.ZegoCallInvitationData;
import com.zegocloud.uikit.prebuilt.call.invite.internal.ZegoUIKitPrebuiltCallConfigProvider;
import com.zegocloud.uikit.service.defines.ZegoUIKitUser;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class Home extends AppCompatActivity {
    CircleImageView avt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        final String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference userDocRef = db.collection("Users").document(userId);
        ArrayList<ImageView> imageList = new ArrayList<>();
        imageList.add(findViewById(R.id.action_home));
        imageList.add(findViewById(R.id.action_search));
        imageList.add(findViewById(R.id.action_post));
        imageList.add(findViewById(R.id.action_video));
        CircleImageView avt = findViewById(R.id.action_profile);
        userDocRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                Users user = documentSnapshot.toObject(Users.class);
                if (user != null) {
                    Glide.with(Home.this)
                            .load(user.getProfilePhoto())
                            .into(avt);
                } else {
                    FirebaseAuth.getInstance().signOut();
                    Intent intent = new Intent(Home.this, Login.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK );
                    startActivity(intent);
                }
            }
        });
        avt.setAlpha(0.6f);
        setActive(imageList, findViewById(R.id.action_home));
        replaceFragment(new HomeFragment());
    }

    public void onItemClick(View view) {
        ImageView clickedImageView = (ImageView) view;
        ArrayList<ImageView> imageList = new ArrayList<>();
        imageList.add(findViewById(R.id.action_home));
        imageList.add(findViewById(R.id.action_search));
        imageList.add(findViewById(R.id.action_post));
        imageList.add(findViewById(R.id.action_video));
        CircleImageView avt = findViewById(R.id.action_profile);

        if (clickedImageView == imageList.get(0)) {
            replaceFragment(new HomeFragment());
        } else if (clickedImageView == imageList.get(1)) {
            replaceFragment(new SearchFragment());
        }
        else if (clickedImageView == imageList.get(2)) {
            showPopupMenu(clickedImageView);      }
        else if (clickedImageView == imageList.get(3)) {
             replaceFragment(new ReelFragment());
        }
        avt.setAlpha(0.6f);
        setActive(imageList, clickedImageView);

    }
    private void showPopupMenu(View view) {
        PopupMenu popupMenu = new PopupMenu(this, view);
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (item.getItemId() == R.id.Post) {
                    // Xử lý khi người dùng chọn mục "Post"
                    startActivity(new Intent(Home.this, PostActivity.class));
                    return true;
                }
                    else if( item.getItemId() == R.id.Reel) {
                    // Xử lý khi người dùng chọn mục "Reel"
                    startActivity(new Intent(Home.this, ReelActivity.class));
                    return true;
                }
                    else
                        return false;
                }

        });
        popupMenu.inflate(R.menu.post_menu);
        popupMenu.show();
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.Post) {
            startActivity(new Intent(Home.this, PostActivity.class));
            return true;
        } else if (id == R.id.Reel) {
            // Xử lý khi người dùng chọn mục "Reel"
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    public void onItemClick2(View view) {
        CircleImageView clickedImageView = (CircleImageView) view;
        ArrayList<ImageView> imageList = new ArrayList<>();
        imageList.add(findViewById(R.id.action_home));
        imageList.add(findViewById(R.id.action_search));
        imageList.add(findViewById(R.id.action_post));
        imageList.add(findViewById(R.id.action_video));
        CircleImageView avt = findViewById(R.id.action_profile);
        avt.setAlpha(1.0f);
        for (ImageView imageView : imageList) {
            imageView.setAlpha(0.6f);
        }
        replaceFragment(new ProfileFragment());
    }

    private void setActive(ArrayList<ImageView> imgList, ImageView active) {
        for (ImageView imageView : imgList) {
            imageView.setAlpha(imageView == active ? 1.0f : 0.6f);
        }
    }

    private void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frameMainLayout, fragment);
        fragmentTransaction.commit();
    }

//    @Override
//    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
//        Fragment fragment = null;
//        switch (item.getItemId()) {
//            case R.id.Home:
//                fragment = new HomeFragment();
//                break;
//
//            case R.id.search:
//                fragment = new SearchFragment();
//                break;
//
//            case R.id.post:
//                // No need to set a fragment here, just start the PostActivity
//                startActivity(new Intent(Home.this, PostActivity.class));
//                return true;
//
//            case R.id.likes:
//                fragment = new LikeFragment();
//                break;
//
//            case R.id.profile:
//                fragment = new ProfileFragment();
//                break;
//        }
    // Load the selected fragment
//        return loadFragment(fragment);
//    }

//    @Override
//    public void onBackPressed() {
//        super.onBackPressed();
//        new AlertDialog.Builder(this)
//                .setMessage("Are you sure you want to exit?")
//                .setCancelable(false)
//                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
//                    public void onClick(DialogInterface dialog, int id) {
//                        finish();
//                    }
//                })
//                .setNegativeButton("No", null)
//                .show();
//    }
}
