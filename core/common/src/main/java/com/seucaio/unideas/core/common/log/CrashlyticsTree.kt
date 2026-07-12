package com.seucaio.unideas.core.common.log

import android.util.Log
import com.google.firebase.crashlytics.FirebaseCrashlytics
import timber.log.Timber

/**
 * A Timber Tree that sends error logs and exceptions to Firebase Crashlytics.
 * Only logs with priority WARN or higher are sent.
 */
class CrashlyticsTree : Timber.Tree() {
    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        if (priority == Log.VERBOSE || priority == Log.DEBUG || priority == Log.INFO) {
            return
        }

        val crashlytics = FirebaseCrashlytics.getInstance()

        // Log the message to Crashlytics custom logs
        crashlytics.log("${priorityString(priority)}/${tag ?: "NoTag"}: $message")

        // Record the exception if provided
        if (t != null) {
            crashlytics.recordException(t)
        } else if (priority >= Log.ERROR) {
            // If it's an ERROR but no exception was provided, create a synthetic one
            crashlytics.recordException(Exception(message))
        }
    }

    private fun priorityString(priority: Int): String = when (priority) {
        Log.WARN -> "W"
        Log.ERROR -> "E"
        Log.ASSERT -> "A"
        else -> priority.toString()
    }
}
