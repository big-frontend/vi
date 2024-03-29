package com.electrolytej.startup;

import static com.electrolytej.startup.StartupKt.TAG_STARTUP_MONITOR;
import androidx.annotation.Keep;
import androidx.fragment.app.Fragment;
import com.electrolytej.vi.util.TraceUtil;

@Keep
public class Hook_MainFragment_onHomeDrawCallback {
    public static String className = "XMainFragment";
    public static String methodName = "onHomeDrawCallback";
    public static String methodSig = "()V";

    public static void hook(Fragment fragment) {
        TraceUtil.i(TAG_STARTUP_MONITOR, "MainFragment#onHomeDrawCallback");
        backup(fragment);
        TraceUtil.o();
    }

    public static void backup(Fragment fragment) {
    }
}
