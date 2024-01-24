package lab.galaxy.yahfa;

import android.content.Context;

import androidx.annotation.Keep;

import com.electrolytej.vi.AbsHookRegistry;
import com.electrolytej.vi.Priority;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

import kotlin.collections.CollectionsKt;

@Keep
public class HookInfo {
    public static List<String> hookItemNames;
    public static List<AbsHookRegistry> hookRegistry;

    static {
        hookRegistry = CollectionsKt.sortedBy(ServiceLoader.load(AbsHookRegistry.class, Thread.currentThread().getContextClassLoader()), absHookRegistry -> {
            Priority a = absHookRegistry.getClass().getAnnotation(Priority.class);
            return a != null ? a.value() : 0;
        });
        ArrayList<String> h = new ArrayList<>();
        for (AbsHookRegistry r : hookRegistry) {
            r.registerItem(h);
        }
        hookItemNames = h;
    }

    public static void attachBaseContext(Context base) {
        for (AbsHookRegistry r : hookRegistry) {
            r.attachBaseContext(base);
        }
    }
}
