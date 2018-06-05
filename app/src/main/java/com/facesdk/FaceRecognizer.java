package com.facesdk;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class FaceRecognizer {

    private static final String TAG = "FaceRecognizer";
    private static String mUrl = "http://0.0.0.0:8080/identify";
    private static String mToken = "test";
    private static String mGroup = "test000";

   public interface RecognizerCallback {
       void onRespond(String response);
       void onNetworkFail();
   }

    private static final OkHttpClient sHttpClient = new OkHttpClient();
    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");


    public void setToken(String token) {
        mToken = token;
    }

    public void setUrl(String url) {
        mUrl = url;
    }

    public void setGroup(String group) { }


        public void recognize(String base64Image, RecognizerCallback callback) {
        JSONObject json = new JSONObject();

        try {
            json.put("group_name", mGroup);
            json.put("is_aligned", false);
            json.put("image_base64", base64Image);
        } catch (JSONException e) {
            Log.e(TAG, "recognize: ", e);
            callback.onNetworkFail();
            return;
        }

        RequestBody body = RequestBody.create(JSON, json.toString());
        Request request = new Request.Builder() .url(mUrl) .addHeader("token", mToken) .post(body) .build();

        sHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onNetworkFail();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                ResponseBody body = response.body();
                if (body != null) {
                    callback.onRespond(body.string());
                } else {
                    callback.onNetworkFail();
                }

                response.close();
            }
        });
    }

}
