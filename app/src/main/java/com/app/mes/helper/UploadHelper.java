package com.app.mes.helper;

import android.net.Uri;
import android.widget.Toast;

import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;

import java.util.Map;

public class UploadHelper {
    public interface UploadCallbackListener {
        void onSuccess(String imageUrl);
        void onError(String error);
    }

    public static void uploadImage(Uri imageUri, String folder, String publicId, UploadCallbackListener listener) {
        com.cloudinary.android.MediaManager.get().upload(imageUri)
                .option("folder", folder)
                .option("public_id", publicId)
                .callback(new UploadCallback() {
                    @Override
                    public void onStart(String requestId) {
                        // Không cần làm gì
                    }

                    @Override
                    public void onProgress(String requestId, long bytes, long totalBytes) {
                        // Không cần làm gì
                    }

                    @Override
                    public void onSuccess(String requestId, Map resultData) {
                        String imageUrl = resultData.get("url").toString();
                        listener.onSuccess(imageUrl);
                    }

                    @Override
                    public void onError(String requestId, ErrorInfo error) {
                        listener.onError(error.getDescription());
                    }

                    @Override
                    public void onReschedule(String requestId, ErrorInfo error) {
                        // Không cần làm gì
                    }
                })
                .dispatch();
    }
}
