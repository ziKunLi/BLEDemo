package com.example.newbies.bluetoothdemo;

import android.app.Application;
import android.content.Context;

/**
 * 每当程序启动时，Application类就会被调用，我们这里继承了它，实现了自己的MyApplication
 * 然后在AndroidManifest.xml中注册一下，程序启动时就会调用这个类了，我们可以在该类中进行
 * 一些初始化操作。
 * @author NewBies
 * @date 2018/2/2
 */

public class MyApplication extends Application{

    private static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
        CrashHandler.getInstance().init(context);
        //详情请见Android第一行代码，第二版，第十三章460页
//        LitePal.initialize(context);
    }

    /**
     * 方便的获取到上下文对象
     * @return
     */
    public static Context getContext(){
        return context;
    }

}
