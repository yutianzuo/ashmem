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

import com.example.sharedmemlib.*;

public class MainActivity extends AppCompatActivity implements ServiceConnection {

    ISharedMem ShmMemService;
    Button b;
    EditText ed, ed2;
    TextView tv;
    Button btnRequireLock;
    Button btnReleaseLock;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        runOnUiThread(() -> {
            boolean ret =
                    bindService(
                            new Intent("com.example.developer.testashmem.ShmService").setPackage(
                                    "com"
                                            + ".example.developer.testashmem"), this,
                            BIND_AUTO_CREATE);
            Toast.makeText(this, "result:" + ret, Toast.LENGTH_SHORT).show();
        });

        tv = findViewById(R.id.tv);
        ed = findViewById(R.id.ed);
        ed2 = findViewById(R.id.ed2);
        b = findViewById(R.id.btnGet);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tv.setText(
                        "val:" + ShmClientLib.getVal(Integer.parseInt(ed2.getText().toString())));

            }
        });
        Button bset = (Button) findViewById(R.id.btnSet);
        bset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ShmClientLib.setVal(Integer.parseInt(ed2.getText().toString()),
                        Integer.parseInt(ed.getText().toString()));
            }
        });

        btnRequireLock = findViewById(R.id.btnLock);
        btnRequireLock.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                /// 高版本需要赋予文件的完全访问权限，需要手动去设置中设置
                String strPath = Environment.getExternalStorageDirectory().getAbsolutePath()
                        + "/lock"
                        + ".lock";
                boolean bRet = ShmClientLib.requireProcLock(strPath);
                Log.e("testLock", "require lock:" + bRet);
                Toast.makeText(MainActivity.this, "require lock:" + bRet, Toast.LENGTH_SHORT)
                        .show();
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
        Log.e("aidlcallback", "onServiceConnected:" + Thread.currentThread().getName());
        ShmMemService = ISharedMem.Stub.asInterface(iBinder);
        try {
            final int[] fd = new int[1];
            ParcelFileDescriptor p = ShmMemService.getSharedMemFD("sh1",
                    new LoadListener.Stub() {
                        @Override
                        public void onSuccess(int size) throws RemoteException {
                            //run in binder thread in current process
                            MainActivity.this.runOnUiThread(() -> {
                                Toast.makeText(MainActivity.this, "onSuccess", Toast.LENGTH_SHORT)
                                        .show();
                            });
                            Log.e("aidlcallback",
                                    "onSuccess thread:" + Thread.currentThread().getName());
                            Log.e("aidlcallback", "size:" + size);
                            Log.e("aidlcallback", "fd:" + fd[0]);
                            ShmClientLib.setMap(fd[0], size);
                        }

                        @Override
                        public void onFail(int errorCode, String msg) throws RemoteException { /// for test
                            MainActivity.this.runOnUiThread(() -> {
                                Toast.makeText(MainActivity.this, "onFail", Toast.LENGTH_SHORT)
                                        .show();
                            });
                            Log.e("aidlcallback",
                                    "onFail thread:" + Thread.currentThread().getName() +
                                            " msg:" + msg);
                        }
                    });

            Log.e("aidlcallback", "fd in testashemclient is:" + p.getFd() + "procid:"
                    + android.os.Process.myPid());
            fd[0] = p.getFd();
        } catch (RemoteException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) {
        Log.e("aidlcallback", "onServiceDisconnected:" + Thread.currentThread().getName());
    }
}
