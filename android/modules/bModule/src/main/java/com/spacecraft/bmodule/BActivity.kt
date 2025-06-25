package com.spacecraft.bmodule

import android.app.Activity
import android.os.Bundle
import android.os.PersistableBundle

class BActivity :Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_b)
    }
}