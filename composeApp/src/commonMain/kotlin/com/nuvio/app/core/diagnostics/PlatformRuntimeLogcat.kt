package com.nuvio.app.core.diagnostics

expect object PlatformRuntimeLogcat {
    fun appendAppLog(level: String, message: String)
    fun snapshot(): String?
}
