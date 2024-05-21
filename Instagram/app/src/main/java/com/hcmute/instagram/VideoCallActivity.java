package com.hcmute.instagram;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.hcmute.instagram.Messages.MessageActivity;
import com.hcmute.instagram.models.Users;
import com.zegocloud.uikit.components.audiovideo.ZegoAvatarViewProvider;
import com.zegocloud.uikit.prebuilt.call.ZegoUIKitPrebuiltCallConfig;
import com.zegocloud.uikit.prebuilt.call.ZegoUIKitPrebuiltCallService;
import com.zegocloud.uikit.prebuilt.call.config.ZegoNotificationConfig;
import com.zegocloud.uikit.prebuilt.call.invite.ZegoUIKitPrebuiltCallInvitationConfig;
import com.zegocloud.uikit.prebuilt.call.invite.internal.ZegoCallInvitationData;
import com.zegocloud.uikit.prebuilt.call.invite.internal.ZegoUIKitPrebuiltCallConfigProvider;
import com.zegocloud.uikit.service.defines.ZegoUIKitUser;

public class VideoCallActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_call);

        final String[] fullname = new String[1];

        final String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference userDocRef = db.collection("Users").document(userId);
        userDocRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                Users user = documentSnapshot.toObject(Users.class);
                if (user != null) {
                    fullname[0] = user.getUsername();
                } else {
                    FirebaseAuth.getInstance().signOut();
                    Intent intent = new Intent(VideoCallActivity.this, Login.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK );
                    startActivity(intent);
                }
            }
        }).addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                Intent intent = new Intent(VideoCallActivity.this, Home.class);
                startActivity(intent);
                videoCallServices(userId, fullname[0]);
            }
        });

    }

    private void videoCallServices(String userID, String fullname) {
        long appID = 652926078; // your App ID of Zoge Cloud
        String appSign = "c4280820d2898e8fcc1475f022940b462cbc8ccb7d198ee9490f4466dcb2c9a5"; // your App Sign of Zoge Cloud

        ZegoUIKitPrebuiltCallInvitationConfig callInvitationConfig = new ZegoUIKitPrebuiltCallInvitationConfig();

        ZegoNotificationConfig notificationConfig = new ZegoNotificationConfig();

        notificationConfig.sound = "zego_uikit_sound_call";
        notificationConfig.channelID = "CallInvitation";
        notificationConfig.channelName = "CallInvitation";

        callInvitationConfig.notificationConfig = notificationConfig;

        callInvitationConfig.provider = new ZegoUIKitPrebuiltCallConfigProvider() {
            @Override
            public ZegoUIKitPrebuiltCallConfig requireConfig(ZegoCallInvitationData invitationData) {
                ZegoUIKitPrebuiltCallConfig config = ZegoUIKitPrebuiltCallConfig.oneOnOneVideoCall();
                config.avatarViewProvider = new ZegoAvatarViewProvider() {
                    @Override
                    public View onUserIDUpdated(ViewGroup parent, ZegoUIKitUser uiKitUser) {
                        ImageView imageView = new ImageView(parent.getContext());
                        // Please note that here you need to return different avatars for different users based on the user parameter in the callback parameters.
                        // If you hardcode a URL, then everyone's avatar will be the one you hardcoded.
                        String userId = uiKitUser.userID;
                        FirebaseFirestore db = FirebaseFirestore.getInstance();
                        DocumentReference userDocRef = db.collection("Users").document(userId);

                        userDocRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                Users user = documentSnapshot.toObject(Users.class);
                                if (user != null) {
                                    String avatarUrl = user.getProfilePhoto();
                                    if (!TextUtils.isEmpty(avatarUrl)) {
                                        RequestOptions requestOptions = new RequestOptions().circleCrop();
                                        Glide.with(parent.getContext()).load(avatarUrl).apply(requestOptions).into(imageView);
                                    }
                                }
                            }
                        });
                        return imageView;
                    }
                };
                return config;
            }
        };
        ZegoUIKitPrebuiltCallService.init(getApplication(), appID, appSign, userID, fullname, callInvitationConfig);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ZegoUIKitPrebuiltCallService.unInit();
    }

}