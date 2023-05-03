package com.sharespro.flutter_callkit_incoming

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.Window
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.sharespro.flutter_callkit_incoming.CallkitIncomingBroadcastReceiver.Companion.ACTION_CALL_INCOMING
import com.sharespro.flutter_callkit_incoming.CallkitIncomingBroadcastReceiver.Companion.EXTRA_CALLKIT_INCOMING_DATA


class CallkitActivity : AppCompatActivity() {

    companion object {

        const val ACTION_ENDED_CALL_INCOMING = "ACTION_ENDED_CALL_INCOMING"

        fun getIntent(context: Context, data: Bundle) = Intent(ACTION_CALL_INCOMING).apply {
            action = "${context.packageName}.${ACTION_CALL_INCOMING}"
            putExtra(EXTRA_CALLKIT_INCOMING_DATA, data)
            flags =
                Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
        }

        fun getIntentEnded(context: Context) =
            Intent("${context.packageName}.${ACTION_ENDED_CALL_INCOMING}")

    }

    inner class EndedCallkitIncomingBroadcastReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (!isFinishing) {
                finishAndRemoveTask()
            }
        }
    }

    private var endedCallkitIncomingBroadcastReceiver = EndedCallkitIncomingBroadcastReceiver()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            setTurnScreenOn(true)
            setShowWhenLocked(true)
        } else {
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            window.addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON)
            window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED)
            window.addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD)
        }
        transparentStatusAndNavigation()
        initView()
        setContentView(R.layout.activity_callkit_incoming)
        registerReceiver(
            endedCallkitIncomingBroadcastReceiver,
            IntentFilter("$packageName.$ACTION_ENDED_CALL_INCOMING")
        )
    }

    private fun initView() {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.fullscreenCallFrameLayout, IncomingCallFullScreenFragment())
        transaction.commit()
    }

    private fun transparentStatusAndNavigation() {
        setWindowFlag(
            WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
                    or WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION, true
        )
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION)
        setWindowFlag(
            (WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
                    or WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION), false
        )
        window.statusBarColor = Color.TRANSPARENT
        window.navigationBarColor = Color.TRANSPARENT
    }

    private fun setWindowFlag(bits: Int, on: Boolean) {
        val win: Window = window
        val winParams: WindowManager.LayoutParams = win.attributes
        if (on) {
            winParams.flags = winParams.flags or bits
        } else {
            winParams.flags = winParams.flags and bits.inv()
        }
        win.attributes = winParams
    }

    override fun onDestroy() {
        unregisterReceiver(endedCallkitIncomingBroadcastReceiver)
        super.onDestroy()
    }
}
