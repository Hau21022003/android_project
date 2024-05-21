package com.hcmute.instagram.Utils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.hcmute.instagram.Profile.FollowersFollowing;
import com.hcmute.instagram.R;
import com.hcmute.instagram.Search.UserSearchProfileActivity;
import com.hcmute.instagram.models.Notification;
import com.hcmute.instagram.models.Users;

public class SearchUsersAdapter extends RecyclerView.Adapter<SearchUsersAdapter.ViewHolder> {
    private Context mcontext;
    private List<Users> muser;
    private String flag;

    String TAG = "SearchUserAdapter";
//    private LayoutInflater inflater;


    private FirebaseUser firebaseUser;

    public SearchUsersAdapter(Context mcontext, List<Users> muser, String flag) {
        this.mcontext = mcontext;
        this.muser = muser;
        this.flag = flag;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(mcontext).inflate(R.layout.user_search_items,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        final Users users = muser.get(position);

        switch (flag) {
            case ("search"):
                setNoDisplayBtn(holder);
                break;

            case ("follow"):
                FirebaseFirestore db = FirebaseFirestore.getInstance();
                DocumentReference userRef = db.collection("Users").document(FirebaseAuth.getInstance().getCurrentUser().getUid());
                userRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            ArrayList<String> idListCurrentUser = (ArrayList<String>) documentSnapshot.get("following");
                            if (idListCurrentUser.contains(String.valueOf(users.getUser_id()))) {
                                setBtnFollowing(holder);
                            } else if (users.getUser_id().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                                setNoDisplayBtn(holder);
                            } else {
                                setBtnFollow(holder);
                            }
                        }
                    }
                });
                break;

            case ("remove"):
                setBtnRemove(holder);
                break;
        }


        holder.username.setText(users.getUsername());
        holder.fullname.setText(users.getFullName());
        Glide.with(mcontext)
                .load(users.getProfilePhoto())
                .into(holder.profileimage);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(mcontext, UserSearchProfileActivity.class);
                intent.putExtra("SearchedUserid",users.getUser_id());
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                mcontext.startActivity(intent);
            }
        });

        holder.follow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseFirestore db = FirebaseFirestore.getInstance();
                // Thêm ID của người dùng khác vào trường "following" của người dùng hiện tại
                DocumentReference currentUserRef = db.collection("Users").document(FirebaseAuth.getInstance().getCurrentUser().getUid());
                currentUserRef.update("following", FieldValue.arrayUnion(users.getUser_id()));

                // Thêm ID của người dùng hiện tại vào trường "followers" của người dùng khác
                DocumentReference otherUserRef = db.collection("Users").document(users.getUser_id());
                otherUserRef.update("followers", FieldValue.arrayUnion(FirebaseAuth.getInstance().getCurrentUser().getUid()));

                setBtnFollowing(holder);
                addFollowNotification(users.getUser_id());

            }
        });
        holder.following.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseFirestore db = FirebaseFirestore.getInstance();

                // Xóa ID của người dùng khác khỏi trường "following" của người dùng hiện tại
                DocumentReference currentUserRef = db.collection("Users").document(FirebaseAuth.getInstance().getCurrentUser().getUid());
                currentUserRef.update("following", FieldValue.arrayRemove(users.getUser_id()));

                // Xóa ID của người dùng hiện tại khỏi trường "followers" của người dùng khác
                DocumentReference otherUserRef = db.collection("Users").document(users.getUser_id());
                otherUserRef.update("followers", FieldValue.arrayRemove(FirebaseAuth.getInstance().getCurrentUser().getUid()));

                setBtnFollow(holder);

            }
        });

        holder.remove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Tạo AlertDialog.Builder
                AlertDialog.Builder builder = new AlertDialog.Builder(v.getRootView().getContext());

                // Thiết lập tiêu đề và thông điệp
                builder.setTitle("Remove follower?");
                builder.setMessage("We won't tell (" + users.getUsername()+ ") they were removed from your followers.");

                // Thiết lập các nút xác nhận và hủy bỏ
                builder.setPositiveButton("Remove", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        FirebaseFirestore db = FirebaseFirestore.getInstance();

                        // Xóa ID của người dùng khác khỏi trường "followers" của người dùng hiện tại
                        DocumentReference currentUserRef = db.collection("Users").document(FirebaseAuth.getInstance().getCurrentUser().getUid());
                        currentUserRef.update("followers", FieldValue.arrayRemove(users.getUser_id()));

                        // Xóa ID của người dùng hiện tại khỏi trường "following" của người dùng khác
                        DocumentReference otherUserRef = db.collection("Users").document(users.getUser_id());
                        otherUserRef.update("following", FieldValue.arrayRemove(FirebaseAuth.getInstance().getCurrentUser().getUid()));

                        muser.remove(holder.getAdapterPosition());
                        notifyItemRemoved(holder.getAdapterPosition());
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });

                // Hiển thị hộp thoại xác nhận
                builder.show();

            }
        });

    }
    private String getTimestamp() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(new Date());
    }

    private void addFollowNotification(String userid){
        Notification notification = new Notification(userid,FirebaseAuth.getInstance().getCurrentUser().getUid(), " started following you ", "", false, false,getTimestamp());
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

    private void setBtnRemove(ViewHolder holder) {
        holder.follow.setVisibility(View.GONE);
        holder.remove.setVisibility(View.VISIBLE);
        holder.following.setVisibility(View.GONE);
    }

    private void setBtnFollow(ViewHolder holder) {
        holder.follow.setVisibility(View.VISIBLE);
        holder.remove.setVisibility(View.GONE);
        holder.following.setVisibility(View.GONE);
    }

    private void setBtnFollowing(ViewHolder holder) {
        holder.follow.setVisibility(View.GONE);
        holder.remove.setVisibility(View.GONE);
        holder.following.setVisibility(View.VISIBLE);
    }

    private void setNoDisplayBtn(ViewHolder holder) {
        holder.follow.setVisibility(View.GONE);
        holder.remove.setVisibility(View.GONE);
        holder.following.setVisibility(View.GONE);
    }

    @Override
    public int getItemCount() {
        return muser.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        public TextView username,fullname;
        public CircleImageView profileimage;

        public Button follow, following, remove;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            username = (TextView)itemView.findViewById(R.id.userName);
            fullname = (TextView)itemView.findViewById(R.id.fullName);
            profileimage = (CircleImageView)itemView.findViewById(R.id.user_img);

            follow = (Button) itemView.findViewById(R.id.UserSearchItem_Followbtn);
            following = (Button) itemView.findViewById(R.id.UserSearchItem_Followingbtn);
            remove = (Button) itemView.findViewById(R.id.UserSearchItem_Removebtn);
        }
    }
}
