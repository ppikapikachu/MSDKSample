package com.example.msdksample.manager;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.msdksample.entity.Movement;
import com.gosuncn.lib28181agent.GS28181SDKManager;
import com.gosuncn.lib28181agent.bean.AngleEvent;

import java.util.List;

import dji.sdk.keyvalue.key.CameraKey;
import dji.sdk.keyvalue.key.GimbalKey;
import dji.sdk.keyvalue.key.KeyTools;
import dji.sdk.keyvalue.value.camera.CameraVideoStreamSourceType;
import dji.sdk.keyvalue.value.camera.VideoResolutionFrameRate;
import dji.sdk.keyvalue.value.camera.ZoomRatiosRange;
import dji.sdk.keyvalue.value.common.Attitude;
import dji.sdk.keyvalue.value.common.CameraLensType;
import dji.sdk.keyvalue.value.common.ComponentIndexType;
import dji.sdk.keyvalue.value.gimbal.GimbalAttitudeRange;
import dji.v5.common.callback.CommonCallbacks;
import dji.v5.common.error.IDJIError;
import dji.v5.manager.KeyManager;

public class CameraManager {
    private static boolean isStart = false;
    public static boolean getStart(){
        return isStart;
    }

    private String TAG = "CameraManager";
    private CameraManager() {
    }

    private static class CameraHolder {
        private static final CameraManager INSTANCE = new CameraManager();
    }
    public static CameraManager getInstance() {
        isStart = true;
        return CameraHolder.INSTANCE;
    }
    public void initCameraInfo() {
        //        监听云台的姿态数据
        KeyManager.getInstance().listen(KeyTools.createKey(GimbalKey.KeyGimbalAttitude), this, new CommonCallbacks.KeyListener<Attitude>() {
            @Override
            public void onValueChange(@Nullable Attitude attitude, @Nullable Attitude t1) {
                if (t1 != null) {
                    Log.i(TAG,t1.getPitch()+":"+t1.getYaw());
                    Movement.getInstance().setGimbalRoll(t1.getRoll().toString());
                    Movement.getInstance().setGimbalPitch(t1.getPitch().toString());
                    Movement.getInstance().setGimbalYaw(t1.getYaw().toString());
                }
            }
        });
//        获取云台可变动的范围
        KeyManager.getInstance().listen(KeyTools.createKey(GimbalKey.KeyGimbalAttitudeRange), this, new CommonCallbacks.KeyListener<GimbalAttitudeRange>() {
            @Override
            public void onValueChange(@Nullable GimbalAttitudeRange gimbalAttitudeRange, @Nullable GimbalAttitudeRange t1) {
                if (t1 != null){
                    Movement.getInstance().setGimbalPitchRange(t1.getPitch());
                    Movement.getInstance().setGimbalYawRange(t1.getYaw());
                }
            }
        });

        //        监听焦距前设置相机视频源
        KeyManager.getInstance().setValue(KeyTools.createKey(CameraKey.KeyCameraVideoStreamSource), CameraVideoStreamSourceType.ZOOM_CAMERA, new CommonCallbacks.CompletionCallback() {
            @Override
            public void onSuccess() {
                Log.i(TAG, "监听焦距前设置相机视频源--成功");
            }

            @Override
            public void onFailure(@NonNull IDJIError idjiError) {
                Log.i(TAG, "监听焦距前设置相机视频源--失败：" + idjiError);
            }
        });
        KeyManager.getInstance().setValue(KeyTools.createCameraKey(CameraKey.KeyCameraZoomRatios,
                        ComponentIndexType.LEFT_OR_MAIN, CameraLensType.CAMERA_LENS_ZOOM), 1.0,
                new CommonCallbacks.CompletionCallback() {
                    @Override
                    public void onSuccess() {
                        Log.i(TAG, "初始设置变焦倍率为1");
                    }

                    @Override
                    public void onFailure(@NonNull IDJIError idjiError) {
                        Log.i(TAG, "初始设置变焦倍率为1失败" + idjiError);
                    }
                });
//        监听相机焦距，计算变焦倍率
        KeyManager.getInstance().listen(KeyTools.createCameraKey(CameraKey.KeyCameraZoomFocalLength,
                ComponentIndexType.LEFT_OR_MAIN, CameraLensType.CAMERA_LENS_ZOOM), this, new CommonCallbacks.KeyListener<Integer>() {
            @Override
            public void onValueChange(@Nullable Integer integer, @Nullable Integer t1) {
                if (t1 != null) {
                    Log.i(TAG,"当前焦距为："+t1);
                    //测试视场角
                    AngleEvent angleEvent = GS28181SDKManager.getInstance().countCmos(6.4f, 4.8f, t1);//t1为焦距
                    if (angleEvent != null) {
                        Movement.getInstance().setAngleH(angleEvent.getAngleH());
                        Movement.getInstance().setAngleV(angleEvent.getAngleV());
                    }
//                    赋值镜头变焦倍数，最小焦距应该是24
                    Movement.getInstance().setCameraZoomRatios(t1/24);
                    Movement.getInstance().setFocalLenght(t1);
                }
            }
        });
//        获取相机变焦是否连续和关键档位
        KeyManager.getInstance().listen(KeyTools.createCameraKey(CameraKey.KeyCameraZoomRatiosRange,
                ComponentIndexType.LEFT_OR_MAIN, CameraLensType.CAMERA_LENS_ZOOM), this, new CommonCallbacks.KeyListener<ZoomRatiosRange>() {
            @Override
            public void onValueChange(@Nullable ZoomRatiosRange zoomRatiosRange, @Nullable ZoomRatiosRange t1) {
                if (t1 != null){
                    Movement.getInstance().setContinuous(t1.isContinuous());
                    Movement.getInstance().setGears(t1.getGears());
                    for (int i:t1.getGears()){
                        Log.i(TAG,t1.isContinuous()+"倍率为"+i);
                    }
                }
            }
        });
//        获取镜头分辨率范围
        KeyManager.getInstance().getValue(KeyTools.createKey(CameraKey.KeyVideoResolutionFrameRateRange), new CommonCallbacks.CompletionCallbackWithParam<List<VideoResolutionFrameRate>>() {
            @Override
            public void onSuccess(List<VideoResolutionFrameRate> videoResolutionFrameRates) {
                Movement.getInstance().setKeyVideoResolutionFrameRateRange(videoResolutionFrameRates);
            }

            @Override
            public void onFailure(@NonNull IDJIError idjiError) {

            }
        });
    }
}
