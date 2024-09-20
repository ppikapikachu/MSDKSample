//package com.example.msdksample
//
//import android.content.Context
//import android.util.Log
//import androidx.lifecycle.MutableLiveData
//import androidx.lifecycle.ViewModel
//import dji.v5.common.error.IDJIError
//import dji.v5.common.register.DJISDKInitEvent
//import dji.v5.manager.SDKManager
//import dji.v5.manager.interfaces.SDKManagerCallback
//
//class MSDKManagerVM {
////    // The data is held in livedata mode, but you can also save the results of the sdk callbacks any way you like.
////    val lvRegisterState = MutableLiveData<Pair<Boolean, IDJIError?>>()
////    val lvProductConnectionState = MutableLiveData<Pair<Boolean, Int>>()
////    val lvProductChanges = MutableLiveData<Int>()
////    val lvInitProcess = MutableLiveData<Pair<DJISDKInitEvent, Int>>()
////    val lvDBDownloadProgress = MutableLiveData<Pair<Long, Long>>()
//    val TAG = "MSDKManagerVM"
//    fun initMobileSDK(appContext: Context) {
//        // 初始化 MSDK，建议初始化逻辑放在 Application 中，当然也可以根据自己的需要放在任意地方。
//        SDKManager.getInstance().init(appContext, object : SDKManagerCallback {
//            override fun onInitProcess(event: DJISDKInitEvent?, totalProcess: Int) {
//                Log.i(TAG, "onInitProcess: ")
//                if (event == DJISDKInitEvent.INITIALIZE_COMPLETE) {
//                    SDKManager.getInstance().registerApp()
//                }
//            }
//
//            override fun onRegisterSuccess() {
//                Log.i(TAG, "onRegisterSuccess: ")
//            }
//
//            override fun onRegisterFailure(error: IDJIError?) {
//                Log.i(TAG, "onRegisterFailure: ")
//            }
//
//            override fun onProductConnect(productId: Int) {
//                Log.i(TAG, "onProductConnect: ")
//            }
//
//            override fun onProductDisconnect(productId: Int) {
//                Log.i(TAG, "onProductDisconnect: ")
//            }
//
//            override fun onProductChanged(productId: Int) {
//                Log.i(TAG, "onProductChanged: ")
//            }
//
//            override fun onDatabaseDownloadProgress(current: Long, total: Long) {
//                Log.i(TAG, "onDatabaseDownloadProgress: ${current / total}")
//            }
//        })
//    }
//
//    fun destroyMobileSDK() {
//        SDKManager.getInstance().destroy()
//    }
//
//}