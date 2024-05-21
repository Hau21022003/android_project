package com.hcmute.instagram.Messages;

import java.util.Date;
import java.util.concurrent.TimeUnit;

public class ConvertObjectTime {
    public static String convertTimestampToString(Object timestampObj) {
        if (timestampObj instanceof com.google.firebase.Timestamp) {
            com.google.firebase.Timestamp timestamp = (com.google.firebase.Timestamp) timestampObj;
            Date date = timestamp.toDate(); // Convert Firestore Timestamp to Date
            long currentTimeMillis = System.currentTimeMillis();
            long timestampMillis = date.getTime();
            long diffMillis = currentTimeMillis - timestampMillis;

            long seconds = Math.abs(TimeUnit.MILLISECONDS.toSeconds(diffMillis));
            long minutes = Math.abs(TimeUnit.MILLISECONDS.toMinutes(diffMillis));
            long hours =Math.abs( TimeUnit.MILLISECONDS.toHours(diffMillis));
            long days = Math.abs(TimeUnit.MILLISECONDS.toDays(diffMillis));
            long weeks = days / 7;
            long months = days / 30;
            long years = days / 365;

            if (seconds < 60) {
                return seconds + " seconds ago";
            } else if (minutes < 60) {
                return minutes + " minutes ago";
            } else if (hours < 24) {
                return hours + " hours ago";
            } else if (days < 7) {
                return days + " days ago";
            } else if (weeks < 4) {
                return weeks + " weeks ago";
            } else if (months < 12) {
                return months + " months ago";
            } else {
                return years + " years ago";
            }
        } else {
            return timestampObj.toString(); // Handle other types of timestamps if necessary
        }
    }
}
