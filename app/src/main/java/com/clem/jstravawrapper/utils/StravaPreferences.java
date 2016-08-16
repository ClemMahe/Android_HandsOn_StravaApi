package com.clem.jstravawrapper.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by CMA10935 on 16/08/2016.
 */
public class StravaPreferences {

    private static final String PREF_STRAVA = "PREF_STRAVA";
    private static final String PREF_STRAVA_TOKEN = "PREF_STRAVA_TOKEN";

    public static void setStravaAccessToken(Context context, String token){
        SharedPreferences preferences = context.getSharedPreferences(PREF_STRAVA, 0);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(PREF_STRAVA_TOKEN, token);
        editor.commit();
    }

    public static String getStravaAccessToken(Context context){
        SharedPreferences preferences = context.getSharedPreferences(PREF_STRAVA, 0);
        String accessToken = preferences.getString(PREF_STRAVA_TOKEN, null);
        return accessToken;
    }

}
