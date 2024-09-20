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
import dji.sdk.keyvalue.value.camera.CameraHybridZoomSpec;
import dji.sdk.keyvalue.value.camera.CameraVideoStreamSourceType;
import dji.sdk.keyvalue.value.camera.VideoMimeType;
import dji.sdk.keyvalue.value.camera.VideoResolutionFrameRate;
import dji.sdk.keyvalue.value.camera.ZoomRatiosRange;
import dji.sdk.keyvalue.value.common.Attitude;
import dji.sdk.keyvalue.value.common.CameraLensType;
import dji.sdk.keyvalue.value.common.ComponentIndexType;
import dji.sdk.keyvalue.value.gimbal.GimbalAttitudeRange;
import dji.sdk.keyvalue.value.gimbal.GimbalMode;
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
        // 设置无人机的云台跟踪模式
        KeyManager.getInstance().setValue(KeyTools.createKey(GimbalKey.KeyGimbalMode,ComponentIndexType.LEFT_OR_MAIN), GimbalMode.FPV, new CommonCallbacks.CompletionCallback() {
            @Override
            public void onSuccess() {
                Log.i(TAG,"云台设置跟踪模式成功: ");
            }

            @Override
            public void onFailure(@NonNull IDJIError error) {
                Log.i(TAG,"云台设置跟踪模式失败: " + error.description());
            }
        });
        //        监听云台的姿态数据
        KeyManager.getInstance().listen(KeyTools.createKey(GimbalKey.KeyGimbalAttitude), this, new CommonCallbacks.KeyListener<Attitude>() {
            @Override
            public void onValueChange(@Nullable Attitude attitude, @Nullable Attitude t1) {
                if (t1 != null) {
//                    Log.i(TAG,"云台姿态"+t1.getRoll()+" : "+t1.getPitch()+" : "+t1.getYaw());
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

        // TODO: 2024/9/18 不知道对不对
//        监听变焦倍率
        KeyManager.getInstance().listen(KeyTools.createCameraKey(CameraKey.KeyCameraZoomRatios,
                ComponentIndexType.LEFT_OR_MAIN, CameraLensType.CAMERA_LENS_ZOOM), this, new CommonCallbacks.KeyListener<Double>() {
            @Override
            public void onValueChange(@Nullable Double aDouble, @Nullable Double t1) {
                Movement.getInstance().setCameraZoomRatios(t1);
                Log.i(TAG,"当前的变焦倍率是------------============="+t1);
            }
        });

        KeyManager.getInstance().setValue(KeyTools.createCameraKey(CameraKey.KeyCameraZoomRatios,
                        ComponentIndexType.LEFT_OR_MAIN, CameraLensType.CAMERA_LENS_ZOOM), 1.0,
                new CommonCallbacks.CompletionCallback() {
                    @Override
                    public void onSuccess() {
                        Log.i(TAG, "初始设置变焦倍率为1");
                        Integer value = KeyManager.getInstance().getValue(KeyTools.createCameraKey(CameraKey.KeyCameraZoomFocalLength,
                                ComponentIndexType.LEFT_OR_MAIN, CameraLensType.CAMERA_LENS_ZOOM));
//                        Movement.getInstance().setMinFocalLenght(value);
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
                    //视场角
                    AngleEvent angleEvent = GS28181SDKManager.getInstance().countCmos(4.8f, 6.4f, t1);//t1为焦距
                    if (angleEvent != null) {
                        Log.i(TAG,"=====================视场角==========="+angleEvent.getAngleH()+"水平===垂直"+angleEvent.getAngleV());
                        Movement.getInstance().setAngleH(angleEvent.getAngleH());
                        Movement.getInstance().setAngleV(angleEvent.getAngleV());
                    }
//                    赋值镜头变焦倍数，最小焦距资料显示御3T是29.85
//                    Movement.getInstance().setCameraZoomRatios(t1/Movement.getInstance().getMinFocalLenght());
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
                Log.i(TAG,"获取分辨率为："+videoResolutionFrameRates);
                Movement.getInstance().setKeyVideoResolutionFrameRateRange(videoResolutionFrameRates);
            }

            @Override
            public void onFailure(@NonNull IDJIError idjiError) {
                Log.i(TAG,"获取分辨率失败："+idjiError);
            }
        });
//        获取相机编码格式
        KeyManager.getInstance().getValue(KeyTools.createKey(CameraKey.KeyVideoMimeType), new CommonCallbacks.CompletionCallbackWithParam<VideoMimeType>() {
            @Override
            public void onSuccess(VideoMimeType videoMimeType) {
                Log.i(TAG,"获取相机支持的编码格式为："+videoMimeType);
            }

            @Override
            public void onFailure(@NonNull IDJIError idjiError) {
                Log.i(TAG,"获取相机支持的编码格式失败："+idjiError);
            }
        });
//        KeyManager.getInstance().setValue(KeyTools.createKey(CameraKey.KeyVideoMimeType),VideoMimeType.H265,null);
    }
}
