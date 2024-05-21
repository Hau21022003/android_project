package com.hcmute.instagram.models;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

public class Video implements Parcelable {

    private String Caption,dateCreated,thumbnailUrl,videoId,userId,Tags, videoUrl;
    private List<Likes> likes;
    private List<Comments> comments;

    public Video(String caption, String dateCreated, String thumbnailUrl, String video_path, String videoId, String userId, String tags, List<Likes> likes, List<Comments> comments) {
        Caption = caption;
        this.dateCreated = dateCreated;
        this.thumbnailUrl = thumbnailUrl;
        this.videoUrl = video_path;
        this.videoId = videoId;
        this.userId = userId;
        Tags = tags;
        this.likes = likes;
        this.comments = comments;
    }

    public Video(){
    }


    protected Video(Parcel in) {
        Caption = in.readString();
        dateCreated = in.readString();
        thumbnailUrl = in.readString();
        videoUrl = in.readString();
        videoId = in.readString();
        userId = in.readString();
        Tags = in.readString();
    }

    public static final Creator<Video> CREATOR = new Creator<Video>() {
        @Override
        public Video createFromParcel(Parcel in) {
            return new Video(in);
        }

        @Override
        public Video[] newArray(int size) {
            return new Video[size];
        }
    };


    public String getCaption() {
        return Caption;
    }

    public void setCaption(String caption) {
        Caption = caption;
    }

    public String getDate_Created() {
        return dateCreated;
    }

    @Override
    public String toString() {
        return "Video{" +
                "Caption='" + Caption + '\'' +
                ", Date_Created='" + dateCreated + '\'' +
                ", Thumbnail_Path='" + thumbnailUrl + '\'' +
                ", Video_id='" + videoId + '\'' +
                ", User_id='" + userId + '\'' +
                ", Tags='" + Tags + '\'' +
                ", Video_path='" + videoUrl + '\'' +
                ", likes=" + likes +
                ", comments=" + comments +
                '}';
    }

    public void setDate_Created(String date_Created) {
        dateCreated = date_Created;
    }

    public String getThumbnail_Path() {
        return thumbnailUrl;
    }

    public void setThumbnail_Path(String thumbnail_Path) {
        thumbnailUrl = thumbnail_Path;
    }

    public String getVideo_id() {
        return videoId;
    }

    public void setVideo_id(String video_id) {
        videoId = video_id;
    }

    public String getUser_id() {
        return userId;
    }

    public void setUser_id(String user_id) {
        userId = user_id;
    }

    public String getTags() {
        return Tags;
    }

    public void setTags(String tags) {
        Tags = tags;
    }

    public String getVideo_path() {
        return videoUrl;
    }

    public void setVideo_path(String video_path) {
        videoUrl = video_path;
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
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(Caption);
        dest.writeString(dateCreated);
        dest.writeString(thumbnailUrl);
        dest.writeString(videoUrl);
        dest.writeString(videoId);
        dest.writeString(userId);
        dest.writeString(Tags);
    }
}
