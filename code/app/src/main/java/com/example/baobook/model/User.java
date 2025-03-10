package com.example.baobook.model;

import java.util.ArrayList;
import java.util.List;

public class User {
    private String username;
    private String password;

    private ArrayList<String> followers;
    private ArrayList<String> followings;


    /**
     * Default constructor initializing empty followers and followings lists.
     */
    public User() {
        this.followers = new ArrayList<>();
        this.followings = new ArrayList<>();
    }

    /**
     * Creates a user with specified username and password.
     * Initializes empty followers and followings lists.
     *
     * @param username the username of the user
     * @param password the user's password
     */

    public User(String username, String password) {
        this.username = username;
        this.password = password;
        this.followers = new ArrayList<>();
        this.followings = new ArrayList<>();
    }

    /**
     * Creates a user with username, password, and predefined lists of followers and followings.
     *
     * @param username  the user's username
     * @param password  the user's password
     * @param followers initial list of followers
     * @param following initial list of followings
     */

    public User(String username, String password, ArrayList<String> followers, ArrayList<String> following) {
        this.username = username;
        this.password = password;
        this.followers = followers;
        this.followings = following;
    }

    /**
     * Sets the username.
     * @param username new username
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Sets the user's password.
     * @param password new password
     */

    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Retrieves the username.
     * @return username
     */

    public String getUsername() {
        return this.username;
    }

    /**
     * Retrieves the password.
     * @return password
     */

    public String getPassword() {
        return this.password;
    }

    /**
     * Retrieves the list of followers.
     * @return list of followers
     */

    public List<String> getFollowers() {
        return followers;
    }

    /**
     * Retrieves the list of users this user is following.
     * @return list of followings
     */
    public List<String> getFollowings() {
        return followings;
    }

    /**
     * Adds a follower to this user's followers list.
     * @param follower username of the new follower
     * @throws RuntimeException if the user tries to add themselves as follower
     */
    public void addFollower(String follower) throws RuntimeException {
        if (follower.equals(username)) {
            throw new RuntimeException("Attempted to follow self.");
        }

        if (!followers.contains(follower)) {
            followers.add(follower);
        }
    }

    /**
     * Removes a follower from this user's follower list.
     * @param follower username to remove
     */
    public void removeFollower(String follower) {
        followers.remove(follower);
    }


    /**
     * Adds a new user to the followings list.
     * @param following username to follow
     * @throws RuntimeException if attempting to follow oneself
     */
    public void addFollowing(String following) throws RuntimeException {
        if (following.equals(username)) {
            throw new RuntimeException("Attempted to follow self.");
        }

        if (!followings.contains(following)) {
            followings.add(following);
        }
    }

    /**
     * Removes a user from followers.
     * @param following username to remove from followers
     */
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
