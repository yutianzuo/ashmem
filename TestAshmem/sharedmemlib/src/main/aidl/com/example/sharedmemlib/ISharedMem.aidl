// ISharedMem.aidl
package com.example.sharedmemlib;

import android.os.ParcelFileDescriptor;
import com.example.sharedmemlib.LoadListener;

interface ISharedMem {
    ParcelFileDescriptor OpenSharedMem(String name, int size, boolean create, LoadListener listener);
}
