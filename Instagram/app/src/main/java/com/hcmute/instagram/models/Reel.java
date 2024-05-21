package com.hcmute.instagram.models;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

public class Reel implements Parcelable {

        private String Caption,dateCreated,thumbnailUrl,videoId,userId,Tags, videoUrl;
        private List<Likes> likes;
        private List<Comments> comments;

    public Reel(String caption, String dateCreated, String thumbnailUrl, String videoId, String userId, String tags, String videoUrl, List<Likes> likes, List<Comments> comments) {
        Caption = caption;
        this.dateCreated = dateCreated;
        this.thumbnailUrl = thumbnailUrl;
        this.videoId = videoId;
        this.userId = userId;
        Tags = tags;
        this.videoUrl = videoUrl;
        this.likes = likes;
        this.comments = comments;
    }

    public Reel() {
    }

    protected Reel(Parcel in) {
        Caption = in.readString();
        dateCreated = in.readString();
        thumbnailUrl = in.readString();
        videoId = in.readString();
        userId = in.readString();
        Tags = in.readString();
        videoUrl = in.readString();
    }

    public static final Creator<Reel> CREATOR = new Creator<Reel>() {
        @Override
        public Reel createFromParcel(Parcel in) {
            return new Reel(in);
        }

        @Override
        public Reel[] newArray(int size) {
            return new Reel[size];
        }
    };

    public String getCaption() {
        return Caption;
    }

    public void setCaption(String caption) {
        Caption = caption;
    }

    public String getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(String dateCreated) {
        this.dateCreated = dateCreated;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }

    public String getVideoId() {
        return videoId;
    }

    public void setVideoId(String videoId) {
        this.videoId = videoId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getTags() {
        return Tags;
    }

    public void setTags(String tags) {
        Tags = tags;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }

    public List<Likes> getLikes() {
        return likes;
    }

    public void setLikes(List<Likes> likes) {
        this.likes = likes;
    }

    public List<Comments> getComments() {
        return comments;
    }

    public void setComments(List<Comments> comments) {
        this.comments = comments;
    }

    @Override
    public String toString() {
        return "Reel{" +
                "Caption='" + Caption + '\'' +
                ", dateCreated='" + dateCreated + '\'' +
                ", thumbnailUrl='" + thumbnailUrl + '\'' +
                ", videoId='" + videoId + '\'' +
                ", userId='" + userId + '\'' +
                ", Tags='" + Tags + '\'' +
                ", videoUrl='" + videoUrl + '\'' +
                ", likes=" + likes +
                ", comments=" + comments +
                '}';
    }
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(Caption);
        dest.writeString(dateCreated);
        dest.writeString(thumbnailUrl);
        dest.writeString(videoId);
        dest.writeString(userId);
        dest.writeString(Tags);
        dest.writeString(videoUrl);

    }
}
