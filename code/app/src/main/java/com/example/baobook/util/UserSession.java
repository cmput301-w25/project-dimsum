package com.example.baobook.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.baobook.constant.SharedPreferencesConstants;
import com.example.baobook.model.User;

public class UserSession {
    private final SharedPreferences sharedPreferences;

    public UserSession(Context context) {
        this.sharedPreferences = context.getSharedPreferences(SharedPreferencesConstants.USER_SESSION, Context.MODE_PRIVATE);
    }

    public void setUsername(String username) {
        sharedPreferences.edit()
                .putString(SharedPreferencesConstants.USERNAME, username)
                .apply();
    }
    public User getUser(){
        return new User(this.getUsername(), null);
    }
    public String getUsername() {
        return sharedPreferences.getString(SharedPreferencesConstants.USERNAME, "null");
    }

    public void setLoggedIn(Boolean isLoggedIn) {
        sharedPreferences.edit()
                .putBoolean(SharedPreferencesConstants.IS_LOGGED_IN, isLoggedIn)
                .apply();
    }

    public Boolean getLoggedIn() {
        return sharedPreferences.getBoolean(SharedPreferencesConstants.IS_LOGGED_IN, false);
    }
}
