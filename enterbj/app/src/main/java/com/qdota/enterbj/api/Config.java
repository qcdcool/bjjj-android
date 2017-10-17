package com.qdota.enterbj.api;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 * Created by zhaopan on 2017/10/13.
 * Copyright (c) 2017 zhaopan. All rights reserved.
 */
public class Config {
    private static final String TAG = Config.class.getSimpleName();
    /**
     * 定义常量
     */
    public static final String Host = "https://enterbj.zhongchebaolian.com";
    public static final String Domain = "enterbj.zhongchebaolian.com";
    public static final String AppSource = "bjjj";
    public static final String AppKey = "kkk";
    public static final String DeviceId = "ddd";
    public static final String Platform_iOS = "01";
    public static final String Platform_Android = "02";
    public static final String Platform = Platform_Android;
    public static final String Page_Index = "/enterbj/jsp/enterbj/index.html";
    public static final String Page_EnterCarList = "/enterbj/platform/enterbj/entercarlist";
    public static final String Page_AddCarType = "/enterbj/platform/enterbj/addcartype";
    public static final String Page_ApplyBjMessage = "/enterbj/platform/enterbj/applyBjMessage";
    public static final String Page_LoadOtherDrivers = "/enterbj-img/platform/enterbj/loadotherdrivers";
    public static final String Page_SubmitPaper = "/enterbj-img/platform/enterbj/submitpaper_03";
    public static final String Page_CurTime = "/enterbj/platform/enterbj/curtime_03";
    public static final String Page_ToVehicleType = "/enterbj/platform/enterbj/toVehicleType";

    public static final HashMap<String, String> Headers = new HashMap<String, String>() {
        {
            put("Host", Domain);
            put("Accept", "*/*");
            put("X-Requested-With", "XMLHttpRequest");
            put("Accept-Encoding", "gzip, deflate");
            put("Accept-Language", "zh-cn");
            put("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
            put("Origin", Host);
            put("Connection", "keep-alive");
            put("User-Agent", "Mozilla/5.0 (Linux; Android 4.4.2; E6883 Build/KOT49H) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/30.0.0.0 Mobile Safari/537.36");
        }
    };

    /**
     * Trust every server - dont check for any certificate
     */
    static SSLSocketFactory sSSLSocketFactory;
    static X509TrustManager sX509TrustManager = new X509TrustManager() {

        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
            return new java.security.cert.X509Certificate[] {};
        }

        public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
            Log.i(TAG, "checkClientTrusted");
        }

        public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
            Log.i(TAG, "checkServerTrusted");
        }
    };
    static HostnameVerifier sHostnameVerifier = new HostnameVerifier() {
        @Override
        public boolean verify(String hostname, SSLSession session) {
            return true;
        }
    };
    private static void trustAllHosts() {
        final String TAG = "trustAllHosts";
        // Create a trust manager that does not validate certificate chains
        TrustManager[] trustAllCerts = new TrustManager[] { sX509TrustManager };

        // Install the all-trusting trust manager
        try {
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());

            sSSLSocketFactory = sc.getSocketFactory();

            HttpsURLConnection.setDefaultSSLSocketFactory(new NoSSLv3SocketFactory(sSSLSocketFactory));
            HttpsURLConnection.setDefaultHostnameVerifier(sHostnameVerifier);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    static  {
        trustAllHosts();
    }

    /*
     * 时间戳函数
     */
    public static GregorianCalendar generateTimestampPoint(GregorianCalendar ttt) {
        // 根据当前时间计算时间戳
        GregorianCalendar now = (GregorianCalendar) ttt.clone();
        GregorianCalendar zero = new GregorianCalendar(
                now.get(Calendar.YEAR),
                now.get(Calendar.MONTH),
                now.get(Calendar.DAY_OF_MONTH));
        long duration = now.getTimeInMillis() - zero.getTimeInMillis();
        long t = duration - duration % 3600000;
        now.setTimeInMillis(t + zero.getTimeInMillis());
        return now;
    }
    public static GregorianCalendar generateInbjTime(GregorianCalendar ttt) {
        GregorianCalendar c = (GregorianCalendar) ttt.clone();
        c.add(Calendar.DAY_OF_MONTH, +1);
        return c;
    }
    public static String formatTimestampPoint(GregorianCalendar c) {
        // 格式化字符串
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(c.getTime());
    }
    public static String formatTimestampDate(GregorianCalendar c) {
        // 格式化字符串
        return new SimpleDateFormat("yyyy-MM-dd").format(c.getTime());
    }
    public static String formatApplyDate(GregorianCalendar c) {
        // 格式化字符串
        return new SimpleDateFormat("yyyy-M-dd").format(c.getTime());
    }

    /*
     * 数据保存路径相关函数
     */
    public static String getDataPath(Context context) {
        return context.getExternalFilesDir("data").getPath();
    }
    public static String formatSignPath(Context context, String userid, String date, String platform) {
        String path = "{data}/{userid}/{date}/{platform}/";
        return path.replace("{data}", getDataPath(context))
                .replace("{userid}", userid)
                .replace("{date}", date)
                .replace("{platform}", platform);
    }
    public static String formatCarPath(Context context, String userid, String licenseno) {
        String path = "{data}/{userid}/{licenseno}/";
        return path.replace("{data}", getDataPath(context))
                .replace("{userid}", userid)
                .replace("{licenseno}", licenseno);
    }
    /*
     * Json读取
     */
    public static JsonObject loadJsonFrom(String path, String filename) {
        try {
            JsonElement element = new JsonParser().parse(new InputStreamReader(new FileInputStream(new File(path + filename))));
            return element.getAsJsonObject();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }
    /*
     * Json保存
     */
    public static void saveJsonTo(String path, String filename, JsonObject jsonObject) throws IOException {
        final String json = jsonObject.toString();

        File f = new File(path + filename);
        if (!f.exists()) {
            File fileParentDir = f.getParentFile();
            if (!fileParentDir.exists()) {
                fileParentDir.mkdirs();
            }
            f.createNewFile();
        }
        OutputStream out = new FileOutputStream(f);
        out.write(json.getBytes());
        out.close();
    }
    /*
     * Car.json创建
     */
    public static JsonObject createCarJson(String licenseno,
                                           String engineno,
                                           String cartypecode,
                                           String vehicletype,
                                           String carid,
                                           String carmodel,
                                           String carregtime,
                                           String envGrade) {
        JsonObject jsonCar = new JsonObject();
        jsonCar.addProperty("licenseno", licenseno);
        jsonCar.addProperty("engineno", engineno);
        jsonCar.addProperty("cartypecode", cartypecode);
        jsonCar.addProperty("vehicletype", vehicletype);

        jsonCar.addProperty("carid", carid);
        jsonCar.addProperty("carmodel", carmodel);
        jsonCar.addProperty("carregtime", carregtime);
        jsonCar.addProperty("envGrade", envGrade);
        return jsonCar;
    }
    /*
     * Person.json
     */
    public static JsonObject createPersonJson(String drivingphoto,
                                              String carphoto,
                                              String drivername,
                                              String driverlicenseno,
                                              String driverphoto,
                                              String personphoto) {
        JsonObject jsonPerson = new JsonObject();
        jsonPerson.addProperty("drivingphoto", drivingphoto);
        jsonPerson.addProperty("carphoto", carphoto);

        jsonPerson.addProperty("drivername", drivername);
        jsonPerson.addProperty("driverlicenseno", driverlicenseno);

        jsonPerson.addProperty("driverphoto", driverphoto);
        jsonPerson.addProperty("personphoto", personphoto);
        return jsonPerson;
    }
    /*
     * 配置保存
     */
    private static final String PRE_CONFIG = "config.xml";
    public static void savePreference(Context context,
                                      String userid,
                                      String platform,
                                      String licenseno) {
        final SharedPreferences preferences = context.getSharedPreferences(PRE_CONFIG, 0);
        preferences.edit()
                .putString("userid", userid)
                .putString("platform", platform)
                .putString("licenseno", licenseno)
                .commit();
    }
    /*
     * 配置读取
     */
    public static String readPreference(Context context, String key) {
        final SharedPreferences preferences = context.getSharedPreferences(PRE_CONFIG, 0);
        return preferences.getString(key, "");
    }
}
