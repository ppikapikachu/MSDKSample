package com.example.msdksample.utils;

import android.os.Environment;
import android.util.Log;

import com.example.msdksample.MyLayoutActivity;
import com.example.msdksample.entity.H264Frame;
import com.example.msdksample.entity.Movement;
import com.gosuncn.lib28181agent.GS28181SDKManager;

public class VideoStreamThread implements Runnable {

    private SendVideoStream mSendVideoStream;
    private FileUtil mFileUtil;
    private final String TAG = "VideoStreamThread";
    private GS28181SDKManager manager = GS28181SDKManager.getInstance();
    private String absolutePath = Environment.getExternalStorageDirectory().getAbsolutePath();
    //    防抖相关
    private float debounceThresholdPitch = 0.0f;
    private float debounceThresholdYaw = 0.0f;
    private float preFrameYaw ;
    private float preFramePitch ;

    public VideoStreamThread(FileUtil mFileUtil) {
        this.mFileUtil = mFileUtil;
    }

    @Override
    public void run() {
        long startTime;
        long endTime;
        while (!Thread.currentThread().isInterrupted()) { //   终结线程
            startTime = System.currentTimeMillis();
            Log.i(TAG, startTime + "-----发送的时间--开始");
            mSendVideoStream.sendVideoStreamFun();
            endTime = System.currentTimeMillis();
            Log.i(TAG, "发送的时间--结束-----" + endTime);
            //推太快就等待直到满足33ms推一帧，即30的帧率
            long delay = endTime - startTime;
            if (delay < 40) {
                Log.i(TAG, "推太快，用时-----" + delay);
                try {
                    //将上一次的推流耗时近似当做下一次的耗时，也就是两帧之间是（40-delay）+下次网络推流用时delay，即保证每两帧间隔40ms
                    Thread.sleep((long) 40 - delay);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    e.printStackTrace();
                }
            }

        }
    }

    public void setStartListen(SendVideoStream sendVideoStream) {
        mSendVideoStream = sendVideoStream;
    }

    public interface SendVideoStream {
        void sendVideoStreamFun();
    }
    public void init(float debounceThresholdPitch,float debounceThresholdYaw){
        preFramePitch = Float.parseFloat(Movement.getInstance().getGimbalPitch());
        preFrameYaw = Float.parseFloat(Movement.getInstance().getGimbalYaw());
        this.debounceThresholdPitch = debounceThresholdPitch ;
        this.debounceThresholdYaw =debounceThresholdYaw ;
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

    public void sendVideoWithARInfoFun(int mimeType) {
        int re;
        H264Frame enqueueFrame = mFileUtil.getEnqueueFrame();
        if (enqueueFrame == null) {
            return;
        }
        countPT(Float.parseFloat(Movement.getInstance().getGimbalPitch()),
                Float.parseFloat(Movement.getInstance().getGimbalYaw()),
                debounceThresholdYaw, debounceThresholdPitch);
        if (mimeType == 4) {
            re = manager.sendVideoWithARInfo(enqueueFrame.getTimestamp(), enqueueFrame.getFrameType(), enqueueFrame.getFrameData(),
                    Float.parseFloat(Movement.getInstance().getGimbalRoll()),
                    preFramePitch,
                    preFrameYaw,
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
            re = manager.sendVideoWithARInfoH265(enqueueFrame.getTimestamp(), enqueueFrame.getFrameType(), enqueueFrame.getFrameData(),
                    Float.parseFloat(Movement.getInstance().getGimbalRoll()),
                    Float.parseFloat(Movement.getInstance().getGimbalPitch()),
                    Float.parseFloat(Movement.getInstance().getGimbalYaw()),
                    Float.parseFloat(Movement.getInstance().getCurrentLongitude()),
                    Float.parseFloat(Movement.getInstance().getCurrentLatitude()),
                    Movement.getInstance().getCurrentAltitude());
            Log.i(TAG, "发送完整的帧 + 摄像机姿态信息（ AR 信息）sendVideoWithARInfoH265:" + re);
        }
    }
    public void sendVideoWithARInfoToLocalFun(int mimeType) {
        int re;
        H264Frame enqueueFrame = mFileUtil.getEnqueueFrame();
        if (enqueueFrame == null) {
            return;
        }
        countPT(Float.parseFloat(Movement.getInstance().getGimbalPitch()),
                Float.parseFloat(Movement.getInstance().getGimbalYaw()),
                debounceThresholdYaw, debounceThresholdPitch);
        if (mimeType == 4) {
            Log.i(TAG, "h264格式");
            re = manager.sendVideoWithARInfoToLocal(enqueueFrame.getTimestamp(), enqueueFrame.getFrameType(), enqueueFrame.getFrameData(),
                    Float.parseFloat(String.valueOf(Movement.getInstance().getCameraZoomRatios())),//double转float
                    preFramePitch,
                    preFrameYaw,
                    Movement.getInstance().getAngleH(), Movement.getInstance().getAngleV(),
                    Float.parseFloat(Movement.getInstance().getCurrentLongitude()),
                    Float.parseFloat(Movement.getInstance().getCurrentLatitude()),
                    Movement.getInstance().getCurrentAltitude(), absolutePath + "/111.h264");
            Log.i(TAG, "传输视频流与 AR 信息流，直接上传到指定路径 sendVideoWithARInfoToLocal:re=" + re);
        } else {
            re = manager.sendVideoWithARInfoToLocalH265(enqueueFrame.getTimestamp(), enqueueFrame.getFrameType(), enqueueFrame.getFrameData(),
                    Float.parseFloat(String.valueOf(Movement.getInstance().getCameraZoomRatios())),//double转float
                    preFramePitch,
                    preFrameYaw,
                    Movement.getInstance().getAngleH(), Movement.getInstance().getAngleV(),
                    Float.parseFloat(Movement.getInstance().getCurrentLongitude()),
                    Float.parseFloat(Movement.getInstance().getCurrentLatitude()),
                    Movement.getInstance().getCurrentAltitude(), absolutePath + "/111.h265");
            Log.i(TAG, "传输视频流与 AR 信息流，直接上传到指定路径 sendVideoWithARInfoToLocalH265:" + absolutePath + re);
        }
//        没成功则清空缓存
//        if (re != 0 || re != 1000) {
//            mFileUtil.onClear();
//        }
    }

    public void onClearEnqueue(){
        mFileUtil.onClear();
    }
    public void onDestroy(){
        Thread.currentThread().interrupt();
    }
}

