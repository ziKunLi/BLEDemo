package com.example.newbies.bluetoothdemo;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
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

import java.util.List;

/**
 *
 * @author NewBies
 * @date 2018/3/10
 */
public class BLEService extends Service {

    private BluetoothGatt bluetoothGatt;
    private BluetoothManager bluetoothManager;
    private BluetoothAdapter bluetoothAdapter;

    class LocalBind extends Binder{

        public BLEService getService(){
            return BLEService.this;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new LocalBind();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        // After using a given device, you should make sure that BluetoothGatt.close() is called
        // such that resources are cleaned up properly.  In this particular example, close() is
        // invoked when the UI is disconnected from the Service.
        if (bluetoothGatt == null) {
            return super.onUnbind(intent);
        }
        bluetoothGatt.close();
        bluetoothGatt = null;
        return super.onUnbind(intent);
    }

    private BluetoothGattCallback bluetoothGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            //连接成功
            if(newState == BluetoothProfile.STATE_CONNECTED){
                //连接成功后尝试搜寻服务
                //如果搜到服务将会触发onServicesDiscovered回调
                bluetoothGatt.discoverServices();
                Log.v("info","连接成功");
            }
            //连接失败
            else if(newState == BluetoothProfile.STATE_DISCONNECTED){
                Log.v("info","连接失败 == BLEService:70");
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                sendBroadcast(StaticDataPool.ACTION_SERVICE_DISCOVERED,null);
            } else {

            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {

            }
        }

        /**
         * 我们可以在这里接收数据
         * @param gatt
         * @param characteristic
         */
        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            sendBroadcast(StaticDataPool.ACTION_DATA,characteristic);
        }
    };

    public void init(){
        // For API level 18 and above, get a reference to BluetoothAdapter through
        // BluetoothManager.
        if (bluetoothManager == null) {
            bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            if (bluetoothManager == null) {
                Log.v("info", "Unable to initialize BluetoothManager.");
                return;
            }
        }

        bluetoothAdapter = bluetoothManager.getAdapter();
        if (bluetoothAdapter == null) {
            Log.v("info", "Unable to obtain a BluetoothAdapter.");
            return;
        }
    }

    /**
     * 连接设备
     * @param address
     */
    public void connect(String address){
        if(bluetoothAdapter == null){
            Log.v("info","bluetoothAdapter为null");
            return;
        }
        Log.v("info",address + "BLEService:128");
        bluetoothGatt = bluetoothAdapter.getRemoteDevice(address).connectGatt(this,false,bluetoothGattCallback);
    }

    public void disconnect(){
        if (bluetoothAdapter == null || bluetoothGatt == null) {
            Log.w("info", "BluetoothAdapter not initialized == BLEService： 133");
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

    /**
     * 发送数据
     * @param characteristic
     */
    public void writeCharacteristic(BluetoothGattCharacteristic characteristic){
        if (bluetoothAdapter == null || bluetoothGatt == null) {
            Log.v("info", "蓝牙服务未初始化");
            return;
        }
        bluetoothGatt.writeCharacteristic(characteristic);
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
}
