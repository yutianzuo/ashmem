package com.example.testashmemclient;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Environment;
import android.os.IBinder;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.sharedmemlib.ISharedMem;
import com.example.sharedmemlib.LoadListener;
import com.example.sharedmemlib.LoadListener.Stub;

public class MainActivity extends AppCompatActivity implements ServiceConnection{

    ISharedMem ShmMemService;
    Button b;
    EditText ed,ed2;
    TextView tv;
    Button btnRequireLock;
    Button btnReleaseLock;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bindService(new Intent("com.example.developer.testashmem.ShmService").setPackage("com.example.developer.testashmem"),this,BIND_AUTO_CREATE);
        tv = (TextView)findViewById(R.id.tv);
        ed=(EditText)findViewById(R.id.ed);
        ed2 = (EditText)findViewById(R.id.ed2);
        b=(Button)findViewById(R.id.btnGet);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                    tv.setText("val:" + ShmClientLib.getVal(Integer.parseInt(ed2.getText().toString())));

             }
        });
        Button bset = (Button)findViewById(R.id.btnSet);
        bset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ShmClientLib.setVal(Integer.parseInt(ed2.getText().toString()),Integer.parseInt(ed.getText().toString()));
            }
        });

        btnRequireLock = findViewById(R.id.btnLock);
        btnRequireLock.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                String strPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/lock"
                        + ".lock";
                boolean bRet = ShmClientLib.requireProcLock(strPath);
                Log.e("testLock", "require lock:" + bRet);
                Toast.makeText(MainActivity.this, "require lock:" + bRet, Toast.LENGTH_SHORT).show();
            }
        });


        btnReleaseLock = findViewById(R.id.btnRelease);
        btnReleaseLock.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                ShmClientLib.releaseProcLock();
            }
        });
    }

    @Override
    public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
        ShmMemService = ISharedMem.Stub.asInterface(iBinder);
        try {
            ParcelFileDescriptor p = ShmMemService.OpenSharedMem("sh1", 1000, false,
                    new LoadListener.Stub() {
                @Override
                public void onSuccess(int[] arr) throws RemoteException {
                    //run in binder thread in current process
                    Log.e("aidlcallback", "onSuccess thread:" + Thread.currentThread().getName());
                    Log.e("aidlcallback", "out params:" + arr[0] + " " + arr[1]);
                    arr[0] = 11;
                    arr[1] = 12;
                }

                @Override
                public void onFail(int errorCode, String msg) throws RemoteException {
                    Log.e("aidlcallback", "onFail thread:" + Thread.currentThread().getName() +
                            " msg:" + msg);
                }
            });
            ShmClientLib.setMap(p.getFd(), 1000);
            Log.e("FILEFD", "fd in testashemclient is:" + p.getFd() + "procid:" + android.os.Process.myPid());
        } catch (RemoteException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) {

    }
}
