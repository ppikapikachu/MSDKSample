//package com.example.msdksample.utils;
//
//
//import android.util.Log;
//import com.gosuncn.djidemo.bus.EventRxBus;
//import com.gosuncn.djidemo.bus.event.GyroscopeStatusEvent;
//import com.gosuncn.djidemo.listeners.ComObserver;
//import com.gosuncn.djidemo.module.gimbal.GimbalAttitudeInfo;
//import com.gosuncn.djidemo.module.gps.AircraftGpsInfo;
//import com.gosuncn.djidemo.module.rtk.RtkGpsInfo;
//import java.util.concurrent.atomic.AtomicBoolean;
//import java.util.concurrent.atomic.AtomicReference;
//
//import dji.sdk.keyvalue.value.common.Attitude;
//import dji.sdk.keyvalue.value.common.LocationCoordinate3D;
//import io.reactivex.disposables.CompositeDisposable;
//import io.reactivex.disposables.Disposable;
//
//
//
//public class GlobleContent {
//
//    private final String TAG = "GlobleContent";
//
//
//    private AircraftGpsInfo airGpsInfo;
//    private RtkGpsInfo globalRtkGpsInfo;
////    private GimbalAttitudeInfo globalGimbalAttitudeInfo;
//    private AtomicReference<Attitude> attitude = new AtomicReference<>(new Attitude(0.0,0.0,0.0));
//
//    public static int compensatePitch = 0; // 垂直方向补偿
//    public static int compensateYaw = 0;   // 水平方向补偿
//    public static int debounceThresholdPitch = 0;      // 垂直方向防抖阈值
//    public static int debounceThresholdYaw = 0;        // 水平方向防抖阈值
//    public static float altitudeCompensating = 0;      // 海拔补偿
//
//    private CompositeDisposable mCompositeDisposable;
//    private AtomicBoolean isGyroscopeOpen = new AtomicBoolean(false);  // 方向角模式：陀螺仪或者机身指南针
//
//
//    private static GlobleContent INSTANCE;
//
//    public static GlobleContent getInstance() {
//        if (INSTANCE == null) {
//            INSTANCE = new GlobleContent();
//        }
//        return INSTANCE;
//    }
//
//    private GlobleContent() {
//        mCompositeDisposable = new CompositeDisposable();
//
//        EventRxBus.getInstance().getObservable(GyroscopeStatusEvent.class)
//                .subscribe(new ComObserver<GyroscopeStatusEvent>(){
//
//                    @Override
//                    public void onSubscribe(Disposable d) {
//                        mCompositeDisposable.add(d);
//                    }
//
//                    @Override
//                    public void onNext(GyroscopeStatusEvent gyroscopeStatusEvent) {
//                        isGyroscopeOpen.set(gyroscopeStatusEvent.isGyroscopeOpen());
//                        Log.e(TAG,"  isGyroscopeOpen:::---------------------------------------------------"+isGyroscopeOpen.get());
//                    }
//                });
//    }
//
//    public synchronized AircraftGpsInfo getAirGpsInfoBean() {
//        if (airGpsInfo == null) {
//            airGpsInfo = new AircraftGpsInfo();
//        }
//        return airGpsInfo;
//    }
//
//    public synchronized RtkGpsInfo getRtkGpsInfoBean() {
//        if (globalRtkGpsInfo == null) {
//            globalRtkGpsInfo = new RtkGpsInfo();
//        }
//        return globalRtkGpsInfo;
//    }
//
//
//
//
//    public Attitude getAttitude() {
//        return attitude.get();
//    }
//
//    public void setAttitude(Attitude attitude) {
//        this.attitude.set(attitude);
//    }
//
//    public boolean getIsGyroscopeOpen() {
//        return isGyroscopeOpen.get();
//    }
//
//
//    public static void destory(){
//        INSTANCE = null;
//    }
//}
