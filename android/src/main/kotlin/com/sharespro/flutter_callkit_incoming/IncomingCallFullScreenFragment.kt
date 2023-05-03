package com.sharespro.flutter_callkit_incoming

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.PowerManager
import android.text.TextUtils
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.math.abs

class IncomingCallFullScreenFragment : Fragment() {
    private lateinit var tvNameCaller: TextView
    private lateinit var ivAvatar: CircleImageView

    private lateinit var llAction: LinearLayout
    private lateinit var ivAcceptCall: ImageView
    private lateinit var tvAccept: TextView

    private lateinit var ivDeclineCall: ImageView
    private lateinit var tvDecline: TextView

    private var hasAcceptedCall: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_incoming_call_full_screen, container, false)
        initView(view)
        incomingData()

        return view
    }

    private fun initView(view: View) {
        tvNameCaller = view.findViewById(R.id.callerName)
        ivAvatar = view.findViewById(R.id.callerAvatar)

        llAction = view.findViewById(R.id.llAction)

        val params = llAction.layoutParams as ViewGroup.MarginLayoutParams
        params.setMargins(0, 0, 0, Utils.getNavigationBarHeight(view.context))
        llAction.layoutParams = params

        ivAcceptCall = view.findViewById(R.id.ivAcceptCall)
        tvAccept = view.findViewById(R.id.tvAccept)
        ivDeclineCall = view.findViewById(R.id.ivDeclineCall)
        tvDecline = view.findViewById(R.id.tvDecline)
        animateAcceptCall()

        ivAcceptCall.setOnClickListener {
            onAcceptClick()
        }
        ivDeclineCall.setOnClickListener {
            onDeclineClick()
        }
    }

    private fun animateAcceptCall() {
        val context = requireContext()
        val shakeAnimation =
            AnimationUtils.loadAnimation(context, R.anim.shake_anim)
        ivAcceptCall.animation = shakeAnimation
    }

    private fun onAcceptClick() {
        val activity = requireActivity()
        val data = activity.intent.extras?.getBundle(CallkitIncomingBroadcastReceiver.EXTRA_CALLKIT_INCOMING_DATA)
        val intent = activity.packageManager.getLaunchIntentForPackage(activity.packageName)?.cloneFilter()
        if (activity.isTaskRoot) {
            intent?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        } else {
            intent?.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        var needToFinishActivityFlag = true
        if (intent != null) {
            val intentTransparent = TransparentActivity.getIntentAccept(activity, data)
            if (Utils.isDeviceLocked(activity)) {
                val transaction = activity.supportFragmentManager.beginTransaction()
                transaction.replace(R.id.fullscreenCallFrameLayout, AcceptCallFullScreenFragment())
                transaction.commit()
                needToFinishActivityFlag = false
            } else {
                activity.startActivities(arrayOf(intent, intentTransparent))
            }
        } else {
            val acceptIntent = CallkitIncomingBroadcastReceiver.getIntentAccept(activity, data)
            acceptIntent.addFlags(Intent.FLAG_RECEIVER_FOREGROUND)
            activity.sendBroadcast(acceptIntent)
        }

        if (data != null) {
            val context = requireContext()
            sendEventFlutter(CallkitIncomingBroadcastReceiver.ACTION_CALL_ACCEPT, data)
            context.stopService(Intent(context, CallkitSoundPlayerService::class.java))
            // TODO: fix
//            CallkitNotificationManager(context).clearIncomingNotification(data)
            addCall(context, Data.fromBundle(data), true)
        }

        if (needToFinishActivityFlag) {
            activity.finishAndRemoveTask()
        }

        hasAcceptedCall = true
    }

    private fun onDeclineClick() {
        val activity = requireActivity()
        val data = activity.intent.extras?.getBundle(CallkitIncomingBroadcastReceiver.EXTRA_CALLKIT_INCOMING_DATA)
        val intent =
            CallkitIncomingBroadcastReceiver.getIntentDecline(activity, data)
        activity.sendBroadcast(intent)
        activity.finishAndRemoveTask()
    }

    private fun incomingData() {
        val activity = requireActivity()
        val data = activity.intent.extras?.getBundle(CallkitIncomingBroadcastReceiver.EXTRA_CALLKIT_INCOMING_DATA)
        if (data == null) activity.finish()

        tvNameCaller.text = data?.getString(CallkitIncomingBroadcastReceiver.EXTRA_CALLKIT_NAME_CALLER, "")

        val callType = data?.getInt(CallkitIncomingBroadcastReceiver.EXTRA_CALLKIT_TYPE, 0) ?: 0
        if (callType > 0) {
            ivAcceptCall.setImageResource(R.drawable.ic_video)
        }
        wakeLockRequest()
        finishTimeout(data)

        val textAccept = data?.getString(CallkitIncomingBroadcastReceiver.EXTRA_CALLKIT_TEXT_ACCEPT, "")
        tvAccept.text = if (TextUtils.isEmpty(textAccept)) getString(R.string.text_accept) else textAccept
        val textDecline = data?.getString(CallkitIncomingBroadcastReceiver.EXTRA_CALLKIT_TEXT_DECLINE, "")
        tvDecline.text = if (TextUtils.isEmpty(textDecline)) getString(R.string.text_decline) else textDecline
    }

    private fun finishTimeout(data: Bundle?) {
        val duration = 60000L
        val activity = requireActivity()
        val currentSystemTime = System.currentTimeMillis()
        val timeStartCall =
            data?.getLong(CallkitNotificationManager.EXTRA_TIME_START_CALL, currentSystemTime)
                ?: currentSystemTime

        val timeOut = duration - abs(currentSystemTime - timeStartCall)
        Handler(Looper.getMainLooper()).postDelayed({
            // If accepted the call, then the conversation in process...
            if (hasAcceptedCall) return@postDelayed
            if (!activity.isFinishing) {
                activity.finishAndRemoveTask()
            }
        }, timeOut)
    }

    private fun wakeLockRequest() {
        val pm = requireActivity().getSystemService(Activity.POWER_SERVICE) as PowerManager
        val wakeLock = pm.newWakeLock(
            PowerManager.SCREEN_BRIGHT_WAKE_LOCK or PowerManager.FULL_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP,
            "Callkit:PowerManager"
        )
        wakeLock.acquire()
    }
}