package com.example.msdksample

import android.app.Application
import android.content.Context
import android.util.Log
import com.example.msdksample.models.MSDKManagerVM
import com.example.msdksample.models.globalViewModels
import com.secneo.sdk.Helper
import dji.v5.common.error.IDJIError
import dji.v5.common.register.DJISDKInitEvent
import dji.v5.manager.SDKManager
import dji.v5.manager.interfaces.SDKManagerCallback

class MyApplication : Application() {

    private val TAG = this::class.simpleName
    private val msdkManagerVM: MSDKManagerVM by globalViewModels()
    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        // 在调用 install 前，请勿调用任何 MSDK 相关
        Helper.install(this)
    }

    override fun onCreate() {
        super.onCreate()
        // 初始化 MSDK，建议初始化逻辑放在 Application 中，当然也可以根据自己的需要放在任意地方。
        msdkManagerVM.initMobileSDK(this)

//         初始化 MSDK，建议初始化逻辑放在 Application 中，当然也可以根据自己的需要放在任意地方。
//        SDKManager.getInstance().init(this,object: SDKManagerCallback {
//            override fun onInitProcess(event: DJISDKInitEvent?, totalProcess: Int) {
//                Log.i(TAG, "onInitProcess: ")
//                if (event == DJISDKInitEvent.INITIALIZE_COMPLETE) {
//                    SDKManager.getInstance().registerApp()
//                }
//            }
//            override fun onRegisterSuccess() {
//                Log.i(TAG, "onRegisterSuccess: ")
//            }
//            override fun onRegisterFailure(error: IDJIError?) {
//                Log.i(TAG, "onRegisterFailure: ")
//            }
//            override fun onProductConnect(productId: Int) {
//                Log.i(TAG, "onProductConnect: ")
//            }
//            override fun onProductDisconnect(productId: Int) {
//                Log.i(TAG, "onProductDisconnect: ")
//            }
//            override fun onProductChanged(productId: Int)
//            {
//                Log.i(TAG, "onProductChanged: ")
//            }
//            override fun onDatabaseDownloadProgress(current: Long, total: Long) {
//                Log.i(TAG, "onDatabaseDownloadProgress: ${current/total}")
//            }
//        })
    }
}