package test.helloworld;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class BluetoothActivity extends AppCompatActivity {

    private BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    private static final int REQUEST_ENABLE_BT = 2;
    private static final int PERMISSION_REQUEST = 1;
    private RecyclerView recycler_view;

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d("action1", "mBluetoothReceiver action =" + action + BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action));
            //每扫描到一个设备，系统都会发送此广播。
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                //获取蓝牙设备
                BluetoothDevice scanDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                if (scanDevice == null || scanDevice.getName() == null) return;
                TextView text1 = (TextView) findViewById(R.id.text1);
                text1.append("\n" + scanDevice.getName() + "==>" + scanDevice.getAddress() + "\n");
                Log.d("getname", "name=" + scanDevice.getName() + "address=" + scanDevice.getAddress());

            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                Toast.makeText(context, "扫描完成", Toast.LENGTH_SHORT).show();
                TextView text = (TextView) findViewById(R.id.text);
                text.setText("扫描完成");
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth);

//        打开可见性
//        Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
//        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
//        startActivity(discoverableIntent);


        findViewById(R.id.open).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                open();
                Set<BluetoothDevice> devices = mBluetoothAdapter.getBondedDevices();
                Log.d("Shaw2", "绑定的设备数量：" + devices.size());
                for (BluetoothDevice bonddevice : devices) {
                    TextView text2 = (TextView) findViewById(R.id.text2);
                    text2.append("\n" + bonddevice.getName() + "==>" + bonddevice.getAddress() + "\n");
                    Log.d("Shaw3", "bonded=" + bonddevice.getName() + " address" + bonddevice.getAddress());
                }
            }
        });

        findViewById(R.id.scan).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mBluetoothAdapter.startDiscovery();
                TextView text = (TextView) findViewById(R.id.text);
                text.setText("正在搜索...");
                scan();
            }
        });
    }


    @Override
    protected void onResume() {
        super.onResume();
        List<String> permission = getPermissionList(BluetoothActivity.this);
        if (permission.size() > 0) {
            // 申请一个（或多个）权限，并提供用于回调返回的获取码（用户定义）
            requestPermissions(permission.toArray(new String[permission.size()]), PERMISSION_REQUEST);
            return;
        }

    }

    //申请网络定位权限
    public List<String> getPermissionList(Activity activity) {
        List<String> permission = new ArrayList<>();
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            permission.add(Manifest.permission.ACCESS_FINE_LOCATION);
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            permission.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED)
            permission.add(Manifest.permission.INTERNET);

        return permission;
    }


    //回调处理
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            // requestCode即所声明的权限获取码
            case PERMISSION_REQUEST:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // 权限被用户同意。

                } else {
                    // 权限被用户拒绝了。
                    Toast.makeText(BluetoothActivity.this, "定位权限被禁止，蓝牙扫描无法使用！", Toast.LENGTH_LONG).show();
                }
                break;
        }
    }

    //打开蓝牙
    private void open() {
        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        } else if (mBluetoothAdapter.isEnabled()) {
            Toast.makeText(this, "蓝牙已经打开", Toast.LENGTH_SHORT).show();
        }
    }

    private void scan() {
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(mReceiver, filter);
        IntentFilter filter2 = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(mReceiver, filter2);
    }
}
