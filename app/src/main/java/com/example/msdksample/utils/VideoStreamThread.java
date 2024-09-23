package com.example.msdksample.utils;

import android.os.Environment;
import android.util.Log;

import com.example.msdksample.MyLayoutActivity;
import com.example.msdksample.entity.H264Frame;
import com.example.msdksample.entity.Movement;
import com.gosuncn.lib28181agent.GS28181SDKManager;

import dji.v5.manager.interfaces.ICameraStreamManager;

public class VideoStreamThread implements Runnable {

    private SendVideoStream mSendVideoStream;
    private FileUtil mFileUtil;
    private final String TAG = "VideoStreamThread";


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
            //推太快就等待
            long delay = startTime - endTime;
            if (delay < 42) {
                Log.i(TAG, "推太快，用时-----" + delay);
                try {
                    //将上一次的推流耗时近似当做下一次的耗时，也就是两帧之间是（40-delay）+下次网络推流用时delay，即保证每两帧间隔40ms
                    Thread.sleep((long) 42 - delay);
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


    public void onClearEnqueue(){
        mFileUtil.onClear();
    }
    public void onDestroy(){
        Thread.currentThread().interrupt();
    }
}

