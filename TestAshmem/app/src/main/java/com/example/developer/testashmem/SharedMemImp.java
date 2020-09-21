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
                        listener.onSuccess();
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
