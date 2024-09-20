package com.example.msdksample.utils;


import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.media.MediaRecorder;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.view.Surface;

import androidx.annotation.NonNull;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.ArrayBlockingQueue;


public class VideoDecoder {
    private final static String TAG = "VideoDecoder";
    private final static int CONFIGURE_FLAG_DECODE = 0;

    private MediaCodec mMediaCodec;
    private MediaFormat mMediaFormat;
    private Surface mSurface;
    /*    private int mPreviewWidth = 640;
        private int mPreviewHeight = 480;*/
    private int mPreviewWidth = 1920;
    private int mPreviewHeight = 1080;

    private MediaRecorder.VideoEncoder mVideoEncoder;
    private Handler mVideoDecoderHandler;
    private HandlerThread mVideoDecoderHandlerThread = new HandlerThread("VideoDecoder");
    private final static String MIME_FORMAT = "video/avc"; //support h.264

    //待播放视频缓冲队列，静态成员
    public static ArrayBlockingQueue<byte[]> savedata = new ArrayBlockingQueue<byte[]>(20);

    private MediaCodec.Callback mCallback = new MediaCodec.Callback() {
        @Override
        public void onInputBufferAvailable(@NonNull MediaCodec mediaCodec, int id) {
            try {
                ByteBuffer inputBuffer = mediaCodec.getInputBuffer(id);
                inputBuffer.clear();

                byte[] dataSources = null;

                if (savedata.size() > 0) {
                    dataSources = savedata.poll();
                }
                int length = 0;
                if (dataSources != null) {
                    inputBuffer.put(dataSources);
                    length = dataSources.length;
                }

                mediaCodec.queueInputBuffer(id, 0, length, 0, 1000);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        @Override
        public void onOutputBufferAvailable(@NonNull MediaCodec mediaCodec, int id, @NonNull MediaCodec.BufferInfo bufferInfo) {
            ByteBuffer outputBuffer = mMediaCodec.getOutputBuffer(id);
            MediaFormat outputFormat = mMediaCodec.getOutputFormat(id);
            if (mMediaFormat == outputFormat && outputBuffer != null && bufferInfo.size > 0) {
                byte[] buffer = new byte[outputBuffer.remaining()];
                outputBuffer.get(buffer);
            }
            mMediaCodec.releaseOutputBuffer(id, true);
        }

        @Override
        public void onError(@NonNull MediaCodec mediaCodec, @NonNull MediaCodec.CodecException e) {
            Log.d(TAG, "------> onError");
        }

        @Override
        public void onOutputFormatChanged(@NonNull MediaCodec mediaCodec, @NonNull MediaFormat mediaFormat) {
            Log.d(TAG, "------> onOutputFormatChanged");
        }
    };

    public VideoDecoder(Surface surface) {
        try {
            mMediaCodec = MediaCodec.createDecoderByType(MIME_FORMAT);
        } catch (IOException e) {
            Log.e(TAG, Log.getStackTraceString(e));
            mMediaCodec = null;
            return;
        }

        if (surface == null) {
            return;
        }
        this.mSurface = surface;

        mVideoDecoderHandlerThread.start();
        mVideoDecoderHandler = new Handler(mVideoDecoderHandlerThread.getLooper());

        mMediaFormat = MediaFormat.createVideoFormat(MIME_FORMAT, mPreviewWidth, mPreviewHeight);
        mMediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Planar);
        mMediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, 1920 * 1080);
        mMediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE, 30);
        mMediaFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1);
/*        mMediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, 960 * 720);
        mMediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE, 25);
        mMediaFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1);*/
    }

    public void startDecoder() {
        if (mMediaCodec != null && mSurface != null) {
            mMediaCodec.setCallback(mCallback, mVideoDecoderHandler);
            mMediaCodec.configure(mMediaFormat, mSurface, null, CONFIGURE_FLAG_DECODE);
            mMediaCodec.start();
        } else {
            throw new IllegalArgumentException("startDecoder failed, please check the MediaCodec is init correct");
        }
    }

    /**
     * 添加到缓存队列
     *
     * @param buffer
     * @param length
     */
    public void setH264Data(byte[] buffer, int length) {
        if (savedata.size() >= 20) {
            savedata.poll();
        }
        savedata.add(buffer);
    }

    public void stopDecoder() {
        if (mMediaCodec != null) {
            mMediaCodec.stop();
        }

    }

    /**
     * release all resource that used in Encoder
     */
    public void release() {
        if (mMediaCodec != null) {
            mMediaCodec.release();
            mMediaCodec = null;
        }
    }


}
