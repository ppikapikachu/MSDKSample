//package com.example.msdksample.utils;
//
//import android.Manifest;
//import android.animation.ValueAnimator;
//import android.app.ProgressDialog;
//import android.content.Context;
//import android.content.DialogInterface;
//import android.content.SharedPreferences;
//import android.os.Build;
//import android.os.Handler;
//import android.util.Log;
//import android.view.Menu;
//import android.view.MenuItem;
//import android.view.View;
//import android.widget.AdapterView;
//import android.widget.ProgressBar;
//import android.widget.Spinner;
//import android.widget.TextView;
//import androidx.appcompat.app.ActionBar;
//import androidx.appcompat.app.AlertDialog;
//
//import com.gosuncn.djidemo.R;
//import com.gosuncn.djidemo.app.Constant;
//import com.gosuncn.djidemo.app.MApplication;
//import com.gosuncn.djidemo.base.BaseDataBindingActivity;
//import com.gosuncn.djidemo.databinding.ActivityLoginBinding;
//import com.gosuncn.djidemo.listeners.SdkStatusListener;
//import com.gosuncn.djidemo.module.login.LoginSDKHelper;
//import com.gosuncn.djidemo.utils.DisposableObserver;
//import com.gosuncn.djidemo.utils.SPUtil;
//import com.gosuncn.glog.ALog;
//import com.gosuncn.lib28181agent.Jni28181AgentSDK;
//import com.gosuncn.lib28181agent.RetCode;
//import com.tbruyelle.rxpermissions2.RxPermissions;
//
//import java.net.Inet4Address;
//import java.net.InetAddress;
//import java.net.NetworkInterface;
//import java.net.SocketException;
//import java.util.Enumeration;
//
//import dji.sdk.keyvalue.key.FlightControllerKey;
//import dji.sdk.keyvalue.key.KeyTools;
//import dji.v5.manager.KeyManager;
//import io.reactivex.Observable;
//import io.reactivex.ObservableOnSubscribe;
//import io.reactivex.android.schedulers.AndroidSchedulers;
//import io.reactivex.disposables.CompositeDisposable;
//import io.reactivex.disposables.Disposable;
//import io.reactivex.schedulers.Schedulers;
//
//import static com.gosuncn.djidemo.net.NetworkStateHelper.NETWORK_MOBILE;
//import static com.gosuncn.djidemo.net.NetworkStateHelper.NETWORK_NONE;
//import static com.gosuncn.djidemo.net.NetworkStateHelper.NETWORK_WIFI;
//import static com.gosuncn.djidemo.net.NetworkStateHelper.getConnectWifiSSID;
//
///**
// * 正式登陆Activity
// * 整合大疆SDK登陆+28181配置登陆
// */
//public class LoginActivity extends BaseDataBindingActivity<ActivityLoginBinding> {
//
//    private static final String TAG = "LoginActivity";
//    private static final String[] requiredPermissionsGroup = new String[] {
//            Manifest.permission.VIBRATE,
//            Manifest.permission.INTERNET,
//            Manifest.permission.ACCESS_WIFI_STATE,
//            Manifest.permission.WAKE_LOCK,
//            Manifest.permission.ACCESS_COARSE_LOCATION,
//            Manifest.permission.ACCESS_NETWORK_STATE,
//            Manifest.permission.ACCESS_FINE_LOCATION,
//            Manifest.permission.CHANGE_WIFI_STATE,
//            Manifest.permission.WRITE_EXTERNAL_STORAGE,
//            Manifest.permission.BLUETOOTH,
//            Manifest.permission.BLUETOOTH_ADMIN,
//            Manifest.permission.READ_EXTERNAL_STORAGE,
//            Manifest.permission.READ_PHONE_STATE,
//    };
//
//    private static final int UPDATE_DB_DOWNLOAD_PROGRESS = 1;
//    private static final int FINISH_LOADING_PROGRESS = 2;
//    private Handler mHandler = new Handler(msg -> {
//        switch (msg.what) {
//            case UPDATE_DB_DOWNLOAD_PROGRESS:
//                int progress = msg.arg1;
//                if (progress == 100) {
//                    binding.tvShowLoadingState.setText("数据库资源下载完成， SDK注册中......");
//                    binding.pbInitLoading.setVisibility(View.GONE);
//                } else {
//                    binding.tvShowLoadingState.setText("数据库资源下载中： " + progress + "/100");
//                    binding.pbInitLoading.setVisibility(View.VISIBLE);
//                    binding.pbInitLoading.setProgress(progress);
//                }
//                break;
//            case FINISH_LOADING_PROGRESS:
//                binding.pbInitLoading.setVisibility(View.GONE);
//                break;
//        }
//        return true;
//    });
//
//    private LoginSDKHelper loginSDKHelper;
//    private TextView tv_title;
//    private SharedPreferences preferences;
////    private String deviceGBCode = "50010000002000000008";   // 44010101901521793901
//    private String deviceGBCode = "34020000001320000008";   // 44010101901521793901
//    private String defaultLocalIp = "0.0.0.0";   // 192.168.37.139 / 10.1.3.222
//    private int defaultLocalPort = 5063;
//    private String serverGBCode = "34020000002000000001";   // 云防平台服务端默认的国标编码 34020000002000000001 34020000001320000008
////    private String defaultServerIp = "210.21.52.94";      // 172.16.11.117（DGW部署的服务）
////    private String defaultServerPort = "40100";
//    private String defaultServerIp = "192.168.159.70";      // 172.16.11.117（DGW部署的服务）
//    private String defaultServerPort = "15060";
//    private String defaultUsername = "admin";
//    private String defaultPassword = "12345678";
//    private boolean isDeviceRegister = false;       // 设备是否注册成功
//    private boolean isAgentSDKInit = false;         // 28181SDK是否已初始化
//    private ProgressDialog progressDialog;
//    private String currentWifiSSID = "";
//    private SdkStatusListener statusListener;
//    private CompositeDisposable mCompositeDisposable = new CompositeDisposable();
//
//
//
//    @Override
//    protected int getLayoutId() {
//        return R.layout.activity_login;
//    }
//
//    @Override
//    protected void beforeSetContentView() {
//        super.beforeSetContentView();
//        initCustomActionBar();
//    }
//
//    @Override
//    protected void afterSetContentView() {
//        super.afterSetContentView();
//        ALog.w("login ---- onCreate ---- ----------------------- ");
//        preferences = MApplication.getPreferences();
//        initUI();
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            startPermissionRequest();
//        } else {
//            binding.tvShowLoadingState.setText("资源加载完成，开始SDK注册......");
//            ALog.w(" ---- onCreate ---- updateProductConnectState");
//            initDjiSdk();
//        }
//        currentWifiSSID = getConnectWifiSSID();
//
//    }
//
//    @Override
//    public void onResume() {
//        super.onResume();
//
////        ALog.w(" ---- onResume ---- updateProductConnectState");
////        updateProductConnectState(MApplication.getAircraftInstance());
//    }
//
//    private void initUI() {
//        initGatewaySettings();
//        initProgressDialog();
//        initSpinner();
//    }
//
//    // 显示自定义ActionBar
//    private void initCustomActionBar() {
//        ActionBar mActionBar = getSupportActionBar();
//        if (mActionBar != null) {
//            mActionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
//            mActionBar.setCustomView(R.layout.layout_action_bar);
//            tv_title = mActionBar.getCustomView().findViewById(R.id.tv_action_bar_title);
//        }
//    }
//
//    private void initSpinner(){
//        binding.includeGatewaySettings.selectType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//            @Override
//            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
//                int location = adapterView.getSelectedItemPosition();
//                if(location == 0){
//                    preferences.edit()
//                            .putInt(Constant.Camera_sum,2)
//                            .apply();
//                }else if(location == 1){
//                    preferences.edit()
//                            .putInt(Constant.Camera_sum,2)
//                            .putString(Constant.CAMERA_ANGLE_H,"71")
//                            .putString(Constant.CAMERA_ANGLE_V,"56")
//                            .putString(Constant.CAMERA_CMOS_H,"2.6")
//                            .putString(Constant.CAMERA_CMOS_W,"1.8")
//                            .apply();
//
//                }else if(location == 2){
//                    preferences.edit()
//                            .putInt(Constant.Camera_sum,3)
//                            .putString(Constant.CAMERA_ANGLE_H,"71")
//                            .putString(Constant.CAMERA_ANGLE_V,"56")
//                            .putString(Constant.CAMERA_CMOS_H,"2.6")
//                            .putString(Constant.CAMERA_CMOS_W,"1.8")
//                            .apply();
//
//                }
//            }
//
//            @Override
//            public void onNothingSelected(AdapterView<?> adapterView) {
//
//            }
//
//        });
//    }
//    private void initGatewaySettings() {
//        SPUtil.showSPStringInfo(binding.includeGatewaySettings.etDeviceGbcode, Constant.DEVICE_GBCODE, deviceGBCode);
//        SPUtil.showSPStringInfo(binding.includeGatewaySettings.etServerGbCode, Constant.SERVER_GBCODE, serverGBCode);
//        SPUtil.showSPStringInfo(binding.includeGatewaySettings.etServerIp, Constant.SERVER_IP,defaultServerIp);
//        SPUtil.showSPStringInfo(binding.includeGatewaySettings.etServerPort, Constant.SERVER_PORT, defaultServerPort);
////        binding.includeGatewaySettings.etServerIp.setText(defaultServerIp);
////        binding.includeGatewaySettings.etServerPort.setText(defaultServerPort);
//        SPUtil.showSPStringInfo(binding.includeGatewaySettings.etUsername, Constant.USERNAME, defaultUsername);
//        SPUtil.showSPStringInfo(binding.includeGatewaySettings.etPassword, Constant.PASSWORD, defaultPassword);
//    }
//
//    private void initProgressDialog() {
//        progressDialog = new ProgressDialog(this);
//        progressDialog.setMessage("加载中......");
//        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
//        progressDialog.setCancelable(false);
//    }
//
//    private void setAnimation(final ProgressBar view, final int progress) {
//        ValueAnimator animator = ValueAnimator.ofInt(0, progress).setDuration(2000);
//        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
//            @Override
//            public void onAnimationUpdate(ValueAnimator valueAnimator) {
//                view.setProgress((int) valueAnimator.getAnimatedValue());
//            }
//        });
//        animator.start();
//    }
//
//
//    private void startPermissionRequest() {
//        RxPermissions rxPermissions = new RxPermissions(this);
//        rxPermissions.request(requiredPermissionsGroup)
//                .subscribe(new DisposableObserver<Boolean>() {
//                    @Override
//                    public void onSubscribe(Disposable disposable) {
//                        mCompositeDisposable.add(disposable);
//                    }
//
//                    @Override
//                    public void onNext(Boolean aBoolean) {
//                        if (aBoolean) {
//                            ALog.w("所有权限请求成功");
////                            setAnimation(binding.pbInitLoading, 100);
//                            initDjiSdk();
//                        }
//                    }
//                });
//    }
//
//
//    private void initDjiSdk(){
//        statusListener = new SdkStatusListener() {
//            @Override
//            public void onRegisterSuccess() {
//                checkAircraftStatus();
//            }
//
//            @Override
//            public void onProductDisconnect(int productId) {
//                ALog.e(TAG," 设备断开::::::::::::::::::::::::::::::::::"+productId);
//            }
//
//            @Override
//            public void onProductConnect(int productId) {
//                ALog.e(TAG," 设备连接::::::::::::::::::::::::::::::::::"+productId);
//            }
//
//            @Override
//            public void onProductChanged(int productId) {
//                ALog.e(TAG," 设备变化::::::::::::::::::::::::::::::::::"+productId);
//            }
//        };
//
//        Log.e(TAG," 10秒设备注册::::::::::::::::::::::::::::::::::");
//        LoginSDKHelper.getInstance().registListener(statusListener);
//        LoginSDKHelper.getInstance().startSDKRegistration();
//    }
//
//    private void checkAircraftStatus(){
//        ALog.e(TAG," 注册成功检查设备连接 ::::::::::::::::::::::::::::::::::");
//        KeyManager.getInstance().listen(KeyTools.createKey(FlightControllerKey.KeyConnection), this, (oldValue, isConnect) -> {
//            if(isConnect != null && isConnect){
//                binding.tvShowLoadingState.setVisibility(View.GONE);
//                binding.llGatewaySettings.setVisibility(View.VISIBLE);
//            }
//        });
//    }
//
//
//    public void onClickSaveSettings(View view) {
//        if (checkConfigValid()) {
//            String deviceGBCode = binding.includeGatewaySettings.etDeviceGbcode.getText().toString();
//            String serverIp = binding.includeGatewaySettings.etServerIp.getText().toString();
//            String serverPort = binding.includeGatewaySettings.etServerPort.getText().toString();
//            String serverGBCode = binding.includeGatewaySettings.etServerGbCode.getText().toString();
//            String username = binding.includeGatewaySettings.etUsername.getText().toString();
//            String password = binding.includeGatewaySettings.etPassword.getText().toString();
//
//            // 保存账号密码信息
//            preferences.edit()
//                    .putString(Constant.DEVICE_GBCODE, deviceGBCode)
//                    .putString(Constant.SERVER_IP, serverIp)
//                    .putString(Constant.SERVER_PORT, serverPort)
//                    .putString(Constant.SERVER_GBCODE, serverGBCode)
//                    .putString(Constant.USERNAME, username)
//                    .putString(Constant.PASSWORD, password)
//                    .apply();
//            showShortToast("保存配置成功！");
//        }
//    }
//
//    public void onClickOffline(View v){
//        gotoActivity(OfflineMainActivity.class, true);
//    }
//    int count = 2;  // 注册次数限制：2次
//    public void onClickLoginGateway(View view) {
//        // 检查网络
//        if (!checkNetAvailable()) {
//            showDialog("当前无网络连接，请先检查网络");
//            return;
//        }
//
//        // 1、校验各种注册信息；2、反复发起注册直到成功；3、跳转页面
//        if (!checkConfigValid()) {
//            showDialog("!checkConfigValid");
//            return;
//        }
//        // 切换网络后，需要重新进行注册
//        if (!initAgentSDK()) {
//            showDialog("!initAgentSDK");
//            return;
//        }
//        progressDialog.show();
//
//
//        Observable.create((ObservableOnSubscribe<Boolean>) emitter -> {
//            while (!isDeviceRegister && count > 0) {
//                Log.e(TAG,"登录28181--------------------------------------------------------------------------------------------------------------------------------------------");
//                int expires = 10800;
//                String ip = preferences.getString(Constant.SERVER_IP, defaultServerIp);
//                int code = Jni28181AgentSDK.getInstance().register(preferences.getString(Constant.SERVER_IP, defaultServerIp),
//                        Integer.parseInt(preferences.getString(Constant.SERVER_PORT, defaultServerPort)),
//                        preferences.getString(Constant.SERVER_GBCODE, serverGBCode), expires,
//                        preferences.getString(Constant.USERNAME, defaultUsername),
//                        preferences.getString(Constant.PASSWORD, defaultPassword));
//                ALog.w("设备注册：" + code + " --- " + RetCode.getErrorMsg(code)+" ip:"+ip);
//                count -= 1;
//                if (code == 0) {
//                    isDeviceRegister = true;
//                    break;
//                }
//            }
//            emitter.onNext(isDeviceRegister);
//            emitter.onComplete();
//        })
//        .subscribeOn(Schedulers.io())
//        .observeOn(AndroidSchedulers.mainThread())
//        .subscribe(new DisposableObserver<Boolean>() {
//            @Override
//            public void onSubscribe(Disposable d) {
//                mCompositeDisposable.add(d);
//            }
//
//            @Override
//            public void onNext(Boolean aBoolean) {
//                count = 2;
//                if (progressDialog.isShowing()) {
//                    progressDialog.dismiss();
//                }
//                showShortToast("设备注册：" + ((isDeviceRegister) ? "成功" : "失败，请检查网络是否正确"));
//                if (aBoolean) {
//                    KeyManager.getInstance().cancelListen(KeyTools.createKey(FlightControllerKey.KeyConnection));
//                    onClickSaveSettings(view);
//                    gotoActivity(NewMainActivity.class, true);
//                }
//            }
//        });
//    }
//
//    private boolean initAgentSDK() {
//        if (isAgentSDKInit) {
//            return true;
//        }
//        String ip = getIP(this);
//        ALog.w("获取本地IP： " + ip);
//        if (ip != null) {
//            int result = Jni28181AgentSDK.getInstance().initSDK(ip, defaultLocalPort,
//                    preferences.getString(Constant.DEVICE_GBCODE, deviceGBCode));
//            ALog.w("AgentSDK Init :" + result);
//            return isAgentSDKInit = result == 0;
//        }
//        return false;
//    }
//
//    // 检查28181登录配置
//    private boolean checkConfigValid() {
//        return checkInputNotEmpty(binding.includeGatewaySettings.etDeviceGbcode.getText(), "设备国标编码")
//                && checkInputNotEmpty(binding.includeGatewaySettings.etServerIp.getText(), "服务IP")
//                && checkInputNotEmpty(binding.includeGatewaySettings.etServerPort.getText(), "服务端口")
//                && checkInputNotEmpty(binding.includeGatewaySettings.etServerGbCode.getText(), "服务国标编码")
//                && checkInputNotEmpty(binding.includeGatewaySettings.etUsername.getText(), "用户名")
//                && checkInputNotEmpty(binding.includeGatewaySettings.etPassword.getText(), "密码");
//    }
//
//    // 获取本地IP
//    public static String getIP(Context context) {
//        try {
//            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
//                NetworkInterface intf = en.nextElement();
//                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();)
//                {
//                    InetAddress inetAddress = enumIpAddr.nextElement();
//                    if (!inetAddress.isLoopbackAddress() && (inetAddress instanceof Inet4Address))
//                    {
//                        return inetAddress.getHostAddress().toString();
//                    }
//                }
//            }
//        }
//        catch (SocketException ex) {
//            ex.printStackTrace();
//        }
//        return null;
//    }
//
//    public static String getIpAddressString() {
//        try {
//            for (Enumeration<NetworkInterface> enNetI = NetworkInterface
//                    .getNetworkInterfaces(); enNetI.hasMoreElements(); ) {
//                NetworkInterface netI = enNetI.nextElement();
//                for (Enumeration<InetAddress> enumIpAddr = netI
//                        .getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
//                    InetAddress inetAddress = enumIpAddr.nextElement();
//                    if (inetAddress instanceof Inet4Address && !inetAddress.isLoopbackAddress()) {
//                        return inetAddress.getHostAddress();
//                    }
//                }
//            }
//        } catch (SocketException e) {
//            e.printStackTrace();
//        }
//        return "0.0.0.0";
//    }
//
//    private void showStateInfo(TextView tv, String msg) {
//        tv.post(new Runnable() {
//            @Override
//            public void run() {
//                tv.setText(msg);
//            }
//        });
//    }
//
//    @Override
//    public void onNetworkChange(int netType) {
//        switch (netType) {
//            case NETWORK_NONE:
//                showDialog("当前无网络连接，请先检查网络连接");
//                break;
//            case NETWORK_WIFI:
//                String wifiSSID = getConnectWifiSSID();
//                if (!wifiSSID.equals(currentWifiSSID)) {
//                    // wifi环境变化，如果已初始化则进行反初始化，登陆的时候进行重新绑定当前wifi获取到的IP
//                    if (isAgentSDKInit) {
//                        Jni28181AgentSDK.getInstance().unInitSDK();
//                        isAgentSDKInit = false;
//                    }
//                    showDialog("检测到当前Wifi连接发生变化");
////                    showDialog("检测到当前Wifi由 " + currentWifiSSID + "更换为 " + wifiSSID);
//                    currentWifiSSID = wifiSSID;
//                    if(!isDeviceRegister){
//                        LoginSDKHelper.getInstance().startSDKRegistration();
//                    }
//                }
//                break;
//            case NETWORK_MOBILE:
//                break;
//        }
//    }
//
//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        this.getMenuInflater().inflate(R.menu.menu_login_page, menu);
//        return true;
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        return super.onOptionsItemSelected(item);
//    }
//
//
//    private AlertDialog mDialog;
//    private void showDialog(String msg) {
//        if (mDialog != null && mDialog.isShowing()) {
//            mDialog.dismiss();
//            showDialog(msg);
//            return;
//        }
//        mDialog = new AlertDialog.Builder(LoginActivity.this)
//                .setMessage(msg)
//                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        dialog.dismiss();
//                    }
//                })
//                .create();
//        mDialog.show();
//    }
//
//    @Override
//    protected void onDestroy() {
//        LoginSDKHelper.getInstance().removerListener(statusListener);
//        mCompositeDisposable.dispose();
//        super.onDestroy();
//    }
//
//}
