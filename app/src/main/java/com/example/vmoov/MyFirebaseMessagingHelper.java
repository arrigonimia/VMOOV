package com.example.vmoov;

import android.util.Log;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONException;
import org.json.JSONObject;

public class MyFirebaseMessagingHelper {

    private static final String TAG = "MyFirebaseMessagingHelper";

    public static void sendFCMNotification(String title, String message) {
        // Get your Firebase server key from the Firebase Console
        // Project Settings -> Cloud Messaging -> Server key
        String serverKey = "YOUR_FIREBASE_SERVER_KEY";

        // Define the FCM endpoint
        String fcmEndpoint = "https://fcm.googleapis.com/fcm/send";

        // Create a JSON object for the FCM message
        JSONObject jsonMessage = new JSONObject();
        try {
            jsonMessage.put("to", "/topics/all"); // Send to a specific topic or device token
            jsonMessage.put("priority", "high");

            JSONObject notification = new JSONObject();
            notification.put("title", title);
            notification.put("body", message);

            jsonMessage.put("notification", notification);
        } catch (JSONException e) {
            Log.e(TAG, "Error creating JSON message", e);
            return;
        }

        // Send the FCM message using FirebaseMessaging
        FirebaseMessaging.getInstance().send(new RemoteMessage.Builder(serverKey + "@fcm.googleapis.com")
                .setMessageId(Integer.toString(0))
                .addData("message", jsonMessage.toString())
                .build());

        Log.d(TAG, "FCM message sent");
    }
}

