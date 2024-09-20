package com.example.msdksample;


import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import com.example.msdksample.entity.Movement;
import com.example.msdksample.manager.CameraManager;
import com.example.msdksample.manager.FlightManager;
import com.example.msdksample.utils.FileLogger;
import com.example.msdksample.utils.FileUtil;
import com.google.gson.Gson;

import java.io.File;

import dji.sdk.keyvalue.key.DJIKey;
import dji.sdk.keyvalue.key.FlightControllerKey;
import dji.sdk.keyvalue.key.KeyTools;
import dji.sdk.keyvalue.value.common.EmptyMsg;
import dji.v5.common.callback.CommonCallbacks;
import dji.v5.common.error.IDJIError;
import dji.v5.common.utils.GeoidManager;
import dji.v5.manager.KeyManager;
import dji.v5.manager.aircraft.waypoint3.WaypointMissionManager;
import dji.v5.utils.common.FileUtils;
import dji.v5.ux.core.communication.DefaultGlobalPreferences;
import dji.v5.ux.core.communication.GlobalPreferencesManager;
import dji.v5.ux.core.util.UxSharedPreferencesUtil;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Button bu = null, uploadKMZ = null, startMission = null, pauseMission = null, continueMission = null,
            takeOff = null, goHome = null, stopGoHome = null, gimbal = null;
    private TextView location3D = null;
    private String curMissionPath = null;
    private String TAG = "MainActivity";

    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE};

    Handler handler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        UxSharedPreferencesUtil.initialize(this);
        GlobalPreferencesManager.initialize(new DefaultGlobalPreferences(this));
        GeoidManager.getInstance().init(this);

        verifyStoragePermissions(MainActivity.this);

//        如果飞控一直没连接就一直等待
        isOk();

    }

    public void isOk() {
        //        监听是否连接飞控，设置到Movement中的isFlightController属性中
        Boolean value = KeyManager.getInstance().getValue(DJIKey.create(FlightControllerKey.KeyConnection));
        Log.i(TAG, value + "isOk的状态");
        Movement.getInstance().setFlightController(value == null ? false : value);
        Log.i(TAG, "等待1一次");
        if (!Movement.getInstance().isFlightController()) {
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    isOk();
                }
            }, 1000);
        } else {
            init();
//            飞行器数据监听
            FlightManager.getInstance().initFlightInfo();
//            云台相机数据监听
            CameraManager.getInstance().initCameraInfo();
        }


    }

    //    手动申请权限
    public static void verifyStoragePermissions(Activity activity) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(activity, PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE);
        }
    }

    public void init() {
        Movement.getInstance();
        bu = findViewById(R.id.default_layout_button);
        uploadKMZ = findViewById(R.id.uploadKMZ);
        startMission = findViewById(R.id.startMission);
        pauseMission = findViewById(R.id.pauseMission);
        continueMission = findViewById(R.id.continuMission);
        takeOff = findViewById(R.id.takeOff);
        location3D = findViewById(R.id.location);
        goHome = findViewById(R.id.goHome);
        stopGoHome = findViewById(R.id.stopGoHome);
        gimbal = findViewById(R.id.gimbal);

        bu.setOnClickListener(this);
        uploadKMZ.setOnClickListener(this);
        startMission.setOnClickListener(this);
        pauseMission.setOnClickListener(this);
        continueMission.setOnClickListener(this);
        goHome.setOnClickListener(this);
        takeOff.setOnClickListener(this);
        stopGoHome.setOnClickListener(this);
        gimbal.setOnClickListener(this);
//        static <T> DJIKey<T> createKey(DJIKeyInfo<T> mKeyInfo) 传入某个Key接口的DJIKeyInfo实例的方式创建Key实例。
//                  此方法适用于跟云台负载和相机无关的Key的实例创建。

//        获取移动设备国际编码
//        getIMEI(this);

    }

    public static void getIMEI(Context context) {
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        if (telephonyManager != null) {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
                Movement.getInstance().setDevieGBCode(telephonyManager.getDeviceId());
                return ; // Deprecated in API level 26

            }
        }
        return ;
    }

    @Override
    public void onClick(View view) {
        Intent intent = null;
        switch (view.getId()) {
            case R.id.default_layout_button:
                intent = new Intent(this, MyLayoutActivity.class);
                startActivity(intent);
                break;
            case R.id.uploadKMZ:
                // 获取SD卡的根目录
                // 在xml/filepaths.xml文件中药从
                File file = Environment.getExternalStorageDirectory();
//                File file = new File("/storage/emulated");
                Uri uri;
                intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {// 前面代表的是当前操作系统的版本号，后面是给定的版本号，Ctrl鼠标放置显示版本号
                    intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    //记得修改com.xxx.fileprovider与androidmanifest相同
                    // 获取的是应用唯一区分的id即applicationId
                    uri = FileProvider.getUriForFile(MainActivity.this, MainActivity.this.getPackageName() + ".fileprovider", file);
                    intent.setDataAndType(uri, "*/*");// 打开apk文件
                } else {
                    uri = Uri.parse("file://" + file.toString());
                }

//                intent.setDataAndType(Uri.fromFile(file), "*/*");
                startActivityForResult(Intent.createChooser(intent, "需要选择文件"), 1);

                break;
            case R.id.takeOff:
                KeyManager.getInstance().performAction(KeyTools.createKey(FlightControllerKey.KeyStartTakeoff), new CommonCallbacks.CompletionCallbackWithParam<EmptyMsg>() {
                    @Override
                    public void onSuccess(EmptyMsg emptyMsg) {
                        Log.i(TAG, "起飞成功");
                    }

                    @Override
                    public void onFailure(@NonNull IDJIError idjiError) {
                        Log.i(TAG, "起飞失败" + "  " + idjiError.toString());
                    }
                });
                break;
            case R.id.startMission:
                if (curMissionPath==null){
                    Toast.makeText(this, "没有上传航线", Toast.LENGTH_SHORT).show();
                    return;
                }
                WaypointMissionManager.getInstance().startMission(FileUtils.getFileName(curMissionPath, ".kmz"), new CommonCallbacks.CompletionCallback() {
                    @Override
                    public void onSuccess() {
                        Log.i(TAG, "航线执行");
                        Toast.makeText(MainActivity.this, "执行航线任务" + curMissionPath, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(@NonNull IDJIError idjiError) {
                        Log.i(TAG, "航线执行失败" + idjiError.toString());
                        Toast.makeText(MainActivity.this, "航线执行失败" + curMissionPath, Toast.LENGTH_SHORT).show();

                    }
                });
//                        不注释会报抽象方法错误WaylineExecutingInfoListener
//                        WaypointMissionManager.getInstance().addWaylineExecutingInfoListener(new WaylineExecutingInfoListener() {
//                            @Override
//                            public void onWaylineExecutingInfoUpdate(WaylineExecutingInfo excutingWaylineInfo) {
//                                System.out.println("航线状态"+excutingWaylineInfo);
//                            }
//                        });

                break;
            case R.id.pauseMission:
                WaypointMissionManager.getInstance().pauseMission(new CommonCallbacks.CompletionCallback() {
                    @Override
                    public void onSuccess() {
                        Log.i(TAG, "暂停成功");
                        Toast.makeText(MainActivity.this, "航线暂停" + curMissionPath, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(@NonNull IDJIError idjiError) {
                        Log.i(TAG, "暂停失败" + idjiError);
                        Toast.makeText(MainActivity.this, "航线暂停失败" + curMissionPath, Toast.LENGTH_SHORT).show();
                    }
                });
                break;
            case R.id.continuMission:
                WaypointMissionManager.getInstance().resumeMission(new CommonCallbacks.CompletionCallback() {
                    @Override
                    public void onSuccess() {
                        Log.i(TAG, "继续航线");
                        Toast.makeText(MainActivity.this, "继续航线" + curMissionPath, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(@NonNull IDJIError idjiError) {
                        Log.i(TAG, "继续航线失败" + idjiError.toString());
                        Toast.makeText(MainActivity.this, "继续航线失败" + curMissionPath, Toast.LENGTH_SHORT).show();
                    }
                });
                break;
            case R.id.stopMission:
                WaypointMissionManager.getInstance().stopMission(FileUtils.getFileName(curMissionPath, ".kmz"), new CommonCallbacks.CompletionCallback() {
                    @Override
                    public void onSuccess() {
                        Log.i(TAG, "停止航线成功");
                    }

                    @Override
                    public void onFailure(@NonNull IDJIError idjiError) {
                        Log.i(TAG, "停止航线失败" + idjiError);
                    }
                });
                break;
            case R.id.goHome:
                KeyManager.getInstance().performAction(KeyTools.createKey(FlightControllerKey.KeyStartGoHome), new CommonCallbacks.CompletionCallbackWithParam<EmptyMsg>() {
                    @Override
                    public void onSuccess(EmptyMsg emptyMsg) {
                        Log.i(TAG, "返航成功");
                    }

                    @Override
                    public void onFailure(@NonNull IDJIError idjiError) {
                        Log.i(TAG, "返航失败" + "   " + idjiError.toString());
                    }
                });
//                KeyManager.getInstance().listen(KeyTools.createKey(FlightControllerKey.KeyGoHomeStatus), this, new CommonCallbacks.KeyListener<GoHomeState>() {
//                    @Override
//                    public void onValueChange(@Nullable GoHomeState goHomeState, @Nullable GoHomeState t1) {
//                        System.out.println(goHomeState+""+ t1.value());
//                    }
//                });
                break;
            case R.id.stopGoHome:
                KeyManager.getInstance().performAction(KeyTools.createKey(FlightControllerKey.KeyStopGoHome), new CommonCallbacks.CompletionCallbackWithParam<EmptyMsg>() {
                    @Override
                    public void onSuccess(EmptyMsg emptyMsg) {
                        Log.i(TAG, "取消返航成功");
                    }

                    @Override
                    public void onFailure(@NonNull IDJIError idjiError) {
                        Log.i(TAG, "取消返航失败" + "   " + idjiError.toString());
                    }
                });
                break;
            case R.id.gimbal:
                intent = new Intent(this, Gimbal.class);
                startActivity(intent);

                break;
        }
    }


    //    上传kmz
    public void upload(String missionPath) {
        WaypointMissionManager.getInstance().pushKMZFileToAircraft(missionPath, new CommonCallbacks.CompletionCallbackWithProgress<Double>() {
            @Override
            public void onProgressUpdate(Double aDouble) {
                if (aDouble != null) {
                    Log.i(TAG, aDouble + "%---航线上传进度");
                }
            }

            @Override
            public void onSuccess() {
                Log.i(TAG, "航线上传成功");

            }

            @Override
            public void onFailure(@NonNull IDJIError idjiError) {
                Log.i(TAG, "航线上传失败:" + new Gson().toJson(idjiError));
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK) {
            Uri uri = data.getData();
            curMissionPath = FileUtil.getPath(MainActivity.this, uri);
            if (!TextUtils.isEmpty(curMissionPath)) {
                int start = curMissionPath.lastIndexOf(".");
                String format = "";
                if (start < 0) {
                    format = curMissionPath;
                } else {
                    format = curMissionPath.substring(start + 1);
                }
                if (format.equals("kmz"))
                    upload(curMissionPath);
                else{
//                    不是kmz文件
                    curMissionPath = null;
                    Toast.makeText(this, "需要kmz文件", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //           将所有的Callbacks和Messages全部清除掉,移除Handler中所有的消息和回调，避免内存泄露
        handler.removeCallbacksAndMessages(null);
        handler = null;
    }
}