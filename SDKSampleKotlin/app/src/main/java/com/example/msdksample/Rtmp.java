package com.example.msdksample;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.gosuncn.lib28181agent.GS28181SDKManager;

import dji.v5.common.callback.CommonCallbacks;
import dji.v5.common.error.IDJIError;
import dji.v5.manager.datacenter.livestream.LiveStreamManager;
import dji.v5.manager.datacenter.livestream.LiveStreamSettings;
import dji.v5.manager.datacenter.livestream.LiveStreamType;
import dji.v5.manager.datacenter.livestream.settings.RtmpSettings;

public class Rtmp extends AppCompatActivity implements View.OnClickListener {


    private Button startRtmp = null,stopRtmp = null;
    private String TAG = "Rtmp";
    private String RtmpUri = "";
    private GS28181SDKManager manager = GS28181SDKManager.getInstance();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rtmp);


        init();
    }

    private void init(){
        startRtmp = findViewById(R.id.startRtmp);
        stopRtmp = findViewById(R.id.stopRtmp);

        startRtmp.setOnClickListener(this);
        stopRtmp.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.startRtmp:

                LiveStreamSettings.Builder builder = new LiveStreamSettings.Builder();
                builder.setLiveStreamType(LiveStreamType.RTMP).setRtmpSettings(new RtmpSettings.Builder().setUrl(RtmpUri).build()).build();
                LiveStreamManager.getInstance().setLiveStreamSettings(new LiveStreamSettings(builder));
                //开始推流
                LiveStreamManager.getInstance().startStream(new CommonCallbacks.CompletionCallback() {
                    @Override
                    public void onSuccess() {
                        Log.i(TAG,"开启rtmp");
                    }

                    @Override
                    public void onFailure(@NonNull IDJIError idjiError) {
                        Log.i(TAG,"开启rtmp失败"+idjiError);
                    }
                });
                break;
            case R.id.stopRtmp:
                LiveStreamManager.getInstance().stopStream(new CommonCallbacks.CompletionCallback() {
                    @Override
                    public void onSuccess() {
                        Log.i(TAG,"关闭rtmp");
                    }

                    @Override
                    public void onFailure(@NonNull IDJIError idjiError) {
                        Log.i(TAG,"关闭rtmp失败"+idjiError);
                    }
                });
        }
    }
}