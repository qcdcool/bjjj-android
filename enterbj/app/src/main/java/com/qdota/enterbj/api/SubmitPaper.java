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
 * Created by zhaopan on 2017/10/16.
 * Copyright (c) 2017 zhaopan. All rights reserved.
 */
public class SubmitPaper {
    /**
     * 对应https的请求，异步的
     * @param userId 用户唯一标识
     * @param callback 异步回调
     */
    public static boolean request(final Context context,
                                  String userId,
                                  String platform,
                                  String licenseno,
                                  final Callback callback) {
        OkHttpClient client = new OkHttpClient.Builder()
                .sslSocketFactory(Config.sSSLSocketFactory, Config.sX509TrustManager)
                .hostnameVerifier(Config.sHostnameVerifier)
                .followRedirects(false)
                .build();

        final GregorianCalendar point = Config.generateTimestampPoint(new GregorianCalendar(2017, 9, 11));
        final String date = Config.formatTimestampDate(point);
        final String timestamp = Config.formatTimestampPoint(point);

        // 车辆信息
        final String path = Config.formatCarPath(context, userId, licenseno);
        JsonObject jsonCar = Config.loadJsonFrom(path, "car.json");
        JsonObject jsonPerson = Config.loadJsonFrom(path, "person.json");
        if (jsonCar == null || jsonPerson == null)
            return false;
        // car
        final String engineno = jsonCar.get("engineno").getAsString();
        final String cartypecode = jsonCar.get("cartypecode").getAsString();
        final String vehicletype = jsonCar.get("vehicletype").getAsString();
        final String carid = jsonCar.get("carid").getAsString();
        final String carmodel = jsonCar.get("carmodel").getAsString();
        final String carregtime = jsonCar.get("carregtime").getAsString();
        final String envGrade = jsonCar.get("envGrade").getAsString();
        // person
        final String drivingphoto = jsonPerson.get("drivingphoto").getAsString();
        final String carphoto = jsonPerson.get("carphoto").getAsString();
        final String drivername = jsonPerson.get("drivername").getAsString();
        final String driverlicenseno = jsonPerson.get("driverlicenseno").getAsString();
        final String driverphoto = jsonPerson.get("driverphoto").getAsString();
        final String personphoto = jsonPerson.get("personphoto").getAsString();

        // 其余无用的信息
        final String imei = "";
        final String imsi = "";
        final String gpslon = "";
        final String gpslat = "";
        final String phoneno = "";
        final String code = "";

        // 进京时间选择
        final String inbjentrancecode1 = "16";
        final String inbjentrancecode = "13";
        final String inbjduration = "7";

        // 时间戳为申请日，进京日为申请日+1
        final String inbjtime = Config.formatApplyDate(Config.generateInbjTime(point));

        // var imageId = $("#inbjentrancecode").val()+$("#inbjduration").val()+$("#inbjtime").val()+$("#userid").val()+$("#engineno").val()+$("#cartypecode").val()+$("#driverlicensenow").val()+$("#carid").val()+timestamp;
        final String imageId = inbjentrancecode + inbjduration + inbjtime + userId + engineno + cartypecode + driverlicenseno + carid + timestamp;

        // timestamp.json
        final String pathOfSign = Config.formatSignPath(context, userId, date, platform);
        JsonObject jsonSign = Config.loadJsonFrom(pathOfSign, "timestamp.json");
        if (jsonSign == null || !jsonSign.has(timestamp))
            return false;
        final String sign = jsonSign.get(timestamp).getAsString();

        FormBody formBody = new FormBody.Builder()
                .add("appsource", Config.AppSource)
                .add("hiddentime", timestamp)
                .add("inbjentrancecode1", inbjentrancecode1)
                .add("inbjentrancecode", inbjentrancecode)
                .add("inbjduration", inbjduration)
                .add("inbjtime", inbjtime)
                .add("appkey", "")
                .add("deviceid", "")
                .add("token", "")
                .add("timestamp", timestamp)
                .add("userid", userId)
                .add("licenseno", licenseno)
                .add("engineno", engineno)
                .add("cartypecode", cartypecode)
                .add("vehicletype", vehicletype)
                .add("drivingphoto", drivingphoto)
                .add("carphoto", carphoto)
                .add("drivername", drivername)
                .add("driverlicenseno", driverlicenseno)
                .add("driverphoto", driverphoto)
                .add("personphoto", personphoto)
                .add("gpslon", gpslon)
                .add("gpslat", gpslat)
                .add("phoneno", phoneno)
                .add("imei", imei)
                .add("imsi", imsi)
                .add("carid", carid)
                .add("carmodel", carmodel)
                .add("carregtime", carregtime)
                .add("envGrade", envGrade)
                .add("imageId", imageId)
                .add("code", code)
                .add("sign", sign)
                .add("platform", platform)
                .build();

        Request request = new Request.Builder()
                .url(Config.Host + Config.Page_SubmitPaper)
                .headers(Headers.of(Config.Headers))
                .header("Referer", Config.Host + Config.Page_LoadOtherDrivers)
                .post(formBody)
                .build();
        client.newCall(request).enqueue(new CallbackInMainThread(context, callback));
        return true;
    }
}
