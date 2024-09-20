//package com.example.msdksample.utils;
//
//import androidx.annotation.NonNull;
//import androidx.annotation.Nullable;
//
//import com.gosuncn.djidemo.common.GlobleContent;
//import com.gosuncn.djidemo.utils.DJIErrorCallbackHandler;
//import com.gosuncn.djidemo.utils.ModuleVerificationUtil;
//import com.gosuncn.glog.ALog;
//import java.util.Timer;
//import java.util.TimerTask;
//import java.util.concurrent.atomic.AtomicReference;
//
//import dji.sdk.keyvalue.key.DJIKey;
//import dji.sdk.keyvalue.key.GimbalKey;
//import dji.sdk.keyvalue.key.KeyTools;
//import dji.sdk.keyvalue.key.RemoteControllerKey;
//import dji.sdk.keyvalue.value.common.ComponentIndexType;
//import dji.sdk.keyvalue.value.common.EmptyMsg;
//import dji.sdk.keyvalue.value.gimbal.CtrlInfo;
//import dji.sdk.keyvalue.value.gimbal.GimbalAngleRotation;
//import dji.sdk.keyvalue.value.gimbal.GimbalAngleRotationMode;
//import dji.sdk.keyvalue.value.gimbal.GimbalAttitudeRange;
//import dji.sdk.keyvalue.value.gimbal.GimbalMode;
//import dji.sdk.keyvalue.value.gimbal.GimbalResetType;
//import dji.sdk.keyvalue.value.gimbal.GimbalSpeedRotation;
//import dji.v5.common.callback.CommonCallbacks;
//import dji.v5.common.error.IDJIError;
//import dji.v5.manager.KeyManager;
//
//
///**
// * TODO 优化：可以将AircraftGimbalControl的功能整合到这里
// */
//public class GimbalControlHelper {
//
//    private DJIKey.ActionKey<GimbalAngleRotation, EmptyMsg> actionKey;
//    private Timer timer;
//    private float maxPitch = 0,minPitch = 0;
//    private double firstYaw = -300f;
//    private AtomicReference<Double> currentRelativeHeading = new AtomicReference<>(0.0);
//    private GimbalRotateTimerTask rotateTimerTask;
//    private ComponentIndexType componentIndexType;
//    private GimbalDataListener gimbalDataListener;
//
//
//    public GimbalControlHelper(GimbalDataListener gimbalDataListener) {
//        this.gimbalDataListener = gimbalDataListener;
//
//        KeyManager.getInstance().listen(KeyTools.createKey(GimbalKey.KeyConnection, ComponentIndexType.LEFT_OR_MAIN), this, (oldValue, newValue) -> {
//            ALog.w("  ComponentIndexType.LEFT_OR_MAIN::::::::::::::"+newValue);
//            if(newValue){
//                componentIndexType = ComponentIndexType.LEFT_OR_MAIN;
//                if(gimbalDataListener != null){
//                    gimbalDataListener.upadateGimbalIndex(componentIndexType);
//                }
//                initGimbal();
//            }
//        });
//
//
//        KeyManager.getInstance().listen(KeyTools.createKey(GimbalKey.KeyConnection, ComponentIndexType.RIGHT), this, (oldValue, newValue) -> {
//            ALog.w("  ComponentIndexType.RIGHT::::::::::::::"+newValue);
//            if(newValue){
//                componentIndexType = ComponentIndexType.RIGHT;
//                if(gimbalDataListener != null){
//                    gimbalDataListener.upadateGimbalIndex(componentIndexType);
//                }
//                initGimbal();
//            }
//        });
//
//        KeyManager.getInstance().listen(KeyTools.createKey(GimbalKey.KeyConnection, ComponentIndexType.UP), this, (oldValue, newValue) -> {
//            ALog.w("  ComponentIndexType.UP::::::::::::::"+newValue);
//            if(newValue){
//                componentIndexType = ComponentIndexType.UP;
//                if(gimbalDataListener != null){
//                    gimbalDataListener.upadateGimbalIndex(componentIndexType);
//                }
//                initGimbal();
//            }
//        });
//    }
//
//
//    private void initGimbal(){
//        if(componentIndexType == null){
//            return;
//        }
//
//
//        KeyManager.getInstance().setValue(KeyTools.createKey(RemoteControllerKey.KeyControllingGimbal), componentIndexType, new CommonCallbacks.CompletionCallback() {
//            @Override
//            public void onSuccess() {
//                ALog.w("KeyControllingGimbal set success!!!!!!!!!!!!!: ");
//            }
//
//            @Override
//            public void onFailure(@NonNull IDJIError error) {
//                ALog.w("KeyControllingGimbal set Failed: " + error.errorCode()+"  type::"+error.errorType());
//            }
//        });
//
//
//        // 设置无人机的云台跟踪模式
//        KeyManager.getInstance().setValue(KeyTools.createKey(GimbalKey.KeyGimbalMode,componentIndexType), GimbalMode.FPV, new CommonCallbacks.CompletionCallback() {
//            @Override
//            public void onSuccess() {
//
//            }
//
//            @Override
//            public void onFailure(@NonNull IDJIError error) {
//                ALog.w("Gimbal setMode Failed: " + error.description());
//            }
//        });
//
//
//        KeyManager.getInstance().getValue(KeyTools.createKey(GimbalKey.KeyGimbalAttitudeRange,componentIndexType), new CommonCallbacks.CompletionCallbackWithParam<GimbalAttitudeRange>() {
//            @Override
//            public void onSuccess(GimbalAttitudeRange gimbalAttitudeRange) {
////                maxPitch = gimbalAttitudeRange.pitch.max.floatValue();
////                minPitch = gimbalAttitudeRange.pitch.min.floatValue();
//                maxPitch = gimbalAttitudeRange.getPitch().getMax().floatValue();
//                minPitch = gimbalAttitudeRange.getPitch().getMin().floatValue();
//            }
//
//            @Override
//            public void onFailure(@NonNull IDJIError error) {
//
//            }
//        });
//        KeyManager.getInstance().listen(KeyTools.createKey(RemoteControllerKey.KeyCustomButton1Down), this, (oldValue, newValue) -> {
//            ALog.d("KeyCustomButton1Down................newValue：："+newValue);
//            if(newValue){
//                resetGimbal();
//            }
//        });
//
//
////        if(!ModuleVerificationUtil.isM300Product()){
////            initRemoteControler();
////        }
//
//        KeyManager.getInstance().listen(KeyTools.createKey(GimbalKey.KeyGimbalAttitude,componentIndexType), this, (oldValue, newValue) -> {
//            final double newPitch = newValue.getPitch();
////            final double newYaw = newValue.getYaw();
//            newValue.setPitch(newPitch + GlobleContent.compensatePitch);
//            ALog.d("机身的夹角是::::::::::::::::::::::::"+currentRelativeHeading.get());
//            if(GlobleContent.getInstance().getIsGyroscopeOpen()){
////                newValue.setYaw(newYaw + GlobleContent.compensateYaw);
//                newValue.setYaw(currentRelativeHeading.get() + GlobleContent.compensateYaw);
//
//            }else{
////                if(ModuleVerificationUtil.isM300Product()){
////                    if(firstYaw == -300f){
////                        firstYaw = newYaw;
////                    }
////                    newValue.setYaw(newYaw - firstYaw);
////                }else{
////                    newValue.setYaw(currentRelativeHeading.get());
////                }
//
//                newValue.setYaw(currentRelativeHeading.get());
//            }
//
//            gimbalDataListener.onGimbalAttitudeReceive(newValue);
//        });
//
//        KeyManager.getInstance().listen(KeyTools.createKey(GimbalKey.KeyYawRelativeToAircraftHeading,componentIndexType), this, (oldValue, newValue) -> currentRelativeHeading.set(newValue));
//    }
//
//
//
//    public void moveGimbal(GimbalAngleRotationMode rotationMode, double pitch, double roll, double yaw,Boolean pitchIgnored,Boolean rollIgnored,Boolean yawIgnored) {
//        if(rotationMode == GimbalAngleRotationMode.ABSOLUTE_ANGLE){
//            ALog.w("以绝对角度模式移动云台::::::::::::::::::::::::::::::::::::::::::::::::");
//            if(maxPitch != 0 && pitch > maxPitch){
//                pitch = maxPitch;
//            }else if(minPitch != 0 && pitch < minPitch){
//                pitch = minPitch;
//            }
//        }
//        GimbalAngleRotation rotation = new GimbalAngleRotation();
//        rotation.setMode(rotationMode);
//        rotation.setPitch(pitch);
//        rotation.setRoll(roll);
//        rotation.setYaw(yaw);
//        rotation.setPitchIgnored(pitchIgnored);
//        rotation.setYawIgnored(yawIgnored);
//        rotation.setRollIgnored(rollIgnored);
//
//        sendGimbalRotateCommand(rotation);
//    }
//
//    // time是执行时间
//    public void moveGimbalWithTime(GimbalAngleRotationMode rotationMode, double pitch, double yaw, double roll, double duration) {
//        ALog.w("开始移动云台::::::::::::::::::::::::::::::::::::::::::::::::"+pitch);
//        GimbalAngleRotation rotation = new GimbalAngleRotation();
//        rotation.setMode(rotationMode);
//        rotation.setPitch(pitch);
//        rotation.setRoll(roll);
//        rotation.setYaw(yaw);
//        rotation.setPitchIgnored(false);
//        rotation.setYawIgnored(false);
//        rotation.setRollIgnored(false);
//        rotation.setDuration(duration);
//        sendGimbalRotateCommand(rotation);
//    }
//
//    private void initRemoteControler(){
////        GimbalComponentType componentType = KeyManager.getInstance().getValue(KeyTools.createKey(GimbalKey.KeyGimbalComponentType));
//
//        KeyManager.getInstance().listen(KeyTools.createKey(RemoteControllerKey.KeyCustomButton1Down), this, (oldValue, newValue) -> {
//            if(newValue){
//                resetGimbal();
//            }
//        });
//
//        KeyManager.getInstance().listen(KeyTools.createKey(RemoteControllerKey.KeyCustomButton2Down), this, (oldValue, newValue) -> {
//            if(newValue){
//                moveGimbalWithSpeed(0.0,35.0,0.0);
//            }
//        });
//    }
//
//    private void sendGimbalRotateCommand(GimbalAngleRotation rotation) {
//        if(componentIndexType == null){
//            return;
//        }
//        if(actionKey == null){
//            actionKey = KeyTools.createKey(GimbalKey.KeyRotateByAngle,componentIndexType);
//        }
//
//        KeyManager.getInstance().performAction(actionKey, rotation, new CommonCallbacks.CompletionCallbackWithParam<EmptyMsg>() {
//            @Override
//            public void onSuccess(EmptyMsg emptyMsg) {
//
//            }
//
//            @Override
//            public void onFailure(@NonNull IDJIError error) {
//                ALog.w("Gimbal rotate Failed: " + error.errorCode()+ error.description());
//            }
//        });
//
////        KeyManager.getInstance().setValue(actionKey, rotation, new CommonCallbacks.CompletionCallback() {
////            @Override
////            public void onSuccess() {
////
////            }
////
////            @Override
////            public void onFailure(@NonNull IDJIError error) {
////                ALog.w("Gimbal rotate Failed: " + error.errorCode()+ error.description());
////            }
////        });
//
//    }
//
//    // Timer与TimerTask的启动与暂停（频繁操作云台控制会引起Timer和TimerTask的创建以及回收而导致频繁GC）
//    public void startGimbalRotate(float pitch, float yaw) {
//        if(componentIndexType == null){
//            return;
//        }
//        if (timer == null) {
//            timer = new Timer();
//            if (rotateTimerTask == null) {
//                rotateTimerTask = new GimbalRotateTimerTask();
//            }
//        }
//        rotateTimerTask.setPitchValue(pitch);
//        rotateTimerTask.setYawValue(yaw);
//        timer.schedule(rotateTimerTask, 0, 100);
//    }
//
//    public void stopGimbalRotate() {
//        if (timer != null) {
//            if (rotateTimerTask != null) {
//                rotateTimerTask.cancel();
//            }
//            timer.purge();
//            timer.cancel();
//            rotateTimerTask = null;
//            timer = null;
//        }
//        moveGimbalWithSpeed(0, 0, 0);
//    }
//
//
//
//    private class GimbalRotateTimerTask extends TimerTask {
//        double pitchValue;
//        double yawValue;
//
//        GimbalRotateTimerTask() {
//            super();
//        }
//
//        void setPitchValue(float pitchValue) {
//            this.pitchValue = pitchValue;
//        }
//
//        void setYawValue(float yawValue) {
//            this.yawValue = yawValue;
//        }
//
//        @Override
//        public void run() {
//            moveGimbalWithSpeed(pitchValue,yawValue,0);
//        }
//    }
//
//    public void moveGimbalWithSpeed(double pitch,double yaw,double roll){
//        GimbalSpeedRotation rotation = new GimbalSpeedRotation(pitch,yaw,roll,new CtrlInfo(true,true));
//
//        KeyManager.getInstance().performAction(KeyTools.createKey(GimbalKey.KeyRotateBySpeed, componentIndexType), rotation, new CommonCallbacks.CompletionCallbackWithParam<EmptyMsg>() {
//            @Override
//            public void onSuccess(EmptyMsg emptyMsg) {
//                ALog.w("Gimbal rotation success::::::::::::::::: ");
//            }
//
//            @Override
//            public void onFailure(@NonNull IDJIError error) {
//                ALog.w("Gimbal rotation Failed: " + error.description());
//            }
//        });
////        KeyManager.getInstance().setValue(KeyTools.createKey(GimbalKey.KeyRotateBySpeed, componentIndexType), rotation, new CommonCallbacks.CompletionCallback() {
////            @Override
////            public void onSuccess() {
////
////            }
////
////            @Override
////            public void onFailure(@NonNull IDJIError error) {
////                ALog.w("Gimbal rotation Failed: " + error.description());
////            }
////        });
//    }
//
//
//    public void resetGimbal() {
//        if(componentIndexType == null){
//            return;
//        }
//        KeyManager.getInstance().performAction(KeyTools.createKey(GimbalKey.KeyGimbalReset, componentIndexType), GimbalResetType.PITCH_UP_OR_DOWN_WITH_YAW_CENTER, new CommonCallbacks.CompletionCallbackWithParam<EmptyMsg>() {
//            @Override
//            public void onSuccess(EmptyMsg emptyMsg) {
//                ALog.w("Gimbal reset success:::::::::::::::: ");
//            }
//
//            @Override
//            public void onFailure(@NonNull IDJIError error) {
//                ALog.w("Gimbal reset Failed: " + error.hint());
//            }
//        });
//    }
//
//    public void calibrationGimbal(){
//        if(componentIndexType == null){
//            return;
//        }
//        KeyManager.getInstance().setValue(KeyTools.createKey(GimbalKey.KeyGimbalCalibrate, componentIndexType), new EmptyMsg(), new CommonCallbacks.CompletionCallback() {
//            @Override
//            public void onSuccess() {
//
//            }
//
//            @Override
//            public void onFailure(@NonNull IDJIError error) {
//                ALog.w("Gimbal Calibration Failed: " + error.description());
//            }
//        });
//
//    }
//
//
//    public void onDestory(){
//        stopGimbalRotate();
//        KeyManager.getInstance().cancelListen(KeyTools.createKey(RemoteControllerKey.KeyCustomButton1Down));
//        KeyManager.getInstance().cancelListen(KeyTools.createKey(RemoteControllerKey.KeyCustomButton2Down));
//        KeyManager.getInstance().cancelListen(KeyTools.createKey(GimbalKey.KeyGimbalAttitude));
//
//        KeyManager.getInstance().cancelListen(KeyTools.createKey(GimbalKey.KeyConnection, ComponentIndexType.UP));
//        KeyManager.getInstance().cancelListen(KeyTools.createKey(GimbalKey.KeyConnection, ComponentIndexType.RIGHT));
//        KeyManager.getInstance().cancelListen(KeyTools.createKey(GimbalKey.KeyConnection, ComponentIndexType.LEFT_OR_MAIN));
//    }
//
//}
