package com.electrolytej.vi

import com.android.build.gradle.api.BaseVariant
import com.didiglobal.booster.gradle.getReport
import com.didiglobal.booster.kotlinx.touch
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import java.io.PrintWriter

class L : AutoCloseable {
    lateinit var log: Logger
    lateinit var pw: PrintWriter

    constructor(pw: PrintWriter) {
        this.pw = pw
    }

    constructor(log: Logger) {
        this.log = log
    }


    companion object {
        fun create(variant: BaseVariant): L {
            val log = variant.getReport("vi-optimizer-minify-resource", "report.txt").touch()
                .printWriter()
            return L(log)
        }

        fun create(): L {
            return L(Logging.getLogger(L::class.java))
        }
    }

    fun println(s: String) {
        if (this::log.isInitialized) {
            log.info(s)
        } else if (this::pw.isInitialized) {
            pw.println(s)
        } else {
            throw IllegalCallerException("没有log")
        }
    }

    override fun close() {
        if (this::pw.isInitialized) {
            pw.close()
        }
    }
}