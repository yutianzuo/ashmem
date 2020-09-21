package com.example.developer.testashmem;

import android.os.Handler;
import android.os.Looper;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;
import android.util.Log;

import com.example.sharedmemlib.ISharedMem;
import com.example.sharedmemlib.LoadListener;

import java.io.IOException;
import java.util.HashMap;


public class SharedMemImp extends ISharedMem.Stub {
    public String test = "123456";

    @Override
    public ParcelFileDescriptor OpenSharedMem(String name, int size, boolean create,
            final LoadListener listener) throws RemoteException {
        //run in binder thread in current process
        int fd = ShmLib.OpenSharedMem(name, size, create);
        Log.e("FILEFD", "fd in app is:" + fd + " procid:" + android.os.Process.myPid());
        try {

            Handler handler = new Handler(Looper.getMainLooper());
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    try {
                        int arrtest[] = new int[2];
                        arrtest[0] = 999;
                        arrtest[1] = 998;
                        listener.onSuccess(arrtest);
                        Log.e("aidlserver", "out params:" + arrtest[0] + " " + arrtest[1]);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
            }, 500);
            listener.onFail(0, "Fail from Server");

            return ParcelFileDescriptor.fromFd(fd);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

}
