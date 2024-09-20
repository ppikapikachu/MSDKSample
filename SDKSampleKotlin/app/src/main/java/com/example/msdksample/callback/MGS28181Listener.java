package com.example.msdksample.callback;

import static com.gosuncn.lib28181agent.Types.PTZ_DOWN;
import static com.gosuncn.lib28181agent.Types.PTZ_LEFT;
import static com.gosuncn.lib28181agent.Types.PTZ_LEFT_DOWN;
import static com.gosuncn.lib28181agent.Types.PTZ_LEFT_UP;
import static com.gosuncn.lib28181agent.Types.PTZ_RIGHT;
import static com.gosuncn.lib28181agent.Types.PTZ_RIGHT_DOWN;
import static com.gosuncn.lib28181agent.Types.PTZ_RIGHT_UP;
import static com.gosuncn.lib28181agent.Types.PTZ_UP;

import android.os.Looper;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.msdksample.entity.Movement;
import com.google.gson.Gson;
import com.gosuncn.lib28181agent.GS28181SDK;
import com.gosuncn.lib28181agent.GS28181SDKManager;
import com.gosuncn.lib28181agent.Jni28181AgentSDK;
import com.gosuncn.lib28181agent.Types;
import com.gosuncn.lib28181agent.bean.MobilePosSubInfo;

import java.security.Key;
import java.text.SimpleDateFormat;

import dji.sdk.keyvalue.key.CameraKey;
import dji.sdk.keyvalue.key.FlightControllerKey;
import dji.sdk.keyvalue.key.GimbalKey;
import dji.sdk.keyvalue.key.KeyTools;
import dji.sdk.keyvalue.value.camera.CameraFocusMode;
import dji.sdk.keyvalue.value.camera.CameraVideoStreamSourceType;
import dji.sdk.keyvalue.value.camera.TapZoomMode;
import dji.sdk.keyvalue.value.camera.ZoomRatiosRange;
import dji.sdk.keyvalue.value.camera.ZoomTargetPointInfo;
import dji.sdk.keyvalue.value.common.CameraLensType;
import dji.sdk.keyvalue.value.common.ComponentIndexType;
import dji.sdk.keyvalue.value.common.DoublePoint2D;
import dji.sdk.keyvalue.value.common.EmptyMsg;
import dji.sdk.keyvalue.value.common.LocationCoordinate3D;
import dji.sdk.keyvalue.value.gimbal.CtrlInfo;
import dji.sdk.keyvalue.value.gimbal.GimbalAngleRotation;
import dji.sdk.keyvalue.value.gimbal.GimbalResetType;
import dji.sdk.keyvalue.value.gimbal.GimbalSpeedRotation;
import dji.v5.common.callback.CommonCallbacks;
import dji.v5.common.error.IDJIError;
import dji.v5.manager.KeyManager;

import android.os.Handler;
import android.os.Looper;

//处理服务器的操作
public class MGS28181Listener implements GS28181SDKManager.listenerServerControl {

    private String TAG = getClass().getSimpleName();

    /**
     * 设备信息查询回调 onQueryDevInfoAll(long
     * sessionHandle, String deviceGBCode,String deviceName, String
     * devManufacturer,String devModel, String devFirmware, int channel);
     *
     * @param sessionHandle   会话句柄
     * @param deviceGBCode    设备国标编码
     * @param deviceName      设备名称
     * @param devManufacturer 设备生产商
     * @param devModel        设备型号
     * @param devFirmware     设备固件版本
     * @param channel         视频输入通道数
     * @return 错误码
     */
//    应该是调用某个查询方法，回调方法就是这个，数据给我们自己用
    @Override
    public void onQueryDevInfoAll(long sessionHandle, String deviceGBCode, String deviceName, String devManufacturer,
                                  String devModel, String devFirmware, int channel) {

        Jni28181AgentSDK.getInstance().responseDevInfoQuery(sessionHandle, deviceGBCode, deviceName,
                devManufacturer, devModel, devFirmware, channel);
    }

    /**
     * 设备状态查询回调 onQueryDevStatus(long
     * sessionHandle, String deviceGBCode, String dateTime, String
     * errReason, boolean isEncode, boolean isRecord,boolean isOnline,
     * boolean isStatusOK);
     *
     * @param sessionHandle 会话句柄
     *                      * @param deviceGBCode 设备国标编码
     *                      * @param dateTime 设备时间和日期
     *                      * @param errReason 不正常工作原因
     *                      * @param isEncode 是否编码
     *                      * @param isRecord 是否录像
     *                      * @param isOnline 是否在线
     *                      * @param isStatusOK 是否正常工作
     *                      * @return 错误码
     */
    @Override
    public void onQueryDevStatus(long sessionHandle, String deviceGBCode, String dateTime, String errReason,
                                 boolean isEncode, boolean isRecord, boolean isOnline, boolean isStatusOK) {
        Jni28181AgentSDK.getInstance().responseDevStatusQuery(sessionHandle, deviceGBCode, dateTime, errReason, isEncode, isRecord, isOnline, isStatusOK);
    }

    /**
     * Rtp 流回调 onRtpStreamErr(int
     * rtpErrCode)
     *
     * @param rtpErrCode 点流请求
     */
    @Override
    public void onRtpStreamErr(int rtpErrCode) {
        Log.i(TAG, "Rtp流传输返回状态码--" + rtpErrCode);
    }

    /**
     * 透传数据回调 onTransDataReceive(String
     * transData)
     *
     * @param transData 透传数据
     */
    @Override
    public void onTransDataReceive(String transData) {
        Log.i(TAG, "透传数据：" + transData);
    }

    /**
     * 云台 PTZ 控制 onPTZControl(int ctrlType,int
     * ptzType,int speedParam)
     *
     * @param ctrlType   0:停止 1:开始
     * @param ptzType    PTZ 操作类型（参考 Types.PTZCtrlType）
     * @param speedParam 摄像头相关速度参数
     */
    @Override
    public void onPTZControl(int ctrlType, int ptzType, int speedParam) {
        GimbalSpeedRotation speedRotation = new GimbalSpeedRotation();
//        KeyManager.getInstance().setValue(KeyTools.createKey(GimbalKey.KeyPitchControlMaxSpeed),35,null);
        CtrlInfo ctrlInfo = new CtrlInfo();

        float pitch = 0, yaw = 0;
        float up_down = speedParam, right_left = speedParam;
        if (ctrlType == 1) {
            switch (ptzType) {
                case PTZ_UP:
                    pitch = up_down;
                    break;
                case PTZ_DOWN:
                    pitch = -up_down;
                    break;
                case PTZ_RIGHT:
                    yaw = right_left;
                    break;
                case PTZ_LEFT:
                    yaw = -right_left;
                    break;
                case PTZ_LEFT_UP:
                    pitch = up_down;
                    yaw = -right_left;
                    break;
                case PTZ_LEFT_DOWN:
                    pitch = -up_down;
                    yaw = -right_left;
                    break;
                case PTZ_RIGHT_UP:
                    pitch = up_down;
                    yaw = right_left;
                    break;
                case PTZ_RIGHT_DOWN:
                    pitch = -up_down;
                    yaw = right_left;
                    break;
            }
            speedRotation.setPitch((double) pitch);
            speedRotation.setYaw((double) yaw);
            ctrlInfo.setEnableGimbalLock(false);
            speedRotation.setCtrlInfo(ctrlInfo);
            KeyManager.getInstance().performAction(KeyTools.createKey(GimbalKey.KeyRotateBySpeed), speedRotation, null);

        } else {
            ctrlInfo.setEnableGimbalLock(true);
            speedRotation.setCtrlInfo(ctrlInfo);
        }
    }

//    窗口的大小和0与1换算比例，再根据拉框的中心和长宽得到0与1的位置和大小
    //放大就是比前一个拉框更小的拉框，缩小就是比前一个拉框更大的拉框
//    先对焦再变焦

    /**
     * 焦点控制
     *
     * @param zoomType  焦点控制类型
     * @param winLength 播放窗口长度像素值
     * @param winWidth  播放窗口宽度像素值
     * @param lenX      拉框长度像素值
     * @param lenY      拉框宽度像素值 (这个值 imp 没有用)
     * @param midPointX 拉框中心的横轴坐标像素值
     * @param midPointY 拉框中心的纵轴坐标像素值
     */
    @Override
    public void onZoomControl(int zoomType, int winLength, int winWidth,
                              int lenX, int lenY, int midPointX, int midPointY) {
//
//        Log.e(TAG, "onZoomControl:" + "zoomType:" + zoomType + "winLength:" + winLength + "winWidth:" + winWidth
//                + "lenX:" + lenX + "lenY:" + lenY + "midPointX:" + midPointX + "midPointY:" + midPointY
//        );

//        PT值小于零，云台回中
        if (winLength <= 0 && winWidth <= 0) {
            KeyManager.getInstance().setValue(KeyTools.createKey(GimbalKey.KeyGimbalReset), GimbalResetType.PITCH_YAW, null);
            return;
        }

        double x = ((double) midPointX / winLength) * 100;
        double y = ((double) midPointX / winWidth) * 100;
//        ZoomTargetPointInfo zoomTargetPointInfo = new ZoomTargetPointInfo();
//        zoomTargetPointInfo.setX((double) x);
//        zoomTargetPointInfo.setY((double) y);
//        zoomTargetPointInfo.setTapZoomModeEnable(true);
        Log.i(TAG, getHFOV() + "视场角的水平垂直" + getVFOV());

//      计算角度移动云台
        double xx, yy;
        if (x > 50.0) {
            xx = ((x - 50.0) / 50.0) * (getHFOV() / 2);
        } else {
            xx = -((50.0 - x) / 50.0) * (getHFOV() / 2);
        }
        if (y > 50.0) {
            yy = -((y - 50.0) / 50.0) * (getVFOV() / 2);
        } else {
            yy = ((50.0 - y) / 50.0) * (getVFOV() / 2);
        }
        Log.i(TAG, x + ":" + y + "对焦1");
        Log.i(TAG, xx + ":" + yy + "对焦1");
        Log.i(TAG, Movement.getInstance().getGimbalPitch() + ":::" + Movement.getInstance().getGimbalYaw());
        GimbalAngleRotation gimbalAngleRotation = new GimbalAngleRotation();
        double tarPitch = Double.parseDouble(Movement.getInstance().getGimbalPitch()) + yy;
        double tarYaw = Double.parseDouble(Movement.getInstance().getGimbalYaw()) + xx;
//        判断是否超过边界
        if (tarPitch > Movement.getInstance().getGimbalPitchRange().getMax()) {
            tarPitch = Movement.getInstance().getGimbalPitchRange().getMax();
        } else if (tarPitch < Movement.getInstance().getGimbalPitchRange().getMin()) {
            tarPitch = Movement.getInstance().getGimbalPitchRange().getMin();
        }
        if (tarYaw > Movement.getInstance().getGimbalYawRange().getMax()) {
            tarYaw = Movement.getInstance().getGimbalYawRange().getMax();
        } else if (tarYaw < Movement.getInstance().getGimbalYawRange().getMin()) {
            tarYaw = Movement.getInstance().getGimbalYawRange().getMin();
        }
        gimbalAngleRotation.setPitch(tarPitch);
        gimbalAngleRotation.setYaw(tarYaw);
//        转动云台
        KeyManager.getInstance().performAction(KeyTools.createKey(GimbalKey.KeyRotateByAngle), gimbalAngleRotation, new CommonCallbacks.CompletionCallbackWithParam<EmptyMsg>() {
            @Override
            public void onSuccess(EmptyMsg emptyMsg) {
                Log.i(TAG, "云台转动成功");
            }

            @Override
            public void onFailure(@NonNull IDJIError idjiError) {
                Log.i(TAG, "云台转动失败" + idjiError);
            }
        });

//        指点对焦，将焦点放置屏幕中心
//        KeyManager.getInstance().setValue(KeyTools.createCameraKey(CameraKey.KeyCameraFocusMode,ComponentIndexType.LEFT_OR_MAIN,
//                CameraLensType.CAMERA_LENS_ZOOM), CameraFocusMode.AF,null);
//        KeyManager.getInstance().performAction(KeyTools.createKey(CameraKey.KeyTapZoomAtTarget), zoomTargetPointInfo, new CommonCallbacks.CompletionCallbackWithParam<EmptyMsg>() {
//            @Override
//            public void onSuccess(EmptyMsg emptyMsg) {
//                Log.i(TAG, x + ":" + y + "对焦完成1");
//            }
//
//            @Override
//            public void onFailure(@NonNull IDJIError idjiError) {
//                Log.i(TAG, "对焦失败1" + idjiError);
//            }
//        });


//        KeyManager.getInstance().setValue(KeyTools.createKey(CameraKey.KeyCameraVideoStreamSource), CameraVideoStreamSourceType.ZOOM_CAMERA, new CommonCallbacks.CompletionCallback() {
//            @Override
//            public void onSuccess() {
//                Log.i(TAG, "对焦前设置视频源ZOOM_CAMERA成功");
//            }
//
//            @Override
//            public void onFailure(@NonNull IDJIError idjiError) {
//                Log.i(TAG, "对焦前设置视频源ZOOM_CAMERA失败:" + idjiError);
//            }
//        });
//        DoublePoint2D d = new DoublePoint2D();
//        d.setX((double) x);
//        d.setY((double) y);
//        KeyManager.getInstance().setValue(KeyTools.createKey(CameraKey.KeyCameraFocusTarget), d, new CommonCallbacks.CompletionCallback() {
//            @Override
//            public void onSuccess() {
//                Log.i(TAG, "对焦完成");
//            }
//
//            @Override
//            public void onFailure(@NonNull IDJIError idjiError) {
//                Log.i(TAG, "对焦失败" + idjiError);
//            }
//        });

//        需要设置的倍率，当前倍率
        double ratios = Movement.getInstance().getCameraZoomRatios();

//        获取最接近的倍率下标
        int numberThree = getNumberThree(Movement.getInstance().getGears(), (int) ratios);
        int len = Movement.getInstance().getGears().length;
        if (Movement.getInstance().isContinuous()) {//倍率连续
//            放缩不能超过倍率边界
            if (zoomType == Types.ZOOM_IN_CTRL)
                ratios = ratios + 1 > Movement.getInstance().getGears()[len - 1] ? Movement.getInstance().getGears()[len - 1] : ratios + 1;//倍率递增1
            else if (zoomType == Types.ZOOM_OUT_CTRL)
                ratios = ratios - 1 < Movement.getInstance().getGears()[0] ? Movement.getInstance().getGears()[0] : ratios - 1;//倍率递减1
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
//        变焦
        KeyManager.getInstance().setValue(KeyTools.createCameraKey(CameraKey.KeyCameraZoomRatios,
                        ComponentIndexType.LEFT_OR_MAIN, CameraLensType.CAMERA_LENS_ZOOM), ratios,
                new CommonCallbacks.CompletionCallback() {
                    @Override
                    public void onSuccess() {
                        Log.i(TAG, "放缩成功");
                    }

                    @Override
                    public void onFailure(@NonNull IDJIError idjiError) {
                        Log.i(TAG, "放缩失败" + idjiError);
                    }
                });
//        对焦中间点
        DoublePoint2D d = new DoublePoint2D();
        d.setY(0.5);
        d.setX(0.5);
        KeyManager.getInstance().setValue(KeyTools.createCameraKey(CameraKey.KeyCameraFocusTarget,
                ComponentIndexType.LEFT_OR_MAIN, CameraLensType.CAMERA_LENS_ZOOM), d, new CommonCallbacks.CompletionCallback() {
            @Override
            public void onSuccess() {
                Log.i(TAG, "焦点控制对焦完成");
            }

            @Override
            public void onFailure(@NonNull IDJIError idjiError) {
                Log.i(TAG, "焦点控制对焦失败" + idjiError);
            }
        });
    }

    /**
     * 移动设备位置信息订阅
     *
     * @param interval 移动设备位置信息上报时间间隔 单位：秒 默认值 5
     * @param expires  订阅持续时间 单位：秒 (0：表示取消订阅)
     * @param subID    订阅 ID 大于 0，用于标识不同订阅
     */
    private int interval;
    private int expires;
    private int subID;

    @Override
    public void onMobilePosSub(int interval, int expires, int subID) {
        Log.e(TAG, "onMobilePosSub:" + "interval:" + interval + "expires:" + expires + "subID:" + subID);
        this.subID = subID;
        this.interval = interval;
//      Log.i("经度纬度高度", t1.getLongitude() + " " + t1.getLatitude() + " " + t1.getAltitude());
        if (expires > 0 && subID>0)
            sendMobilePos(expires);
        else {
//           将所有的Callbacks和Messages全部清除掉,移除Handler中所有的消息和回调，避免内存泄露
            handler.removeCallbacksAndMessages(null);
            handler = null;
        }
    }

    Handler handler = new Handler(Looper.getMainLooper());

    /**
     * 根据服务端的订阅，发送移动位置信息
     * subID 订阅ID
     * deviceGBCode 移动设备国标编码
     * dateTime     产生通知时间
     * longitude    经度
     * latitude     纬度
     * speed        速度，单位:km/h
     * direction    方向，取值为当前摄像头方向与正北方的顺时针夹角，取值范围0~360，单位°
     * altitude     海拔高度，单位:m
     *
     * @return
     */
    private void sendMobilePos(int num) {
        Jni28181AgentSDK.getInstance().sendMobilePos(this.subID, "Movement.getInstance().getDevieGBCode()",
                new SimpleDateFormat("yyyy-mm-dd HH:mm:ss").format(System.currentTimeMillis()),
                Double.parseDouble(Movement.getInstance().getCurrentLongitude()),
                Double.parseDouble(Movement.getInstance().getCurrentLatitude()),
                Double.parseDouble(Movement.getInstance().getHorizontalSpeed()),
                Double.parseDouble(Movement.getInstance().getYaw()), Movement.getInstance().getAltitude());
        if (num > 0) {
            handler.postDelayed(() -> sendMobilePos(num - interval), interval * 5000);
        }
    }

    //    返回最接近的值的下标
    public int getNumberThree(int[] intarray, int number) {
        int index = Math.abs(number - intarray[0]);
        int result = intarray[0];
        int j = 0;
        for (int i : intarray) {
            int abs = Math.abs(number - i);
            if (abs <= index) {
                index = abs;
                result = i;
                j++;
            }
        }
        Log.i(TAG, "当前倍率为：" + number + "最接近的数为：" + result + "下标为：" + j);
        return j;
    }

    //    计算水平视场角
    public double getHFOV() {
//        Log.i(TAG,Movement.getInstance().getAngleH()+"  "+Movement.getInstance().getFocalLenght());
        double re = 2 * Math.toDegrees(Math.atan(Movement.getInstance().getAngleH() / (2 * Movement.getInstance().getFocalLenght())));
        return re;
    }

    //    计算垂直视场角
    public double getVFOV() {
        double re = 2 * Math.toDegrees(Math.atan(Movement.getInstance().getAngleV() / (2 * Movement.getInstance().getFocalLenght())));
        return re;
    }
}
