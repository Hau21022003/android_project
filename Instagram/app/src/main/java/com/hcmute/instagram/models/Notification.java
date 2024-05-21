package com.hcmute.instagram.models;

public class Notification {

    private String id, userid,text,postid, receiverId;
    private boolean ispost , isseen;
    String postAt ;
    public Notification() {
    }

    public String getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(String receiverId) {
        this.receiverId = receiverId;
    }

    public Notification(String receiverId, String userid, String text, String postid, boolean ispost, boolean isseen, String postAt) {
        this.userid = userid;
        this.receiverId = receiverId;
        this.text = text;
        this.postid = postid;
        this.ispost = ispost;
        this.isseen = isseen;
        this.postAt = postAt;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setIsseen(boolean isseen) {
        this.isseen = isseen;
    }

    public String getPostAt() {
        return postAt;
    }

    public void setPostAt(String postAt) {
        this.postAt = postAt;
    }

    public String getUserid() {
        return userid;
    }

    public boolean isIsseen() {
        return isseen;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getPostid() {
        return postid;
    }

    public void setPostid(String postid) {
        this.postid = postid;
    }

    public boolean isIspost() {
        return ispost;
    }

    public void setIspost(boolean ispost) {
        this.ispost = ispost;
    }
}
