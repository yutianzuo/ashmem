// ISharedMem.aidl
package com.example.sharedmemlib;

import android.os.ParcelFileDescriptor;
import com.example.sharedmemlib.LoadListener;

interface ISharedMem {
    //String can only be [in]
    ParcelFileDescriptor getSharedMemFD(in String name, in LoadListener
    listener);
}
