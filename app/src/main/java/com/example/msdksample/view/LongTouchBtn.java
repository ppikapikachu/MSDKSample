package com.example.msdksample.view;

import android.app.Activity;
import android.content.Context;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.msdksample.R;
import com.example.msdksample.entity.Movement;
import com.example.msdksample.utils.CameraControllerUtil;
import com.example.msdksample.utils.FileUtil;
import com.example.msdksample.utils.VideoStreamThread;
import com.gosuncn.lib28181agent.GS28181SDKManager;
import com.gosuncn.lib28181agent.Jni28181AgentSDK;

import java.util.concurrent.atomic.AtomicInteger;

import dji.sdk.keyvalue.value.common.ComponentIndexType;
import dji.v5.manager.datacenter.MediaDataCenter;
import dji.v5.manager.datacenter.camera.StreamInfo;
import dji.v5.manager.interfaces.ICameraStreamManager;

public class LongTouchBtn implements View.OnClickListener, View.OnTouchListener {

    private ImageButton btn_right = null;
    private DrawerLayout mDrawerLayout = null;
    private LinearLayout drawer_right = null;
    private RelativeLayout llTouch;//屏幕点击

    private GS28181SDKManager manager = GS28181SDKManager.getInstance();
    private String TAG = getClass().getSimpleName();
    //    帧数据相关
    private ICameraStreamManager cameraManager = MediaDataCenter.getInstance().getCameraStreamManager();
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

    private EditText editYaw;
    private EditText editPitch;
    private Button compensateBtn = null;
    private Boolean isCompensate = false;//是否开启补偿
    //    工具类
    FileUtil fileUtil = FileUtil.getInstance();
    private CameraControllerUtil cameraControllerUtil = CameraControllerUtil.getInstance();
    //    指点移动云台部分
    int width;
    int height;

    Activity activity = null;

    public LongTouchBtn(Activity activity) {
        this.activity = activity;
    }

    private Button btn = null;

    private void init() {

        btn_right = activity.findViewById(R.id.btn_right);
        mDrawerLayout = activity.findViewById(R.id.drawer_layout);
        drawer_right = activity.findViewById(R.id.drawer_right);

        spinner = activity.findViewById(R.id.sendVedioStreamFun);
        sendVideoStreamBtn = activity.findViewById(R.id.sendVideoStreamBtn);
        llTouch = activity.findViewById(R.id.relative_layout);
        videoStreamThread = new VideoStreamThread(fileUtil);//发流线程实现类
        editYaw = activity.findViewById(R.id.et_yaw_compensation);//水平补偿输入
        editPitch = activity.findViewById(R.id.et_pitch_compensation);//垂直补偿输入
        compensateBtn = activity.findViewById(R.id.compensateBtn);
//        videoStreamThread.init(debounceThresholdPitch, debounceThresholdYaw);//初始化防抖阈值

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

        DisplayMetrics displayMetrics = activity.getResources()
                .getDisplayMetrics();
        // 获取屏幕像素宽度 px
        width = displayMetrics.widthPixels;
        Movement.getInstance().setWidth(width);
        // 获取屏幕像素高度 px
        height = displayMetrics.heightPixels;
        Movement.getInstance().setHeight(height);
    }


    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        return false;
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

                    cameraControllerUtil.countPT(Float.parseFloat(Movement.getInstance().getGimbalPitch()),
                            Float.parseFloat(Movement.getInstance().getGimbalYaw()),
                            cameraControllerUtil.debounceThresholdYaw, cameraControllerUtil.debounceThresholdPitch);
                    if (info.getMimeType() == ICameraStreamManager.MimeType.H264) {
                        int re = manager.sendVideoWithARInfo(System.currentTimeMillis(), info.isKeyFrame() ? 1 : FileUtil.getFrameType(data), data,
                                Float.parseFloat(Movement.getInstance().getGimbalRoll()),
                                cameraControllerUtil.preFramePitch + cameraControllerUtil.compensatePitch,
                                cameraControllerUtil.preFrameYaw + cameraControllerUtil.compensateYaw,
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
                                cameraControllerUtil.preFramePitch + cameraControllerUtil.compensatePitch,
                                cameraControllerUtil.preFrameYaw + cameraControllerUtil.compensateYaw,
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
                    fileUtil.enqueueFrameBuffer(data, data.length, info.isKeyFrame() ? 1 : FileUtil.getFrameType(data));

                    if (info.getMimeType() == ICameraStreamManager.MimeType.H264) {
                        mimeType = 4;
                    } else {
                        mimeType = 5;
                    }

//                    startTime = System.currentTimeMillis();
//                    Log.i(TAG, startTime + "-----发送的时间--开始");
//                    Log.i(TAG, "用时多久多久多久===========" + (startTime - endTime) + "====时间:" + info.getPresentationTimeMs());
//                    countPT(Float.parseFloat(Movement.getInstance().getGimbalPitch()),
//                            Float.parseFloat(Movement.getInstance().getGimbalYaw()),
//                            debounceThresholdYaw, debounceThresholdPitch);
//                    if (info.getMimeType() == ICameraStreamManager.MimeType.H264) {
//                        int re = manager.sendVideoWithARInfoX(System.currentTimeMillis(), info.isKeyFrame() ? 1 : FileUtil.getFrameType(data), data,
//                                Float.parseFloat(String.valueOf(Movement.getInstance().getCameraZoomRatios())),//double转float
//                                preFramePitch+compensatePitch, preFrameYaw+compensateYaw,
//                                Movement.getInstance().getAngleH(), Movement.getInstance().getAngleV(),
//                                Float.parseFloat(Movement.getInstance().getCurrentLongitude()),
//                                Float.parseFloat(Movement.getInstance().getCurrentLatitude()),
//                                Movement.getInstance().getCurrentAltitude());
//                        Log.i(TAG, "发送 视频流与 AR信息流 上传sendVideoWithARInfoX:" + re);
//                    } else {
//                        int re = manager.sendVideoWithARInfoXH265(System.currentTimeMillis(), info.isKeyFrame() ? 1 : FileUtil.getFrameType(data), data,
//                                Float.parseFloat(String.valueOf(Movement.getInstance().getCameraZoomRatios())),//double转float
//                                preFramePitch+compensatePitch, preFrameYaw+compensateYaw,
//                                Movement.getInstance().getAngleH(), Movement.getInstance().getAngleV(),
//                                Float.parseFloat(Movement.getInstance().getCurrentLongitude()),
//                                Float.parseFloat(Movement.getInstance().getCurrentLatitude()),
//                                Movement.getInstance().getCurrentAltitude());
//                        Log.i(TAG, "发送 视频流与 AR信息流 上传sendVideoWithARInfoXH265:" + re);
//                    }

//                    endTime = System.currentTimeMillis();
//                    Log.i(TAG, "发送的时间--结束-----" + endTime);
////                    推太快就等待直到满足33ms推一帧，即30的帧率
//                    long delay = startTime - endTime;
//                    if (delay < 42) {
//                        Log.i(TAG, "推太快，用时-----" + delay);
//                        try {
//                            //将上一次的推流耗时近似当做下一次的耗时，也就是两帧之间是（40-delay）+下次网络推流用时delay，即保证每两帧间隔40ms
//                            Thread.sleep((long) 42 - delay);
//                        } catch (InterruptedException e) {
//                            e.printStackTrace();
//                        }
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
                if (isCompensate) {
                    isCompensate = false;//设置关闭状态
                    compensateBtn.setText(R.string.startCompensate);//设置为提示“开启补偿”的文字
                    editPitch.setEnabled(true);
                    editYaw.setEnabled(true);
                    cameraControllerUtil.compensateYaw = 0.0f;//设置为0，不补偿
                    cameraControllerUtil.compensatePitch = 0.0f;
                    break;
                }
                if (editYaw.getText().toString().length() < 1 || editPitch.getText().toString().length() < 1) {
                    Toast.makeText(activity, "输入补偿值", Toast.LENGTH_SHORT).show();
                    break;
                }
                isCompensate = true;
                editPitch.setEnabled(false);
                editYaw.setEnabled(false);
                compensateBtn.setText(R.string.stopCompensate);//设置为提示“关闭补偿”的文字
                cameraControllerUtil.compensateYaw = Float.parseFloat(editYaw.getText().toString());
                cameraControllerUtil.compensatePitch = Float.parseFloat(editPitch.getText().toString());
                break;
            case R.id.sendVideoStreamBtn:
                if (continueSendVideo)//正在发流，点击结束发流
                {
//                    设置按钮文字为可以点击开始样式
                    sendVideoStreamBtn.setText(activity.getResources().getString(R.string.startSendVedioStream));
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
                fileUtil.onClear();//开始发流前清空栈
                //获取第一次发流的时间
                lastTime = System.currentTimeMillis();
//                设置按钮文字可以点击结束样式
                sendVideoStreamBtn.setText(activity.getResources().getString(R.string.stopSendVedioStream));
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
                        //启动发流线程
                        videoStreamThread.setStartListen(new VideoStreamThread.SendVideoStream() {
                            @Override
                            public void sendVideoStreamFun() {
                                cameraControllerUtil.sendVideoWithARInfoXFun(mimeType);
                            }
                        });
                        thread = new Thread(videoStreamThread);
                        thread.start();
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
                                cameraControllerUtil.sendVideoWithARInfoToLocalFun(mimeType);
                            }
                        });
                        thread = new Thread(videoStreamThread);
                        thread.start();
                        break;
                }
                break;
        }
    }

    //结束
    protected void onDestroy() {
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
//        移除监听回调
        GS28181SDKManager.getInstance().setListenerServer(null);
        fileUtil.onDestroy();
    }
}
