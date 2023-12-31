package com.electrolytej.vi;

import android.os.Build;
import android.view.WindowManager;

/**
 * Compatibility wrapper for apps targeting API level 26 or later. See
 * https://developer.android.com/about/versions/oreo/android-8.0-changes.html#cwt
 */
public class WindowOverlayCompat {

  private static final int ANDROID_OREO = 26;
  private static final int TYPE_APPLICATION_OVERLAY = 2038;

  public static final int TYPE_SYSTEM_ALERT =
      Build.VERSION.SDK_INT < ANDROID_OREO
          ? WindowManager.LayoutParams.TYPE_SYSTEM_ALERT
          : TYPE_APPLICATION_OVERLAY;
  public static final int TYPE_SYSTEM_OVERLAY =
      Build.VERSION.SDK_INT < ANDROID_OREO
          ? WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY
          : TYPE_APPLICATION_OVERLAY;
}
