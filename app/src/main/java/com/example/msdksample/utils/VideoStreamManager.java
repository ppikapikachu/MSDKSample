//package com.example.msdksample.utils;
//
//import android.media.MediaRecorder;
//import android.os.Handler;
//import android.os.HandlerThread;
//import android.text.format.DateFormat;
//
//import androidx.lifecycle.Lifecycle;
//import androidx.lifecycle.LifecycleObserver;
//import androidx.lifecycle.LifecycleOwner;
//import androidx.lifecycle.OnLifecycleEvent;
//import com.gosuncn.djidemo.bean.H264Frame;
//import com.gosuncn.djidemo.bus.EventRxBus;
//import com.gosuncn.djidemo.bus.event.CommonEvent;
//import com.gosuncn.djidemo.bus.event.ComosAndZoomEvent;
//import com.gosuncn.djidemo.common.GlobleContent;
//import com.gosuncn.djidemo.listeners.ComObserver;
//import com.gosuncn.djidemo.module.gps.AircraftGpsInfo;
//import com.gosuncn.djidemo.utils.FilePathUtil;
//import com.gosuncn.djidemo.utils.ModuleVerificationUtil;
//import com.gosuncn.djidemo.utils.StreamTestUtil;
//import com.gosuncn.glog.ALog;
//import com.gosuncn.lib28181agent.Jni28181AgentSDK;
//import com.gosuncn.lib28181agent.Types;
//
//import java.io.File;
//import java.nio.ByteBuffer;
//import java.util.concurrent.BlockingQueue;
//import java.util.concurrent.LinkedBlockingQueue;
//import java.util.concurrent.atomic.AtomicBoolean;
//import java.util.concurrent.atomic.AtomicInteger;
//
//import dji.v5.manager.datacenter.MediaDataCenter;
//import dji.v5.manager.interfaces.ICameraStreamManager;
//import io.reactivex.disposables.CompositeDisposable;
//import io.reactivex.disposables.Disposable;
//import static com.gosuncn.lib28181agent.Types.I_FRAME;
//import static com.gosuncn.lib28181agent.Types.P_FRAME;
//
//public class VideoStreamManager implements LifecycleObserver,Runnable{
//
//    private final String TAG = "VideoStreamManager";
//
//    private final int BUFFER_QUEUE_SIZE = 120;
//    private BlockingQueue<H264Frame> frameQueue = new LinkedBlockingQueue<>(BUFFER_QUEUE_SIZE);
//
//    // 帧数据缓存池，每次回调的大小基本为2032
//    private final int FRAME_BUFFER_SIZE = 1024 * 1024;   // 最大不能超过1024*1024（1M），当前申请150K
//    private final int FILE_BUFFER_SIZE = 1024 * 1024 * 30;
//    private ByteBuffer frameBuffer = ByteBuffer.allocate(FRAME_BUFFER_SIZE);
//    private ByteBuffer outputBuffer = null;
//
//    private AtomicInteger currentFrameType = new AtomicInteger(Types.I_FRAME);
//    private AtomicBoolean isRtpRunning = new AtomicBoolean(false);
//    private AtomicBoolean isRTKUsing = new AtomicBoolean(false); // 当前是否使用RTK
//    private AtomicBoolean isARSupported = new AtomicBoolean(true);  // 云台是否支持AR信息
////    private AtomicBoolean isGyroscopeOpen = new AtomicBoolean(false);  // 云台是否支持AR信息
//
//    private boolean isDestory = false;
//    private boolean needSave = false;
//    private Handler pushHandler = null;
//
//    private float preFrameLongtitude = 0f;
//    private float preFrameLatutide = 0f;
//    // 记录上一帧的PT，用来与当前帧进行比对
//    private float preFrameYaw = 0f;
//    private float preFramePitch = 0f;
//    private static float comosW = -1;
//    private static float comosH = -1;
//    private static int currentZoom = -1;
//    private  float angleH =(float) 69.9;      // 水平视场角
//    private  float angleV = (float) 55.25;        // 垂直视场角
//    private float curFocal = 0;
//    private String tenHexBytePath;
//    private AircraftGpsInfo airGpsInfo;
//
//    private CompositeDisposable mCompositeDisposable;
//
//    private AtomicBoolean localSave = new AtomicBoolean(false);
//    //    private AtomicBoolean islocalRtpRunning = new AtomicBoolean(false);
//    private String BASE_SAVE_PAHT = "/storage/emulated/0/video_output/";
//    //    private Handler localHandler = null;
//    private String saveVideoPath = "";
//
//
//    public VideoStreamManager(LifecycleOwner owner) {
//        if (owner != null) {
//            owner.getLifecycle().addObserver(this);
//        }
//        airGpsInfo = GlobleContent.getInstance().getAirGpsInfoBean();
//
//        HandlerThread handlerThread = new HandlerThread("video_push");
//        handlerThread.start();
//        pushHandler = new Handler(handlerThread.getLooper());
//        initEventRxBus();
//        File file = new File(BASE_SAVE_PAHT);
//        if(!file.exists()){
//            file.mkdir();
//        }
//    }
//
//
//    private void initEventRxBus() {
//        mCompositeDisposable = new CompositeDisposable();
//        EventRxBus.getInstance().getObservable(CommonEvent.class)
//                .subscribe(new ComObserver<CommonEvent>(){
//                    @Override
//                    public void onSubscribe(Disposable d) {
//                        mCompositeDisposable.add(d);
//                    }
//
//                    @Override
//                    public void onNext(CommonEvent commonEvent) {
//                        switch (commonEvent.msgType){
//                            case CommonEvent.MSG_TYPE_AR_SUPPORT:
//                                isARSupported.set((Boolean) commonEvent.data);
//                                break;
//                            case CommonEvent.MSG_TYPE_RTK_STATUS:
//                                isRTKUsing.set((Boolean) commonEvent.data);
//                                break;
//                            case CommonEvent.MSG_TYPE_VIDEO_PUSH:
//                                boolean needPush = (boolean) commonEvent.data;
//                                if (needPush) {
//                                    startPushVideo();
//                                } else {
//                                    stopVideoPush();
//                                }
//                                break;
//                        }
//                    }
//                });
//        if(ModuleVerificationUtil.isM300Product()){
//            return;
//        }
//        EventRxBus.getInstance().getObservable(ComosAndZoomEvent.class)
//                .subscribe(new ComObserver<ComosAndZoomEvent>(){
//
//                    @Override
//                    public void onSubscribe(Disposable d) {
//                        mCompositeDisposable.add(d);
//                    }
//
//                    @Override
//                    public void onNext(ComosAndZoomEvent comosEvent) {
//                        if(comosEvent.currentZoom != -1){
//                            currentZoom = comosEvent.currentZoom / 10;
//                            curFocal = comosEvent.curFocal;
////                            ALog.w(TAG,"   currentZoom::::::::::::::::::::::::::::::::::"+currentZoom);
//                        }
//                        if(comosEvent.comosH != (float) -1){
//                            comosW = comosEvent.comosW;
//                            comosH = comosEvent.comosH;
////                            ALog.w(TAG,"  comosW::::::::::::::::::::::::::::::::::"+comosW+"  comosH::"+comosH);
//                        }
//                        if(comosEvent.sourceType == 0){
//                            angleH = comosEvent.angleH;
//                            angleV =  comosEvent.angleV;
//                        }
//                        if(comosEvent.sourceType != 0){
//                            angleH = (float) (2 * Math.atan(comosW/(2 * currentZoom)) * 360 / 2 / Math.PI);
//                            angleV = (float) (2 * Math.atan(comosH/(2 * currentZoom)) * 360 / 2 / Math.PI);
////                            ALog.w(TAG,"      currentZoom::::::"+currentZoom+"   angleH::::::::::::::::::::::::::::::::::"+angleH+"  angleV::"+angleV);
//                        }
//                    }
//                });
//    }
//
//
//    public void enqueueFrameBuffer(byte[] bufferData, int size) {
//        // 校验接收到的videoBuffer，等待缓存池缓存到一帧再放到缓存队列
//        if (frameBuffer == null || !isRtpRunning.get() || isDestory) {
//            return;
//        }
//        int result = 0;//getEnqueueFrameResult(bufferData, size);
//        if (result >= 0) {
//            byte[] data = new byte[size];
//            //frameBuffer.flip();
//            //frameBuffer.get(data);
//
//            System.arraycopy(bufferData, 0, data, 0, size);
//
//            H264Frame h264Frame = new H264Frame();
//            h264Frame.frameLen = data.length;
//            h264Frame.frameData = data;
//            h264Frame.timestamp = System.currentTimeMillis();
//            h264Frame.frameType = currentFrameType.get();
//
//
////            Log.i(TAG, "当前帧类型（-1：NULL; 1: I_FRAME; 2: P_FRAME）: " + h264Frame.frameType + ", 大小： " + h264Frame.frameLen);
//
//            if (!frameQueue.offer(h264Frame)) {
////                ALog.w("队列已满，添加队列失败，去掉栈顶帧数据再重新添加");
//                frameQueue.poll();
//                frameQueue.offer(h264Frame);
////                ALog.i("线程生成一帧，时间：" + h264Frame.timestamp + "帧长度" + h264Frame.frameLen + "帧类型" + h264Frame.frameType);
//            }
//            data = null;
//            frameBuffer.clear();
//            // 清空之后将当前帧缓存进去
//            frameBuffer.put(bufferData, 0, size);
//            if (result == I_FRAME) {
//                currentFrameType.set(Types.I_FRAME);
//            } else if (result == P_FRAME) {
//                currentFrameType.set(Types.P_FRAME);
//            }
//        }
//    }
//    public void enqueueFrameBuffer(byte[] bufferData,int offset, int size) {
//        // 校验接收到的videoBuffer，等待缓存池缓存到一帧再放到缓存队列
//        if (frameBuffer == null || !isRtpRunning.get() || isDestory) {
//            return;
//        }
//        int result = 0;//getEnqueueFrameResult(bufferData, size);
//        if (result >= 0) {
//            byte[] data = new byte[size];
//            //frameBuffer.flip();
//            //frameBuffer.get(data);
//
//            System.arraycopy(bufferData, offset, data, 0, size);
//            H264Frame h264Frame = new H264Frame();
//            h264Frame.frameLen = data.length;
//            h264Frame.frameData = data;
//            h264Frame.timestamp = System.currentTimeMillis();
//            h264Frame.frameType = currentFrameType.get();
//
////            Log.i(TAG, "当前帧类型（-1：NULL; 1: I_FRAME; 2: P_FRAME）: " + h264Frame.frameType + ", 大小： " + h264Frame.frameLen);
//
//            if (!frameQueue.offer(h264Frame)) {
////                ALog.w("队列已满，添加队列失败，去掉栈顶帧数据再重新添加");
//                frameQueue.poll();
//                frameQueue.offer(h264Frame);
////                ALog.i("线程生成一帧，时间：" + h264Frame.timestamp + "帧长度" + h264Frame.frameLen + "帧类型" + h264Frame.frameType);
//            }
//            data = null;
//            frameBuffer.clear();
//            // 清空之后将当前帧缓存进去
//            frameBuffer.put(bufferData, 0, size);
//            if (result == I_FRAME) {
//                currentFrameType.set(Types.I_FRAME);
//            } else if (result == P_FRAME) {
//                currentFrameType.set(Types.P_FRAME);
//            }
//        }
//    }
//
//    @Override
//    public void run() {
//        long delay = 0;
//        H264Frame h264Frame = frameQueue.poll();
//        if (h264Frame != null) {
//            // TODO debug ---- 15M码流调试: 不掉帧，不花屏（从缓存队列中取出的裸流）
//            if(needSave){
//                testCacheVideo2OutputBuffer(h264Frame.frameData, h264Frame.frameLen);
////                testWriteTenByteHexOfVideo(h264Frame.frameData, h264Frame.frameLen);
//            }
////            ALog.w("计算PT前输入的数据 --- longitude: " + MApplication.getAircraftGpsInfoBean().getLongitude()
////                    + " , latitude: " + MApplication.getAircraftGpsInfoBean().getLatitude()
////                    + " , Altitude: " + MApplication.getAircraftGpsInfoBean().getAltitude()
////                    + " , Heading: " + MApplication.getAircraftGpsInfoBean().getHeading()
////                    + " , yaw: " + MApplication.getGimbalAttitudeInfoBean().getYaw()
////                    + " , pitch: " + MApplication.getGimbalAttitudeInfoBean().getPitch()
////                    + " , angleH: " + angleH
////                    + " , angleV: " + angleV);
//
//            // 1、PT值去抖预处理MApplication.getCompensateYaw()
////            float pitch = GlobleContent.getInstance().getAttitude().pitch.floatValue();
//            float pitch = GlobleContent.getInstance().getAttitude().getPitch().floatValue();
//
//            // 去抖算法二：比对当前帧与上一帧的PT差值，如果差值在阈值范围内则使用上一帧的PT
//            if (Math.abs(Math.abs(pitch) - Math.abs(preFramePitch)) > GlobleContent.debounceThresholdPitch) {
//                preFramePitch = pitch;
//            }
//
//            // 2、根据RTK是否正常使用来获取相应的GPS
//            float longitude;
//            float latitude;
//            float altitude;
//            double heading;
//            if (isRTKUsing.get()) {     // 使用RTK的位置信息
//                longitude = (float) GlobleContent.getInstance().getRtkGpsInfoBean().getMobileLongitude();
//                latitude = (float) GlobleContent.getInstance().getRtkGpsInfoBean().getMobileLatitude();
//                altitude = (float) GlobleContent.getInstance().getRtkGpsInfoBean().getMobileAltitude();
//                heading = GlobleContent.getInstance().getRtkGpsInfoBean().getHeading();
//            } else {// 使用Gps位置信息
//                if(airGpsInfo.getLongitude() == 0){
//                    longitude = preFrameLongtitude;
//                    latitude = preFrameLatutide;
//                }else{
//                longitude = (float) airGpsInfo.getLongitude();
//                latitude = (float) airGpsInfo.getLatitude();
//                }
//                altitude = (float) (airGpsInfo.getAltitude() + GlobleContent.altitudeCompensating);
//                heading = airGpsInfo.getHeading();
//            }
//            float yaw;
//            if(GlobleContent.getInstance().getIsGyroscopeOpen()){
//                yaw = GlobleContent.getInstance().getAttitude().getYaw().floatValue();
//            }else{
//                yaw = (float) (heading + GlobleContent.getInstance().getAttitude().getYaw().floatValue());
//                if(yaw > 180){
//                    yaw -= 360;
//                }else if(yaw < - 180){
//                    yaw += 360;
//                }
//            }
//            if (Math.abs(Math.abs(yaw) - Math.abs(preFrameYaw)) > GlobleContent.debounceThresholdYaw) {
//                preFrameYaw = yaw;
//            }
//            if (heading > 0) {
//                preFrameLongtitude = Math.max( longitude,preFrameLongtitude);
//                preFrameLatutide = Math.max( latitude, preFrameLatutide);
//            }
////            ALog.e(TAG,"   传输的缩放倍数是::::::::::::::::::::::::::::::::::::::::::::::::::::::::::"+curFocal);
//            // 3、根据序列号判段结果确定是否发送带AR信息的视频流
//            if (isARSupported.get()) {
//                if(localSave.get()){
//                    int code =Jni28181AgentSDK.getInstance().savePsToLocal(
//                            h264Frame.timestamp,
//                            h264Frame.frameType,
//                            h264Frame.frameData,
//                            curFocal,
//                            preFramePitch,
//                            preFrameYaw,
//                            angleH,
//                            angleV,
//                            longitude,
//                            latitude,
//                            altitude,saveVideoPath);
//                    ALog.e("在线写流的结果是 is" + h264Frame.frameLen + "  result code" + code);
//                }
//                int code = Jni28181AgentSDK.getInstance().inputWholeFrameWithARInfoX(
//                        h264Frame.timestamp,
//                        h264Frame.frameType,
//                        h264Frame.frameData,
//                        curFocal,
//                        preFramePitch,
//                        preFrameYaw,
//                        angleH,
//                        angleV,
//                        longitude,
//                        latitude,
//                        altitude);
//                ALog.e("AR mincheng java sent Frame len is" + h264Frame.frameLen + "  result code" + code);
//            } else {
//                // TODO 优化 --- 不支持AR信息的云台发流可以不经过前面的云台GPS和PT计算
//                int code = Jni28181AgentSDK.getInstance().inputWholeFrame(
//                        h264Frame.timestamp, h264Frame.frameType, h264Frame.frameData);
////                ALog.e("noAR mincheng java  " + h264Frame.frameLen + "  result code" + code);
//
//            }
//            h264Frame.frameData = null;
//            h264Frame = null;
//
//        } else {
//            delay = 5;
//        }
//        if(isRtpRunning.get() && !isDestory && pushHandler != null){
//            pushHandler.postDelayed(this,delay);
//        }
//    }
//
//
//    public void startPushVideo(){
//        if(pushHandler != null){
//            ALog.w("响应服务端请求 --- 点流：Rtp服务运行中，无人机开始发流");
//            isRtpRunning.compareAndSet(false, true);
//            pushHandler.post(this);
//        }
//    }
//
//    public void stopVideoPush(){
//        if(pushHandler != null){
//            ALog.w("响应服务端请求 --- 断流");
//            isRtpRunning.compareAndSet(true, false);
//            pushHandler.removeCallbacks(this);
//        }
//    }
//
//    public void setNeedSave(boolean needSave) {
//        this.needSave = needSave;
//    }
//
//
//    public boolean getIsRTKUsing() {
//        return isRTKUsing.get();
//    }
//    public boolean isLocalSave(){
//        return localSave.get();
//    }
//    //TODO 按钮本地写流
//
//
//    public void startPushLocalVideo(){
//        if(localSave.get()){
//            return;
//        }
//        if(pushHandler != null){
//            ALog.w("写流开始!!!!!!!!!!!!!!");
//            String fileName = "ps_video_"+ DateFormat.format("yyyy-MM-dd_HHmmss", System.currentTimeMillis())+".h264";
//            saveVideoPath = BASE_SAVE_PAHT + fileName;
//            ALog.d(TAG,"   当前文件路径是::::::::::::::::::::::::::::::::::::"+saveVideoPath);
//            localSave.compareAndSet(false, true);
//            isRtpRunning.compareAndSet(false, true);
//            pushHandler.post(this);
//        }
//    }
//
//    public void stopLocalVideoPush(){
//        if(!localSave.get()){
//            return;
//        }
//        if(pushHandler != null){
//            ALog.w("写流中断!!!!!!!!!!!!!!");
//            localSave.compareAndSet(true, false);
//            isRtpRunning.compareAndSet(true, false);
//            pushHandler.removeCallbacks(this);
//            Jni28181AgentSDK.getInstance().stopWriteToFile();
//        }
//    }
//    // 测试:写入outputBuffer流
//    private void testCacheVideo2OutputBuffer(byte[] bytes, int size) {
//        if (outputBuffer == null) {
//            outputBuffer = ByteBuffer.allocate(FILE_BUFFER_SIZE);
//        }
//        if (outputBuffer.remaining() >= size) {
//            outputBuffer.put(bytes, 0, size);
//        }
//    }
//
//    // 测试：将每次回调的前十个字节流转为16进制字符串写到文件
//    private void testWriteTenByteHexOfVideo(byte[] bytes, int size) {
//        ALog.i("testWriteTenByteHexOfVideo started ...");
//        if (outputBuffer != null) {
//            if (tenHexBytePath == null) {
//                tenHexBytePath = FilePathUtil.getRootFilePath("/video_output/output_ten_hex_byte.txt");
//            }
//            ALog.i("tenHexBytePath" + tenHexBytePath);
//            StreamTestUtil.writeRawStreamToHexFile(tenHexBytePath, bytes, size, 10, true);
//        }
//    }
//
//    private void testWriteStreamToFileThread() {
//        if (outputBuffer == null) {
//            ALog.w("outputBuffer == null");
//            return;
//        }
//        ALog.w("outputBuffer 大小为： " + outputBuffer.position());
//        ALog.i("tenHexBytePath" + FilePathUtil.getRootFilePath("/video_output/output_byte_buffer.h264"));
//        StreamTestUtil.writeByteBufferToFile(
//                FilePathUtil.getRootFilePath("/video_output/output_byte_buffer.h264"),
//                outputBuffer);
//        needSave = false;
//        outputBuffer.clear();
//        if(isDestory){
//            pushHandler.removeCallbacksAndMessages(null);
//            pushHandler.getLooper().quit();
//            pushHandler = null;
//            outputBuffer = null;
//        }
//    }
//
//
//    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
//    public void onStop() {
//        if(needSave){
//            //TODO debug --- 将20M的流写到文件，用于测试获取到的回调流是否正常
//            pushHandler.post(() -> testWriteStreamToFileThread());
//        }
//        isRtpRunning.compareAndSet(true, false);
//    }
//
//    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
//    public void onDestroy() {
//        isDestory = true;
//        EventRxBus.unregisterEventRxBus(mCompositeDisposable);
//        if(!needSave){
//            pushHandler.removeCallbacksAndMessages(null);
//            pushHandler.getLooper().quit();
//            pushHandler = null;
//        }else{
////            startPushVideo();
//            stopVideoPush();
//        }
//        frameBuffer.clear();
//        frameBuffer = null;
//    }
//}
