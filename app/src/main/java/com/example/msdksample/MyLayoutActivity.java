package com.example.msdksample;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.Manifest;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.msdksample.callback.MGS28181Listener;
import com.example.msdksample.entity.H264Frame;
import com.example.msdksample.entity.Movement;
import com.example.msdksample.utils.CameraControllerUtil;
import com.example.msdksample.utils.FileUtil;
import com.example.msdksample.utils.VideoStreamThread;
import com.gosuncn.lib28181agent.GS28181SDKManager;
import com.gosuncn.lib28181agent.Jni28181AgentSDK;
import com.gosuncn.lib28181agent.Log.LogToFile;
import com.gosuncn.lib28181agent.Types;

import org.jcodec.containers.mp4.boxes.Edit;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;

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

public class MyLayoutActivity extends AppCompatActivity implements View.OnClickListener, View.OnTouchListener {

    private ImageButton btn_right = null;
    private DrawerLayout mDrawerLayout = null;
    private LinearLayout drawer_right = null;

    private GS28181SDKManager manager = GS28181SDKManager.getInstance();
    private CameraControllerUtil cameraControllerUtil = new CameraControllerUtil();
    private String TAG = getClass().getSimpleName();
    private boolean isInitSDK, isRegisterSDK = false;
    //    发送AR视频流时的横滚值等
    boolean isSendStream = false;
    //    帧数据相关
    private ICameraStreamManager cameraManager = MediaDataCenter.getInstance().getCameraStreamManager();
    Handler handler = new Handler(Looper.getMainLooper());//本来没private static 的，但是会内存泄漏
    private boolean continueSendVideo = false;
    private String absolutePath = Environment.getExternalStorageDirectory().getAbsolutePath();
    private int mimeType = 4;
    //    帧数据监听
    private ICameraStreamManager.ReceiveStreamListener sendVideoStreamListener = null;
    private ICameraStreamManager.ReceiveStreamListener sendVideoWithARInfoListener = null;
    private ICameraStreamManager.ReceiveStreamListener sendVideoWithARInfoXListener = null;
    private ICameraStreamManager.ReceiveStreamListener sendVideoWithARInfoToLocalListener = null;

    private static AtomicInteger countD = new AtomicInteger(0);
    private static AtomicInteger countM = new AtomicInteger(0);
    private long startTime = 0;
    private long endTime = 90;
    private long lastTime;
    private long curTime;
    //    发流相关
    VideoStreamThread videoStreamThread = null;
    private Spinner spinner = null;
    private Button sendVideoStreamBtn = null;
    private int vedioPos = -1;//选项卡的发流方式
    long delay = 0;
    //相机索引
    private ComponentIndexType componentIndexType = ComponentIndexType.LEFT_OR_MAIN;
    //    防抖相关
    private float debounceThresholdPitch = 1.0f;
    private float debounceThresholdYaw = 1.0f;
    private float preFrameYaw = 0.0f;
    private float preFramePitch = 0.0f;
    //    水平和垂直的云台补偿
    private volatile float compensateYaw = 0.0f;
    private volatile float compensatePitch = 0.0f;
    private EditText editYaw;
    private EditText editPitch;
    private Button compensateBtn = null;
    private Boolean isCompensate = false;//是否开启补偿
    //    工具类
    FileUtil fileUtil = new FileUtil();
    private RelativeLayout llTouch;//屏幕点击

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);//隐藏状态栏
        // When the compile and target version is higher than 22, please request the
        // following permissions at runtime to ensure the
        // SDK work well.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.VIBRATE,
                            Manifest.permission.INTERNET, Manifest.permission.ACCESS_WIFI_STATE,
                            Manifest.permission.WAKE_LOCK, Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.ACCESS_NETWORK_STATE, Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.CHANGE_WIFI_STATE, Manifest.permission.MOUNT_UNMOUNT_FILESYSTEMS,
                            Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.SYSTEM_ALERT_WINDOW,
                            Manifest.permission.READ_PHONE_STATE,
                    }
                    , 1);
        }

        setContentView(R.layout.activity_my_layout1);
        //initSDK,初始化和登录
        init_re_SDK();
        init();
        //初始化数据帧监听
        initReceiveStreamListener();
//      降低码率和分辨率
        setBitRate();
//        spinner
        initSpinner();
    }

    private void init() {

        btn_right = findViewById(R.id.btn_right);
        mDrawerLayout = findViewById(R.id.drawer_layout);
        drawer_right = findViewById(R.id.drawer_right);
        // TODO: 2024/9/13  要延迟获取，不然空指针
        preFramePitch = Float.parseFloat(Movement.getInstance().getGimbalPitch());
        preFrameYaw = Float.parseFloat(Movement.getInstance().getGimbalYaw());
        spinner = findViewById(R.id.sendVedioStreamFun);
        sendVideoStreamBtn = findViewById(R.id.sendVideoStreamBtn);
        llTouch = findViewById(R.id.relative_layout);
        videoStreamThread = new VideoStreamThread(fileUtil);//发流线程实现类
        editYaw = findViewById(R.id.et_yaw_compensation);//水平补偿输入
        editPitch = findViewById(R.id.et_pitch_compensation);//垂直补偿输入
        compensateBtn = findViewById(R.id.compensateBtn);
        videoStreamThread.init(debounceThresholdPitch, debounceThresholdYaw);//初始化防抖阈值

        btn_right.setOnClickListener(this);
        sendVideoStreamBtn.setOnClickListener(this);
        llTouch.setOnTouchListener(this);//屏幕点击
        compensateBtn.setOnClickListener(this);
        //监听
        mDrawerLayout.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(@NonNull View view, float v) {
                Log.i("---", "滑动中");
            }

            @Override
            public void onDrawerOpened(@NonNull View view) {
                Log.i("---", "打开");
            }

            @Override
            public void onDrawerClosed(@NonNull View view) {
                Log.i("---", "关闭");
            }

            @Override
            public void onDrawerStateChanged(int i) {
                Log.i("---", "状态改变");
            }
        });

        DisplayMetrics displayMetrics = this.getResources()
                .getDisplayMetrics();
        // 获取屏幕像素宽度 px
        width = displayMetrics.widthPixels;
        Movement.getInstance().setWidth(width);
        // 获取屏幕像素高度 px
        height = displayMetrics.heightPixels;
        Movement.getInstance().setHeight(height);
    }

    int width;
    int height;

    public boolean onTouchEvent(MotionEvent event) {
        // 在这里判断一下如果是按下操作就获取坐标然后执行方法
        int x = (int) event.getX();
        int y = (int) event.getY();
        Log.i(TAG, "点击屏幕的位置：" + x + "--" + y + "屏幕长高" + width + "--" + height);
        new MGS28181Listener().onZoomControl(Types.ZOOM_IN_CTRL, height, width, 98, 99, x, y);
        return super.onTouchEvent(event);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
            /**
             * 点击的开始位置
             */
            case MotionEvent.ACTION_DOWN:
                break;
            /**
             * 触屏实时位置
             */
            case MotionEvent.ACTION_MOVE:
                break;
            /**
             * 离开屏幕的位置
             */
            case MotionEvent.ACTION_UP:
                onTouchEvent(event);
                break;
            default:
                break;
        }
        /**
         *  注意返回值
         *  true：view继续响应Touch操作；
         *  false：view不再响应Touch操作，故此处若为false，只能显示起始位置，不能显示实时位置和结束位置
         */
        return true;
    }

    public void initSpinner() {
        spinner.getSelectedItem();
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long id) {
                String re = adapterView.getItemAtPosition(pos).toString();
                vedioPos = pos;
                Log.i(TAG, "选择下标为：" + pos + "---方式为：" + re);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
     }

    private void setBitRate() {
        //        设置录像模式
        KeyManager.getInstance().setValue(KeyTools.createKey(CameraKey.KeyCameraMode), CameraMode.VIDEO_NORMAL, null);
//        设置镜头分辨率，降低分辨率
        List<VideoResolutionFrameRate> VList = Movement.getInstance().getKeyVideoResolutionFrameRateRange();
        Log.i(TAG, VList + "==============");
//    
        VideoResolutionFrameRate v = VList.get(0);
        KeyManager.getInstance().setValue(KeyTools.createKey(CameraKey.KeyVideoResolutionFrameRate), v, new CommonCallbacks.CompletionCallback() {
            @Override
            public void onSuccess() {
                Log.i(TAG, "设置镜头分辨率和帧率成功：" + v);
            }

            @Override
            public void onFailure(@NonNull IDJIError idjiError) {
                Log.i(TAG, "设置镜头分辨率和帧率失败：" + idjiError);
            }
        });
//        设置码率,降低码率
        KeyManager.getInstance().setValue(KeyTools.createKey(CameraKey.KeyVideoBitrateMode), VideoBitrateMode.VBR, new CommonCallbacks.CompletionCallback() {
            @Override
            public void onSuccess() {
                Log.i(TAG, "相机码率设置VBR成功");
            }

            @Override
            public void onFailure(@NonNull IDJIError idjiError) {
                Log.i(TAG, "相机码率设置VBR失败");
            }
        });
    }

    //        initSDK,初始化和登录
    private void init_re_SDK() {
        int re1 = manager.initSDK(fileUtil.getLocalIpAddress());
        isInitSDK = re1 == 0 ? true : false;
        Log.i(TAG, "initSDK:" + re1);

        LogToFile.init(this);

        int re2 = manager.registerSDK("183.62.9.189", 15060);
//                开心跳包
        fileUtil.startHeartBeatTask();
        Log.i(TAG, "registerSDK:" + re2);
//                开启监听
        GS28181SDKManager.getInstance().setListenerServer(new MGS28181Listener());
    }

    //计算PT
    private void countPT(float pitch, float yaw, float debounceThresholdYaw, float debounceThresholdPitch) {
        if (Math.abs(Math.abs(yaw) - Math.abs(this.preFrameYaw)) > debounceThresholdYaw) {
            this.preFrameYaw = yaw;
        }

        if (Math.abs(Math.abs(pitch) - Math.abs(this.preFramePitch)) > debounceThresholdPitch) {
            this.preFramePitch = pitch;
        }
    }

    //    创建数据帧监听
    private void initReceiveStreamListener() {
        sendVideoStreamListener = new ICameraStreamManager.ReceiveStreamListener() {
            @Override
            public void onReceiveStream(@NonNull byte[] data, int offset, int length, @NonNull StreamInfo info) {
                if (continueSendVideo && data != null) {
                    startTime = System.currentTimeMillis();
                    Log.i(TAG, startTime + "-----发送的时间--开始");

                    if (info.getMimeType() == ICameraStreamManager.MimeType.H264) {
                        int re = manager.sendVideoStream(System.currentTimeMillis(), info.isKeyFrame() ? 1 : FileUtil.getFrameType(data), data);
                        Log.i(TAG, "发送视频流sendVideoStream:" + re);
                    } else {
                        int re = manager.sendVideoStreamH265(System.currentTimeMillis(), info.isKeyFrame() ? 1 : FileUtil.getFrameType(data), data);
                        Log.i(TAG, "发送视频流sendVideoStreamH265:" + re);
                    }

                    endTime = System.currentTimeMillis();
                    Log.i(TAG, "发送的时间--结束-----" + endTime);
//                    推太快就等待直到满足33ms推一帧，即30的帧率
                    long delay = startTime - endTime;
                    if (delay < 40) {
                        Log.i(TAG, "推太快，用时-----" + delay);
                        try {
                            //将上一次的推流耗时近似当做下一次的耗时，也就是两帧之间是（40-delay）+下次网络推流用时delay，即保证每两帧间隔40ms
                            Thread.sleep((long) 40 - delay);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                } else {
                    if (continueSendVideo) {
                        Log.i(TAG, "发送视频流sendVideoStream:" + "已开启");
                    } else {
                        Log.i(TAG, "发送视频流sendVideoStream:" + "数据为空");
                    }
                }
                if (!continueSendVideo) {
                    cameraManager.removeReceiveStreamListener(sendVideoStreamListener);
                }
            }
        };

        sendVideoWithARInfoListener = new ICameraStreamManager.ReceiveStreamListener() {
            @Override
            public void onReceiveStream(@NonNull byte[] data, int offset, int length, @NonNull StreamInfo info) {
//                Log.i("缓存测试",countD.incrementAndGet()+"-----------大疆获取--------"+System.currentTimeMillis());
                if (continueSendVideo && data != null) {

//                    fileUtil.enqueueFrameBuffer(data, data.length, info.isKeyFrame() ? 1 : FileUtil.getFrameType(data));
//
//                    if (info.getMimeType() == ICameraStreamManager.MimeType.H264) {
//                        mimeType = 4;
//                    } else {
//                        mimeType = 5;
//                    }

                    startTime = System.currentTimeMillis();
                    Log.i(TAG, startTime + "-----发送的时间--开始");

                    countPT(Float.parseFloat(Movement.getInstance().getGimbalPitch()),
                            Float.parseFloat(Movement.getInstance().getGimbalYaw()),
                            debounceThresholdYaw, debounceThresholdPitch);
                    //补偿值
                    float comP = 0.0f;
                    float comY = 0.0f;
                    if (isCompensate){
                        comP = compensatePitch;
                        comY = compensateYaw;
                    }
                    if (info.getMimeType() == ICameraStreamManager.MimeType.H264) {
                        int re = manager.sendVideoWithARInfo(System.currentTimeMillis(), info.isKeyFrame() ? 1 : FileUtil.getFrameType(data), data,
                                Float.parseFloat(Movement.getInstance().getGimbalRoll()),
                                preFramePitch + comP,
                                preFrameYaw + comY,
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
                        int re = manager.sendVideoWithARInfoH265(System.currentTimeMillis(), info.isKeyFrame() ? 1 : FileUtil.getFrameType(data), data,
                                Float.parseFloat(Movement.getInstance().getGimbalRoll()),
                                preFramePitch,
                                preFrameYaw,
                                Float.parseFloat(Movement.getInstance().getCurrentLongitude()),
                                Float.parseFloat(Movement.getInstance().getCurrentLatitude()),
                                Movement.getInstance().getCurrentAltitude());
                        Log.i(TAG, "发送完整的帧 + 摄像机姿态信息（ AR 信息）sendVideoWithARInfoH265:" + re);
                    }

                    endTime = System.currentTimeMillis();
                    Log.i(TAG, "发送的时间--结束-----" + endTime);
//                    推太快就等待直到满足33ms推一帧，即30的帧率
                    delay = startTime - endTime;
                    if (delay < 40) {
                        Log.i(TAG, "推太快，用时-----" + delay);
                        try {
                            //将上一次的推流耗时近似当做下一次的耗时，也就是两帧之间是（40-delay）+下次网络推流用时delay，即保证每两帧间隔40ms
                            Thread.sleep((long) 40 - delay);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                } else {
                    if (continueSendVideo) {
                        Log.i(TAG, "发送完整的帧 + 摄像机姿态信息（ AR 信息）sendVideoWithARInfo:" + "已开启");
                    } else {
                        Log.i(TAG, "发送完整的帧 + 摄像机姿态信息（ AR 信息）sendVideoWithARInfo:" + "数据为空");
                    }
                }
                if (!continueSendVideo) {
                    cameraManager.removeReceiveStreamListener(sendVideoWithARInfoListener);
                }
            }
        };
        sendVideoWithARInfoXListener = new ICameraStreamManager.ReceiveStreamListener() {
            @Override
            public void onReceiveStream(@NonNull byte[] data, int offset, int length, @NonNull StreamInfo info) {
                if (continueSendVideo && data != null) {

                    startTime = System.currentTimeMillis();
                    Log.i(TAG, startTime + "-----发送的时间--开始");
                    Log.i(TAG, "用时多久多久多久===========" + (startTime - endTime) + "====时间:" + info.getPresentationTimeMs());
                    countPT(Float.parseFloat(Movement.getInstance().getGimbalPitch()),
                            Float.parseFloat(Movement.getInstance().getGimbalYaw()),
                            debounceThresholdYaw, debounceThresholdPitch);
                    //补偿值
                    float comP = 0.0f;
                    float comY = 0.0f;
                    if (isCompensate){
                        comP = compensatePitch;
                        comY = compensateYaw;
                    }
                    if (info.getMimeType() == ICameraStreamManager.MimeType.H264) {
                        int re = manager.sendVideoWithARInfoX(System.currentTimeMillis(), info.isKeyFrame() ? 1 : FileUtil.getFrameType(data), data,
                                Float.parseFloat(String.valueOf(Movement.getInstance().getCameraZoomRatios())),//double转float
                                preFramePitch+comP, preFrameYaw+comY,
                                Movement.getInstance().getAngleH(), Movement.getInstance().getAngleV(),
                                Float.parseFloat(Movement.getInstance().getCurrentLongitude()),
                                Float.parseFloat(Movement.getInstance().getCurrentLatitude()),
                                Movement.getInstance().getCurrentAltitude());
                        Log.i(TAG, "发送 视频流与 AR信息流 上传sendVideoWithARInfoX:" + re);
                    } else {
                        int re = manager.sendVideoWithARInfoXH265(System.currentTimeMillis(), info.isKeyFrame() ? 1 : FileUtil.getFrameType(data), data,
                                Float.parseFloat(String.valueOf(Movement.getInstance().getCameraZoomRatios())),//double转float
                                preFramePitch+comP, preFrameYaw+comY,
                                Movement.getInstance().getAngleH(), Movement.getInstance().getAngleV(),
                                Float.parseFloat(Movement.getInstance().getCurrentLongitude()),
                                Float.parseFloat(Movement.getInstance().getCurrentLatitude()),
                                Movement.getInstance().getCurrentAltitude());
                        Log.i(TAG, "发送 视频流与 AR信息流 上传sendVideoWithARInfoXH265:" + re);
                    }

                    endTime = System.currentTimeMillis();
                    Log.i(TAG, "发送的时间--结束-----" + endTime);
//                    推太快就等待直到满足33ms推一帧，即30的帧率
                    long delay = startTime - endTime;
                    if (delay < 42) {
                        Log.i(TAG, "推太快，用时-----" + delay);
                        try {
                            //将上一次的推流耗时近似当做下一次的耗时，也就是两帧之间是（40-delay）+下次网络推流用时delay，即保证每两帧间隔40ms
                            Thread.sleep((long) 42 - delay);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

//                    curTime = System.currentTimeMillis();
//                    if (curTime - lastTime>44 + delay){
//                        startTime = System.currentTimeMillis();
//                        Log.i(TAG, startTime + "-----发送的时间--开始");
//                        Log.i(TAG,"用时多久多久多久==========="+(curTime-lastTime)+"====时间:"+info.getPresentationTimeMs());
//                        cameraControllerUtil.sendVideoWithARInfoXFun(preFramePitch,preFrameYaw,data,info);
//                        endTime = System.currentTimeMillis();
//                        Log.i(TAG, "发送的时间--结束-----" + endTime);
//                        delay = endTime - startTime;
//                        lastTime = System.currentTimeMillis();
//                    }
                } else {
                    if (continueSendVideo) {
                        Log.i(TAG, "发送 视频流与 AR信息流 上传sendVideoWithARInfoX:" + "已开启");
                    } else {
                        Log.i(TAG, "发送 视频流与 AR信息流 上传sendVideoWithARInfoX:" + "数据为空");
                    }
                }
                if (!continueSendVideo) {
                    cameraManager.removeReceiveStreamListener(sendVideoWithARInfoXListener);
                }
            }
        };
        sendVideoWithARInfoToLocalListener = new ICameraStreamManager.ReceiveStreamListener() {
            @Override
            public void onReceiveStream(@NonNull byte[] data, int offset, int length, @NonNull StreamInfo info) {

                if (continueSendVideo && data != null) {

                    fileUtil.enqueueFrameBuffer(data, data.length, info.isKeyFrame() ? 1 : FileUtil.getFrameType(data));

                    if (info.getMimeType() == ICameraStreamManager.MimeType.H264) {
                        mimeType = 4;
                    } else {
                        mimeType = 5;
                    }
//                    countPT(Float.parseFloat(Movement.getInstance().getGimbalPitch()),
//                            Float.parseFloat(Movement.getInstance().getGimbalYaw()),
//                            debounceThresholdYaw, debounceThresholdPitch);
//                    if (info.getMimeType() == ICameraStreamManager.MimeType.H264) {
//                        Log.i(TAG, "h264格式");
//                        int re = manager.sendVideoWithARInfoToLocal(System.currentTimeMillis(), info.isKeyFrame() ? 1 : FileUtil.getFrameType(data), data,
//                                Float.parseFloat(String.valueOf(Movement.getInstance().getCameraZoomRatios())),//double转float
//                                preFramePitch,
//                                preFrameYaw,
//                                Movement.getInstance().getAngleH(), Movement.getInstance().getAngleV(),
//                                Float.parseFloat(Movement.getInstance().getCurrentLongitude()),
//                                Float.parseFloat(Movement.getInstance().getCurrentLatitude()),
//                                Movement.getInstance().getAltitude(), absolutePath + "/111.h264");
//                        Log.i(TAG, "传输视频流与 AR 信息流，直接上传到指定路径 sendVideoWithARInfoToLocal:re=" + re + "。。" + info.isKeyFrame() + FileUtil.getFrameType(data) + FrameUtil.INSTANCE.checkFrameType(data));
//                    } else {
//                        int re = manager.sendVideoWithARInfoToLocalH265(System.currentTimeMillis(), info.isKeyFrame() ? 1 : FileUtil.getFrameType(data), data,
//                                Float.parseFloat(String.valueOf(Movement.getInstance().getCameraZoomRatios())),//double转float
//                                Float.parseFloat(Movement.getInstance().getGimbalPitch()), Float.parseFloat(Movement.getInstance().getGimbalYaw()),
//                                Movement.getInstance().getAngleH(), Movement.getInstance().getAngleV(),
//                                Float.parseFloat(Movement.getInstance().getCurrentLongitude()),
//                                Float.parseFloat(Movement.getInstance().getCurrentLatitude()),
//                                Movement.getInstance().getAltitude(), absolutePath + "/111.h265");
//                        Log.i(TAG, "传输视频流与 AR 信息流，直接上传到指定路径 sendVideoWithARInfoToLocalH265:" + absolutePath + re);
//                    }
                } else {
                    if (continueSendVideo) {
                        Log.i(TAG, "传输视频流与 AR 信息流，直接上传到指定路径 sendVideoWithARInfoToLocal:" + "已开启");
                    } else {
                        Log.i(TAG, "传输视频流与 AR 信息流，直接上传到指定路径 sendVideoWithARInfoToLocal:" + "数据为空");
                    }
                }
                if (!continueSendVideo) {
                    cameraManager.removeReceiveStreamListener(sendVideoWithARInfoToLocalListener);
                }
            }
        };
    }



    //设置发流按钮可用状态
    private void setEnableSendStream() {
        spinner.setEnabled(!continueSendVideo);
    }

    Thread thread = null;

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_right:
//                new MGS28181Listener().onZoomControl(Types.ZOOM_IN_CTRL,1080,980,98,99,580,300);
//                new MGS28181Listener().onPTZControl(1, Types.PTZ_LEFT, 4);

                mDrawerLayout.openDrawer(drawer_right);
                break;
            case R.id.compensateBtn:
                if (isCompensate){
                    isCompensate = false;//设置关闭状态
                    compensateBtn.setText(R.string.startCompensate);//设置为提示“开启补偿”的文字
                    editPitch.setEnabled(true);
                    editYaw.setEnabled(true);
                    break;
                }
                if (editYaw.getText().toString().length() < 1 || editPitch.getText().toString().length() < 1){
                    Toast.makeText(this, "输入补偿值", Toast.LENGTH_SHORT).show();
                    break;
                }
                isCompensate = true;
                editPitch.setEnabled(false);
                editYaw.setEnabled(false);
                compensateBtn.setText(R.string.stopCompensate);//设置为提示“关闭补偿”的文字
                compensateYaw = Float.parseFloat(editYaw.getText().toString());
                compensatePitch = Float.parseFloat(editPitch.getText().toString());
                break;
            case R.id.sendVideoStreamBtn:
                if (continueSendVideo)//正在发流，点击结束发流
                {
//                    设置按钮文字为可以点击开始样式
                    sendVideoStreamBtn.setText(getResources().getString(R.string.startSendVedioStream));
//                  设置发流状态和按钮可用状态
                    continueSendVideo = false;
                    setEnableSendStream();
//                  中断写入指定路径
                    manager.stopWriteStream();
//                  停止发流线程
                    if (thread != null)
                        thread.interrupt();
                    fileUtil.onClear();//
                    break;
                }
                //获取第一次发流的时间
                lastTime = System.currentTimeMillis();
//                设置按钮文字可以点击结束样式
                sendVideoStreamBtn.setText(getResources().getString(R.string.stopSendVedioStream));
                switch (vedioPos) {
                    case 0://sendVideoStream
                        if (continueSendVideo) {
                            return;
                        }
//                      设置发流状态和按钮可用状态
                        continueSendVideo = true;
                        setEnableSendStream();
//                      监听指定相机的帧数据
//                      发送视频流
                        cameraManager.addReceiveStreamListener(componentIndexType, sendVideoStreamListener);
                        break;
                    case 1://sendVideoWithARInfo
                        if (continueSendVideo) {
                            return;
                        }
//                      设置发流状态和按钮可用状态
                        continueSendVideo = true;
                        setEnableSendStream();
//                      监听指定相机的帧数据
//                      发送视频流
                        cameraManager.addReceiveStreamListener(componentIndexType, sendVideoWithARInfoListener);
//                      启动发流线程
//                        videoStreamThread.setStartListen(new VideoStreamThread.SendVideoStream() {
//                            @Override
//                            public void sendVideoStreamFun() {
//
//                                videoStreamThread.sendVideoWithARInfoFun(mimeType);
//                            }
//                        });
//                        thread = new Thread(videoStreamThread);
//                        thread.start();
                        break;
                    case 2://sendVideoWithARInfoX
                        if (continueSendVideo) {
                            return;
                        }
//                      设置发流状态和按钮可用状态
                        continueSendVideo = true;
                        setEnableSendStream();
//                      监听指定相机的帧数据
//                      发送视频流
                        cameraManager.addReceiveStreamListener(componentIndexType, sendVideoWithARInfoXListener);
                        break;
                    case 3://sendVideoWithARInfoToLocal
                        if (continueSendVideo) {
                            return;
                        }
//                      设置发流状态和按钮可用状态
                        continueSendVideo = true;
                        setEnableSendStream();
//                      监听指定相机的帧数据
//                      发送视频流
//                        cameraManager.addFrameListener(ComponentIndexType.LEFT_OR_MAIN,ICameraStreamManager.FrameFormat.YUV420_888,listener1);
                        cameraManager.addReceiveStreamListener(ComponentIndexType.FPV, sendVideoWithARInfoToLocalListener);
//                      启动发流线程
                        videoStreamThread.setStartListen(new VideoStreamThread.SendVideoStream() {
                            @Override
                            public void sendVideoStreamFun() {
                                videoStreamThread.sendVideoWithARInfoToLocalFun(mimeType);
                            }
                        });
                        thread = new Thread(videoStreamThread);
                        thread.start();
                        break;
                }
                break;
        }
    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
//        isInitSDK = false;
        manager.uninitSDK();
        Jni28181AgentSDK.getInstance().unregister();
        fileUtil.stopHeartBeatTask();//停心跳包
//        停发流
        manager.stopWriteStream();
//        移除数据帧监听
        cameraManager.removeReceiveStreamListener(sendVideoWithARInfoToLocalListener);
        cameraManager.removeReceiveStreamListener(sendVideoWithARInfoListener);
        cameraManager.removeReceiveStreamListener(sendVideoStreamListener);
        cameraManager.removeReceiveStreamListener(sendVideoWithARInfoXListener);
//        cameraManager.removeCameraStreamSurface(surface.getHolder().getSurface());
//        移除监听回调
        GS28181SDKManager.getInstance().setListenerServer(null);
        //           将所有的Callbacks和Messages全部清除掉,移除Handler中所有的消息和回调，避免内存泄露
        handler.removeCallbacksAndMessages(null);
        handler = null;
        fileUtil.onClear();
    }


}
/**
 * 变焦倍率获取
 * 发流等待时间
 * yaw的坐标北东地
 */