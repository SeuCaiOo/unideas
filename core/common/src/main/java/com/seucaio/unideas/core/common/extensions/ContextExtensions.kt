package com.seucaio.unideas.core.common.extensions

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat

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

/** Opens the system share sheet for plain [text] (e.g. an item's title/description). */
fun Context.shareText(text: String) {
    val sendIntent = Intent(Intent.ACTION_SEND).setType("text/plain").putExtra(Intent.EXTRA_TEXT, text)
    startActivity(Intent.createChooser(sendIntent, null))
}

/** Whether [permission] is currently granted to this app. */
fun Context.hasPermission(permission: String): Boolean =
    ActivityCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
