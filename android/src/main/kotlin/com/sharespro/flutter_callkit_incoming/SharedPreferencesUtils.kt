package com.sharespro.flutter_callkit_incoming

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import com.google.gson.reflect.TypeToken


private const val CALLKIT_PREFERENCES_FILE_NAME = "flutter_callkit_incoming"
private var prefs: SharedPreferences? = null
private var editor: SharedPreferences.Editor? = null

private fun initInstance(context: Context) {
    prefs = context.getSharedPreferences(CALLKIT_PREFERENCES_FILE_NAME, Context.MODE_PRIVATE)
    editor = prefs?.edit()
}


fun addCall(context: Context?, data: Data, isAccepted: Boolean = false) {
    val json = getString(context, "ACTIVE_CALLS", "[]")
    val arrayData: ArrayList<Data> = Utils.getGsonInstance()
        .fromJson(json, object : TypeToken<ArrayList<Data>>() {}.type)
    val currentData = arrayData.find { it == data }
    if(currentData != null) {
        currentData.isAccepted = isAccepted
    }else {
        arrayData.add(data)
    }
    putString(context, "ACTIVE_CALLS", Utils.getGsonInstance().toJson(arrayData))
}

fun sendEventFlutter(event: String, data: Bundle) {
    val android = mapOf(
        "isCustomNotification" to data.getBoolean(CallkitIncomingBroadcastReceiver.EXTRA_CALLKIT_IS_CUSTOM_NOTIFICATION, false),
        "isCustomSmallExNotification" to data.getBoolean(
            CallkitIncomingBroadcastReceiver.EXTRA_CALLKIT_IS_CUSTOM_SMALL_EX_NOTIFICATION,
            false
        ),
        "ringtonePath" to data.getString(CallkitIncomingBroadcastReceiver.EXTRA_CALLKIT_RINGTONE_PATH, ""),
        "backgroundUrl" to data.getString(CallkitIncomingBroadcastReceiver.EXTRA_CALLKIT_BACKGROUND_URL, ""),
        "actionColor" to data.getString(CallkitIncomingBroadcastReceiver.EXTRA_CALLKIT_ACTION_COLOR, ""),
        "incomingCallNotificationChannelName" to data.getString(
            CallkitIncomingBroadcastReceiver.EXTRA_CALLKIT_INCOMING_CALL_NOTIFICATION_CHANNEL_NAME,
            ""
        ),
        "missedCallNotificationChannelName" to data.getString(
            CallkitIncomingBroadcastReceiver.EXTRA_CALLKIT_MISSED_CALL_NOTIFICATION_CHANNEL_NAME,
            ""
        ),
    )
    val forwardData = mapOf(
        "id" to data.getString(CallkitIncomingBroadcastReceiver.EXTRA_CALLKIT_ID, ""),
        "nameCaller" to data.getString(CallkitIncomingBroadcastReceiver.EXTRA_CALLKIT_NAME_CALLER, ""),
        "avatar" to data.getString(CallkitIncomingBroadcastReceiver.EXTRA_CALLKIT_AVATAR, ""),
        "number" to data.getString(CallkitIncomingBroadcastReceiver.EXTRA_CALLKIT_HANDLE, ""),
        "type" to data.getInt(CallkitIncomingBroadcastReceiver.EXTRA_CALLKIT_TYPE, 0),
        "duration" to data.getLong(CallkitIncomingBroadcastReceiver.EXTRA_CALLKIT_DURATION, 0L),
        "textAccept" to data.getString(CallkitIncomingBroadcastReceiver.EXTRA_CALLKIT_TEXT_ACCEPT, ""),
        "textDecline" to data.getString(CallkitIncomingBroadcastReceiver.EXTRA_CALLKIT_TEXT_DECLINE, ""),
        "textMissedCall" to data.getString(CallkitIncomingBroadcastReceiver.EXTRA_CALLKIT_TEXT_MISSED_CALL, ""),
        "textCallback" to data.getString(CallkitIncomingBroadcastReceiver.EXTRA_CALLKIT_TEXT_CALLBACK, ""),
        "extra" to data.getSerializable(CallkitIncomingBroadcastReceiver.EXTRA_CALLKIT_EXTRA) as HashMap<String, Any?>,
        "android" to android
    )
    FlutterCallkitIncomingPlugin.sendEvent(event, forwardData)
}

fun removeCall(context: Context?, data: Data) {
    val json = getString(context, "ACTIVE_CALLS", "[]")
    val arrayData: ArrayList<Data> = Utils.getGsonInstance()
        .fromJson(json, object : TypeToken<ArrayList<Data>>() {}.type)
    arrayData.remove(data)
    putString(context, "ACTIVE_CALLS", Utils.getGsonInstance().toJson(arrayData))
}

fun removeAllCalls(context: Context?) {
    putString(context, "ACTIVE_CALLS", "[]")
    remove(context, "ACTIVE_CALLS")
}

fun getActiveCalls(context: Context?): String {
    val json = getString(context, "ACTIVE_CALLS", "[]")
    val arrayData: ArrayList<Data> = Utils.getGsonInstance()
        .fromJson(json, object : TypeToken<ArrayList<Data>>() {}.type)
    return Utils.getGsonInstance().toJson(arrayData)
}

fun getDataActiveCalls(context: Context?): ArrayList<Data> {
    val json = getString(context, "ACTIVE_CALLS", "[]")
    return Utils.getGsonInstance()
        .fromJson(json, object : TypeToken<ArrayList<Data>>() {}.type)
}

fun getDataActiveCallsForFlutter(context: Context?): ArrayList<Map<String, Any?>> {
    val json = getString(context, "ACTIVE_CALLS", "[]")
    return Utils.getGsonInstance().fromJson(json, object: TypeToken<ArrayList<Map<String, Any?>>>() {}.type)
}

fun putString(context: Context?, key: String, value: String?) {
    if (context == null) return
    initInstance(context)
    editor?.putString(key, value)
    editor?.commit()
}

fun getString(context: Context?, key: String, defaultValue: String = ""): String? {
    if (context == null) return null
    initInstance(context)
    return prefs?.getString(key, defaultValue)
}

fun remove(context: Context?, key: String) {
    if (context == null) return
    initInstance(context)
    editor?.remove(key)
    editor?.commit()
}
