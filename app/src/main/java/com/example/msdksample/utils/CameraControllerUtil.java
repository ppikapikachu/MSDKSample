package com.example.msdksample.utils;

import android.util.Log;

import com.example.msdksample.entity.Movement;
import com.gosuncn.lib28181agent.GS28181SDKManager;
import com.gosuncn.lib28181agent.Types;

import dji.v5.manager.datacenter.camera.StreamInfo;
import dji.v5.manager.interfaces.ICameraStreamManager;

public class CameraControllerUtil {

    private static final String TAG = "CameraControllerUtil";
    private GS28181SDKManager manager = GS28181SDKManager.getInstance();

    public static double searchZoom(int methodSource, int zoomType, double ratios){
        double zoomFactor = 0.8;
        if (methodSource == 1){//1，来自加减变焦
            if (ratios < 3.0){
                zoomFactor = 0.2;
            }else {
                zoomFactor = 0.8;
            }
        }
//        获取最接近的倍率下标
        int numberThree = getNumberThree(Movement.getInstance().getGears(), ratios);
        int len = Movement.getInstance().getGears().length;
        if (Movement.getInstance().isContinuous()) {//倍率连续
//            放缩不能超过倍率边界
            if (zoomType == Types.ZOOM_IN_CTRL)
                ratios = ratios + zoomFactor > Movement.getInstance().getGears()[len - 1] ? Movement.getInstance().getGears()[len - 1] : ratios + zoomFactor;//倍率递增
            else if (zoomType == Types.ZOOM_OUT_CTRL)
                ratios = ratios - zoomFactor < Movement.getInstance().getGears()[0] ? Movement.getInstance().getGears()[0] : ratios - zoomFactor;//倍率递减
        } else {//倍率不连续
            if (zoomType == Types.ZOOM_IN_CTRL) {//放大
//                如果最接近的下标是最大下标
                if (numberThree == len - 1) {
                    ratios = Movement.getInstance().getGears()[numberThree];
                } else if (Movement.getInstance().getGears()[numberThree] < ratios) {//如果最接近的下标值比当前要小，就用下一个值
                    ratios = Movement.getInstance().getGears()[numberThree + 1];
                } else if (Movement.getInstance().getGears()[numberThree] == ratios) {//如果当前倍率更好等于最接近的数
                    ratios = Movement.getInstance().getGears()[numberThree + 1];
                } else {//其他情况则正常用最接近的值
                    ratios = Movement.getInstance().getGears()[numberThree];
                }
            } else if (zoomType == Types.ZOOM_OUT_CTRL) {
                //   如果最接近的下标是最小下标
                if (numberThree == 0) {
                    ratios = Movement.getInstance().getGears()[numberThree];
                } else if (Movement.getInstance().getGears()[numberThree] > ratios) {//如果最接近的下标值比当前要大，就用上一个值
                    ratios = Movement.getInstance().getGears()[numberThree - 1];
                } else if (Movement.getInstance().getGears()[numberThree] == ratios) {//如果当前倍率更好等于最接近的数
                    ratios = Movement.getInstance().getGears()[numberThree - 1];
                } else {//其他情况则正常用最接近的值
                    ratios = Movement.getInstance().getGears()[numberThree];
                }
            }
        }
        Log.i(TAG,"需要放缩至倍率为：==="+ratios);
        return ratios;
    }
    //    返回最接近的值的下标
    public static int getNumberThree(int[] intarray, double number) {
        double temp = Math.abs(number - intarray[0]);
        int result = intarray[0];
        int j = 0;
        for (int i = 0 ;i<intarray.length;i++){
            double abs = Math.abs(number - intarray[i]);
            if (abs <= temp) {
                temp = abs;
                result = intarray[i];
                j = i;
            }
        }
        Log.i(TAG, "当前倍率为：" + number + "最接近的数为：" + result + "下标为：" + j);
        return j;
    }

    public int sendVideoStreamFun(byte[] data, StreamInfo info){
        int re;
        if (info.getMimeType() == ICameraStreamManager.MimeType.H264) {
             re = manager.sendVideoStream(System.currentTimeMillis(), info.isKeyFrame() ? 1 : FileUtil.getFrameType(data), data);
            Log.i(TAG, "发送视频流sendVideoStream:" + re);
        } else {
             re = manager.sendVideoStreamH265(System.currentTimeMillis(), info.isKeyFrame() ? 1 : FileUtil.getFrameType(data), data);
            Log.i(TAG, "发送视频流sendVideoStreamH265:" + re);
        }
        return re;
    }
    public int sendVideoWithARInfoFun(float preFramePitch, float preFrameYaw, byte[] data, StreamInfo info){
        int re;
        if (info.getMimeType() == ICameraStreamManager.MimeType.H264) {
             re = manager.sendVideoWithARInfo(System.currentTimeMillis(), info.isKeyFrame() ? 1 : FileUtil.getFrameType(data), data,
                    Float.parseFloat(Movement.getInstance().getGimbalRoll()),
                    preFramePitch ,
                    preFrameYaw ,
                    Float.parseFloat(Movement.getInstance().getCurrentLongitude()),
                    Float.parseFloat(Movement.getInstance().getCurrentLatitude()),
                    Movement.getInstance().getCurrentAltitude());
//                        Log.i(TAG,"经度纬度高度111111"+Float.parseFloat(Movement.getInstance().getGimbalRoll())+" : "+
//                                Float.parseFloat(Movement.getInstance().getGimbalPitch())+" : "+
//                                Float.parseFloat(Movement.getInstance().getGimbalYaw())+" : "+
//                                Float.parseFloat(Movement.getInstance().getCurrentLongitude())+" : "+
//                                Float.parseFloat(Movement.getInstance().getCurrentLatitude())+" : "+
//                                Movement.getInstance().getAltitude());
            Log.i(TAG, "发送完整的帧 + 摄像机姿态信息（ AR 信息）sendVideoWithARInfo:" + re);
        } else {
             re = manager.sendVideoWithARInfoH265(System.currentTimeMillis(), info.isKeyFrame() ? 1 : FileUtil.getFrameType(data), data,
                    Float.parseFloat(Movement.getInstance().getGimbalRoll()),
                    preFramePitch,
                    preFrameYaw,
                    Float.parseFloat(Movement.getInstance().getCurrentLongitude()),
                    Float.parseFloat(Movement.getInstance().getCurrentLatitude()),
                    Movement.getInstance().getCurrentAltitude());
            Log.i(TAG, "发送完整的帧 + 摄像机姿态信息（ AR 信息）sendVideoWithARInfoH265:" + re);
        }
        return re;
    }
    public int sendVideoWithARInfoXFun(float preFramePitch, float preFrameYaw, byte[] data, StreamInfo info){
        int re;
        if (info.getMimeType() == ICameraStreamManager.MimeType.H264) {
             re = manager.sendVideoWithARInfoX(System.currentTimeMillis(), info.isKeyFrame() ? 1 : FileUtil.getFrameType(data), data,
                    Float.parseFloat(String.valueOf(Movement.getInstance().getCameraZoomRatios())),//double转float
                    preFramePitch, preFrameYaw,
                    Movement.getInstance().getAngleH(), Movement.getInstance().getAngleV(),
                    Float.parseFloat(Movement.getInstance().getCurrentLongitude()),
                    Float.parseFloat(Movement.getInstance().getCurrentLatitude()),
                    Movement.getInstance().getCurrentAltitude());
            Log.i(TAG, "发送 视频流与 AR信息流 上传sendVideoWithARInfoX:" + re);
        } else {
             re = manager.sendVideoWithARInfoXH265(System.currentTimeMillis(), info.isKeyFrame() ? 1 : FileUtil.getFrameType(data), data,
                    Float.parseFloat(String.valueOf(Movement.getInstance().getCameraZoomRatios())),//double转float
                    preFramePitch, preFrameYaw,
                    Movement.getInstance().getAngleH(), Movement.getInstance().getAngleV(),
                    Float.parseFloat(Movement.getInstance().getCurrentLongitude()),
                    Float.parseFloat(Movement.getInstance().getCurrentLatitude()),
                    Movement.getInstance().getCurrentAltitude());
            Log.i(TAG, "发送 视频流与 AR信息流 上传sendVideoWithARInfoXH265:" + re);
        }
        return re;
    }
}
