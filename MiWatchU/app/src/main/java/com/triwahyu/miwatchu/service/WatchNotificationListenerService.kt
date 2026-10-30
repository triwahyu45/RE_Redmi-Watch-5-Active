package com.triwahyu.miwatchu.service

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import com.triwahyu.miwatchu.core.protocol.BleManager

class WatchNotificationListenerService : NotificationListenerService() {

    companion object {
        private const val TAG = "WatchNotifyService"
        var isServiceRunning = false
            private set
        
        // Allowed default apps (WhatsApp, Telegram, Phone dialer)
        val defaultPackages = setOf(
            "com.whatsapp",
            "com.whatsapp.w4b",
            "org.telegram.messenger",
            "com.google.android.dialer",
            "com.android.server.telecom"
        )
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        isServiceRunning = true
        Log.i(TAG, "Notification Listener Connected for Mi WatchU")
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        isServiceRunning = false
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        if (sbn == null) return

        val pkg = sbn.packageName
        val extras = sbn.notification.extras ?: return

        val title = extras.getString(Notification.EXTRA_TITLE) ?: ""
        val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString() ?: ""

        if (title.isBlank() && text.isBlank()) return

        // Periksa apakah notifikasi panggilan WhatsApp / Telepon biasa
        val isCall = sbn.notification.category == Notification.CATEGORY_CALL ||
                     pkg.contains("dialer") ||
                     title.contains("Panggilan", ignoreCase = true) ||
                     title.contains("Calling", ignoreCase = true)

        Log.d(TAG, "New Notification: [$pkg] $title: $text (isCall=$isCall)")

        // Teruskan ke jam tangan via BLE Manager
        BleManager.sendNotification(
            packageName = pkg,
            title = if (isCall) "📞 $title" else "💬 $title",
            message = text,
            isIncomingCall = isCall
        )
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        super.onNotificationRemoved(sbn)
    }
}
