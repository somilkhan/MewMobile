package com.nuvio.app.core.diagnostics

actual object PlatformRuntimeLogcat {
    actual fun appendAppLog(level: String, message: String) = Unit
    actual fun snapshot(): String? = null
}
