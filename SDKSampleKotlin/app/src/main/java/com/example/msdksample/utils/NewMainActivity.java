//package com.example.msdksample.utils;
//
//import android.content.Context;
//import android.content.DialogInterface;
//import android.content.Intent;
//import android.content.SharedPreferences;
//import android.graphics.SurfaceTexture;
//import android.util.Log;
//import android.view.SurfaceHolder;
//import android.view.TextureView;
//import android.view.View;
//
//import androidx.annotation.NonNull;
//import androidx.appcompat.app.AlertDialog;
//import androidx.core.view.GravityCompat;
//import androidx.fragment.app.Fragment;
//
//import com.google.gson.Gson;
//import com.gosuncn.djidemo.R;
//import com.gosuncn.djidemo.app.Constant;
//import com.gosuncn.djidemo.app.MApplication;
//import com.gosuncn.djidemo.base.BaseDataBindingActivity;
//import com.gosuncn.djidemo.bean.WayPointInfo;
//import com.gosuncn.djidemo.bus.EventRxBus;
//import com.gosuncn.djidemo.bus.event.ComosAndZoomEvent;
//import com.gosuncn.djidemo.common.GlobleContent;
//import com.gosuncn.djidemo.databinding.ActivityMainDrawerBinding;
//import com.gosuncn.djidemo.module.GS28181.GS28181ListenerImp;
//import com.gosuncn.djidemo.module.camera.CameraControlHelper;
//import com.gosuncn.djidemo.module.flight.FlightControl;
//import com.gosuncn.djidemo.module.flight.FlightControlListener;
//import com.gosuncn.djidemo.module.gimbal.GimbalControlHelper;
//import com.gosuncn.djidemo.module.gimbal.GimbalDataListener;
//import com.gosuncn.djidemo.module.gps.AircraftGpsInfo;
//import com.gosuncn.djidemo.module.payload.PayloadControlHelper;
//import com.gosuncn.djidemo.module.video.VideoStreamManager;
//import com.gosuncn.djidemo.utils.DJIErrorCallbackHandler;
//import com.gosuncn.djidemo.utils.DensityUtil;
//import com.gosuncn.djidemo.view.adapter.ConfigPagerAdapter;
//import com.gosuncn.djidemo.view.fragment.CalibrationFragment;
//import com.gosuncn.djidemo.view.fragment.RtkFragment;
//import com.gosuncn.glog.ALog;
//import com.gosuncn.lib28181agent.GS28181SDK;
//import com.gosuncn.lib28181agent.Jni28181AgentSDK;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Timer;
//import java.util.TimerTask;
//
//
//import static com.gosuncn.djidemo.app.Constant.Camera_Type;
//import static com.gosuncn.djidemo.module.payload.PayloadControlHelper.EO_ONLY;
//import static com.gosuncn.djidemo.net.NetworkStateHelper.NETWORK_MOBILE;
//import static com.gosuncn.djidemo.net.NetworkStateHelper.NETWORK_NONE;
//import static com.gosuncn.djidemo.net.NetworkStateHelper.NETWORK_WIFI;
//import static com.gosuncn.djidemo.net.NetworkStateHelper.getConnectWifiSSID;
//import android.content.DialogInterface;
//import android.content.Intent;
//import android.graphics.SurfaceTexture;
//import android.os.Handler;
//import android.os.Looper;
//import android.util.Log;
//import android.view.SurfaceHolder;
//import android.view.TextureView;
//import android.view.View;
//import android.widget.AdapterView;
//import android.widget.ArrayAdapter;
//
//import androidx.annotation.NonNull;
//import androidx.appcompat.app.AlertDialog;
//import androidx.core.view.GravityCompat;
//import androidx.fragment.app.Fragment;
//
//import com.google.gson.Gson;
//import com.gosuncn.djidemo.R;
//import com.gosuncn.djidemo.base.BaseDataBindingActivity;
//import com.gosuncn.djidemo.bean.WayPointInfo;
//import com.gosuncn.djidemo.common.GlobleContent;
//import com.gosuncn.djidemo.databinding.ActivityMainDrawerBinding;
//import com.gosuncn.djidemo.module.GS28181.GS28181ListenerImp;
//import com.gosuncn.djidemo.module.camera.CameraControlHelper;
//import com.gosuncn.djidemo.module.flight.FlightControl;
//import com.gosuncn.djidemo.module.flight.FlightControlListener;
//import com.gosuncn.djidemo.module.gimbal.GimbalControlHelper;
//import com.gosuncn.djidemo.module.gimbal.GimbalDataListener;
//import com.gosuncn.djidemo.module.gps.AircraftGpsInfo;
//import com.gosuncn.djidemo.module.payload.PayloadControlHelper;
//import com.gosuncn.djidemo.module.video.VideoStreamManager;
//import com.gosuncn.djidemo.utils.DJIErrorCallbackHandler;
//import com.gosuncn.djidemo.utils.DensityUtil;
//import com.gosuncn.djidemo.view.adapter.ConfigPagerAdapter;
//import com.gosuncn.djidemo.view.fragment.CalibrationFragment;
//import com.gosuncn.djidemo.view.fragment.RtkFragment;
//import com.gosuncn.glog.ALog;
//import com.gosuncn.lib28181agent.GS28181SDK;
//import com.gosuncn.lib28181agent.Jni28181AgentSDK;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Timer;
//import java.util.TimerTask;
//import java.util.concurrent.atomic.AtomicBoolean;
//
//import dji.sdk.keyvalue.key.AirLinkKey;
//import dji.sdk.keyvalue.key.KeyTools;
//import dji.sdk.keyvalue.key.RemoteControllerKey;
//import dji.sdk.keyvalue.value.camera.CameraVideoStreamSourceType;
//import dji.sdk.keyvalue.value.common.Attitude;
//import dji.sdk.keyvalue.value.common.ComponentIndexType;
//import dji.sdk.keyvalue.value.common.EmptyMsg;
//import dji.sdk.keyvalue.value.gimbal.GimbalAngleRotationMode;
//import dji.v5.common.callback.CommonCallbacks;
//import dji.v5.common.error.IDJIError;
//import dji.v5.common.video.channel.VideoChannelType;
//import dji.v5.common.video.decoder.DecoderOutputMode;
//import dji.v5.common.video.decoder.DecoderState;
//import dji.v5.common.video.decoder.VideoDecoder;
//import dji.v5.common.video.interfaces.IVideoChannel;
//import dji.v5.common.video.interfaces.IVideoDecoder;
//import dji.v5.common.video.stream.PhysicalDevicePosition;
//import dji.v5.common.video.stream.StreamSource;
//import dji.v5.manager.KeyManager;
//import dji.v5.manager.datacenter.MediaDataCenter;
//import dji.v5.manager.datacenter.camera.StreamInfo;
//import dji.v5.manager.interfaces.ICameraStreamManager;
//
//import static com.gosuncn.djidemo.module.payload.PayloadControlHelper.EO_ONLY;
//import static com.gosuncn.djidemo.net.NetworkStateHelper.NETWORK_MOBILE;
//import static com.gosuncn.djidemo.net.NetworkStateHelper.NETWORK_NONE;
//import static com.gosuncn.djidemo.net.NetworkStateHelper.NETWORK_WIFI;
//import static com.gosuncn.djidemo.net.NetworkStateHelper.getConnectWifiSSID;
//
///**
// * 主界面（调试面板/视频流界面）：代理端
// * 负责无人机信令以及视频流传输
// */
//public class NewMainActivity extends BaseDataBindingActivity<ActivityMainDrawerBinding> implements  SurfaceHolder.Callback {//TextureView.SurfaceTextureListener,
//
//    private static final String TAG = NewMainActivity.class.getSimpleName();
//    private static final int HEART_BEAT_INTERVAL = 29 * 1000;    // 心跳频率
//    private static int SCREEN_WIDTH;
//    private static int SCREEN_HEIGHT;
//
//    private ConfigPagerAdapter mConfigPagerAdapter;
//    private static CameraVideoStreamSourceType cameraVideoStreamSourceType =CameraVideoStreamSourceType.WIDE_CAMERA;
//    private VideoStreamManager videoStreamManager;
//    //    private IVideoChannel videoChannel = null;
////    private IVideoDecoder videoDecoder = null;
////    private VideoChannelType videoChannelType = VideoChannelType.PRIMARY_STREAM_CHANNEL;
//    private ComponentIndexType curIndex = null;
//    private SurfaceHolder curHolder = null;
//    private ICameraStreamManager.ReceiveStreamListener streamListener = null;
//
//    private CameraControlHelper cameraHelper;
//    private GimbalControlHelper gimbalHelper;
//    private FlightControl flightControl;
//    private PayloadControlHelper payloadHelper;
//    private GS28181ListenerImp gs28181ListenerImp;
//    private SharedPreferences preferences;
//    private static int click=0;
//
//    private Timer heartBeatTimer;
//    private TimerTask heartBeatTask;
//    private String currentWifiSSID = "";
//    public static NewMainActivity ctx = null;
////    private boolean isPlayingGimbal = false;
////    private  StreamSource gimblaSource = null;
//
//    private AtomicBoolean requestIFrame = new AtomicBoolean(true);
//
//
//    @Override
//    protected int getLayoutId() {
//        return R.layout.activity_main_drawer;
//    }
//
//    @Override
//    protected void beforeSetContentView() {
//        super.beforeSetContentView();
//        Log.w(TAG, "beforeSetContentView");
//
//        setFullScreen();
//        keepScreenOn();
//        if (getSupportActionBar() != null) {
//            getSupportActionBar().hide();
//        }
//    }
//
//    @Override
//    protected void afterSetContentView() {
//        super.afterSetContentView();
//        Log.w(TAG, "afterSetContentView");
//
//        SCREEN_WIDTH = DensityUtil.getScreenWidth(this);
//        SCREEN_HEIGHT = DensityUtil.getScreenHeight(this);
//        ALog.w("屏幕宽：" + SCREEN_WIDTH + " ， 屏幕高：" + SCREEN_HEIGHT);
//
//        currentWifiSSID = getConnectWifiSSID();
//
//        ctx = this;
//
//        initUI();
//        initDJIComponent();
//        if(gs28181ListenerImp == null){
//            gs28181ListenerImp = new GS28181ListenerImp(cameraHelper,gimbalHelper,flightControl,payloadHelper);
//            GS28181SDK.getInstance().setGS28181Listener(gs28181ListenerImp);
//        }
//        startHeartBeatTask();
//        preferences = MApplication.getPreferences();
//
//        String mode = preferences.getString(Camera_Type, "广角");
//        binding.includeMainContent.spinnerMode.setText(mode);
//        binding.includeMainContent.spinnerMode.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                int position = click++;
//                if(preferences.getInt(Constant.Camera_sum,0) == 2){
//                    position = position % 2;
//                    cameraTwo(position);}
//                else if(preferences.getInt(Constant.Camera_sum,0) == 3){
//                    position = position % 3;
//                    cameraThree(position);}
//                EventRxBus.getInstance().post(new ComosAndZoomEvent(position));
//                ALog.w("  镜头i------------------------------------------"+ position);
//                cameraHelper.switchCameraMode(cameraVideoStreamSourceType);
//            }
//
//
//        });
//
//        binding.includeMainContent.videoPreviewerSurface.setZOrderOnTop(false);
//        binding.includeMainContent.videoPreviewerSurface.getHolder().addCallback(this);
//    }
//
//    private void initUI() {
//
//        GlobleContent.getInstance();
//        videoStreamManager = new VideoStreamManager(this);
//        binding.includeMainContent.btnMore.setOnClickListener(v -> binding.drawerLayout.openDrawer(GravityCompat.END));
//
//        //初始化侧边栏
//        List<Fragment> configFragments = new ArrayList<>();
//        configFragments.add(new RtkFragment());
//        configFragments.add(new CalibrationFragment());
//
//        mConfigPagerAdapter = new ConfigPagerAdapter(getSupportFragmentManager(), configFragments, new String[]{"RTK", "校准"});
//        binding.vpConfig.setAdapter(mConfigPagerAdapter);
//        binding.tabLayoutConfig.setupWithViewPager(binding.vpConfig);
//    }
//
//    @Override
//    public void onResume() {
//        super.onResume();
////        binding.includeMainContent.videoPreviewerSurface.setSurfaceTextureListener(this);
////        initPreviewer();
//    }
//
//
//    @Override
//    public void onNetworkChange(int netType) {
//        switch (netType) {
//            case NETWORK_NONE:
//                showWifiAlertDialog("检测到当前无网络连接，是否继续当前操作？");
//                break;
//            case NETWORK_WIFI:
//                String wifiSSID = getConnectWifiSSID();
//                if (!wifiSSID.equals(currentWifiSSID)) {
//                    showWifiAlertDialog("检测到当前Wifi连接发生变化, 是否继续当前操作？\n（如需重新上线平台需手动重启APP）");
//                    currentWifiSSID = wifiSSID;
//                }
//                break;
//            case NETWORK_MOBILE:
//                break;
//        }
//    }
//
//    private AlertDialog mDialog;
//
//    private void showWifiAlertDialog(String msg) {
//        if (mDialog != null && mDialog.isShowing()) {
//            mDialog.dismiss();
//            showWifiAlertDialog(msg);
//            return;
//        }
//        mDialog = new AlertDialog.Builder(NewMainActivity.this)
//                .setMessage(msg)
//                .setPositiveButton("继续飞行", (dialog, which) -> dialog.dismiss())
//                .create();
//        mDialog.show();
//    }
//    private void cameraThree(int position){
//        if(position == 1) {
//            cameraVideoStreamSourceType = CameraVideoStreamSourceType.ZOOM_CAMERA;
//            binding.includeMainContent.spinnerMode.setText("变焦");
//            preferences.edit()
//                    .putString(Constant.Camera_Type,"变焦")
//                    .apply();
//            ALog.w("  切换变焦镜头------------------------------------------");
//        }
//        else if(position ==2){
//            cameraVideoStreamSourceType = CameraVideoStreamSourceType.INFRARED_CAMERA;
//            binding.includeMainContent.spinnerMode.setText("红外");
//            preferences.edit()
//                    .putString(Constant.Camera_Type,"红外")
//                    .apply();
//            ALog.w("  切换红外镜头------------------------------------------");
//        }
//        else if(position == 0){
//            cameraVideoStreamSourceType = CameraVideoStreamSourceType.WIDE_CAMERA;
//            binding.includeMainContent.spinnerMode.setText("广角");
//            preferences.edit()
//                    .putString(Constant.Camera_Type,"广角")
//                    .apply();
//            ALog.w("  切换广角镜头------------------------------------------");
//        }
//    }
//    private void cameraTwo(int position){
//        if(position == 1) {
//            cameraVideoStreamSourceType = CameraVideoStreamSourceType.ZOOM_CAMERA;
//            binding.includeMainContent.spinnerMode.setText("变焦");
//            preferences.edit()
//                    .putString(Constant.Camera_Type,"变焦")
//                    .apply();
//            ALog.w("  切换变焦镜头------------------------------------------");
//        }
//        else if(position == 0){
//            cameraVideoStreamSourceType = CameraVideoStreamSourceType.WIDE_CAMERA;
//            binding.includeMainContent.spinnerMode.setText("广角");
//            preferences.edit()
//                    .putString(Constant.Camera_Type,"广角")
//                    .apply();
//            ALog.w("  切换广角镜头------------------------------------------");
//        }
//    }
//
//
//    /************************************ DJI Component Start ***********************************/
//    // 初始化各DJI组件
//    private void initDJIComponent() {
//        initCameraControl();
//        initPayloadControl();
//        initGimbalControl();
//        initFlightControl();
////        initRtkService();
//    }
//
//    // Camera摄像头控制
//    private void initCameraControl() {
//        if(cameraHelper != null){
//            return;
//        }
//        cameraHelper = new CameraControlHelper();
//        cameraHelper.setCameraListener(new CameraControlHelper.CameraControlListener() {
//            @Override
//            public void onControlSuccess() {
//
//            }
//
//            @Override
//            public void onControlFailed(String errMsg) {
//                ALog.w(errMsg);
//            }
//        });
//    }
//
//    // Gimbal云台控制
//    private void initGimbalControl() {
//        if(gimbalHelper != null){
//            return;
//        }
//        gimbalHelper = new GimbalControlHelper(new GimbalDataListener() {
//            @Override
//            public void onGimbalAttitudeReceive(Attitude attitude) {
//                GlobleContent.getInstance().setAttitude(attitude);
//                String formattedPitch = String.format("%.3f", attitude.getPitch());
//                String formattedYaw = String.format("%.3f", attitude.getYaw());
//
//                binding.includeMainContent.tvPitch.setText("俯仰: " + formattedPitch);
//                binding.includeMainContent.tvYaw.setText("偏航: " + formattedYaw);
//            }
//
//            @Override
//            public void upadateGimbalIndex(ComponentIndexType componentIndex) {
//                curIndex = componentIndex;
//                ALog.w("  设置挂载相机监听------------------------------------------");
//                reSetVideoIndex();
//            }
//        });
//    }
//
//    // Flight飞行控制
//    private void initFlightControl() {
//        if(flightControl != null){
//            return;
//        }
//        flightControl = new FlightControl();
//        flightControl.setFlightStateListener(new FlightControlListener() {
//            @Override
//            public void onFlightStateReceive(AircraftGpsInfo airGpsInfo) {
//                // 获取飞行器GPS信息
//                if (videoStreamManager.getIsRTKUsing()) {
//                    return;
//                }
//                String formattedHeading = String.format("%.3f", airGpsInfo.getHeading());
//                binding.includeMainContent.tvHeading.setText("航向: " + formattedHeading);
//                String formattedLongtitude = String.format("%.3f", airGpsInfo.getLongitude());
//                binding.includeMainContent.tvLongitude.setText("经度: " + formattedLongtitude);
//                String formattedLatitude = String.format("%.3f", airGpsInfo.getLatitude());
//                binding.includeMainContent.tvLatitude.setText("纬度: " + formattedLatitude);
//                String formattedAltitude = String.format("%.3f", airGpsInfo.getAltitude());
//                binding.includeMainContent.tvAltitude.setText("海拔: " + formattedAltitude);
//                String formattedDistance = String.format("%.3f", airGpsInfo.getDistance());
//                binding.includeMainContent.tvDistance.setText("距离: " + formattedDistance);
//                binding.includeMainContent.tvTowards.setText("朝向" + airGpsInfo.getTowards(airGpsInfo.getHeading()));
//            }
//
//            @Override
//            public void onFlightConnected(boolean isConnect) {
////                Intent intent = new Intent(NewMainActivity.this,LoginActivity.class);
////                startActivity(intent);
////                finish();
//            }
//        });
//    }
//
//
//    private void initPayloadControl() {
//        if(payloadHelper != null){
//            return;
//        }
//        payloadHelper = new PayloadControlHelper();
//        payloadHelper.sendData(EO_ONLY);
//        payloadHelper.checkPayloadSupportAR();
//
//        KeyManager.getInstance().setValue(KeyTools.createKey(AirLinkKey.KeyVideoDataRate),7.0,new DJIErrorCallbackHandler("video_rate"));
//    }
//
//    private Handler handler = new Handler(Looper.getMainLooper());
//
//    private void reSetVideoIndex(){
//        if(curIndex == null || curHolder == null){
//            return;
//        }
//        if(streamListener == null){
//            streamListener = (data, offset, length, info) -> {
////                ALog.w("  视频流入栈：：：：：：：：：：：：：：：： "+length+"  offset:::"+offset);
//                videoStreamManager.enqueueFrameBuffer(data,offset,length);
//            };
//        }
//        ICameraStreamManager streamManager = MediaDataCenter.getInstance().getCameraStreamManager();
//        handler.removeCallbacksAndMessages(null);
//        handler.postDelayed(() ->{
//            streamManager.removeReceiveStreamListener(streamListener);
//            streamManager.addReceiveStreamListener(curIndex,streamListener);
//
//            int width = binding.includeMainContent.videoPreviewerSurface.getWidth();
//            int height = binding.includeMainContent.videoPreviewerSurface.getHeight();
////            streamManager.putCameraStreamSurface(curIndex,curHolder.getSurface(),width,height, ICameraStreamManager.ScaleType.FIX_XY);
//            //TODO 遥控器屏幕视频畸变
//            streamManager.putCameraStreamSurface(curIndex,curHolder.getSurface(),width,height, ICameraStreamManager.ScaleType.CENTER_CROP);
//        },5000);
//
//    }
//
//
//    /************************************ DJI Component End ***********************************/
//
//
//    /************************************ GS28181SDK Start ************************************/
//
//    // 启动心跳线程
//    private void startHeartBeatTask() {
//        if (heartBeatTimer == null) {
//            heartBeatTimer = new Timer();
//        }
//        if (heartBeatTask == null) {
//            heartBeatTask = new TimerTask() {
//                @Override
//                public void run() {
//                    int code = Jni28181AgentSDK.getInstance().sendHeartBeat();
//                    ALog.w("发送心跳信息：" + code);
//                }
//            };
//        }
//        heartBeatTimer.schedule(heartBeatTask, 100, HEART_BEAT_INTERVAL);
//    }
//
//
//    // 停止心跳线程
//    private void stopHeartBeatTask() {
//        ALog.w("停止心跳线程：");
//        if (heartBeatTimer != null && heartBeatTask != null) {
//            heartBeatTimer.purge();
//            heartBeatTimer.cancel();
//            heartBeatTask = null;
//            heartBeatTimer = null;
//        }
//    }
//
//    // 释放28181AgentSDK的相关资源
//    private void unInitAgentSDK() {
//        int code = Jni28181AgentSDK.getInstance().unregister();
//        if (code == 0) {
//            stopHeartBeatTask();
//        }
//        Jni28181AgentSDK.getInstance().unInitSDK();
//        GS28181SDK.getInstance().setGS28181Listener(null);
//    }
//
//    /************************************ GS28181SDK End *************************************/
//
//
//    /********************************* 视频流相关部分 Start ***********************************/
//
//    // 反初始化视频流预览
//    private void unInitPreviewer() {
////        if(videoChannel != null){
////            videoChannel.clearAllStreamDataListener();
////            videoChannel.closeChannel(new DJIErrorCallbackHandler("desotry_video"));
////        }
//
//        if(streamListener != null){
//            MediaDataCenter.getInstance().getCameraStreamManager().removeReceiveStreamListener(streamListener);
//        }
//
//    }
//
////    @Override
////    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
////        if (videoDecoder == null) {
////            videoDecoder = new VideoDecoder(this, VideoChannelType.EXTENDED_STREAM_CHANNEL, DecoderOutputMode.SURFACE_MODE, surface);
////        } else if (videoDecoder.getDecoderStatus() == DecoderState.PAUSED) {
////            videoDecoder.onResume();
////        }
////    }
////
////    @Override
////    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
////        ALog.w("onSurfaceTextureSizeChanged");
////    }
////
////    @Override
////    public void onSurfaceTextureUpdated(SurfaceTexture surface) {
//////        ALog.w(TAG, "onSurfaceTextureUpdated");
////    }
////
////    @Override
////    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
////        Log.w(TAG, "onSurfaceTextureDestroyed");
////        if (videoDecoder != null) {
////            videoDecoder.onPause();
////        }
////        return false;
////    }
//
//    @Override
//    public void surfaceCreated(SurfaceHolder holder) {
//        Log.w(TAG, "surfaceCreated---------------------------------------设置相机!!!");
//        if(curHolder != null){
//            MediaDataCenter.getInstance().getCameraStreamManager().removeCameraStreamSurface(curHolder.getSurface());
//        }
//        curHolder = holder;
//        reSetVideoIndex();
////        curHolder = holder;
//
////        if (videoDecoder == null) {
////            videoDecoder = new VideoDecoder(this, VideoChannelType.EXTENDED_STREAM_CHANNEL, DecoderOutputMode.SURFACE_MODE, holder);
////        } else if (videoDecoder.getDecoderStatus() == DecoderState.PAUSED) {
////            videoDecoder.onResume();
////        }
//    }
//
//    @Override
//    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
//        Log.w(TAG, "surfaceChanged---------------------------------------设置相机!!!");
//        if(curHolder != null){
//            MediaDataCenter.getInstance().getCameraStreamManager().removeCameraStreamSurface(curHolder.getSurface());
//        }
//        curHolder = holder;
//        reSetVideoIndex();
//    }
//
//    @Override
//    public void surfaceDestroyed(SurfaceHolder holder) {
//        Log.w(TAG, "surfaceDestroyed");
//        curHolder = null;
//        MediaDataCenter.getInstance().getCameraStreamManager().removeCameraStreamSurface(holder.getSurface());
////        if (videoDecoder != null) {
////            videoDecoder.onPause();
////        }
//    }
//
//    /********************************* 视频流相关部分 End **************************************/
//
//
//    // TODO debug --- 测试写裸流
//    public void onClickStartBuffer(View view) {
//        videoStreamManager.setNeedSave(true);
//        view.setEnabled(false);
////        if(videoStreamManager.isLocalSave()){
////            videoStreamManager.stopLocalVideoPush();
////            binding.includeMainContent.btnOpenBuffer.setText("录像");
////        }else{
////            videoStreamManager.startPushLocalVideo();
////            binding.includeMainContent.btnOpenBuffer.setText("停止");
////        }
//    }
//
//    double pitch = -20;
//    public void onClickRotate1(View view) {
////        String para = binding.includeMainContent.etPara.getText().toString();
////        String[] paraArray = para.split(",");
////
////        showShortToast(
////                "pitch: " + Float.valueOf(paraArray[1]) + "\n" +
////                        "yaw: " + Float.valueOf(paraArray[2]) + "\n" +
////                        "duration: " + Float.valueOf(paraArray[4])
////        );
//
////        pitch = -pitch;
////        gimbalHelper.moveGimbalWithTime(GimbalAngleRotationMode.ABSOLUTE_ANGLE,pitch, 0,0, 0);
//
//        flightControl.setVirtualStickCtrStatus(false,true,yaw);
//    }
//
//    int yaw = 50;
////    boolean isOpen = false;
//
//    public void onClickRotate2(View view) {
////        String para = binding.includeMainContent.etPara.getText().toString();
////        String[] paraArray = para.split(",");
////
////        RotationMode mode;
////        switch (Integer.valueOf(paraArray[0])) {
////            case 1:
////                mode = RotationMode.ABSOLUTE_ANGLE;
////                break;
////            case 2:
////                mode = RotationMode.RELATIVE_ANGLE;
////                break;
////            default:
////                mode = RotationMode.SPEED;
////                break;
////        }
////
////        showShortToast(
////                "mode: " + Float.valueOf(paraArray[0]) + ": " + mode.toString() + "\n" +
////                        "pitch: " + Float.valueOf(paraArray[1]) + "\n" +
////                        "yaw: " + Float.valueOf(paraArray[2]) + "\n" +
////                        "roll: " + Float.valueOf(paraArray[3]) + "\n" +
////                        "duration: " + Float.valueOf(paraArray[4])
////        );
////        GimbalControlHelper gimbalControlHelper = new GimbalControlHelper(mAircraft);
////        gimbalControlHelper.moveGimbalWithTime(
////                mode,
////                Float.valueOf(paraArray[1]),
////                Float.valueOf(paraArray[2]),
////                Float.valueOf(paraArray[3]),
////                Float.valueOf(paraArray[4])
////        );
//
//
////        if(yaw == 0){
////            yaw = 30;
////        }else{
////            yaw = 0;
////        }
////        yaw = yaw * -1;
////        Log.e(TAG,"     onClickRotate2:::::::::::::"+yaw);
////        AircraftGimbalControl.getInstance().move(RotationMode.ABSOLUTE_ANGLE,yaw, Rotation.NO_ROTATION, 0);
//
//        if(flightControl.isFlying()){
//            if(yaw == 90){
//                yaw = -90;
//            }else{
//                yaw = 90;
//            }
//            flightControl.rotateTotarget(yaw);
//        }
//
//
////        if(yaw == 0){
////            yaw = 734;
////        }else{
////            yaw = 0;
////        }
////
////        CameraControlHelper.getInstance().startSearchZoom(yaw);
//
////        String result = FileIOUtil.readFile2String("sdcard/test.kml","UTF-8");
////        Gson gson = new Gson();
////        WayPointInfo info = gson.fromJson(result,WayPointInfo.class);
////        WayPointHelper.startWayPointTask(info,this);
////        Log.e(TAG,"result::::::::::::::"+gson.toJson(info));
//    }
//
//    public void onClickReset(View view) {
////        AircraftGimbalControl.getInstance().calibration();
////        isOpen = !isOpen;
////        FlightControl.getInstance().setVirtualStickCtrStatus(isOpen);
//    }
//
//
//    @Override
//    public void onStop() {
////        ALog.w("onStop");
//        unInitPreviewer();
//        super.onStop();
//    }
//
//    @Override
//    protected void onDestroy() {
//        ALog.w("onDestroy");
//        // TODO debug --- 写Payload视频流用于测试分析时,不回收ByteBuffer
//        unInitAgentSDK();
//        if(gs28181ListenerImp != null){
//            gs28181ListenerImp.stopService();
//        }
//        ctx = null;
//        unInitDJIComponent();
//        GlobleContent.destory();
//        super.onDestroy();
//    }
//
//    private void unInitDJIComponent(){
//        if(cameraHelper != null){
//            cameraHelper.onDestory();
//            cameraHelper = null;
//        }
//        if(flightControl != null){
//            flightControl.onDesoty();
//            flightControl = null;
//        }
//
//        if(gimbalHelper != null){
//            gimbalHelper.onDestory();
//            gimbalHelper = null;
//        }
//
//        if(payloadHelper != null){
//            payloadHelper.destroy();
//            payloadHelper = null;
//        }
//    }
//
//
//}