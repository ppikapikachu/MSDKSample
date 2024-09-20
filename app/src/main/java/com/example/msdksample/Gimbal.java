package com.example.msdksample;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.msdksample.entity.Movement;

import java.security.Key;

import dji.sdk.keyvalue.key.CameraKey;
import dji.sdk.keyvalue.key.GimbalKey;
import dji.sdk.keyvalue.key.KeyTools;
import dji.sdk.keyvalue.msdkkeyinfo.KeyCameraMode;
import dji.sdk.keyvalue.value.base.DJIValue;
import dji.sdk.keyvalue.value.camera.CameraMode;
import dji.sdk.keyvalue.value.camera.CameraModeMsg;
import dji.sdk.keyvalue.value.common.Attitude;
import dji.sdk.keyvalue.value.common.DoubleMinMax;
import dji.sdk.keyvalue.value.common.EmptyMsg;
import dji.sdk.keyvalue.value.gimbal.GimbalAngleRotation;
import dji.sdk.keyvalue.value.gimbal.GimbalAttitudeRange;
import dji.v5.common.callback.CommonCallbacks;
import dji.v5.common.error.IDJIError;
import dji.v5.common.utils.GeoidManager;
import dji.v5.manager.KeyManager;


public class Gimbal extends AppCompatActivity implements View.OnClickListener {

    ImageButton shang = null, xia = null, zuo = null, you = null;
    Double pitch = 0.0;
    Double yaw = 0.0;
    Button shootPhone = null, record = null;
    boolean cameraConnection = false;
    TextView cameraMode = null;
    private String TAG = "Gimbal";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gimbal);


        init();
    }

    public void init() {
        shang = findViewById(R.id.shang);
        xia = findViewById(R.id.xia);
        zuo = findViewById(R.id.zuo);
        you = findViewById(R.id.you);
        shootPhone = findViewById(R.id.shootphone);
        record = findViewById(R.id.record);
        cameraMode = findViewById(R.id.cameraMode);
        shang.setOnClickListener(this);
        xia.setOnClickListener(this);
        zuo.setOnClickListener(this);
        you.setOnClickListener(this);
        shootPhone.setOnClickListener(this);
        record.setOnClickListener(this);

//        KeyManager.getInstance().listen(KeyTools.createKey(GimbalKey.KeyGimbalAttitude), this, new CommonCallbacks.KeyListener<Attitude>() {
//            @Override
//            public void onValueChange(@Nullable Attitude attitude, @Nullable Attitude t1) {
//                if (t1!=null){
////                    Log.i("云台",t1.toString());
//                    //转动
//                    pitch = t1.getPitch();
//                    yaw = t1.getYaw();
//                }
//
//            }
//        });

        pitch = Double.parseDouble(Movement.getInstance().getGimbalPitch());
        yaw = Double.parseDouble(Movement.getInstance().getGimbalYaw());

        KeyManager.getInstance().listen(KeyTools.createKey(CameraKey.KeyConnection), this, new CommonCallbacks.KeyListener<Boolean>() {
            @Override
            public void onValueChange(@Nullable Boolean aBoolean, @Nullable Boolean t1) {
                if (t1 != null) {
                    cameraConnection = t1;
                }

            }
        });
    }

    //设置俯仰和横滚
    private GimbalAngleRotation setYawRoll(double pi, double yaw) {
        pitch += pi;
        yaw += yaw;
//        Log.i("云台","云台角度"+pitch+   +roll);
        GimbalAngleRotation gimbalAngleRotation = new GimbalAngleRotation();
        gimbalAngleRotation.setPitch(pitch);
        gimbalAngleRotation.setYaw(yaw);
        return gimbalAngleRotation;
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.shang:
                KeyManager.getInstance().performAction(KeyTools.createKey(GimbalKey.KeyRotateByAngle), setYawRoll(10, 0), new CommonCallbacks.CompletionCallbackWithParam<EmptyMsg>() {
                    @Override
                    public void onSuccess(EmptyMsg emptyMsg) {
                        Log.i(TAG, "云台转动成功");
                    }

                    @Override
                    public void onFailure(@NonNull IDJIError idjiError) {
                        Log.i(TAG, "云台转动失败" + idjiError);
                    }
                });
                break;
            case R.id.xia:
                KeyManager.getInstance().performAction(KeyTools.createKey(GimbalKey.KeyRotateByAngle), setYawRoll(-10, 0), new CommonCallbacks.CompletionCallbackWithParam<EmptyMsg>() {
                    @Override
                    public void onSuccess(EmptyMsg emptyMsg) {
                        Log.i(TAG, "云台转动成功");
                    }

                    @Override
                    public void onFailure(@NonNull IDJIError idjiError) {
                        Log.i(TAG, "云台转动失败" + idjiError);
                    }
                });
                break;
            case R.id.zuo:
                KeyManager.getInstance().performAction(KeyTools.createKey(GimbalKey.KeyRotateByAngle), setYawRoll(0, -10), new CommonCallbacks.CompletionCallbackWithParam<EmptyMsg>() {
                    @Override
                    public void onSuccess(EmptyMsg emptyMsg) {
                        Log.i(TAG, "云台转动成功");
                    }

                    @Override
                    public void onFailure(@NonNull IDJIError idjiError) {
                        Log.i(TAG, "云台转动失败" + idjiError);
                    }
                });
                break;
            case R.id.you:
                KeyManager.getInstance().performAction(KeyTools.createKey(GimbalKey.KeyRotateByAngle), setYawRoll(0, 10), new CommonCallbacks.CompletionCallbackWithParam<EmptyMsg>() {
                    @Override
                    public void onSuccess(EmptyMsg emptyMsg) {
                        Log.i(TAG, "云台转动成功");
                    }

                    @Override
                    public void onFailure(@NonNull IDJIError idjiError) {
                        Log.i(TAG, "云台转动失败" + idjiError);
                    }
                });
                break;
            case R.id.shootphone:
                if (!cameraConnection) {
                    Log.i(TAG, "相机未连接");
                    Toast.makeText(this, "相机未连接", Toast.LENGTH_SHORT).show();
                    return;
                }
//                KeyManager.getInstance().performAction(KeyTools.createKey(CameraKey.KeyCameraMode),CameraMode.);
                KeyManager.getInstance().setValue(KeyTools.createKey(CameraKey.KeyCameraMode), CameraMode.PHOTO_NORMAL, new CommonCallbacks.CompletionCallback() {
                    @Override
                    public void onSuccess() {
                        Log.i(TAG, "设置为拍照模式");
                        cameraMode.setText("拍照模式");
                    }

                    @Override
                    public void onFailure(@NonNull IDJIError idjiError) {
                        Log.i(TAG, "设置拍照模式失败" + idjiError);
                    }
                });
                KeyManager.getInstance().performAction(KeyTools.createKey(CameraKey.KeyStartShootPhoto), new CommonCallbacks.CompletionCallbackWithParam<EmptyMsg>() {
                    @Override
                    public void onSuccess(EmptyMsg emptyMsg) {
                        Log.i(TAG, "拍照成功");
                    }

                    @Override
                    public void onFailure(@NonNull IDJIError idjiError) {
                        Log.i(TAG, "拍照失败" + idjiError);
                    }
                });
                break;

            case R.id.record:
                if (!cameraConnection) {
                    Log.i(TAG, "相机未连接");
                    Toast.makeText(this, "相机未连接", Toast.LENGTH_SHORT).show();
                    return;
                }
                KeyManager.getInstance().setValue(KeyTools.createKey(CameraKey.KeyCameraMode), CameraMode.VIDEO_NORMAL, new CommonCallbacks.CompletionCallback() {
                    @Override
                    public void onSuccess() {
                        Log.i(TAG, "设置为录像模式");
                        cameraMode.setText("录像模式");
                    }

                    @Override
                    public void onFailure(@NonNull IDJIError idjiError) {
                        Log.i(TAG, "设置录像模式失败" + idjiError);
                    }
                });

                KeyManager.getInstance().performAction(KeyTools.createKey(CameraKey.KeyStartRecord), new CommonCallbacks.CompletionCallbackWithParam<EmptyMsg>() {
                    @Override
                    public void onSuccess(EmptyMsg emptyMsg) {
                        Log.i(TAG, "录像成功");
                    }

                    @Override
                    public void onFailure(@NonNull IDJIError idjiError) {
                        Log.i(TAG, "录像失败" + idjiError);
                    }
                });
                break;
        }
    }
}