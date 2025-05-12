package com.app.mes.helper;

import android.content.Context;
import android.util.Log;
import com.cloudinary.android.MediaManager;
import java.util.HashMap;
import java.util.Map;

public class CloudinaryHelper {
    private static final String CLOUD_NAME = "dltmvm6q7";
    private static final String API_KEY = "561189422563119";
    private static final String API_SECRET = "he4KZOsYCZkShIpENn5_f-6BtXM";
    private static boolean isInitialized = false;

    public static void init(Context context) {
        if (isInitialized) {
            Log.d("CloudinaryHelper", "MediaManager đã được khởi tạo trước đó");
            return;
        }

        try {
            Map<String, String> config = new HashMap<>();
            config.put("cloud_name", CLOUD_NAME);
            config.put("api_key", API_KEY);
            config.put("api_secret", API_SECRET);

            MediaManager.init(context, config);
            isInitialized = true;
        } catch (IllegalStateException e) {
            // MediaManager đã được khởi tạo trước đó
            isInitialized = true;
        }
    }
}