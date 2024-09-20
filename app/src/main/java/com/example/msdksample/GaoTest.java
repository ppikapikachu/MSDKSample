package com.example.msdksample;

import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.msdksample.callback.MGS28181Listener;
import com.example.msdksample.entity.Movement;
import com.example.msdksample.utils.FileUtil;
import com.gosuncn.lib28181agent.GS28181SDKManager;
import com.gosuncn.lib28181agent.Jni28181AgentSDK;
import com.gosuncn.lib28181agent.Log.LogToFile;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import dji.sdk.keyvalue.key.CameraKey;
import dji.sdk.keyvalue.key.KeyTools;
import dji.sdk.keyvalue.value.camera.CameraMode;
import dji.sdk.keyvalue.value.camera.VideoBitrateMode;
import dji.sdk.keyvalue.value.camera.VideoResolutionFrameRate;
import dji.sdk.keyvalue.value.common.ComponentIndexType;
import dji.v5.common.callback.CommonCallbacks;
import dji.v5.common.error.IDJIError;
import dji.v5.manager.KeyManager;
import dji.v5.manager.datacenter.MediaDataCenter;
import dji.v5.manager.datacenter.camera.StreamInfo;
import dji.v5.manager.interfaces.ICameraStreamManager;

public class GaoTest extends AppCompatActivity implements View.OnClickListener {

    private Button registerSDK, initSDK, sendVideoStream, sendVideoWithARInfo,
            sendVideoWithARInfoX, sendVideoWithARInfoToLocal, stopWriteStream, startSurface = null;
    private GS28181SDKManager manager = GS28181SDKManager.getInstance();
    private String TAG = "GaoTest";
    private boolean isInitSDK,isRegisterSDK = false;
    //    发送AR视频流时的横滚值等
    boolean isSendStream = false;
    //    帧数据相关
    private ICameraStreamManager cameraManager = MediaDataCenter.getInstance().getCameraStreamManager();
    Handler handler = new Handler(Looper.getMainLooper());//本来没private static 的，但是会内存泄漏
    private boolean continueSendVideo = false;
    private SurfaceView surface = null;
    //    帧数据监听
    private ICameraStreamManager.ReceiveStreamListener sendVideoStreamListener = null;
    private ICameraStreamManager.ReceiveStreamListener sendVideoWithARInfoListener = null;
    private ICameraStreamManager.ReceiveStreamListener sendVideoWithARInfoXListener = null;
    private ICameraStreamManager.ReceiveStreamListener sendVideoWithARInfoToLocalListener = null;
    //相机索引
    private ComponentIndexType componentIndexType =  ComponentIndexType.LEFT_OR_MAIN;;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gao_test);

        init();
//        initSendStreamButton();
        //            初始化数据帧监听
        initReceiveStreamListener();
    }

    //    判断相机是否连接，并且设置button是否可用
//    private void initSendStreamButton() {
//        Boolean va = KeyManager.getInstance().getValue(KeyTools.createKey(CameraKey.KeyConnection));
//        Movement.getInstance().setCameraConnection(va == null ? false : va);//防止为空，没连飞机时为空
//        if (!Movement.getInstance().isCameraConnection()) {
//            handler.postDelayed(new Runnable() {
//                @Override
//                public void run() {
//                    Log.i(TAG, "相机未连接");
//                    sendVideoStream.setEnabled(false);
//                    sendVideoWithARInfo.setEnabled(false);
//                    sendVideoWithARInfoX.setEnabled(false);
//                    sendVideoWithARInfoToLocal.setEnabled(false);
//                    stopWriteStream.setEnabled(false);
//                    initSendStreamButton();
//                }
//            }, 1000);
//        } else {
//            Log.i(TAG, "相机已经连接");
////            相机连接后开始相机数据监听
////            if (!CameraManager.getStart()){
////                CameraManager.getInstance().initCameraInfo();
////            }else {
////                Log.i(TAG,"已经开启监听");
////            }
//            isSendStream = true;
//            sendVideoStream.setEnabled(true);
//            sendVideoWithARInfo.setEnabled(true);
//            sendVideoWithARInfoX.setEnabled(true);
//            sendVideoWithARInfoToLocal.setEnabled(true);
//            stopWriteStream.setEnabled(true);
//            startSurface.setEnabled(true);
//
////            获取可用相机索引      这个内存泄漏
////            cameraManager.addAvailableCameraUpdatedListener(new ICameraStreamManager.AvailableCameraUpdatedListener() {
////                @Override
////                public void onAvailableCameraUpdated(@NonNull List<ComponentIndexType> availableCameraList) {
////                    Movement.getInstance().setAvailableCameraList(availableCameraList);
////                    Log.i(TAG, availableCameraList + "可用相机");
////                }
////            });
//
//        }
//    }

    private void init() {
        registerSDK = findViewById(R.id.registerSDK);
        initSDK = findViewById(R.id.initSDK);
        sendVideoStream = findViewById(R.id.sendVideoStream);
        sendVideoWithARInfo = findViewById(R.id.sendVideoWithARInfo);
        sendVideoWithARInfoX = findViewById(R.id.sendVideoWithARInfoX);
        sendVideoWithARInfoToLocal = findViewById(R.id.sendVideoWithARInfoToLocal);
        stopWriteStream = findViewById(R.id.stopWriteStream);
        surface = findViewById(R.id.surface1);
        startSurface = findViewById(R.id.startSurface);

        registerSDK.setOnClickListener(this);
        initSDK.setOnClickListener(this);
        sendVideoStream.setOnClickListener(this);
        sendVideoWithARInfo.setOnClickListener(this);
        sendVideoWithARInfoX.setOnClickListener(this);
        sendVideoWithARInfoToLocal.setOnClickListener(this);
        stopWriteStream.setOnClickListener(this);
        startSurface.setOnClickListener(this);

//        设置录像模式
        KeyManager.getInstance().setValue(KeyTools.createKey(CameraKey.KeyCameraMode), CameraMode.VIDEO_NORMAL,null);
//        设置镜头分辨率，降低分辨率
        VideoResolutionFrameRate v = new VideoResolutionFrameRate();
        List<VideoResolutionFrameRate> VList = Movement.getInstance().getKeyVideoResolutionFrameRateRange();
        Log.i(TAG,VList+"==============");
        v.setResolution(VList.get(0).getResolution());
        KeyManager.getInstance().setValue(KeyTools.createKey(CameraKey.KeyVideoResolutionFrameRate), v, new CommonCallbacks.CompletionCallback() {
            @Override
            public void onSuccess() {
                Log.i(TAG,"设置镜头分辨率成功："+VList.get(0).getResolution());
            }

            @Override
            public void onFailure(@NonNull IDJIError idjiError) {
                Log.i(TAG,"设置镜头分辨率失败："+idjiError);
            }
        });
//        设置码率,降低码率
        KeyManager.getInstance().setValue(KeyTools.createKey(CameraKey.KeyVideoBitrateMode), VideoBitrateMode.VBR, new CommonCallbacks.CompletionCallback() {
            @Override
            public void onSuccess() {
                Log.i(TAG,"相机码率设置VBR成功");
            }

            @Override
            public void onFailure(@NonNull IDJIError idjiError) {
                Log.i(TAG,"相机码率设置VBR失败");
            }
        });
    }

//    创建数据帧监听
    private void initReceiveStreamListener() {
        sendVideoStreamListener = new ICameraStreamManager.ReceiveStreamListener() {
            @Override
            public void onReceiveStream(@NonNull byte[] data, int offset, int length, @NonNull StreamInfo info) {
                if (continueSendVideo && data != null) {
                    if (info.getMimeType() == ICameraStreamManager.MimeType.H264) {
                        int re = manager.sendVideoStream(System.currentTimeMillis(), info.isKeyFrame() ? 1 : FileUtil.getFrameType(data), data);
                        Log.i(TAG, "发送视频流sendVideoStream:" + re);
                    }else {
                        int re = manager.sendVideoStreamH265(System.currentTimeMillis(), info.isKeyFrame() ? 1 : FileUtil.getFrameType(data), data);
                        Log.i(TAG, "发送视频流sendVideoStreamH265:" + re);
                    }
                } else {
                    if (continueSendVideo){
                        Log.i(TAG, "发送视频流sendVideoStream:" + "已开启");
                    }else {
                        Log.i(TAG, "发送视频流sendVideoStream:" + "数据为空");
                    }
                }
            }
        };

        sendVideoWithARInfoListener = new ICameraStreamManager.ReceiveStreamListener() {
            @Override
            public void onReceiveStream(@NonNull byte[] data, int offset, int length, @NonNull StreamInfo info) {
                if (continueSendVideo && data != null) {
                    if (info.getMimeType() == ICameraStreamManager.MimeType.H264) {
                        int re = manager.sendVideoWithARInfo(System.currentTimeMillis(), info.isKeyFrame() ? 1 : FileUtil.getFrameType(data), data,
                                Float.parseFloat(Movement.getInstance().getGimbalRoll()),
                                Float.parseFloat(Movement.getInstance().getGimbalPitch()),
                                Float.parseFloat(Movement.getInstance().getGimbalYaw()),
                                Float.parseFloat(Movement.getInstance().getCurrentLongitude()),
                                Float.parseFloat(Movement.getInstance().getCurrentLatitude()),
                                Movement.getInstance().getCurrentAltitude());
                        Log.i(TAG, "发送完整的帧 + 摄像机姿态信息（ AR 信息）sendVideoWithARInfo:" + re);
                    }else {
                        int re = manager.sendVideoWithARInfoH265(System.currentTimeMillis(), info.isKeyFrame() ? 1 : FileUtil.getFrameType(data), data,
                                Float.parseFloat(Movement.getInstance().getGimbalRoll()),
                                Float.parseFloat(Movement.getInstance().getGimbalPitch()),
                                Float.parseFloat(Movement.getInstance().getGimbalYaw()),
                                Float.parseFloat(Movement.getInstance().getCurrentLongitude()),
                                Float.parseFloat(Movement.getInstance().getCurrentLatitude()),
                                Movement.getInstance().getCurrentAltitude());
                        Log.i(TAG, "发送完整的帧 + 摄像机姿态信息（ AR 信息）sendVideoWithARInfoH265:" + re);
                    }
                } else {
                    if (continueSendVideo){
                        Log.i(TAG, "发送完整的帧 + 摄像机姿态信息（ AR 信息）sendVideoWithARInfo:" + "已开启");
                    }else {
                        Log.i(TAG, "发送完整的帧 + 摄像机姿态信息（ AR 信息）sendVideoWithARInfo:" + "数据为空");                }
                }
            }
        };
        sendVideoWithARInfoXListener = new ICameraStreamManager.ReceiveStreamListener() {
            @Override
            public void onReceiveStream(@NonNull byte[] data, int offset, int length, @NonNull StreamInfo info) {
                if (continueSendVideo && data != null) {
                    if (info.getMimeType() == ICameraStreamManager.MimeType.H264) {
                        int re = manager.sendVideoWithARInfoX(System.currentTimeMillis(), info.isKeyFrame() ? 1 : FileUtil.getFrameType(data), data,
                                Float.parseFloat(String.valueOf(Movement.getInstance().getCameraZoomRatios())),//double转float
                                Float.parseFloat(Movement.getInstance().getGimbalPitch()), Float.parseFloat(Movement.getInstance().getGimbalYaw()),
                                Movement.getInstance().getAngleH(), Movement.getInstance().getAngleV(),
                                Float.parseFloat(Movement.getInstance().getCurrentLongitude()),
                                Float.parseFloat(Movement.getInstance().getCurrentLatitude()),
                                Movement.getInstance().getCurrentAltitude());
                        Log.i(TAG, "发送 视频流与 AR信息流 上传sendVideoWithARInfoX:" + re);
                    }else {
                        int re = manager.sendVideoWithARInfoXH265(System.currentTimeMillis(), info.isKeyFrame() ? 1 : FileUtil.getFrameType(data), data,
                                Float.parseFloat(String.valueOf(Movement.getInstance().getCameraZoomRatios())),//double转float
                                Float.parseFloat(Movement.getInstance().getGimbalPitch()), Float.parseFloat(Movement.getInstance().getGimbalYaw()),
                                Movement.getInstance().getAngleH(), Movement.getInstance().getAngleV(),
                                Float.parseFloat(Movement.getInstance().getCurrentLongitude()),
                                Float.parseFloat(Movement.getInstance().getCurrentLatitude()),
                                Movement.getInstance().getCurrentAltitude());
                        Log.i(TAG, "发送 视频流与 AR信息流 上传sendVideoWithARInfoXH265:" + re);
                    }
                } else {
                    if (continueSendVideo){
                        Log.i(TAG, "发送 视频流与 AR信息流 上传sendVideoWithARInfoX:" + "已开启");
                    }else {
                        Log.i(TAG, "发送 视频流与 AR信息流 上传sendVideoWithARInfoX:" + "数据为空");
                    }
                }
            }
        };
        sendVideoWithARInfoToLocalListener = new ICameraStreamManager.ReceiveStreamListener() {
            @Override
            public void onReceiveStream(@NonNull byte[] data, int offset, int length, @NonNull StreamInfo info) {

                String absolutePath = Environment.getExternalStorageDirectory().getAbsolutePath();

                if (continueSendVideo && data != null) {
                    if (info.getMimeType() == ICameraStreamManager.MimeType.H264) {
                        Log.i(TAG, "h264格式");
                        int re = manager.sendVideoWithARInfoToLocal(System.currentTimeMillis(), info.isKeyFrame() ? 1 : FileUtil.getFrameType(data), data,
                                Float.parseFloat(String.valueOf(Movement.getInstance().getCameraZoomRatios())),//double转float
                                Float.parseFloat(Movement.getInstance().getGimbalPitch()), Float.parseFloat(Movement.getInstance().getGimbalYaw()),
                                Movement.getInstance().getAngleH(), Movement.getInstance().getAngleV(),
                                Float.parseFloat(Movement.getInstance().getCurrentLongitude()),
                                Float.parseFloat(Movement.getInstance().getCurrentLatitude()),
                                Movement.getInstance().getCurrentAltitude(), absolutePath + "/111.h264");
                        Log.i(TAG, "传输视频流与 AR 信息流，直接上传到指定路径 sendVideoWithARInfoToLocal:re="+re);
                    } else {
                        int re = manager.sendVideoWithARInfoToLocalH265(System.currentTimeMillis(), info.isKeyFrame() ? 1 : FileUtil.getFrameType(data), data,
                                Float.parseFloat(String.valueOf(Movement.getInstance().getCameraZoomRatios())),//double转float
                                Float.parseFloat(Movement.getInstance().getGimbalPitch()), Float.parseFloat(Movement.getInstance().getGimbalYaw()),
                                Movement.getInstance().getAngleH(), Movement.getInstance().getAngleV(),
                                Float.parseFloat(Movement.getInstance().getCurrentLongitude()),
                                Float.parseFloat(Movement.getInstance().getCurrentLatitude()),
                                Movement.getInstance().getCurrentAltitude(), absolutePath + "/111.h265");
                        Log.i(TAG, "传输视频流与 AR 信息流，直接上传到指定路径 sendVideoWithARInfoToLocalH265:" + absolutePath + re);
                    }
                } else {
                    if (continueSendVideo){
                        Log.i(TAG, "传输视频流与 AR 信息流，直接上传到指定路径 sendVideoWithARInfoToLocal:" + "已开启");
                    }else {
                        Log.i(TAG, "传输视频流与 AR 信息流，直接上传到指定路径 sendVideoWithARInfoToLocal:" + "数据为空");
                    }
                }
            }
        };
    }


    private Timer heartBeatTimer;
    private TimerTask heartBeatTask;
    public final int HEART_BEAT_INTERVAL = 29*1000;
    // 启动心跳线程
    private void startHeartBeatTask() {
        if (heartBeatTimer == null) {
            heartBeatTimer = new Timer();
        }
        if (heartBeatTask == null) {
            heartBeatTask = new TimerTask() {
                @Override
                public void run() {
                    int code = Jni28181AgentSDK.getInstance().sendHeartBeat();
                    Log.i(TAG,"发送心跳信息：" + code);
                }
            };
        }
        heartBeatTimer.schedule(heartBeatTask, 100, HEART_BEAT_INTERVAL);
    }
    // 停止心跳线程
    private void stopHeartBeatTask() {
        Log.i(TAG,"停止心跳线程：");
        if (heartBeatTimer != null && heartBeatTask != null) {
            heartBeatTimer.purge();
            heartBeatTimer.cancel();
            heartBeatTask = null;
            heartBeatTimer = null;
        }
    }
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.initSDK:
                if (isInitSDK) {
                    Log.i(TAG, "initSDK已初始化");
                    return;
                }
                int re1 = manager.initSDK(getLocalIpAddress());
                isInitSDK = re1 == 0 ? true : false;
                Log.i(TAG, "initSDK:" + re1);

                LogToFile.init(this);
                break;
            case R.id.registerSDK:
                if (isRegisterSDK) {
                    Log.i(TAG, "registerSDK已注册");
                    return;
                }
                int re2 = manager.registerSDK("183.62.9.189", 15060);
//                开心跳包
                startHeartBeatTask();
                isRegisterSDK = re2 == 0 ? true : false;
                Log.i(TAG, "registerSDK:" + re2);
//                开启监听
                GS28181SDKManager.getInstance().setListenerServer(new MGS28181Listener());
                break;
            case R.id.startSurface:
                //                小窗口视频
                cameraManager.putCameraStreamSurface(componentIndexType, surface.getHolder().getSurface(),
                        surface.getWidth(), surface.getHeight(), ICameraStreamManager.ScaleType.CENTER_INSIDE);
                break;
            case R.id.sendVideoStream:
                if (continueSendVideo) {
                    return;
                }
//                监听指定相机的帧数据
//                发送视频流
                continueSendVideo = true;
                cameraManager.addReceiveStreamListener(componentIndexType, sendVideoStreamListener);
                break;
            case R.id.sendVideoWithARInfo:
                if (continueSendVideo) {
                    return;
                }
                //    发送完整的帧 + 摄像机姿态信息（ AR 信息）
                continueSendVideo = true;
                cameraManager.addReceiveStreamListener(componentIndexType, sendVideoWithARInfoListener);
                break;
            case R.id.sendVideoWithARInfoX:
                if (continueSendVideo) {
                    return;
                }
                //  视频流与 AR 信息流上传else
                continueSendVideo = true;
                cameraManager.addReceiveStreamListener(componentIndexType, sendVideoWithARInfoXListener);
                break;
            case R.id.sendVideoWithARInfoToLocal:
                if (continueSendVideo) {
                    return;
                }
                continueSendVideo = true;
//                cameraManager.addFrameListener(ComponentIndexType.LEFT_OR_MAIN,ICameraStreamManager.FrameFormat.YUV420_888,listener1);
                cameraManager.addReceiveStreamListener(componentIndexType, sendVideoWithARInfoToLocalListener);
                break;
            case R.id.stopWriteStream:
//                中断写入指定路径
                continueSendVideo = false;
                manager.stopWriteStream();
//                cameraManager.removeFrameListener(listener1);
                cameraManager.removeReceiveStreamListener(sendVideoWithARInfoToLocalListener);
                cameraManager.removeReceiveStreamListener(sendVideoWithARInfoListener);
                cameraManager.removeReceiveStreamListener(sendVideoStreamListener);
                cameraManager.removeReceiveStreamListener(sendVideoWithARInfoXListener);

//                new MGS28181Listener().onPTZControl(1,Types.PTZ_UP,120);
//                new MGS28181Listener().onMobilePosSub(5,20,10900000);
//                new MGS28181Listener().onZoomControl(Types.ZOOM_IN_CTRL,1080,980,98,99,808,80);
                break;
        }
    }

    //    ip获取
    public String getLocalIpAddress() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address) {
                        return inetAddress.getHostAddress();
                    }
                }
            }
        } catch (SocketException ex) {
            // 处理异常
            ex.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        isInitSDK = false;
        manager.uninitSDK();
        Jni28181AgentSDK.getInstance().unregister();
        stopHeartBeatTask();//停心跳包
//        移除数据帧监听
        cameraManager.removeReceiveStreamListener(sendVideoWithARInfoToLocalListener);
        cameraManager.removeReceiveStreamListener(sendVideoWithARInfoListener);
        cameraManager.removeReceiveStreamListener(sendVideoStreamListener);
        cameraManager.removeReceiveStreamListener(sendVideoWithARInfoXListener);
        cameraManager.removeCameraStreamSurface(surface.getHolder().getSurface());
//        移除监听回调
        GS28181SDKManager.getInstance().setListenerServer(null);
        //           将所有的Callbacks和Messages全部清除掉,移除Handler中所有的消息和回调，避免内存泄露
        handler.removeCallbacksAndMessages(null);
        handler = null;
    }
}
