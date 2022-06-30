package com.mcs.talktome.utilities;

import java.util.HashMap;

public class Constants {
    public static final int FRAGMENT_HOME_ID = 0;
    public static final int FRAGMENT_PROFILE_ID = 1;
    public static final int FRAGMENT_CHAT_ID = 2;
    public static final int FRAGMENT_FRIENDS_ID = 3;
    public static final int FRAGMENT_FRIENDS_ID_BIS = 6;
    public static final int FRAGMENT_ABOUT_ID = 4;
    public static final int FRAGMENT_SETTINGS_ID = 5;
    public static final String KEY_PREFERENCE_NAME = "chatAppPreference";
    public static final String KEY_COLLECTION_USERS = "Users";
    public static final String KEY_USER_NAME = "Name";
    public static final String KEY_USER_EMAIL = "Email";
    public static final String KEY_USER_PASSWORD = "Password";
    public static final String KEY_USER_IMAGE = "Image";
    public static final String KEY_IS_LOGIN = "isLogin";
    public static final String KEY_USER = "User";
    public static final String KEY_USER_ID = "User Id";
    public static final String KEY_SENDER_ID = "Sender Id";
    public static final String KEY_RECEIVER_ID = "Receiver Id";
    public static final String KEY_USER_MESSAGE = "Message";
    public static final String KEY_COLLECTION_MSG = "All messages";
    public static final String KEY_COLLECTION_RECENT_MSG = "Last messages";
    public static final String KEY_TIMESTAMP = "DateTime";
    public static final String KEY_FCM_TOKEN = "fcmToken";
    public static final String KEY_SENDER_NAME = "Sender name";
    public static final String KEY_SENDER_IMAGE = "Sender image";
    public static final String KEY_RECEIVER_NAME = "Receiver name";
    public static final String KEY_RECEIVER_IMAGE = "Receiver image";
    public static final String KEY_LAST_MESSAGE = "LastMessage";
    public static final String KEY_AVAILABILITY = "Availability";
    public static final String REMOTE_MSG_AUTHORIZATION = "Authorization";
    public static final String REMOTE_MSG_CONTENT_TYPE = "Content-Type";
    public static final String REMOTE_MSG_DATA = "data";
    public static final String REMOTE_MSG_REGISTRATION_IDS = "registration_ids";

    public static HashMap<String, String> remoteMsgHeaders = null;

    public static HashMap<String, String> getRemoteMsgHeaders() {
        if (remoteMsgHeaders == null) {
            remoteMsgHeaders = new HashMap<>();
            remoteMsgHeaders.put(
                    REMOTE_MSG_AUTHORIZATION,
                    "key=AAAAYGeo20w:APA91bE9bp5cHr214XasZiZWUTIsUbHc9lVcAv6L2hO0nHgVWQgY0lIS0B-aJqyAYTbTqXChsCGnH3hbuTSXu_KRHXAzCvBeH4I0JeFnWJtnGXUlcOo9pWIJlujoYm9Y9MaiLFiICerM"
            );
            remoteMsgHeaders.put(
                    REMOTE_MSG_CONTENT_TYPE,
                    "application/json"
            );

        }
        return remoteMsgHeaders;
    }
}
