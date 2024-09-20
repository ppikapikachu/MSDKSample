package com.example.msdksample.manager;

import android.util.Log;

import androidx.annotation.Nullable;

import com.example.msdksample.entity.Movement;

import dji.sdk.keyvalue.key.FlightControllerKey;
import dji.sdk.keyvalue.key.KeyTools;
import dji.sdk.keyvalue.value.common.Attitude;
import dji.sdk.keyvalue.value.common.LocationCoordinate3D;
import dji.sdk.keyvalue.value.common.Velocity3D;
import dji.v5.common.callback.CommonCallbacks;
import dji.v5.manager.KeyManager;

public class FlightManager {
    private static boolean isStart = false;
    public static boolean getStart(){
        return isStart;
    }

    private String TAG = "FlightManager";
    private FlightManager() {
    }

    private static class FlightHolder {
        private static final FlightManager INSTANCE = new FlightManager();
    }
    public static FlightManager getInstance() {
        isStart = true;
        return FlightManager.FlightHolder.INSTANCE;
    }

    public void initFlightInfo(){
        KeyManager.getInstance().listen(KeyTools.createKey(FlightControllerKey.KeyAircraftLocation3D), this, new CommonCallbacks.KeyListener<LocationCoordinate3D>() {
            @Override
            public void onValueChange(@Nullable LocationCoordinate3D locationCoordinate3D, @Nullable LocationCoordinate3D t1) {
                if (t1 != null) {
                    Log.i("经度纬度高度", t1.getLongitude() + " " + t1.getLatitude() + " " + t1.getAltitude());
                    Movement.getInstance().setCurrentAltitude(t1.getAltitude().floatValue());
                    Movement.getInstance().setCurrentLongitude(t1.getLongitude().toString());
                    Movement.getInstance().setCurrentLatitude(t1.getLatitude().toString());
                }

            }
        });
//            获取飞行姿态俯仰角
        KeyManager.getInstance().listen(KeyTools.createKey(FlightControllerKey.KeyAircraftAttitude), this, new CommonCallbacks.KeyListener<Attitude>() {
            @Override
            public void onValueChange(@Nullable Attitude attitude, @Nullable Attitude t1) {
                if (t1!=null) {
                    Movement.getInstance().setPitch(t1.getPitch().toString());
                    Movement.getInstance().setYaw(t1.getYaw().toString());
                    Movement.getInstance().setRoll(t1.getRoll().toString());
                }
            }
        });
//            获取水平速度，END坐标轴
        KeyManager.getInstance().listen(KeyTools.createKey(FlightControllerKey.KeyAircraftVelocity), this, new CommonCallbacks.KeyListener<Velocity3D>() {
            @Override
            public void onValueChange(@Nullable Velocity3D velocity3D, @Nullable Velocity3D t1) {
                if (t1 != null) {
                    Double x = t1.getX();
                    Double y = t1.getY();
                    Double speed = Math.pow(Math.pow(x, 2) + Math.pow(y, 2), 0.5);
                    Movement.getInstance().setHorizontalSpeed(speed.toString());
                } else {
                    Log.i(TAG, "速度为空");
                }
            }
        });

    }
}
