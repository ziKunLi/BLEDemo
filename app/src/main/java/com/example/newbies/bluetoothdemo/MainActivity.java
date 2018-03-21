package com.example.newbies.bluetoothdemo;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

/**
 * @author NewBies
 */
public class MainActivity extends AppCompatActivity {

    private Adapter adapter;
    private List<String> name;
    private List<String> address;
    private RecyclerView recyclerView;
    private BluetoothAdapter bluetoothAdapter;
    private boolean isSearching = false;
    private Handler handler;
    private EditText message;

    private BluetoothAdapter.LeScanCallback leScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
            LogUtil.v("device",device.toString() + device.getName());
            if(!address.contains(device.getAddress())){
                if(device.getName() == null || device.getName().equals("")){
                    name.add("未知设备");
                }
                else {
                    name.add(device.getName());
                }
                address.add(device.getAddress());
                adapter.notifyDataSetChanged();
            }
        }
    };
    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(action.equals(StaticDataPool.ACTION_DATA)){
                Toast.makeText(MainActivity.this,"我接收到了数据:" + intent.getStringExtra(StaticDataPool.DATA),Toast.LENGTH_SHORT).show();
                LogUtil.v("message", intent.getStringExtra(StaticDataPool.DATA));
            }
            else if(action.equals(StaticDataPool.ACTION_SERVICE_DISCOVERED_FILED)){
                Toast.makeText(context, "尝试重连...", Toast.LENGTH_SHORT).show();
            }
            else if(action.equals(StaticDataPool.ACTION_SERVICE_DISCOVERED)){
                Toast.makeText(context, "连接成功...", Toast.LENGTH_SHORT).show();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initData();
        initView();
        initListener();
        initPermission();
        initBLE();
        initBroadcastReceiver();
    }

    public void initData(){
        name = new ArrayList<>();
        address = new ArrayList<>();
        adapter = new Adapter(name,address);
        handler = new Handler();
    }

    public void initView(){
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        message = findViewById(R.id.message);
    }

    public void initListener(){
        adapter.setItemClickCallBack(new Adapter.ItemClickCallBack() {
            @Override
            public void onItemClick(View view, int position) {
                //点击跳转页面，并将其设备地址传过去，方便进行设备连接
                Intent intent = new Intent(MainActivity.this,BLEDeviceActivity.class);
                intent.putExtra("address",address.get(position));
                startActivity(intent);
                finish();
            }

            @Override
            public void onItemLongClick(View view, int position) {

            }
        });
    }

    public void initPermission(){
        if(ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},2);
        }
    }

    /**
     * 初始化低功耗蓝牙
     */
    public void initBLE(){

        // 使用此检查确定BLE是否支持在设备上，然后你可以有选择性禁用BLE相关的功能
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, "您的设备不支持低功耗蓝牙！", Toast.LENGTH_SHORT).show();
            finish();
        }
        //使用getSystemService（）返回BluetoothManager，然后将其用于获取适配器的一个实例。
        // 初始化蓝牙适配器
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // 确保蓝牙在设备上可以开启
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, 1);
        }
        startService(new Intent(this,BLEService.class));
    }

    /**
    * 初始化广播接收器
    */
    public void initBroadcastReceiver(){
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(StaticDataPool.ACTION_DATA);
        registerReceiver(broadcastReceiver,intentFilter);
    }

    /**
     * 搜索附近的蓝牙设备
     * @param view
     */
    public void search(final View view){
        //如果现在没有在搜索，那么搜索
        if(!isSearching){
            isSearching = true;
            view.setClickable(false);
            ((Button)view).setText("正在搜索...请稍后");
            name.clear();
            address.clear();
            adapter.notifyDataSetChanged();
            Log.v("info","正在搜索 == MainActivity:132");
            bluetoothAdapter.startLeScan(leScanCallback);
            //在搜索30秒后停止搜索
            handler.postDelayed(
                    new Runnable() {
                        @Override
                        public void run() {
                            ((Button)view).setText("点击搜索");
                            view.setClickable(true);
                            isSearching = false;
                            bluetoothAdapter.stopLeScan(leScanCallback);
                        }
                    },30000
            );
        }
    }

    public void sendMessage(View view){
        String sendStr = message.getText().toString();
        if(sendStr.isEmpty()){
            return;
        }
        if(StaticDataPool.characteristic != null) {
            LogUtil.v("info","onClick" + sendStr);
            StaticDataPool.characteristic.setValue(sendStr);
            StaticDataPool.bleService.writeCharacteristic(StaticDataPool.characteristic);
            LogUtil.v("发送成功了吧");
        }
        else {
            Toast.makeText(this, "您还没选择服务！", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        LogUtil.v("被摧毁");
    }
}
