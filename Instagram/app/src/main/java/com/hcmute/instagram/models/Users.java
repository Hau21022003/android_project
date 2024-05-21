package com.hcmute.instagram.models;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

public class Users implements Parcelable {

    private String Description, FullName, Posts, ProfilePhoto, Username, Website,User_id;
    private ArrayList<String> Followers, Following;

    public Users() {
    }

    public Users(String Description, ArrayList<String> followers, ArrayList<String> following, String fullName, String posts, String profilePhoto, String username, String website, String user_id) {
        Description = Description;
        Followers = followers;
        Following = following;
        FullName = fullName;
        Posts = posts;
        ProfilePhoto = profilePhoto;
        Username = username;
        Website = website;
        User_id = user_id;
    }



    protected Users(Parcel in) {
        Description = in.readString();
        Followers = in.createStringArrayList();
        Following = in.createStringArrayList();
        FullName = in.readString();
        Posts = in.readString();
        ProfilePhoto = in.readString();
        Username = in.readString();
        Website = in.readString();
        User_id = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(Description);
        dest.writeStringList(Followers);
        dest.writeStringList(Following);
        dest.writeString(FullName);
        dest.writeString(Posts);
        dest.writeString(ProfilePhoto);
        dest.writeString(Username);
        dest.writeString(Website);
        dest.writeString(User_id);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Users> CREATOR = new Creator<Users>() {
        @Override
        public Users createFromParcel(Parcel in) {
            return new Users(in);
        }

        @Override
        public Users[] newArray(int size) {
            return new Users[size];
        }
    };

    public String getDescription() {
        return Description;
    }

    public void setDescription(String Description) {
        Description = Description;
    }

    public ArrayList<String> getFollowers() {
        return Followers;
    }

    public void setFollowers(ArrayList<String> followers) {
        Followers = followers;
    }

    public ArrayList<String> getFollowing() {
        return Following;
    }

    public void setFollowing(ArrayList<String> following) {
        Following = following;
    }

    public String getFullName() {
        return FullName;
    }

    public void setFullName(String fullName) {
        FullName = fullName;
    }

    public String getPosts() {
        return Posts;
    }

    public void setPosts(String posts) {
        Posts = posts;
    }

    public String getProfilePhoto() {
        return ProfilePhoto;
    }

    public void setProfilePhoto(String profilePhoto) {
        ProfilePhoto = profilePhoto;
    }

    public String getUsername() {
        return Username;
    }

    public void setUsername(String username) {
        Username = username;
    }

    public String getWebsite() {
        return Website;
    }

    public void setWebsite(String website) {
        Website = website;
    }

    public String getUser_id() {
        return User_id;
    }

    public void setUser_id(String user_id) {
        User_id = user_id;
    }

    @Override
    public String toString() {
        return "Users{" +
                "Description='" + Description + '\'' +
                ", Followers='" + Followers + '\'' +
                ", Following='" + Following + '\'' +
                ", FullName='" + FullName + '\'' +
                ", Posts='" + Posts + '\'' +
                ", ProfilePhoto='" + ProfilePhoto + '\'' +
                ", Username='" + Username + '\'' +
                ", Website='" + Website + '\'' +
                ", User_id='" + User_id + '\'' +
                '}';
    }
}