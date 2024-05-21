package com.hcmute.instagram.Utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import com.hcmute.instagram.R;
import com.hcmute.instagram.Retrofit.RetrofitClient;
import com.hcmute.instagram.Search.UserSearchProfileActivity;
import com.hcmute.instagram.models.Comments;
import com.hcmute.instagram.models.Users;

public class CommentListAdapter extends ArrayAdapter<Comments> {

    private LayoutInflater mInflater;
    private int layoutResource;
    private Context mContext;
    private FirebaseFirestore mFirestore;

    public CommentListAdapter(@NonNull Context context, @LayoutRes int resource,
                              @NonNull List<Comments> objects) {
        super(context, resource, objects);
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mContext = context;
        layoutResource = resource;
        mFirestore = FirebaseFirestore.getInstance();
    }

    private static class ViewHolder{
        TextView comment, timestamp, reply, likes, name;
        CircleImageView profileImage;
        ImageView like;
        LinearLayout toxicContentLayout;
        TextView showToxicContent;
    }

    @NonNull
    @Override
    public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        final ViewHolder holder;

        if(convertView == null){
            convertView = mInflater.inflate(layoutResource, parent, false);
            holder = new ViewHolder();

            holder.name = convertView.findViewById(R.id.comment_posted);
            holder.comment = convertView.findViewById(R.id.comment);

            holder.timestamp = convertView.findViewById(R.id.comment_time_posted);
            holder.reply = convertView.findViewById(R.id.comment_reply);
            holder.likes = convertView.findViewById(R.id.comment_likes);
            holder.like = convertView.findViewById(R.id.img_heart);
            holder.profileImage = convertView.findViewById(R.id.user_img);
            holder.toxicContentLayout = convertView.findViewById(R.id.layout_each_comment_toxic_layout);
            holder.showToxicContent = convertView.findViewById(R.id.layout_each_comment_show_toxic_content);

            convertView.setTag(holder);
        }else{
            holder = (ViewHolder) convertView.getTag();
        }



        // Set the timestamp difference
        String timestampDifference = getTimestampDifference(getItem(position));
            holder.timestamp.setText(timestampDifference);


        // Set the username and profile photo
        mFirestore.collection("Users")
                .document(getItem(position).getUser_id())
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            Users user = documentSnapshot.toObject(Users.class);
                            if (user != null) {
                                String comment = getItem(position).getComment();
                                holder.comment.setText(comment);
                                holder.name.setText(user.getUsername());
                                Glide.with(mContext)
                                        .load(user.getProfilePhoto())
                                        .into(holder.profileImage);

                                holder.profileImage.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        Intent intent=new Intent(mContext, UserSearchProfileActivity.class);
                                        intent.putExtra("SearchedUserid",user.getUser_id());
                                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                        mContext.startActivity(intent);
                                    }
                                });

                                RetrofitClient.getAPIService().postPredictContent(comment).enqueue(new Callback<Boolean>() {
                                    @Override
                                    public void onResponse(Call<Boolean> call, Response<Boolean> response) {
                                        Boolean isToxicComment = response.body();
                                        if(isToxicComment){
                                            holder.toxicContentLayout.setVisibility(View.VISIBLE);
                                            holder.comment.setVisibility(View.GONE);
                                            holder.showToxicContent.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    holder.toxicContentLayout.setVisibility(View.GONE);
                                                    holder.comment.setVisibility(View.VISIBLE);
                                                }
                                            });
                                        }
                                    }

                                    @Override
                                    public void onFailure(Call<Boolean> call, Throwable t) {

                                    }
                                });
                            }
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(mContext, "Failed to fetch user data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

        try{
            if(position == 0){
                holder.likes.setVisibility(View.GONE);
            }else {
                holder.likes.setVisibility(View.GONE);
            }
        }catch (NullPointerException e){
            Toast.makeText(mContext, "getView: NullPointerException:"+e.getMessage(), Toast.LENGTH_SHORT).show();
        }

        return convertView;
    }

    /**
     * Returns a string representing the number of days ago the post was made
     * @return
     */
    private String getTimestampDifference(Comments comment) {
        String difference = "";
        Calendar c = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        Date today = c.getTime();
        Date timestamp;
        final String photoTimestamp = comment.getDate_created();
        try {
            timestamp = sdf.parse(photoTimestamp);
            long diffInMillis = Math.abs(today.getTime() - timestamp.getTime());
            long seconds = TimeUnit.MILLISECONDS.toSeconds(diffInMillis);
            long minutes = TimeUnit.MILLISECONDS.toMinutes(diffInMillis);
            long hours = TimeUnit.MILLISECONDS.toHours(diffInMillis);
            long days = TimeUnit.MILLISECONDS.toDays(diffInMillis);
            long weeks = days / 7;
            long months = days / 30;
            long years = days / 365;
            if (seconds == 0){
                difference = "Just finished";
            }
            else if (seconds < 60) {
                difference = seconds + " seconds ago";
            } else if (minutes < 60) {
                difference = minutes + " minutes ago";
            } else if (hours < 24) {
                difference = hours + " hours ago";
            } else if (days < 7) {
                difference = days + " days ago";
            } else if (weeks < 4) {
                difference = weeks + " weeks ago";
            } else if (months < 12) {
                difference = months + " months ago";
            } else {
                difference = years + " years ago";
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return difference;
    }

}
