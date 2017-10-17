package com.qdota.enterbj;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonObject;
import com.qdota.enterbj.api.Config;
import com.qdota.enterbj.api.EnterCarList;
import com.qdota.enterbj.api.SubmitPaper;
import com.qdota.enterbj.utility.CallbackInMainThread;
import com.qdota.enterbj.utility.FileUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int REQUEST_CHOOSE_FILE = 0x0001;

    private EditText mEdit_userid;
    private EditText mEdit_platform;
    private EditText mEdit_licenseno;
    private EditText mEdit_engineno;
    private EditText mEdit_cartypecode;
    private EditText mEdit_vehicletype;
    private EditText mEdit_carid;
    private EditText mEdit_carmodel;
    private EditText mEdit_carregtime;
    private EditText mEdit_envGrade;
    private EditText mEdit_drivername;
    private EditText mEdit_driverlicenseno;
    private Button mBtnSaveProfile;
    private Button mBtnChooseFile;
    private Button mBtnEnterCarList;
    private Button mBtnSubmitPaper;
    private TextView mTVCarList;
    private TextView mTVSubmitResponse;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mEdit_userid = (EditText) findViewById(R.id.userid);
        mEdit_platform = (EditText) findViewById(R.id.platform);
        mEdit_licenseno = (EditText) findViewById(R.id.licenseno);
        mEdit_engineno = (EditText) findViewById(R.id.engineno);
        mEdit_cartypecode = (EditText) findViewById(R.id.cartypecode);
        mEdit_vehicletype = (EditText) findViewById(R.id.vehicletype);
        mEdit_carid = (EditText) findViewById(R.id.carid);
        mEdit_carmodel = (EditText) findViewById(R.id.carmodel);
        mEdit_carregtime = (EditText) findViewById(R.id.carregtime);
        mEdit_envGrade = (EditText) findViewById(R.id.envGrade);
        mEdit_drivername = (EditText) findViewById(R.id.drivername);
        mEdit_driverlicenseno = (EditText) findViewById(R.id.driverlicenseno);

        mBtnSaveProfile = (Button) findViewById(R.id.btn_save_profile);
        mBtnSaveProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String userid = mEdit_userid.getText().toString();
                String platform = mEdit_platform.getText().toString();

                String licenseno = mEdit_licenseno.getText().toString();
                String engineno = mEdit_licenseno.getText().toString();
                String cartypecode = mEdit_cartypecode.getText().toString();
                String vehicletype = mEdit_vehicletype.getText().toString();
                String carid = mEdit_carid.getText().toString();
                String carmodel = mEdit_carmodel.getText().toString();
                String carregtime = mEdit_carregtime.getText().toString();
                String envGrade = mEdit_envGrade.getText().toString();

                String drivername = mEdit_drivername.getText().toString();
                String driverlicenseno = mEdit_driverlicenseno.getText().toString();

                // 写到系统配置里，重启本界面时可以直接加载旧数据
                Config.savePreference(MainActivity.this, userid, platform, licenseno);
                // 创建json
                final String path = Config.formatCarPath(MainActivity.this, userid, licenseno);
                try {
                    Config.saveJsonTo(path, "car.json",
                            Config.createCarJson(
                                    licenseno,
                                    engineno,
                                    cartypecode,
                                    vehicletype,
                                    carid,
                                    carmodel,
                                    carregtime,
                                    envGrade));
                    Config.saveJsonTo(path, "person.json",
                            Config.createPersonJson(
                                    "",
                                    "",
                                    drivername,
                                    driverlicenseno,
                                    "",
                                    ""));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        mBtnChooseFile = (Button) findViewById(R.id.btn_choose_file);
        mBtnChooseFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 仍然需要动态请求权限
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    int REQUEST_EXTERNAL_STORAGE = 1;
                    String[] PERMISSIONS_STORAGE = {
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                    };
                    int permission = ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE);

                    if (permission != PackageManager.PERMISSION_GRANTED) {
                        // We don't have permission so prompt the user
                        ActivityCompat.requestPermissions(
                                MainActivity.this,
                                PERMISSIONS_STORAGE,
                                REQUEST_EXTERNAL_STORAGE
                        );
                    }
                }

                Intent act = new Intent(Intent.ACTION_GET_CONTENT);
                act.setType("*/*");
                startActivityForResult(act, REQUEST_CHOOSE_FILE);
                /*new LFilePicker()
                        .withActivity(MainActivity.this)
                        .withRequestCode(REQUEST_CHOOSE_FILE)
                        .withMutilyMode(false)
                        .start();*/
            }
        });

        mBtnEnterCarList = (Button) findViewById(R.id.btn_enter_car_list);
        mBtnEnterCarList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String userid = mEdit_userid.getText().toString();
                String platform = mEdit_platform.getText().toString();
                if (TextUtils.isEmpty(userid) || TextUtils.isEmpty(platform)) {
                    userid = "";
                    platform = "";
                }
                final boolean bSuccess = EnterCarList.request(MainActivity.this, userid, platform, new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        if (response.isSuccessful()) {
                            final String res = CallbackInMainThread.parseResponseToString(response);
                            mTVCarList.setText(res);
                            Toast.makeText(MainActivity.this, res, Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(MainActivity.this, response.code() + ": " + response.message(), Toast.LENGTH_LONG).show();
                        }
                    }
                });
                if (!bSuccess) {
                    Toast.makeText(MainActivity.this, R.string.request_error_no_sign, Toast.LENGTH_LONG).show();
                }
            }
        });
        mTVCarList = (TextView) findViewById(R.id.tv_car_list);

        mBtnSubmitPaper = (Button) findViewById(R.id.btn_submit_paper);
        mBtnSubmitPaper.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String userid = mEdit_userid.getText().toString();
                String platform = mEdit_platform.getText().toString();
                if (TextUtils.isEmpty(userid) || TextUtils.isEmpty(platform)) {
                    userid = "";
                    platform = "";
                }
                String licenseno = mEdit_licenseno.getText().toString();
                if (TextUtils.isEmpty(licenseno)) {
                    licenseno = "";
                }
                final boolean bSuccess = SubmitPaper.request(MainActivity.this, userid, platform, licenseno, new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        if (response.isSuccessful()) {
                            final String res = CallbackInMainThread.parseResponseToString(response);
                            mTVSubmitResponse.setText(res);
                            Toast.makeText(MainActivity.this, res, Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(MainActivity.this, response.code() + ": " + response.message(), Toast.LENGTH_LONG).show();
                        }
                    }
                });
                if (!bSuccess) {
                    Toast.makeText(MainActivity.this, R.string.request_error_no_sign, Toast.LENGTH_LONG).show();
                }
            }
        });
        mTVSubmitResponse = (TextView) findViewById(R.id.tv_submit_response);

        // 加载配置
        final String userid = Config.readPreference(this, "userid");
        final String platform = Config.readPreference(this, "platform");
        final String licenseno = Config.readPreference(this, "licenseno");
        mEdit_userid.setText(userid);
        mEdit_platform.setText(platform);
        mEdit_licenseno.setText(licenseno);
        reloadDataWithUser(userid, licenseno);
    }

    private void reloadDataWithUser(String userid, String licenseno) {
        // 如果car和person存在，则也读取
        if (!TextUtils.isEmpty(userid) && !TextUtils.isEmpty(licenseno)) {
            final String path = Config.formatCarPath(this, userid, licenseno);
            final JsonObject jsonCar = Config.loadJsonFrom(path, "car.json");
            if (jsonCar != null) {
                mEdit_engineno.setText(jsonCar.get("engineno").getAsString());
                mEdit_cartypecode.setText(jsonCar.get("cartypecode").getAsString());
                mEdit_vehicletype.setText(jsonCar.get("vehicletype").getAsString());
                mEdit_carid.setText(jsonCar.get("carid").getAsString());
                mEdit_carmodel.setText(jsonCar.get("carmodel").getAsString());
                mEdit_carregtime.setText(jsonCar.get("carregtime").getAsString());
                mEdit_envGrade.setText(jsonCar.get("envGrade").getAsString());
            }
            final JsonObject jsonPerson = Config.loadJsonFrom(path, "person.json");
            if (jsonPerson != null) {
                mEdit_drivername.setText(jsonPerson.get("drivername").getAsString());
                mEdit_driverlicenseno.setText(jsonPerson.get("driverlicenseno").getAsString());
            }
        }
    }

    private void refreshUI() {
        final String userid = mEdit_userid.getText().toString();
        final String licenseno = mEdit_licenseno.getText().toString();
        reloadDataWithUser(userid, licenseno);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CHOOSE_FILE
                && resultCode == Activity.RESULT_OK
                && data != null) {
            // content://com.android.providers.downloads.documents/document/raw:/storage/emulated/0/Download/xxx.zip
            final Uri uri = data.getData();
            if (uri != null) {
                // 拿到文件路径
                final String path = FileUtils.getSmartFilePath(this, uri);
                // 解压文件到目录
                copyToData(path);
                // 刷新界面数据
                refreshUI();
            }
        }
    }

    private void copyToData(final String path) {
        File dir = getExternalFilesDir(null);
        if (dir != null && dir.exists()) {
            File f = new File(dir, "data");
            if (f.exists() || f.mkdir()) {
                try {
                    ZipFile zf = new ZipFile(new File(path));
                    for (Enumeration<?> entries = zf.entries(); entries.hasMoreElements();) {
                        ZipEntry entry = ((ZipEntry)entries.nextElement());
                        if (entry.isDirectory())
                            continue;
                        InputStream in = zf.getInputStream(entry);
                        File desFile = new File(f, entry.getName());
                        if (!desFile.exists()) {
                            File fileParentDir = desFile.getParentFile();
                            if (!fileParentDir.exists()) {
                                fileParentDir.mkdirs();
                            }
                            desFile.createNewFile();
                        }
                        OutputStream out = new FileOutputStream(desFile);
                        byte buffer[] = new byte[1024 * 16];
                        int realLength;
                        while ((realLength = in.read(buffer)) > 0) {
                            out.write(buffer, 0, realLength);
                        }
                        in.close();
                        out.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
