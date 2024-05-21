package com.hcmute.instagram.Messages.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;

import com.hcmute.instagram.Messages.ConvertObjectTime;
import com.hcmute.instagram.Messages.Model.Chat;
import com.hcmute.instagram.R;

import org.w3c.dom.Text;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {

    public static final int MSG_TYPE_LEFT = 0;
    public static final int MSG_TYPE_RIGHT = 1;

    private Context mContext;
    private List<Chat> mChat;
    private String imageurl;

    FirebaseUser fuser;

    public MessageAdapter(Context mContext, List<Chat> mChat, String imageurl) {
        this.mContext = mContext;
        this.mChat = mChat;
        this.imageurl = imageurl;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == MSG_TYPE_RIGHT) {
            View view = LayoutInflater.from(mContext).inflate(R.layout.msg_item_right, parent, false);
            return new ViewHolder(view);
        } else {
            View view = LayoutInflater.from(mContext).inflate(R.layout.msg_item_left, parent, false);
            return new ViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        Chat chat = mChat.get(position);

        holder.show_message.setText(chat.getMessage());
        Glide.with(mContext).load(imageurl).into(holder.profile_image);
        if (chat.getTimestamp() != null)
            holder.timestamp.setText(ConvertObjectTime.convertTimestampToString(chat.getTimestamp()));
//        if (position == mChat.size()-1){
//            if (chat.isIsseen()){
//                holder.txt_seen.setText("Seen");
//            } else {
//                holder.txt_seen.setText("Delivered");
//            }
//        } else {
//            holder.txt_seen.setVisibility(View.GONE);
//        }
        if (!chat.isIsseen()) holder.state.setText("Sent");
        else holder.state.setText("Seen");
        holder.state.setVisibility(View.GONE);
        holder.timestamp.setVisibility(View.GONE);

        holder.show_message.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Toggle timestamp visibility
                if (holder.timestamp.getVisibility() == View.VISIBLE) {
                    // Slide up animation
                    Animation slideUp = new TranslateAnimation(0, 0, 0, -holder.timestamp.getHeight());
                    slideUp.setDuration(300);
                    slideUp.setFillAfter(true);
                    holder.timestamp.startAnimation(slideUp);
                    holder.timestamp.setVisibility(View.GONE);

                    holder.state.startAnimation(slideUp);
                    holder.state.setVisibility(View.GONE);
                } else {
                    // Slide down animation
                    Animation slideDown = new TranslateAnimation(0, 0, -holder.timestamp.getHeight(), 0);
                    slideDown.setDuration(300);
                    slideDown.setFillAfter(true);
                    holder.timestamp.startAnimation(slideDown);
                    holder.timestamp.setVisibility(View.VISIBLE);

                    holder.state.startAnimation(slideDown);
                    holder.state.setVisibility(View.VISIBLE);
                }
            }
        });

    }

    @Override
    public int getItemCount() {
        return mChat.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public TextView show_message;
        public ImageView profile_image;
        public TextView txt_seen;
        public TextView timestamp;
        public TextView state;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            show_message = itemView.findViewById(R.id.showMessage);
            profile_image = itemView.findViewById(R.id.user_img);
            timestamp = itemView.findViewById(R.id.messageTime);
            state = itemView.findViewById(R.id.messageState);
        }
    }

    @Override
    public int getItemViewType(int position) {
        fuser = FirebaseAuth.getInstance().getCurrentUser();
        if (mChat.get(position).getSender().equals(fuser.getUid())) {
            return MSG_TYPE_RIGHT;
        } else {
            return MSG_TYPE_LEFT;
        }
    }
}
