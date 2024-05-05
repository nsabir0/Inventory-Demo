package com.ms.inventory.utils;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.ms.inventory.activity.LoginActivity;

import java.util.HashMap;

public class UserSession {

    // Shared Preferences reference
    SharedPreferences pref;

    // Editor reference for Shared preferences
    SharedPreferences.Editor editor;

    // Context
    Context _context;

    // Shared preferences mode
    int PRIVATE_MODE = 0;

    // Shared preferences file name
    public static final String PREFER_NAME = "offline_reg";

    // All Shared Preferences Keys
    public static final String IS_USER_LOGIN = "IsUserLoggedIn";

    // User name (make variable public to access from outside)
    public static final String KEY_NAME = "user_name";

    // password
    public static final String KEY_PASSWORD = "password";

    public UserSession(Context context) {
        this._context = context;
        pref = _context.getSharedPreferences(PREFER_NAME, PRIVATE_MODE);
        editor = pref.edit();
    }

    //Create login session
    public void createUserLoginSession(String userName, String uPassword){
        // Storing login value as TRUE
        editor.putBoolean(IS_USER_LOGIN, true);

        // Storing name in preferences
        editor.putString(KEY_NAME, userName);

        // Storing email in preferences
        editor.putString(KEY_PASSWORD,  uPassword);

        // commit changes
        editor.commit();
    }

    /**
     * Check login method will check user login status
     * If false it will redirect user to login page
     * Else do anything
     * */
    public boolean checkLogin(){
        // Check login status
        if(!this.isUserLoggedIn()){
            _context.startActivity(new Intent(_context.getApplicationContext(), LoginActivity.class));
            return true;
        }
        return false;
    }

    /**
     * Get stored session data
     * */
    public HashMap<String, String> getUserDetails(){

        //Use hashmap to store user credentials
        HashMap<String, String> user = new HashMap<String, String>();

        // user name
        user.put(KEY_NAME, pref.getString(KEY_NAME, null));

        // user email id
        user.put(KEY_PASSWORD, pref.getString(KEY_PASSWORD, null));

        // return user
        return user;
    }

    /**
     * Clear session details
     * */
    public void logoutUser(){

        // Clearing all user data from Shared Preferences
        editor.clear();
        editor.commit();
        _context.startActivity(new Intent(_context.getApplicationContext(), LoginActivity.class));
    }

    // Check for login
    public boolean isUserLoggedIn(){
        return pref.getBoolean(IS_USER_LOGIN, false);
    }
}
