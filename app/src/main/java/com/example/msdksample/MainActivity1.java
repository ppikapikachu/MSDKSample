package com.example.msdksample;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TableLayout;

import com.google.android.material.tabs.TabItem;
import com.google.android.material.tabs.TabLayout;

public class MainActivity1 extends AppCompatActivity implements View.OnClickListener{
    private String TAG = getClass().getSimpleName();
    private Spinner spinner = null;
    private Button sendVideoStreamBtn = null;
    private int vedioPos = -1;//选项卡的发流方式
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.temp);

        init();
    }
    private void init(){
        spinner = findViewById(R.id.sendVedioStreamFun);
        sendVideoStreamBtn = findViewById(R.id.sendVideoStreamBtn);

        sendVideoStreamBtn.setOnClickListener(this);
    }
    public void initSpinner(){
        spinner.getSelectedItem();
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long id) {
                String re = adapterView.getItemAtPosition(pos).toString();
                vedioPos = pos;
//                switch (pos){
//                    case 0://sendVideoStream
//                        pos = pos;
//                        break;
//                    case 1://sendVideoWithARInfo
//                        break;
//                    case 2://sendVideoWithARInfoX
//                        break;
//                    case 3://sendVideoWithARInfoToLocal
//                        break;
//                }
                Log.i(TAG,"选择下标为："+pos+"---方式为："+re);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }
    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.sendVideoStreamBtn:
                switch (vedioPos){
                    case 0://sendVideoStream

                        break;
                    case 1://sendVideoWithARInfo
                        break;
                    case 2://sendVideoWithARInfoX
                        break;
                    case 3://sendVideoWithARInfoToLocal
                        break;
                }
                break;
        }
    }
}