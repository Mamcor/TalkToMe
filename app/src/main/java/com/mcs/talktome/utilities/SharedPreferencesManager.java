package com.mcs.talktome.utilities;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.mcs.talktome.models.User;

import java.io.Serializable;
import java.util.Map;

public class SharedPreferencesManager{
    private final SharedPreferences  sharedPreferences;

    public SharedPreferencesManager(Context context) {
        sharedPreferences = context.getSharedPreferences(Constants.KEY_PREFERENCE_NAME, Context.MODE_PRIVATE);
    }

    public void putBoolean(String key, Boolean value) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(key, value);
        editor.apply();
    }

    public Boolean getBoolean(String key) {
        return sharedPreferences.getBoolean(key, false);
    }

    public void putString(String key, String value) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key, value);
        editor.apply();
    }

    public String getString(String key) {
        return sharedPreferences.getString(key, null);
    }

    // add GSON dependency in gradle (implementation 'com.google.code.gson:gson:2.8.9')
    public void putUser(String key, User user) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(user);
        editor.putString(key, json);
        editor.apply();
    }

    public User getUser(String key) {
        Gson gson = new Gson();
        String json = sharedPreferences.getString(key, "No user stored");
        return gson.fromJson(json, User.class);
    }

    public void clear() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
    }

    public Map<String, ?> getAll() {
        return sharedPreferences.getAll();
    }
}
