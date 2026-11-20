package com.iquintero.kavern.core.network

import android.content.Context

object NetworkUtils {
    fun isOnline(context: Context): Boolean = NetworkMonitor.getInstance(context).isOnline()
}
