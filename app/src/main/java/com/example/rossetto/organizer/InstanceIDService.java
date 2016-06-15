package com.example.rossetto.organizer;


import android.content.SharedPreferences;
import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

import java.io.IOException;
import java.util.HashMap;


public class InstanceIDService extends FirebaseInstanceIdService {

    private static final String TAG = InstanceIDService.class.getSimpleName();

    /**
     * Called if InstanceID token is updated. This may occur if the security of
     * the previous token had been compromised. Note that this is called when the InstanceID token
     * is initially generated so this is where you would retrieve the token.
     */
    // [START refresh_token]
    @Override
        public void onTokenRefresh() {
        // Get updated InstanceID token.
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Log.d(TAG, "Refreshed token: " + refreshedToken);

        // TODO: Implement this method to send any registration to your app's servers.
        sendRegistrationToServer(refreshedToken);
    }
    // [END refresh_token]

    /**
     * Persist token to third-party servers.
     *
     * Modify this method to associate the user's FCM InstanceID token with any server-side account
     * maintained by your application.
     *
     * @param token The new token.
     */
    public static void sendRegistrationToServer(String token) {
        String oldToken = MainActivityFragment.prefs.getString(MainActivityFragment.userToken, "none");
        SharedPreferences.Editor editor = MainActivityFragment.prefs.edit();
        editor.putString(MainActivityFragment.userToken, token);
        editor.commit();
        int userId = MainActivityFragment.prefs.getInt(MainActivityFragment.userKey, 0);
        if (userId != 0){
            String url = "http://52.203.161.136/usuarios/"+userId+"/token";
            HashMap<String, String> pars = new HashMap<String, String>();
            pars.put("old_token", oldToken);
            pars.put("token", token);
            try {
                HttpTools tools = new HttpTools(url, pars);
                tools.doPut();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}