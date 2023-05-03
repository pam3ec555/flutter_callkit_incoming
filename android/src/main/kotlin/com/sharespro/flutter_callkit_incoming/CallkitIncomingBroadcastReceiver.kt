package com.sharespro.flutter_callkit_incoming

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle

class CallkitIncomingBroadcastReceiver : BroadcastReceiver() {

    companion object {

        const val ACTION_CALL_INCOMING = "com.sharespro.flutter_callkit_incoming.ACTION_CALL_INCOMING"
        const val ACTION_CALL_START = "com.sharespro.flutter_callkit_incoming.ACTION_CALL_START"
        const val ACTION_CALL_ACCEPT = "com.sharespro.flutter_callkit_incoming.ACTION_CALL_ACCEPT"
        const val ACTION_CALL_DECLINE = "com.sharespro.flutter_callkit_incoming.ACTION_CALL_DECLINE"
        const val ACTION_CALL_ENDED = "com.sharespro.flutter_callkit_incoming.ACTION_CALL_ENDED"
        const val ACTION_CALL_TIMEOUT = "com.sharespro.flutter_callkit_incoming.ACTION_CALL_TIMEOUT"
        const val ACTION_CALL_CALLBACK = "com.sharespro.flutter_callkit_incoming.ACTION_CALL_CALLBACK"


        const val EXTRA_CALLKIT_INCOMING_DATA = "EXTRA_CALLKIT_INCOMING_DATA"

        const val EXTRA_CALLKIT_ID = "EXTRA_CALLKIT_ID"
        const val EXTRA_CALLKIT_NAME_CALLER = "EXTRA_CALLKIT_NAME_CALLER"
        const val EXTRA_CALLKIT_APP_NAME = "EXTRA_CALLKIT_APP_NAME"
        const val EXTRA_CALLKIT_HANDLE = "EXTRA_CALLKIT_HANDLE"
        const val EXTRA_CALLKIT_TYPE = "EXTRA_CALLKIT_TYPE"
        const val EXTRA_CALLKIT_AVATAR = "EXTRA_CALLKIT_AVATAR"
        const val EXTRA_CALLKIT_DURATION = "EXTRA_CALLKIT_DURATION"
        const val EXTRA_CALLKIT_TEXT_ACCEPT = "EXTRA_CALLKIT_TEXT_ACCEPT"
        const val EXTRA_CALLKIT_TEXT_DECLINE = "EXTRA_CALLKIT_TEXT_DECLINE"
        const val EXTRA_CALLKIT_TEXT_MISSED_CALL = "EXTRA_CALLKIT_TEXT_MISSED_CALL"
        const val EXTRA_CALLKIT_TEXT_CALLBACK = "EXTRA_CALLKIT_TEXT_CALLBACK"
        const val EXTRA_CALLKIT_EXTRA = "EXTRA_CALLKIT_EXTRA"
        const val EXTRA_CALLKIT_HEADERS = "EXTRA_CALLKIT_HEADERS"
        const val EXTRA_CALLKIT_IS_CUSTOM_NOTIFICATION = "EXTRA_CALLKIT_IS_CUSTOM_NOTIFICATION"
        const val EXTRA_CALLKIT_IS_CUSTOM_SMALL_EX_NOTIFICATION =
            "EXTRA_CALLKIT_IS_CUSTOM_SMALL_EX_NOTIFICATION"
        const val EXTRA_CALLKIT_IS_SHOW_LOGO = "EXTRA_CALLKIT_IS_SHOW_LOGO"
        const val EXTRA_CALLKIT_IS_SHOW_MISSED_CALL_NOTIFICATION = "EXTRA_CALLKIT_IS_SHOW_MISSED_CALL_NOTIFICATION"
        const val EXTRA_CALLKIT_IS_SHOW_CALLBACK = "EXTRA_CALLKIT_IS_SHOW_CALLBACK"
        const val EXTRA_CALLKIT_RINGTONE_PATH = "EXTRA_CALLKIT_RINGTONE_PATH"
        const val EXTRA_CALLKIT_BACKGROUND_URL = "EXTRA_CALLKIT_BACKGROUND_URL"
        const val EXTRA_CALLKIT_ACTION_COLOR = "EXTRA_CALLKIT_ACTION_COLOR"
        const val EXTRA_CALLKIT_INCOMING_CALL_NOTIFICATION_CHANNEL_NAME = "EXTRA_CALLKIT_INCOMING_CALL_NOTIFICATION_CHANNEL_NAME"
        const val EXTRA_CALLKIT_MISSED_CALL_NOTIFICATION_CHANNEL_NAME = "EXTRA_CALLKIT_MISSED_CALL_NOTIFICATION_CHANNEL_NAME"

        const val EXTRA_CALLKIT_ACTION_FROM = "EXTRA_CALLKIT_ACTION_FROM"

        fun getIntentIncoming(context: Context, data: Bundle?) =
                Intent(context, CallkitIncomingBroadcastReceiver::class.java).apply {
                    action = "${context.packageName}.${ACTION_CALL_INCOMING}"
                    putExtra(EXTRA_CALLKIT_INCOMING_DATA, data)
                }

        fun getIntentStart(context: Context, data: Bundle?) =
                Intent(context, CallkitIncomingBroadcastReceiver::class.java).apply {
                    action = "${context.packageName}.${ACTION_CALL_START}"
                    putExtra(EXTRA_CALLKIT_INCOMING_DATA, data)
                }

        fun getIntentAccept(context: Context, data: Bundle?) =
                Intent(context, CallkitIncomingBroadcastReceiver::class.java).apply {
                    action = "${context.packageName}.${ACTION_CALL_ACCEPT}"
                    putExtra(EXTRA_CALLKIT_INCOMING_DATA, data)
                }

        fun getIntentDecline(context: Context, data: Bundle?) =
                Intent(context, CallkitIncomingBroadcastReceiver::class.java).apply {
                    action = "${context.packageName}.${ACTION_CALL_DECLINE}"
                    putExtra(EXTRA_CALLKIT_INCOMING_DATA, data)
                }

        fun getIntentEnded(context: Context, data: Bundle?) =
                Intent(context, CallkitIncomingBroadcastReceiver::class.java).apply {
                    action = "${context.packageName}.${ACTION_CALL_ENDED}"
                    putExtra(EXTRA_CALLKIT_INCOMING_DATA, data)
                }

        fun getIntentTimeout(context: Context, data: Bundle?) =
                Intent(context, CallkitIncomingBroadcastReceiver::class.java).apply {
                    action = "${context.packageName}.${ACTION_CALL_TIMEOUT}"
                    putExtra(EXTRA_CALLKIT_INCOMING_DATA, data)
                }

        fun getIntentCallback(context: Context, data: Bundle?) =
                Intent(context, CallkitIncomingBroadcastReceiver::class.java).apply {
                    action = "${context.packageName}.${ACTION_CALL_CALLBACK}"
                    putExtra(EXTRA_CALLKIT_INCOMING_DATA, data)
                }
    }


    @SuppressLint("MissingPermission")
    override fun onReceive(context: Context, intent: Intent) {
        val callkitNotificationManager = CallkitNotificationManager(context)
        val action = intent.action ?: return
        val data = intent.extras?.getBundle(EXTRA_CALLKIT_INCOMING_DATA) ?: return
        when (action) {
            "${context.packageName}.${ACTION_CALL_INCOMING}" -> {
                try {
                    callkitNotificationManager.showIncomingNotification(data)
                    sendEventFlutter(ACTION_CALL_INCOMING, data)
                    addCall(context, Data.fromBundle(data))

                    if (callkitNotificationManager.incomingChannelEnabled()) {
                        val soundPlayerServiceIntent =
                            Intent(context, CallkitSoundPlayerService::class.java)
                        soundPlayerServiceIntent.putExtras(data)
                        context.startService(soundPlayerServiceIntent)
                    }
                } catch (error: Exception) {
                    error.printStackTrace()
                }
            }
            "${context.packageName}.${ACTION_CALL_START}" -> {
                try {
                    sendEventFlutter(ACTION_CALL_START, data)
                    addCall(context, Data.fromBundle(data), true)
                } catch (error: Exception) {
                    error.printStackTrace()
                }
            }
            "${context.packageName}.${ACTION_CALL_ACCEPT}" -> {
                try {
                    sendEventFlutter(ACTION_CALL_ACCEPT, data)
                    context.stopService(Intent(context, CallkitSoundPlayerService::class.java))
                    callkitNotificationManager.clearIncomingNotification(data)
                    addCall(context, Data.fromBundle(data), true)
                } catch (error: Exception) {
                    error.printStackTrace()
                }
            }
            "${context.packageName}.${ACTION_CALL_DECLINE}" -> {
                try {
                    sendEventFlutter(ACTION_CALL_DECLINE, data)
                    context.stopService(Intent(context, CallkitSoundPlayerService::class.java))
                    callkitNotificationManager.clearIncomingNotification(data)
                    removeCall(context, Data.fromBundle(data))
                } catch (error: Exception) {
                    error.printStackTrace()
                }
            }
            "${context.packageName}.${ACTION_CALL_ENDED}" -> {
                try {
                    sendEventFlutter(ACTION_CALL_ENDED, data)
                    context.stopService(Intent(context, CallkitSoundPlayerService::class.java))
                    callkitNotificationManager.clearIncomingNotification(data)
                    removeCall(context, Data.fromBundle(data))
                } catch (error: Exception) {
                    error.printStackTrace()
                }
            }
            "${context.packageName}.${ACTION_CALL_TIMEOUT}" -> {
                try {
                    sendEventFlutter(ACTION_CALL_TIMEOUT, data)
                    context.stopService(Intent(context, CallkitSoundPlayerService::class.java))
                    if (data.getBoolean(EXTRA_CALLKIT_IS_SHOW_MISSED_CALL_NOTIFICATION, true)) {
                        callkitNotificationManager.showMissCallNotification(data)
                    }
                    removeCall(context, Data.fromBundle(data))
                } catch (error: Exception) {
                    error.printStackTrace()
                }
            }
            "${context.packageName}.${ACTION_CALL_CALLBACK}" -> {
                try {
                    callkitNotificationManager.clearMissCallNotification(data)
                    sendEventFlutter(ACTION_CALL_CALLBACK, data)
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
                        val closeNotificationPanel = Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)
                        context.sendBroadcast(closeNotificationPanel)
                    }
                } catch (error: Exception) {
                    error.printStackTrace()
                }
            }
        }
    }
}