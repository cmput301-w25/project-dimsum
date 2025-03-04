package com.example.baobook.model;

import java.util.ArrayList;

public class User {
    private String username;
    private String password;
    private ArrayList<MoodEvent> moodHistory;
    private ArrayList<User> followers;
    private ArrayList<User> following;
    public User(String username,String password){
        setUsername(username);
        setPassword(password);
        moodHistory = new ArrayList<>();
        followers = new ArrayList<>();
        following = new ArrayList<>();
    }
    public void setUsername(String username){
        this.username = username;
    }
    public void setPassword(String password){
        this.password = password;
    }
    public String getUsername(){
        return this.username;
    }
    public String getPassword(){
        return this.password;
    }
    public ArrayList<MoodEvent> getMoodHistory(){
        return this.moodHistory;
    }
    public ArrayList<User> getFollowers(){
        return this.followers;
    }
    public ArrayList<User> getFollowing(){
        return this.following;
    }
    public void addMoodEvent(MoodEvent moodEvent) {
        moodHistory.add(moodEvent);
    }

    /**
     * Adds a follower to the list of followers.
     * @param follower user that is following
     */
    public void addFollower(User follower) {
        if (!followers.contains(follower)) {
            followers.add(follower);
        }
    }
    /**
     * Adds a following to the list of following.
     * @param following user that is being followed
     */
    public void addFollowing(User following) {
        if (!this.following.contains(following)) {
            this.following.add(following);
        }
    }
    public void removeFollower(User follower) {
        this.followers.remove(follower);
    }
    public void removeFollowing(User following) {
        this.following.remove(following);
    }
    public void removeMoodEvent(MoodEvent moodEvent) {
        moodHistory.remove(moodEvent);
    }
    public boolean isFollowing(User user) {
        return following.contains(user);
    }
    public boolean isFollower(User user) {
        return followers.contains(user);
    }
}
