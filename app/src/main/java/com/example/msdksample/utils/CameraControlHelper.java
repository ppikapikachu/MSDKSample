//package com.example.msdksample.utils;
//
//import androidx.annotation.NonNull;
//import com.gosuncn.djidemo.bus.EventRxBus;
//import com.gosuncn.djidemo.bus.event.ComosAndZoomEvent;
//import com.gosuncn.djidemo.view.NewMainActivity;
//import com.gosuncn.glog.ALog;
//import dji.sdk.keyvalue.key.CameraKey;
//import dji.sdk.keyvalue.key.DJIKeyInfo;
//import dji.sdk.keyvalue.key.KeyTools;
//import dji.sdk.keyvalue.value.camera.CameraType;
//import dji.sdk.keyvalue.value.camera.CameraVideoStreamSourceType;
//import dji.sdk.keyvalue.value.camera.ZoomRatiosRange;
//import dji.sdk.keyvalue.value.common.DoublePoint2D;
//import dji.v5.common.callback.CommonCallbacks;
//import dji.v5.common.error.IDJIError;
//import dji.v5.manager.KeyManager;
//
//public class CameraControlHelper {
//
//    private final String TAG = "CameraControlHelper";
//    private int curFocal = 0;
//    private CameraControlListener cameraListener;
//    private boolean isCameraAvaliable = true;
//
//    private CameraVideoStreamSourceType sourceType;
//    private ZoomRatiosRange ratiosRange;
//    private int currentGear = 0;
//    private int currentZoom = -1;
//
//    public CameraControlHelper() {
//        KeyManager.getInstance().listen(KeyTools.createKey(CameraKey.KeyCameraConnectState), this, (oldValue, newValue) -> {
//            isCameraAvaliable = newValue;
//            if(isCameraAvaliable){
//                initCamramParam();
//            }
//        });
//    }
//
//    public void switchCameraMode(CameraVideoStreamSourceType cameraVideoStreamSourceType) {
//        if (!isCameraAvaliable) {
//            cameraListener.onControlFailed("Camera == null, 请先装上摄像机");
//            return;
//        }
//        KeyManager.getInstance().setValue(KeyTools.createKey(CameraKey.KeyCameraVideoStreamSource),cameraVideoStreamSourceType, new CommonCallbacks.CompletionCallback() {
//            @Override
//            public void onSuccess() {
//                getCurrentZoom();
//                sourceType = cameraVideoStreamSourceType;
//
//            }
//
//            @Override
//            public void onFailure(@NonNull IDJIError error) {
//                ALog.w("Gimbal setMode Failed: " + error.description());
//            }
//        });
//        setHDLiveViewEnabled();
//        getHybridZoomSpec();
//    }
//    private void initCamramParam(){
//        getCurrentZoom();
//        KeyManager.getInstance().getValue(KeyTools.createKey(CameraKey.KeyCameraVideoStreamSource), new CommonCallbacks.CompletionCallbackWithParam<CameraVideoStreamSourceType>() {
//            @Override
//            public void onSuccess(CameraVideoStreamSourceType cameraVideoStreamSourceType) {
//                sourceType = cameraVideoStreamSourceType;
//            }
//
//            @Override
//            public void onFailure(@NonNull IDJIError error) {
//
//            }
//        });
//
//        setHDLiveViewEnabled();
//        getHybridZoomSpec();
//
//        CameraType result =  KeyManager.getInstance().getValue(KeyTools.createKey(CameraKey.KeyCameraType));
//        if(result != null){
//            ALog.e(TAG,"===============================key::"+result.toString());
//        }else{
//            ALog.e(TAG,"=============================== CameraKey is null::");
//        }
//    }
//
//    public void setCameraListener(CameraControlListener listener) {
//        this.cameraListener = listener;
//    }
//
//
//    public void startToZoom(float zoomFactor,boolean isIn){
//        if(isIn){
//            startDigitalZoom(1);
//        }else{
//            startDigitalZoom(0);
//        }
//    }
//
//    /**
//     * 开启数字缩放
//     * @param zoomFactor
//     */
//    public void startDigitalZoom(int zoomFactor) {
//        if (!isCameraAvaliable) {
//            cameraListener.onControlFailed("Camera == null, 请先装上摄像机");
//            return;
//        }
//        if(ratiosRange == null || sourceType == null){
//            cameraListener.onControlFailed("相机参数获取失败!!!");
//            return;
//        }
//        int geaTemp = currentGear;
//        if(zoomFactor > 0){
//            geaTemp ++;
//        }else{
//            geaTemp --;
//        }
//
//        if(geaTemp < 0 || geaTemp >= ratiosRange.getGears().length){
//            return;
//        }
//
//        startRealZoom(ratiosRange.getGears()[geaTemp],geaTemp);
//
//
////        if(maxHybridFocalLength == null){
////            cameraListener.onControlFailed("相机参数获取失败!!!");
////            return;
////        }
////        final int zoomTemp = currentZoom;
////        if(zoomFactor > 0){
////            currentZoom = currentZoom + focalLengthStep * 10;
////            if(currentZoom > maxHybridFocalLength){
////                currentZoom = maxHybridFocalLength;
////            }
////        }else{
////            currentZoom = currentZoom - focalLengthStep  * 10;
////            if(currentZoom < minHybridFocalLength){
////                currentZoom = minHybridFocalLength;
////            }
////        }
//
////        KeyManager.getInstance().setValue(KeyTools.createKey(CameraKey.KeyCameraZoomFocalLength), currentZoom, new CommonCallbacks.CompletionCallback() {
////            @Override
////            public void onSuccess() {
////                cameraListener.onControlSuccess();
////            }
////
////            @Override
////            public void onFailure(@NonNull IDJIError error) {
////                currentZoom = zoomTemp;
////                cameraListener.onControlFailed("Camera setHybridZoomFocalLength --- " + currentZoom + " ,Failed: " + error.description());
////            }
////        });
//    }
//
//    /**
//     * 用于搜索的缩放
//     * @param zoomFactor
//     */
//    public void startSearchZoom(int zoomFactor) {
//        if (!isCameraAvaliable) {
//            cameraListener.onControlFailed("Camera == null, 请先装上摄像机");
//            return;
//        }
//
//        if(ratiosRange == null || ratiosRange.getGears().length == 0){
//            return;
//        }
//
//        int[] gears = ratiosRange.getGears();
//        int lastGear = gears[0];
//        int gearIndex = 0;
//        int fistFindIndex = 1;
//        int centerGear = gears[gears.length / 2];
//        if(zoomFactor > centerGear){
//            gearIndex = gears.length / 2;
//            lastGear = centerGear;
//            fistFindIndex = gearIndex + 1;
//        }
//        if(zoomFactor < lastGear || fistFindIndex > gears.length){
//            zoomFactor = lastGear;
//        }else {
//            for(int i = fistFindIndex;i<gears.length;i++){
//                if(zoomFactor > gears[i]){
//                    lastGear = gears[i];
//                }else if((zoomFactor - lastGear) < (gears[i] - zoomFactor)){
//                    zoomFactor = lastGear;
//                    gearIndex = i - 1;
//                }else{
//                    zoomFactor = gears[i];
//                    gearIndex = i;
//                }
//            }
//        }
//        startRealZoom(zoomFactor,gearIndex);
//
////        if(maxHybridFocalLength == null){
////            cameraListener.onControlFailed("相机参数获取失败!!!");
////            return;
////        }
////        final int zoomTemp = currentZoom;
////        int realZoomFocal = zoomFactor - curFocal;
////        currentZoom = currentZoom + focalLengthStep  * realZoomFocal;
////        ALog.w("当前进行缩放的操作倍数是::::::::::::::::::::::::::::::::::::::::::::::::::::" + realZoomFocal+"  curFocal"+curFocal+"  currentZoom::"+currentZoom);
////        if(currentZoom > maxHybridFocalLength){
////            currentZoom = maxHybridFocalLength;
////        }else if(currentZoom < minHybridFocalLength){
////            currentZoom = minHybridFocalLength;
////        }
////
////        KeyManager.getInstance().setValue(KeyTools.createKey(CameraKey.KeyCameraHybridZoomFocalLength), currentZoom, new CommonCallbacks.CompletionCallback() {
////            @Override
////            public void onSuccess() {
////                cameraListener.onControlSuccess();
////            }
////
////            @Override
////            public void onFailure(@NonNull IDJIError error) {
////                currentZoom = zoomTemp;
////                cameraListener.onControlFailed("Camera setHybridZoomFocalLength --- " + currentZoom + " ,Failed: " + error.description());
////            }
////        });
//    }
//
//    private void startRealZoom(double zoomParam,final int newGearIndex){
//        DJIKeyInfo keyInfo = null;
//        if(sourceType == CameraVideoStreamSourceType.ZOOM_CAMERA){
//            keyInfo = CameraKey.KeyCameraZoomRatios;
//        }else if(sourceType == CameraVideoStreamSourceType.INFRARED_CAMERA){
//            keyInfo = CameraKey.KeyThermalZoomRatios;
//        }
//
//        if(keyInfo != null){
//            KeyManager.getInstance().setValue(KeyTools.createKey(keyInfo), zoomParam, new CommonCallbacks.CompletionCallback() {
//                @Override
//                public void onSuccess() {
//                    currentGear = newGearIndex;
//                }
//                @Override
//                public void onFailure(@NonNull IDJIError error) {
//                    cameraListener.onControlFailed("Camera setHybridZoomFocalLength --- " + currentGear + " ,Failed: " + error.description());
//                }
//            });
//        }
//    }
//
//    private void getHybridZoomSpec(){
//
////        KeyManager.getInstance().listen(KeyTools.createKey(CameraKey.KeyCameraHybridZoomSpec), this, (oldValue, hybridZoomSpec) -> {
////            focalLengthStep = hybridZoomSpec.getFocalLengthStep();
////            maxHybridFocalLength = hybridZoomSpec.getMaxFocalLength();
////            minHybridFocalLength = hybridZoomSpec.getMinFocalLength();
////            ALog.w("maxHybridFocalLength：" + maxHybridFocalLength
////                    + ",minHybridFocalLength：" + minHybridFocalLength
////                    + ",focalLengthStep：" + focalLengthStep);
////        });
//
//        KeyManager.getInstance().getValue(KeyTools.createKey(CameraKey.KeyCameraZoomRatiosRange), new CommonCallbacks.CompletionCallbackWithParam<ZoomRatiosRange>() {
//            @Override
//            public void onSuccess(ZoomRatiosRange zoomRatiosRange) {
//                ratiosRange = zoomRatiosRange;
//            }
//
//            @Override
//            public void onFailure(@NonNull IDJIError error) {
//                ALog.w("获取缩放范围失败");
//            }
//        });
//
//    }
//
//    private void getCurrentZoom(){
//        KeyManager.getInstance().listen(KeyTools.createKey(CameraKey.KeyCameraZoomFocalLength), this, (oldValue, newValue) -> {
//            currentZoom = newValue;
//            if(ratiosRange != null){
//                curFocal = ratiosRange.getGears()[currentGear];
//            }
//            ALog.i(TAG,"current:::::::::::::::::::::::::::::::::" + currentZoom+"  缩放倍数::"+curFocal);
//            EventRxBus.getInstance().post(new ComosAndZoomEvent(currentZoom,curFocal));
////            if(113<=currentZoom && currentZoom<=405){
////                EventRxBus.getInstance().post(new ComosAndZoomEvent(currentZoom,curFocal));
////                ALog.i(TAG,"Zoom onSuccess:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::" + currentZoom+"  缩放倍数::"+curFocal);
////            }
//
//        });
////        KeyManager.getInstance().listen(KeyTools.createKey(CameraKey.KeyCameraHybridZoomFocalLength), this, (oldValue, newValue) -> {
////            currentZoom = newValue;
////            updataCurZoom();
////        });
//    }
//
////    private void updataCurZoom(){
////        if(minHybridFocalLength != null){
////            curFocal = (currentZoom - minHybridFocalLength)/focalLengthStep;
////        }
////        ALog.i(TAG,"Zoom onSuccess:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::" + currentZoom+"  缩放倍数::"+curFocal+"  最大倍数::"+maxHybridFocalLength);
////        EventRxBus.getInstance().post(new ComosAndZoomEvent(currentZoom,curFocal));
////    }
//
//
//    /**
//     * 对焦
//     * @param target    目标坐标，左上坐标为原点（0，0）.x和y轴区间都为[0.0, 1.0]
//     */
//    public void focusTarget(DoublePoint2D target) {
//        if (!isCameraAvaliable) {
//            ALog.w("Camera == null, 请先装上摄像机");
//            cameraListener.onControlFailed("Camera == null, 请先装上摄像机");
//            return;
//        }
//
//        KeyManager.getInstance().setValue(KeyTools.createKey(CameraKey.KeyCameraFocusTarget), target, new CommonCallbacks.CompletionCallback() {
//            @Override
//            public void onSuccess() {
//                cameraListener.onControlSuccess();
//            }
//
//            @Override
//            public void onFailure(@NonNull IDJIError error) {
//                cameraListener.onControlFailed("Camera SetFocusTarget Failed: " + error.description());
//            }
//        });
//    }
//
//
//    private void setHDLiveViewEnabled(){
//        if (!isCameraAvaliable) {
//            cameraListener.onControlFailed("Camera == null, 请先装上摄像机");
//            return;
//        }
//       KeyManager.getInstance().setValue(KeyTools.createKey(CameraKey.KeyH1HDLiveViewEnabled), true, new CommonCallbacks.CompletionCallback() {
//           @Override
//           public void onSuccess() {
//
//           }
//
//           @Override
//           public void onFailure(@NonNull IDJIError error) {
//               ALog.w( "setHDLiveViewEnabled--- Error: " +  error.description());
//           }
//       });
//    }
//
//
//
//    public interface CameraControlListener {
//
//        void onControlSuccess();
//
//        void onControlFailed(String errMsg);
//    }
//
//    public void onDestory(){
//        this.cameraListener = null;
//        KeyManager.getInstance().cancelListen(KeyTools.createKey(CameraKey.KeyCameraHybridZoomFocalLength));
//        KeyManager.getInstance().cancelListen(KeyTools.createKey(CameraKey.KeyCameraConnectState));
//    }
//
//}
