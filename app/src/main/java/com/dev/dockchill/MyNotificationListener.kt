package com.dev.dockchill

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log

/**
 * Musika aplikazioen (Spotify, YT Music, etab.) Notifikazioak entzuten dituen zerbitzua.
 * Honi esker detektatzen dugu:
 *  - Spotify ireki denean
 *  - Spotify itxi denean
 *  - MediaSession aldatu denean
 *
 * Gure Fragment-ari callback bat bidaltzen dio, updateMediaController() berriz exekutatzeko.
 *
 * NOTA: Zerbitzu hau AndroidManifest.xml-n erregistratu behar da. Baita ere, spotify ezin daiteke
 * kontrolatu erabiltzaileak aplikazioari notifikazioak irakurtzeko baimena eman ezean.
 */
class MyNotificationListener : NotificationListenerService() {

    companion object {
        /**
         * Fragment-ak hemen ematen du callback funtzioa.
         * Zerbitzu honek Spotifyren aktibitate berria jasotzen duenean → callback deitzen da.
         */
        var onNotificationUpdate: (() -> Unit)? = null
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        Log.d("NotificationListener", "Listener connected")
        onNotificationUpdate?.invoke()
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        Log.d("NotificationListener", "Notification posted: ${sbn?.packageName}")
        onNotificationUpdate?.invoke()
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        Log.d("NotificationListener", "Notification removed: ${sbn?.packageName}")
        onNotificationUpdate?.invoke()
    }
}
