package com.hcmute.instagram.models;

import java.util.List;

public class Comments {

    private String comment;
    private String comment_id;
    private String user_id;
    private List<Likes> likes;

    public String getComment_id() {
        return comment_id;
    }

    public void setComment_id(String comment_id) {
        this.comment_id = comment_id;
    }

    private String date_created;

    public Comments(String comment, String user_id, List<Likes> likes, String date_created, String comment_id) {
        this.comment = comment;
        this.user_id = user_id;
        this.likes = likes;
        this.date_created = date_created;
        this.comment_id = comment_id;
    }

    public Comments() {
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public List<Likes> getLikes() {
        return likes;
    }

    public void setLikes(List<Likes> likes) {
        this.likes = likes;
    }

    public String getDate_created() {
        return date_created;
    }

    public void setDate_created(String date_created) {
        this.date_created = date_created;
    }

    @Override
    public String toString() {
        return "Comments{" +
                "comment='" + comment + '\'' +
                ", user_id='" + user_id + '\'' +
                ", likes=" + likes +
                ", date_created='" + date_created + '\'' +
                '}';
    }
}
