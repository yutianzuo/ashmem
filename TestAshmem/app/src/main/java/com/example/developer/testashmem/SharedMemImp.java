package com.example.developer.testashmem;

import android.os.Handler;
import android.os.Looper;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;
import android.util.Log;


import java.io.IOException;

import com.example.sharedmemlib.*;


public class SharedMemImp extends ISharedMem.Stub {
    public String test = "123456";

    @Override
    public ParcelFileDescriptor OpenSharedMem(String name, int size, boolean create,
            final LoadListener listener) throws RemoteException {
        //run in binder thread in current process
        int fd = ShmLib.OpenSharedMem(name, size, create);
        Log.e("aidlserver", "fd in app is:" + fd + " procid:" + android.os.Process.myPid() +
                " threadid:" + Thread.currentThread().getName());
        try {
            Handler handler = new Handler(Looper.getMainLooper());
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    try {
                        int arrtest[] = new int[10];
                        arrtest[0] = 999;
                        /// 由于delay了，所以调用进程的调用线程已经结束了，所以回调函数将会在调用进程的binder线程
                        /// 中执行
                        listener.onSuccess(arrtest);
                        Log.e("aidlserver", "out params:" + outString(arrtest));
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
            }, 500);
            /// 跟return一样运行在调用进程的调用线程，所以这个回调在调用进程的调用线程中执行。
            listener.onFail(0, "Fail from Server");

            return ParcelFileDescriptor.fromFd(fd);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private String outString(int[] arr) {
        StringBuffer sbuffer = new StringBuffer();
        for (int i = 0; i < arr.length; ++i) {
            sbuffer.append(arr[i]);
            if (i < arr.length - 1) {
                sbuffer.append("-");
            }
        }
        return sbuffer.toString();
    }

}
