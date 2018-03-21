package com.example.newbies.bluetoothdemo;

import android.bluetooth.BluetoothGattCharacteristic;

/**
 * 静态数据库
 * @author NewBies
 * @date 2018/3/10
 */
public class StaticDataPool {
    /**
     * 进行数据传输
     */
    public static final String ACTION_DATA = "com.example.newbies.bluetoothdemo.action.data";
    public static final String ACTION_SERVICE_DISCOVERED = "com.example.newbies.bluetoothdemo.service.discovered";
    public static final String ACTION_SERVICE_DISCOVERED_FILED = "com.example.newbies.bluetoothdemo.service.discovered.filed";
    public static final String DATA = "data";
    /**
     * 服务的UUID
     */
    public static final String SIMPLE_PROFILE_SERV_UUID = "";
    /**
     * 用于连续接收数据的UUID，（应该算是心跳机制吧）
     */
    public static final String SIMPLE_PROFILE_CHAR4_UUID = "0000fff4-0000-1000-8000-00805f9b34fb";
    /**
     * 用于蓝牙发送数据的UUID
     */
    public static final String SIMPLE_PROFILE_CHAR5_UUID = "0000fff5-0000-1000-8000-00805f9b34fb";
    /**
     * 用于蓝牙接收数据的UUID
     */
    public static final String SIMPLE_PROFILE_CHAR6_UUID = "0000fff6-0000-1000-8000-00805f9b34fb";
    /**
     * 其中00002902-0000-1000-8000-00805f9b34fb是系统提供接受通知自带的UUID，
     * 通过设置BluetoothGattDescriptor相当于设置BluetoothGattCharacteristic的Descriptor属性来实现通知，
     * 这样只要蓝牙设备发送通知信号，就会回调onCharacteristicChanged(BluetoothGatt gatt,
     * BluetoothGattCharacteristic characteristic) 方法，这你就可以在这方法做相应的逻辑处理。
     * 6。还是当你遍历的UUID服务中关于写数据到设备已达到控制设备的UUID是，
     * 你可以保存对应的BluetoothGattCharacteristic对象。然后向BluetoothGattCharacteristic对象写入数据，
     * 在通过BluetoothGatt调用writeCharacteristic()方法即可向硬件写入数据，
     */
    public static final String CLIENT_CHARACTERISTIC_CONFIG = "00002902-0000-1000-8000-00805f9b34fb";
    public static BLEService bleService;
    public static BluetoothGattCharacteristic characteristic;
}
