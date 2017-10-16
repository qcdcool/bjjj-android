package com.qdota.enterbj.api;

import android.content.Context;

import com.google.gson.JsonObject;
import com.qdota.enterbj.utility.CallbackInMainThread;

import java.util.GregorianCalendar;

import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;

/**
 * Created by zhaopan on 2017/10/13.
 * Copyright (c) 2017 zhaopan. All rights reserved.
 */
public class EnterCarList {
    /**
     * 对应https的请求，异步的
     * @param userId 用户唯一标识
     * @param callback 异步回调
     */
    public static boolean request(final Context context, String userId, String platform, final Callback callback) {
        OkHttpClient client = new OkHttpClient.Builder()
                .sslSocketFactory(Config.sSSLSocketFactory, Config.sX509TrustManager)
                .hostnameVerifier(Config.sHostnameVerifier)
                .followRedirects(false)
                .build();

        final GregorianCalendar point = Config.generateTimestampPoint(new GregorianCalendar(2017, 9, 11));
        final String date = Config.formatTimestampDate(point);
        final String timestamp = Config.formatTimestampPoint(point);

        // 从配置文件中取值
        final String path = Config.formatSignPath(context, userId, date, platform);
        JsonObject jsonToken = Config.loadJsonFrom(path, "token.json");
        JsonObject jsonSign = Config.loadJsonFrom(path, "sign.json");
        if (jsonToken == null || jsonSign == null)
            return false;
        if (!jsonToken.has(timestamp) || !jsonSign.has(timestamp))
            return false;
        final String token = jsonToken.get(timestamp).getAsString();
        final String sign = jsonSign.get(timestamp).getAsString();

        FormBody formBody = new FormBody.Builder()
                .add("userid", userId)
                .add("appkey", Config.AppKey)
                .add("deviceid", Config.DeviceId)
                .add("timestamp", timestamp)
                .add("token", token)
                .add("sign", sign)
                .add("platform", platform)
                .add("appsource", "")
                .build();

        Request request = new Request.Builder()
                .url(Config.Host + Config.Page_EnterCarList)
                .headers(Headers.of(Config.Headers))
                .header("Referer", Config.Host + Config.Page_Index)
                .post(formBody)
                .build();
        client.newCall(request).enqueue(new CallbackInMainThread(context, callback));
        return true;
    }
}
