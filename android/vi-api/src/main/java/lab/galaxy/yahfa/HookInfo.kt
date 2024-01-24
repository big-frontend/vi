@file:JvmName("HookInfo")

package lab.galaxy.yahfa

import android.content.Context
import android.util.Log
import androidx.annotation.Keep
import com.electrolytej.vi.AbsHookRegistry
import com.electrolytej.vi.Priority
import java.util.ServiceLoader

@Keep
object HookInfo {
    var hookItemNames: List<String>? = null
    var hookRegistry: List<AbsHookRegistry>? = null

    init {
        hookRegistry =
            ServiceLoader.load(
                AbsHookRegistry::class.java,
                Thread.currentThread().contextClassLoader
            )
                .sortedBy { it.javaClass.getAnnotation(Priority::class.java)?.value ?: 0 }
        try {
            val h = mutableListOf<String>()
            hookRegistry?.forEach { it.registerItem(h) }
            hookItemNames = h
        } catch (e: ClassNotFoundException) {
        } catch (e: IllegalAccessException) {
        } catch (e: InstantiationException) {
            Log.e("HookInfo", Log.getStackTraceString(e))
        }
    }

    fun attachBaseContext(base: Context) {
        hookRegistry?.forEach { it.attachBaseContext(base) }
    }
}
