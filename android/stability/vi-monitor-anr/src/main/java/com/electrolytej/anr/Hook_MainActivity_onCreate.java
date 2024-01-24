package com.electrolytej.anr;

import static com.electrolytej.anr.AnrKt.TAG_ANR_MONITOR;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import androidx.annotation.Keep;
@Keep
public class Hook_MainActivity_onCreate{
    public static String className = "XActivity";
    public static String methodName = "onCreate";
    public static String methodSig = "(Landroid/os/Bundle;)V";
    public static void hook(Activity activity, Bundle savedInstanceState){
        Log.e(TAG_ANR_MONITOR, "Hook_MainActivity_onCreate hook");
        backup(activity,savedInstanceState);
    }

    public static void backup(Activity activity, Bundle savedInstanceState){}
}
