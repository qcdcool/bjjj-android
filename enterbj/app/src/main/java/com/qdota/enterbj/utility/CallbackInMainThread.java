package com.qdota.enterbj.utility;

import android.content.Context;
import android.os.Handler;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * Created by zhaopan on 2017/10/13.
 * Copyright (c) 2017 zhaopan. All rights reserved.
 */
public class CallbackInMainThread implements Callback, Runnable {
    private final Handler mHandler;
    private final Callback mCallback;
    public CallbackInMainThread(Context context, Callback callback) {
        mHandler = new Handler(context.getMainLooper());
        mCallback = callback;
    }

    private Call mCall;
    private IOException mIOException;
    @Override
    public void onFailure(Call call, IOException e) {
        mCall = call;
        mIOException = e;
        mResponse = null;
        mHandler.post(this);
    }

    private Response mResponse;
    @Override
    public void onResponse(Call call, Response response) throws IOException {
        mCall = call;
        mIOException = null;
        mResponse = response;
        mHandler.post(this);
    }

    @Override
    public void run() {
        if (mResponse != null) {
            try {
                mCallback.onResponse(mCall, mResponse);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            mCallback.onFailure(mCall, mIOException);
        }
    }

    /*
     * 解析内容
     */
    public static String parseResponseToString(Response response) throws IOException {
        if (response.isSuccessful()) {
            final String content_length = response.headers().get("Content-Length");
            if (content_length != null) {
                long length = Integer.valueOf(content_length);
                ResponseBody body = response.peekBody(length);
                return body.string();
            }
        }
        return null;
    }
    public static JsonObject parseResponseToJson(Response response) throws IOException {
        if (response.isSuccessful()) {
            final String content_type = response.headers().get("Content-Type");
            if (content_type != null && content_type.contains("application/json")) {
                final String res = parseResponseToString(response);
                if (res != null) {
                    JsonElement element = new JsonParser().parse(res);
                    return element.getAsJsonObject();
                }
            }
        }
        return null;
    }
}
