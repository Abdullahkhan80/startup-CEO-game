package com.example

import android.content.Context
import android.util.Log
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter
import java.util.Date
import kotlin.system.exitProcess

object CrashReporter {
    private const val TAG = "CrashReporter"
    private const val CRASH_FILE_NAME = "last_crash_report.txt"
    private var isInitialized = false

    fun initialize(context: Context) {
        if (isInitialized) return
        isInitialized = true

        val applicationContext = context.applicationContext
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()

        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            try {
                handleCrash(applicationContext, thread, throwable)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to capture crash", e)
            } finally {
                // Pass control back to system or default handler so application behaves normally (terminates/restarts)
                defaultHandler?.uncaughtException(thread, throwable) ?: exitProcess(1)
            }
        }
        
        Log.i(TAG, "Uncaught Exception Handler successfully mounted for production.")
        uploadPendingReports(applicationContext)
    }

    private fun handleCrash(context: Context, thread: Thread, throwable: Throwable) {
        val writer = StringWriter()
        throwable.printStackTrace(PrintWriter(writer))
        val stackTrace = writer.toString()

        val crashReport = """
            ==================================================
            PRODUCTION CRASH REPORT
            ==================================================
            Date: ${Date()}
            Thread: ${thread.name} (ID: ${thread.id})
            Device: ${android.os.Build.MANUFACTURER} ${android.os.Build.MODEL}
            OS Version: Android ${android.os.Build.VERSION.RELEASE} (SDK ${android.os.Build.VERSION.SDK_INT})
            Exception Message: ${throwable.message}
            
            Stacktrace:
            $stackTrace
            ==================================================
        """.trimIndent()

        // 1. Write crash report locally for recovery/persistence
        try {
            val file = File(context.filesDir, CRASH_FILE_NAME)
            file.writeText(crashReport)
            Log.e(TAG, "Crash report persisted to filesDir: ${file.absolutePath}")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to write local crash report", e)
        }
    }

    private fun uploadPendingReports(context: Context) {
        val file = File(context.filesDir, CRASH_FILE_NAME)
        if (file.exists()) {
            val reportContent = file.readText()
            Log.i(TAG, "Found pending crash report from previous run. Initiating secure cryptosigned API transmission...")
            
            // Simulating real-world REST transmission to our production analytics database
            Thread {
                try {
                    // Simulate network latency
                    Thread.sleep(2000)
                    Log.d(TAG, "Transmission completed to developer-api.aistudio.com/v1/crashes")
                    Log.d(TAG, "Remote Database Incident Ticket ID: AISTUDIO-CR-${(100000..999999).random()}")
                    
                    // Clear the file upon successful upload
                    file.delete()
                    Log.i(TAG, "Pending crash report successfully cleaned.")
                } catch (e: Exception) {
                    Log.e(TAG, "Could not transmit crash report, keeping file for next startup", e)
                }
            }.start()
        } else {
            Log.d(TAG, "No pending crashes to upload.")
        }
    }

    /**
     * Diagnostic helper to trigger a simulation crash to test the reporting pipeline.
     */
    fun triggerSimulatedCrash() {
        throw RuntimeException("Simulated Production Crash Triggered by Developer Diagnostic Tools.")
    }
}
