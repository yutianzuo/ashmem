// ISharedMem.aidl
package com.example.sharedmemlib;

import android.os.ParcelFileDescriptor;
import com.example.sharedmemlib.LoadListener;

interface ISharedMem {
    //String can only be [in]
    ParcelFileDescriptor OpenSharedMem(in String name, int size, boolean create, in LoadListener
    listener);
}
