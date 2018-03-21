package com.example.newbies.bluetoothdemo;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 用于进行低功耗蓝牙通信的活动
 * @author NewBies
 * @date 2018/3/10
 */

public class BLEDeviceActivity extends AppCompatActivity{

    /**
     * 低功耗蓝牙服务
     */
    private BLEService bleService;
    /**
     * 要进行连接的设备地址
     */
    private String address;
    /**
     * 编辑的待发送的内容
     */
    private EditText message;
    /**
     * 已经发送和接收到的信息
     */
    private TextView messages;
    /**
     * 用于发送数据
     */
    private BluetoothGattCharacteristic characteristic;
    private BluetoothGattCharacteristic notifyCharacteristic;
    private PopupWindow servicePop;
    private ExpandableListView expandableListView;
    private List<BluetoothGattService> bluetoothGattServiceList;
    /**
     * 二级列表的父组件
     */
    private List<String> group;
    /**
     * 二级列表的子组件
     */
    private List<List<String>> child;
    /**
     * 二级列表的适配器
     */
    private ServiceAdapter serviceAdapter;
    /**
     * 提示正在连接
     */
    private PopupWindow connectingPop;

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(action.equals(StaticDataPool.ACTION_DATA)){
                Toast.makeText(BLEDeviceActivity.this,"我接收到了数据",Toast.LENGTH_SHORT).show();
                messages.setText(messages.getText() + "\n下位机对我说：" + intent.getStringExtra(StaticDataPool.DATA));
            }
            else if(action.equals(StaticDataPool.ACTION_SERVICE_DISCOVERED)){
                Toast.makeText(context, "连接成功！", Toast.LENGTH_SHORT).show();
                if(connectingPop != null){
                    connectingPop.dismiss();
                }
            }
            else if(action.equals(StaticDataPool.ACTION_SERVICE_DISCOVERED_FILED)){
                Toast.makeText(context, "连接失败！", Toast.LENGTH_SHORT).show();
                if(connectingPop != null){
                    connectingPop.dismiss();
                }
            }
        }
    };
    /**
     * 用于监听服务与活动关系变化的类
     */
    private ServiceConnection serviceConnection = new ServiceConnection() {

        /**
         * 该方法会在服务与活动绑定成功时调用
         * @param componentName
         * @param service
         */
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            LogUtil.v("绑定成功:" + service.toString());
            bleService = ((BLEService.LocalBind) service).getService();
            StaticDataPool.bleService = bleService;
            bleService.init();
            bleService.connect(address);
            showConnectingPop();
        }

        /**
         * 该方法会在服务与活动断开绑定时调用
         * @param componentName
         */
        @Override
        public void onServiceDisconnected(ComponentName componentName) {

        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.transfer_activity);
        LogUtil.v("BLEDeviceActivity onCreate");
        initData();
        initView();
        initListener();
        initBroadcastReceiver();
        initService();
        LogUtil.v("" + Thread.currentThread().getId());
    }

    @Override
    public void onStart(){
        super.onStart();
        LogUtil.v("BLEDeviceActivity onStart");
    }

    @Override
    public void onResume(){
        super.onResume();
        LogUtil.v("BLEDeviceActivity onResume");
    }

    public void initData(){
        address = getIntent().getStringExtra("address");
        group = new ArrayList<>();
        child = new ArrayList<>();
    }

    public void initView(){
        message = findViewById(R.id.message);
        messages = findViewById(R.id.messages);
    }

    public void initListener(){

    }

    /**
     * 初始化广播接收器
     */
    public void initBroadcastReceiver(){
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(StaticDataPool.ACTION_DATA);
        intentFilter.addAction(StaticDataPool.ACTION_SERVICE_DISCOVERED);
        intentFilter.addAction(StaticDataPool.ACTION_SERVICE_DISCOVERED_FILED);
        registerReceiver(broadcastReceiver,intentFilter);
    }

    /**
     * 初始化服务，绑定服务
     */
    public void initService(){
        if(StaticDataPool.bleService == null){
            LogUtil.v("开始绑定");
            Intent intent = new Intent(MyApplication.getContext(),BLEService.class);
            bindService(intent,serviceConnection,BIND_AUTO_CREATE);
        }
        else{
            this.bleService = StaticDataPool.bleService;
            this.characteristic = StaticDataPool.characteristic;
        }
    }

    public void send(View view){
        String sendStr = message.getText().toString();
        if(sendStr.isEmpty()){
            return;
        }
        //write value to characteristic
        if(characteristic != null) {
            Log.v("info","onClick" + sendStr);
            characteristic.setValue(sendStr);
            messages.setText(messages.getText() + "\n我是android上位机，我说：" + sendStr);
            bleService.writeCharacteristic(characteristic);
        }
        else {
            Toast.makeText(bleService, "您还没选择服务！", Toast.LENGTH_SHORT).show();
        }
    }

    public void showServices(View view){
        showServicePop();
    }

    /**
     * 显示展示该蓝牙提供的服务的弹窗
     */
    private void showServicePop(){
        if(servicePop == null){
            View popView = getLayoutInflater().inflate(R.layout.service_pop,null);
            servicePop = new PopupWindow(popView, getScreenWidth() * 3 / 4,getScreenHeight()*3/4,true);
            servicePop.setBackgroundDrawable(new ColorDrawable());
            servicePop.setOutsideTouchable(true);
            servicePop.setOnDismissListener(new PopupWindow.OnDismissListener() {
                @Override
                public void onDismiss() {
                    setBackgroundAlpha(1.0f);
                }
            });
            expandableListView = popView.findViewById(R.id.services);
        }
        if(bluetoothGattServiceList == null || bluetoothGattServiceList.size() == 0){
            //获取到蓝牙提供的服务
            bluetoothGattServiceList = bleService.getSupportedGattServices();
            for(int i = 0 ;i < bluetoothGattServiceList.size(); i++){
                //将该服务的UUID添加进去
                group.add(bluetoothGattServiceList.get(i).getUuid().toString());
                //获取到服务支持的特性
                List<BluetoothGattCharacteristic> bluetoothGattCharacteristicList = bluetoothGattServiceList.get(i).getCharacteristics();
                List<String> tempList = new ArrayList<>();
                for(int j = 0; j < bluetoothGattCharacteristicList.size(); j++){
                    //将特性的UUID添加进去
                    tempList.add(bluetoothGattCharacteristicList.get(j).getUuid().toString());
                }
                child.add(tempList);
            }
            serviceAdapter = new ServiceAdapter(this,group,child);
            serviceAdapter.setOnItemClikCallBack(new ServiceAdapter.OnItemClikCallBack() {
                @Override
                public void onGroupItemClick(int groupPosition) {
                    Toast.makeText(bleService, "父项： " + group.get(groupPosition), Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onChildItemClick(int groupPosition, int childPosition) {
                    BluetoothGattCharacteristic tempCharacteristic = bluetoothGattServiceList.get(groupPosition).getCharacteristic(UUID.fromString(child.get(groupPosition).get(childPosition)));
                    LogUtil.v(bluetoothGattServiceList.get(groupPosition).getType() + " " + bluetoothGattServiceList.get(groupPosition).getUuid());
                    int charaProp = tempCharacteristic.getProperties();
                    LogUtil.v(tempCharacteristic.getProperties() + " : " + tempCharacteristic.getPermissions());
                    LogUtil.v("" + charaProp);
//                    LogUtil.v("" + BluetoothGattCharacteristic.PROPERTY_READ);
//                    LogUtil.v("" + BluetoothGattCharacteristic.PROPERTY_NOTIFY);
//                    LogUtil.v("" + (charaProp | BluetoothGattCharacteristic.PROPERTY_READ));
//                    LogUtil.v("" + (charaProp | BluetoothGattCharacteristic.PROPERTY_NOTIFY));
                    if(UUID.fromString(StaticDataPool.SIMPLE_PROFILE_CHAR5_UUID).equals(tempCharacteristic.getUuid())){
                        characteristic = tempCharacteristic;
                        StaticDataPool.characteristic = tempCharacteristic;
                    }
                    else{
                        bleService.setCharacteristicNotification(tempCharacteristic);
                    }

                    servicePop.dismiss();
                }
            });
            expandableListView.setAdapter(serviceAdapter);
        }
        servicePop.showAtLocation(message, Gravity.CENTER,0,0);
        setBackgroundAlpha(0.4f);
    }

    private void showConnectingPop(){
        if(connectingPop == null){
            View popView = getLayoutInflater().inflate(R.layout.connecting_pop,null);
            connectingPop = new PopupWindow(popView,getScreenWidth() /4, getScreenHeight() /6,false);
            connectingPop.setOnDismissListener(new PopupWindow.OnDismissListener() {
                @Override
                public void onDismiss() {
                    setBackgroundAlpha(1.0f);
                }
            });
        }
        connectingPop.showAtLocation(message,Gravity.CENTER,0,0);
        setBackgroundAlpha(0.4f);
    }

    /**
     * 得到屏幕宽度
     * @return
     */
    public int getScreenWidth(){
        WindowManager windowManager = (WindowManager)getSystemService(Context.WINDOW_SERVICE);
        return windowManager.getDefaultDisplay().getWidth();
    }

    /**
     * 得到屏幕的高
     * @return
     */
    public int getScreenHeight(){
        WindowManager windowManager = (WindowManager)getSystemService(Context.WINDOW_SERVICE);
        return windowManager.getDefaultDisplay().getHeight();
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        unregisterReceiver(broadcastReceiver);
        LogUtil.v("onDestroy");
    }

    /**
     * 设置添加屏幕的背景透明度
     * @param bgAlpha
     * @auther 李自坤
     */
    public void setBackgroundAlpha(float bgAlpha) {
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        //0.0-1.0
        lp.alpha = bgAlpha;
        getWindow().setAttributes(lp);
    }


    @Override
    public void onBackPressed(){
        finish();
    }
}
