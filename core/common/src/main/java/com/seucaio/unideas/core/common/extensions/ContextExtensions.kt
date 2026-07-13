package com.seucaio.unideas.core.common.extensions

import android.content.Context
import android.content.Intent

/**
 * Restarts the app in a fresh process: relaunches the launcher activity, then kills this
 * process outright. A simple activity restart (`finishAffinity()`) is not enough — Android may
 * keep the process alive, leaving DI singletons (Room database, repositories, etc.) built
 * against stale state. Only a real process kill forces them to rebuild from scratch.
 */
fun Context.restartApplication() {
    val launchIntent = packageManager.getLaunchIntentForPackage(packageName)
    launchIntent?.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
    startActivity(launchIntent)
    Runtime.getRuntime().exit(0)
}
