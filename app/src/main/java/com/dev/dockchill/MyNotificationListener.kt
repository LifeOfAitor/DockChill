package com.dev.dockchill

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log

class MyNotificationListener : NotificationListenerService() {

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        // Optional: Log for debugging
        sbn?.let {
            Log.d("NotificationListener", "Notification posted: ${it.packageName}")
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        // Optional: Log for debugging
        sbn?.let {
            Log.d("NotificationListener", "Notification removed: ${it.packageName}")
        }
    }
}