package com.example.baobook.model;

import java.util.ArrayList;
import java.util.List;

public class User {
    private String username;
    private String password;

    private ArrayList<String> followers;
    private ArrayList<String> followings;

    public User() {
        this.followers = new ArrayList<>();
        this.followings = new ArrayList<>();
    }

    public User(String username, String password) {
        this.username = username;
        this.password = password;
        this.followers = new ArrayList<>();
        this.followings = new ArrayList<>();
    }

    public User(String username, String password, ArrayList<String> followers, ArrayList<String> following) {
        this.username = username;
        this.password = password;
        this.followers = followers;
        this.followings = following;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUsername() {
        return this.username;
    }

    public String getPassword() {
        return this.password;
    }

    public List<String> getFollowers() {
        return followers;
    }

    public List<String> getFollowings() {
        return followings;
    }

    public void addFollower(String follower) throws RuntimeException {
        if (follower.equals(username)) {
            throw new RuntimeException("Attempted to follow self.");
        }

        if (!followers.contains(follower)) {
            followers.add(follower);
        }
    }

    public void removeFollower(String follower) {
        followers.remove(follower);
    }

    public void addFollowing(String following) throws RuntimeException {
        if (following.equals(username)) {
            throw new RuntimeException("Attempted to follow self.");
        }

        if (!followings.contains(following)) {
            followings.add(following);
        }
    }

    public void removeFollowing(String following) {
        followings.remove(following);
    }

    public void followUser(User otherUser) {
        otherUser.addFollower(username);
        addFollowing(otherUser.getUsername());
    }

    public void unfollowUser(User user) {
        user.removeFollower(username);
        removeFollowing(user.getUsername());
    }
}
