package com.spacecraft.bmodule

class NativeLib {

    /**
     * A native method that is implemented by the 'bmodule' native library,
     * which is packaged with this application.
     */
    external fun stringFromJNI(): String

    companion object {
        // Used to load the 'bmodule' library on application startup.
        init {
            System.loadLibrary("bmodule")
        }
    }
}