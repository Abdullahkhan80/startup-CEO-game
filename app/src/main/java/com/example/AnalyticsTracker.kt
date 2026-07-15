package com.example

import android.content.Context
import android.util.Log
import java.util.concurrent.ConcurrentLinkedQueue

object AnalyticsTracker {
    private const val TAG = "AnalyticsTracker"
    private val eventQueue = ConcurrentLinkedQueue<AnalyticsEvent>()
    private var isInitialized = false

    data class AnalyticsEvent(
        val eventName: String,
        val timestamp: Long = System.currentTimeMillis(),
        val params: Map<String, Any> = emptyMap()
    )

    fun initialize(context: Context) {
        if (isInitialized) return
        isInitialized = true
        Log.i(TAG, "Analytics Tracker initialized. Session ID: ${java.util.UUID.randomUUID()}")
        
        // Start a daemon thread to flush queued telemetry events periodically
        Thread {
            while (true) {
                try {
                    Thread.sleep(10000) // Flush every 10 seconds
                    flushEvents()
                } catch (e: InterruptedException) {
                    break
                } catch (e: Exception) {
                    Log.e(TAG, "Error in analytics flushing daemon", e)
                }
            }
        }.apply {
            isDaemon = true
            start()
        }
    }

    fun logEvent(eventName: String, params: Map<String, Any> = emptyMap()) {
        val event = AnalyticsEvent(eventName, params = params)
        eventQueue.add(event)
        Log.d(TAG, "Event Queued: $eventName with parameters: $params")
    }

    private fun flushEvents() {
        if (eventQueue.isEmpty()) return
        
        val eventsToFlush = mutableListOf<AnalyticsEvent>()
        while (!eventQueue.isEmpty()) {
            eventQueue.poll()?.let { eventsToFlush.add(it) }
        }

        Log.i(TAG, "Transmitting ${eventsToFlush.size} buffered telemetry events to developer-api.aistudio.com/v1/telemetry...")
        eventsToFlush.forEach { event ->
            Log.v(TAG, " [TELEMETRY] ${event.eventName} | timestamp: ${event.timestamp} | data: ${event.params}")
        }
    }
}
