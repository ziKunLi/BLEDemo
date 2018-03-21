package com.example.newbies.bluetoothdemo;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattServer;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import java.util.List;
import java.util.UUID;

/**
 *
 * @author NewBies
 * @date 2018/3/10
 */
public class BLEService extends Service {

    private BluetoothGatt bluetoothGatt;
    private BluetoothManager bluetoothManager;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothGattCharacteristic notifyCharacteristic;
    private String address;

    class LocalBind extends Binder{

        public BLEService getService(){
            return BLEService.this;
        }
    }

    /**
     * 绑定的生命周期是onCreate --> onBind --> onUnBind --> onDestroy
     * 特殊情况，我先startService再bindService，这样当绑定的组件被摧毁时，会执行onUnBind但不会执行onDestroy
     * @param intent
     * @return
     */
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new LocalBind();
    }

    /**
     *
     * @param intent
     * @return
     */
    @Override
    public boolean onUnbind(Intent intent) {
        // After using a given device, you should make sure that BluetoothGatt.close() is called
        // such that resources are cleaned up properly.  In this particular example, close() is
        // invoked when the UI is disconnected from the Service.
        if (bluetoothGatt == null) {
            return super.onUnbind(intent);
        }
        LogUtil.v("取消绑定");
        return super.onUnbind(intent);
    }

//    @Override
//    public int onStartCommand(Intent intent, int flag, int startId){
//        init();
//        LogUtil.v("onStartCommand");
//        return super.onStartCommand(intent, flag, startId);
//    }
    private BluetoothGattCallback bluetoothGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            //连接成功
            if(newState == BluetoothProfile.STATE_CONNECTED){
                //连接成功后尝试搜寻服务
                //如果搜到服务将会触发onServicesDiscovered回调
                bluetoothGatt.discoverServices();
                LogUtil.v("连接成功");
            }
            //连接失败
            else if(newState == BluetoothProfile.STATE_DISCONNECTED){
                LogUtil.v("连接失败");
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                sendBroadcast(StaticDataPool.ACTION_SERVICE_DISCOVERED,null);
            } else {
                sendBroadcast(StaticDataPool.ACTION_SERVICE_DISCOVERED_FILED,null);
            }
        }

        /**
         * 具有可读属性的特征值在当android主动去读取时调用
         * @param gatt
         * @param characteristic
         * @param status
         */
        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            LogUtil.v("接收数据:onCharacteristicRead" + new String(characteristic.getValue()));
            if (status == BluetoothGatt.GATT_SUCCESS) {
                sendBroadcast(StaticDataPool.ACTION_DATA,characteristic);
            }
            LogUtil.v(characteristic.getUuid().toString());
        }

        /**
         * 我们可以在这里接收实时数据
         * @param gatt
         * @param characteristic
         */
        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            sendBroadcast(StaticDataPool.ACTION_DATA,characteristic);
            LogUtil.v("接收数据：onCharacteristicChanged" + new String(characteristic.getValue()));
        }
    };

    public void init(){
        // For API level 18 and above, get a reference to BluetoothAdapter through
        // BluetoothManager.
        if (bluetoothManager == null) {
            bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            if (bluetoothManager == null) {
                LogUtil.v("初始化失败");
                return;
            }
        }

        bluetoothAdapter = bluetoothManager.getAdapter();
        if (bluetoothAdapter == null) {
            LogUtil.v("无法获取BluetoothAdapter");
            return;
        }
    }

    /**
     * 连接设备
     * @param address
     */
    public void connect(String address){
        if(bluetoothAdapter == null){
            LogUtil.v("bluetoothAdapter为null");
            return;
        }
        this.address = address;
        LogUtil.v(address);
        //第二个参数表示是否需要自动连接。如果设置为 true, 表示如果设备断开了，会不断的尝试自动连接。设置为 false 表示只进行一次连接尝试。
        bluetoothGatt = bluetoothAdapter.getRemoteDevice(address).connectGatt(this,true,bluetoothGattCallback);
    }

    public void disconnect(){
        if (bluetoothAdapter == null || bluetoothGatt == null) {
            LogUtil.w("BluetoothAdapter not initialized");
            return;
        }
        bluetoothGatt.disconnect();
    }

    private void sendBroadcast(String action, BluetoothGattCharacteristic characteristic){
        Intent intent = new Intent();
        intent.setAction(action);

        if(characteristic != null){
            intent.putExtra(StaticDataPool.DATA,new String(characteristic.getValue()));
        }

        sendBroadcast(intent);
    }

    public void readCharacteristic(BluetoothGattCharacteristic characteristic) {
        if (bluetoothAdapter == null || bluetoothAdapter == null) {
            LogUtil.w("BluetoothAdapter not initialized");
            return;
        }
        bluetoothGatt.readCharacteristic(characteristic);
    }

    /**
     * 发送数据
     * @param characteristic
     */
    public void writeCharacteristic(BluetoothGattCharacteristic characteristic){
        if (bluetoothAdapter == null || bluetoothGatt == null) {
            LogUtil.v("蓝牙服务未初始化");
            return;
        }
        bluetoothGatt.writeCharacteristic(characteristic);
    }

    /**
     * 设置是否接受蓝牙发送的数据，是实时监听蓝牙的数据通知，还是主动去取蓝牙的数据
     * @param characteristic
     */
    public void setCharacteristicNotification(BluetoothGattCharacteristic characteristic) {
        if (bluetoothAdapter == null || bluetoothAdapter == null) {
            LogUtil.v( "BluetoothAdapter not initialized");
            return;
        }
        int charaProp = characteristic.getProperties();

        if ((charaProp | BluetoothGattCharacteristic.PROPERTY_READ) > 0) {
            //搞不懂，我参考的demo上说：如果特征上存在活动通知，请先清除它，以便它不更新用户界面上的数据字段。
            //但我注释了还是行呀，先留着吧。
            if (notifyCharacteristic != null) {
                //如果是可读属性，那么就设置为false
                bluetoothGatt.setCharacteristicNotification(characteristic, false);
                notifyCharacteristic = null;
            }
            readCharacteristic(characteristic);
            LogUtil.v(characteristic.getUuid().toString());
        }

        if ((charaProp | BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
            notifyCharacteristic = characteristic;
            //设置接收，但是只有android端调用了该方法才会接收，接收数据时onCharacteristicRead被调用，如果要接受蓝牙的通知，请设置为true，不然没用
            bluetoothGatt.setCharacteristicNotification(characteristic, true);
            LogUtil.v(notifyCharacteristic.getUuid().toString());
            //设置此方法只需调用一次，以后蓝牙有信息就会接收，接收数据时onCharacteristicChanged被调用（实时监听数据）
            if (StaticDataPool.SIMPLE_PROFILE_CHAR4_UUID.equals(characteristic.getUuid().toString())
                    |StaticDataPool.SIMPLE_PROFILE_CHAR6_UUID.equals(characteristic.getUuid().toString())) {
                LogUtil.v("对的");
                BluetoothGattDescriptor descriptor = characteristic.getDescriptor(UUID.fromString(StaticDataPool.CLIENT_CHARACTERISTIC_CONFIG));
                descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                bluetoothGatt.writeDescriptor(descriptor);
            }
            else {
                LogUtil.v("错了");
            }
        }
    }

    /**
     * 获取该低功耗蓝牙所支持的服务
     * @return
     */
    public List<BluetoothGattService> getSupportedGattServices() {
        if (bluetoothGatt == null) {
            return null;
        }
        return bluetoothGatt.getServices();
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        //释放相关资源
        bluetoothGatt.close();
        bluetoothGatt = null;
        LogUtil.v("服务被摧毁 ");
    }
}
