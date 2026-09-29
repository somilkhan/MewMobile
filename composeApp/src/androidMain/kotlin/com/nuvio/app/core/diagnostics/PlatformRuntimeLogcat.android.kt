package com.nuvio.app.core.diagnostics

import android.os.Process
import android.util.Log
import java.io.BufferedReader
import java.io.InputStreamReader

actual object PlatformRuntimeLogcat {
    private const val tag = "MewMobile"
    private const val maxSnapshotChars = 120_000

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
                    reader.readText().trim().let { output ->
                        if (output.length <= maxSnapshotChars) output else output.takeLast(maxSnapshotChars)
                    }
                }
            }.also {
                process.destroy()
            }
        }.getOrNull()?.takeIf { it.isNotBlank() }
    }
}
