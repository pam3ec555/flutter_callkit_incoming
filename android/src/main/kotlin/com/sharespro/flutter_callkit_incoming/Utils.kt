package com.sharespro.flutter_callkit_incoming

import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.os.Build
import android.os.PowerManager
import com.google.gson.Gson

class Utils {

    companion object {

        private var gson: Gson? = null

        fun getGsonInstance(): Gson {
            if(gson == null) gson = Gson()
            return gson!!
        }

        @JvmStatic
        fun dpToPx(dp: Float): Float {
            return dp * Resources.getSystem().displayMetrics.density
        }

        @JvmStatic
        fun pxToDp(px: Float): Float {
            return px / Resources.getSystem().displayMetrics.density
        }

        @JvmStatic
        fun getScreenWidth(): Int {
            return Resources.getSystem().displayMetrics.widthPixels
        }

        @JvmStatic
        fun getScreenHeight(): Int {
            return Resources.getSystem().displayMetrics.heightPixels
        }

        fun getNavigationBarHeight(context: Context): Int {
            val resources = context.resources
            val id = resources.getIdentifier(
                "navigation_bar_height", "dimen", "android"
            )
            return if (id > 0) {
                resources.getDimensionPixelSize(id)
            } else 0
        }

        fun getStatusBarHeight(context: Context): Int {
            val resources = context.resources
            val id: Int =
                resources.getIdentifier("status_bar_height", "dimen", "android")
            return if (id > 0) {
                resources.getDimensionPixelSize(id)
            } else 0
        }

        fun backToForeground(context: Context) {
            val packageName = context.packageName
            val intent = context.packageManager.getLaunchIntentForPackage(packageName)?.cloneFilter()

            intent?.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)

            intent?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        }

        fun isDeviceLocked(context: Context): Boolean {
            val isLocked = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                val keyguardManager: KeyguardManager = context.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
                keyguardManager.isDeviceLocked
            } else {
                val powerManager: PowerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager;
                !powerManager.isInteractive
            }
            return isLocked
        }

    }
}
