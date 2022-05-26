package com.example.forummpt;

import org.jetbrains.annotations.NotNull;
import java.io.File;
import java.io.IOException;
import okhttp3.*;

public class UploadUtility {

    public static void uploadFile(@NotNull File file, Integer messageId, String site, String token) throws IOException {
            String serverURL = site + "/api/messages/images/upload";
            OkHttpClient client = new OkHttpClient().newBuilder().build();
            MediaType mediaType = MediaType.parse("text/plain");
            RequestBody body = new MultipartBody.Builder().setType(MultipartBody.FORM)
                    .addFormDataPart("file",file.getName(), RequestBody.create(MediaType.parse("image/jpeg"), file))
                    .addFormDataPart("messageId",messageId.toString())
                    .addFormDataPart("token", token)
                    .build();
            Request request = new Request.Builder()
                    .url(serverURL)
                    .method("POST", body)
                    .build();
            try {
                Response response = client.newCall(request).execute();
            }
            catch (Exception e) {
                Exception exception = e;
                throw e;
            }
    }

    public static void uploadAvatar(File file, String userId, String site) throws IOException {
        String serverURL = site + "/api/users/images/upload";
        OkHttpClient client = new OkHttpClient().newBuilder().build();
        RequestBody body = new MultipartBody.Builder().setType(MultipartBody.FORM)
                .addFormDataPart("file",file.getName(), RequestBody.create(MediaType.parse("image/jpeg"), file))
                .addFormDataPart("userId",userId)
                .build();
        Request request = new Request.Builder()
                .url(serverURL)
                .method("POST", body)
                .build();
        try {
            Response response = client.newCall(request).execute();
        }
        catch (Exception e) {
            throw e;
        }
    }

    private static void onSuccess(String result, int code) {

    }
}
