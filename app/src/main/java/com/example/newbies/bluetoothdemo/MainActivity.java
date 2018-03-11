package com.example.newbies.bluetoothdemo;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private Adapter adapter;
    private List<String> name;
    private List<String> address;
    private RecyclerView recyclerView;
    private BluetoothAdapter bluetoothAdapter;
    private boolean isSearching = false;



    private BluetoothAdapter.LeScanCallback leScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
            Log.v("info",device.toString() + device.getName() + " == MainActivity:44");
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

    private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initData();
        initView();
        initListener();
        initBLE();
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
    }

    public void initListener(){
        adapter.setItemClickCallBack(new Adapter.ItemClickCallBack() {
            @Override
            public void onItemClick(View view, int position) {
                //点击跳转页面，并将其设备地址传过去，方便进行设备连接
                Intent intent = new Intent(MainActivity.this,BLEDeviceActivity.class);
                intent.putExtra("address",address.get(position));
                startActivity(intent);
            }

            @Override
            public void onItemLongClick(View view, int position) {

            }
        });
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
        final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            bluetoothAdapter = bluetoothManager.getAdapter();
        }

        // 确保蓝牙在设备上可以开启
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, 1);
        }
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
            //在搜索12秒后停止搜索
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
}
