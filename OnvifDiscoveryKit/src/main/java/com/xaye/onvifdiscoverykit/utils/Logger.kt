package com.xaye.onvifdiscoverykit.utils

import android.util.Log
import com.xaye.onvifdiscoverykit.BuildConfig

/**
 * Author xaye
 * @date: 2024/12/25
 */
enum class LogLevel(val priority: Int) {
    TRACE(1),
    DEBUG(2),
    INFO(3),
    WARN(4),
    ERROR(5)
}

object Logger {
    // 全局 TAG，默认值为 "MyApp"
    private var globalTag: String = "ONVIF_EXPLORER"

    // 是否开启调试模式，默认关闭
    private var isDebugEnabled: Boolean = BuildConfig.DEBUG

    // 当前日志级别，默认设置为 TRACE
    private var currentLogLevel: LogLevel = if (!isDebugEnabled) LogLevel.INFO else LogLevel.TRACE

    /**
     * 设置全局 TAG。
     * @param tag 全局 TAG 字符串。
     */
    fun setGlobalTag(tag: String) {
        if (tag.isNotBlank()) {
            globalTag = tag
        }
    }

    /**
     * 获取当前全局 TAG。
     * @return 当前全局 TAG。
     */
    fun getGlobalTag(): String {
        return globalTag
    }

    /**
     * 设置自定义的日志级别。
     * @param level 要设置的日志级别。
     */
    fun setLogLevel(level: LogLevel) {
        currentLogLevel = level
    }

    /**
     * 内部方法，用于打印日志。
     * 显示全局 TAG、调用日志方法的类名、方法名和行号。
     * @param level 日志级别。
     * @param message 日志消息。
     * @param throwable 可选的异常。
     */
    private fun log(level: LogLevel, message: String, throwable: Throwable? = null) {
        if (level.priority >= currentLogLevel.priority) {
            val threadName = Thread.currentThread().name
            val caller = getCallerStackTraceElement()
            val location = caller?.let {
                "${it.fileName}:${it.lineNumber} (${it.methodName})"
            } ?: "Unknown Location"

            // 格式化日志输出
            val logMessage = "[$location] $message"

            // 使用 Android 的 Log 类输出日志
            when (level) {
                LogLevel.TRACE, LogLevel.DEBUG -> Log.d(globalTag, logMessage)
                LogLevel.INFO -> Log.i(globalTag, logMessage)
                LogLevel.WARN -> Log.w(globalTag, logMessage)
                LogLevel.ERROR -> Log.e(globalTag, logMessage, throwable)
            }

            // 如果需要在控制台也打印日志，可以保留 println（可选）
            // println(logMessage)
            // throwable?.printStackTrace()
        }
    }

    /**
     * 获取调用者的堆栈帧信息。
     * @return 调用者的 StackTraceElement。
     */
    private fun getCallerStackTraceElement(): StackTraceElement? {
        val stackTrace = Thread.currentThread().stackTrace
        // 遍历堆栈，找到第一个不属于 Logger 类和系统类的堆栈帧
        for (element in stackTrace) {
            val className = element.className
            if (!className.contains(Logger::class.java.simpleName) &&
                !className.startsWith("java.lang.Thread") &&
                !className.startsWith("dalvik.") &&
                !className.startsWith("android.")
            ) {
                return element
            }
        }
        return null
    }

    // 日志方法
    fun t(message: String) = log(LogLevel.TRACE, message)

    fun d(message: String) = log(LogLevel.DEBUG, message)

    fun i(message: String) = log(LogLevel.INFO, message)

    fun w(message: String) = log(LogLevel.WARN, message)

    fun e(message: String, throwable: Throwable? = null) = log(LogLevel.ERROR, message, throwable)
}
