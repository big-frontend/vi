package com.electrolytej.vi;

import static com.electrolytej.vi.StartupKt.TAG_STARTUP_MONITOR;

import android.app.Application;
import android.util.Log;

import androidx.annotation.Keep;

import com.electrolytej.vi.util.TraceUtil;

@Keep
public class Hook_PerfApp_onCreate {
    public static String className = "XPerfApp";
    public static String methodName = "onCreate";
    public static String methodSig = "()V";
    public static void hook(Application application){
        Perf.onCreate(application);
        TraceUtil.i(TAG_STARTUP_MONITOR,application.getClass().getSimpleName()+"#onCreate");
        backup(application);
        TraceUtil.o();
    }

    public static void backup(Application application){
        Log.e("HookInfo", "backup not be here");
    }
}
