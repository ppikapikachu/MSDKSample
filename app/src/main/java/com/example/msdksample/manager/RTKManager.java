//package com.example.msdksample.manager;
//
//import com.example.msdksample.entity.Movement;
//
//import dji.sdk.keyvalue.key.FlightControllerKey;
//import dji.sdk.keyvalue.key.KeyTools;
//import dji.sdk.keyvalue.value.common.LocationCoordinate3D;
//import dji.v5.manager.KeyManager;
//import dji.v5.manager.aircraft.rtk.RTKCenter;
//import dji.v5.manager.aircraft.rtk.RTKLocationInfo;
//import dji.v5.manager.aircraft.rtk.RTKLocationInfoListener;
//import dji.v5.manager.interfaces.IRTKCenter;
//
//public class RTKManager {
//
//    private static class RTKHolder{
//        private static final RTKManager INSTANCE = new RTKManager();
//    }
//    public static RTKManager getInstance(){
//        return RTKHolder.INSTANCE;
//    }
//
//    private void initRTKInfo(){
//        Boolean isConnectFlight = KeyManager.getInstance().getValue(KeyTools.createKey(FlightControllerKey.KeyConnection));
//        IRTKCenter irtkCenter = new RTKCenter();
//        if (isConnectFlight == true){
//            if (irtkCenter != null){
//                irtkCenter.addRTKLocationInfoListener(new RTKLocationInfoListener() {//添加RTK高精度定位信息的监听器。
//                    @Override
//                    public void onUpdate(RTKLocationInfo newValue) {
//                        LocationCoordinate3D real3DLocation = newValue.getReal3DLocation();
//                        Movement.getInstance().set
//                    }
//                });
//            }
//        }
//    }
//}
