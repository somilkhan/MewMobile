package com.nuvio.app.core.diagnostics

import android.os.Process
import android.util.Log
import java.io.BufferedReader
import java.io.InputStreamReader

actual object PlatformRuntimeLogcat {
    private const val tag = "MewMobile"

    actual fun appendAppLog(level: String, message: String) {
        when (level) {
            "ERROR" -> Log.e(tag, message)
            "WARN" -> Log.w(tag, message)
            "DEBUG" -> Log.d(tag, message)
            else -> Log.i(tag, message)
        }
    }

    actual fun snapshot(): String? {
        return runCatching {
            val process = Runtime.getRuntime().exec(
                arrayOf("logcat", "-d", "--pid=" + Process.myPid(), "-v", "threadtime"),
            )
            process.inputStream.use { input ->
                BufferedReader(InputStreamReader(input)).use { reader ->
                    reader.readText().trim()
                }
            }.also {
                process.destroy()
            }
        }.getOrNull()?.takeIf { it.isNotBlank() }
    }
}
