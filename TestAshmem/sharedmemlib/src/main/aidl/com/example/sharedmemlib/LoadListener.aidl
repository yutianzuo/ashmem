// IServiceManagerService.aidl
package com.example.sharedmemlib;

// Declare any non-default types here with import statements
interface LoadListener {

    /**
    *
    * 加载成功
    *
    */
    void onSuccess();

    /**
    * 加载失败
    *
    * @param errorCode 错误码
    * @param msg 失败原因
    */
    void onFail(int errorCode , in String msg);//String can only be [in]
}
