package com.silver.bet365leader;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class AppSharedInfo {

    private Context context;
    private SharedPreferences preferences;

    public AppSharedInfo(Context context){
        this.context = context;
        this.preferences = PreferenceManager.getDefaultSharedPreferences(this.context);
    }

    public void saveEndpoint(String endpoint){

        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("Endpoint",endpoint);
        editor.apply();
    }

    public String getEndpoint(){

        return preferences.getString("Endpoint", context.getString(R.string.default_endpoint));
    }

    public void saveUsername(String username){
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("Username", username);
        editor.apply();
    }

    public String getUsername(){

        return preferences.getString("Username", "");
    }
}
