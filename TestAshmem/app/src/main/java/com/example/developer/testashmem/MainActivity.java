package com.example.developer.testashmem;

import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    EditText edpos, edval;
    Button bn;
    TextView tv;
    Button btnRequireLock;
    Button btnReleaseLock;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ShmLib.OpenSharedMem("sh1", 1000, true);

        edpos = findViewById(R.id.ed2);
        edval = findViewById(R.id.ed);

        tv = findViewById(R.id.tv);
        bn = findViewById(R.id.btnSet);
        bn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ShmLib.setValue("sh1", Integer.parseInt(edpos.getText().toString()),
                        Integer.parseInt(edval.getText().toString()));
            }
        });
        Button bget = findViewById(R.id.btnGet);
        bget.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int res = ShmLib.getValue("sh1", Integer.parseInt(edpos.getText().toString()));
                tv.setText("res:" + res);

            }
        });

        btnRequireLock = findViewById(R.id.btnLock);
        btnRequireLock.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                String strPath = Environment.getExternalStorageDirectory().getAbsolutePath()
                        + "/lock"
                        + ".lock";
//                String strPath = getFilesDir().getAbsolutePath() + "/lock.lock";
                boolean bRet = ShmLib.requireLock(strPath);
                Log.e("appLock", "require lock:" + bRet);
                Toast.makeText(MainActivity.this, "require lock:" + bRet, Toast.LENGTH_SHORT)
                        .show();
            }
        });

        btnReleaseLock = findViewById(R.id.btnRelease);
        btnReleaseLock.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                ShmLib.releaseLock();
            }
        });
    }
}
