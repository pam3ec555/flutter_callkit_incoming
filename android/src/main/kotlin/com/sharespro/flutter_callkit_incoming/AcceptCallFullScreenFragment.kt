package com.sharespro.flutter_callkit_incoming

import android.app.Service
import android.media.AudioManager
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView

class AcceptCallFullScreenFragment : Fragment() {
    private lateinit var nameCallerTV: TextView
    private lateinit var hangUpButton: ImageButton
    private lateinit var muteButton: ImageButton

    private var isMuted: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_accept_call_full_screen, container, false)
        initView(view)
        incomingData()
        return view
    }

    private fun initView(view: View) {
        nameCallerTV = view.findViewById(R.id.callerName)
        hangUpButton = view.findViewById(R.id.hangUpButton)
        muteButton = view.findViewById(R.id.muteButton)

        hangUpButton.setOnClickListener {
            onHangUpPressed()
        }

        muteButton.setOnClickListener {
            onMutePressed()
        }
    }

    private fun incomingData() {
        val activity = requireActivity()
        val data =
            activity.intent.extras?.getBundle(CallkitIncomingBroadcastReceiver.EXTRA_CALLKIT_INCOMING_DATA)
        if (data == null) activity.finish()

        nameCallerTV.text =
            data?.getString(CallkitIncomingBroadcastReceiver.EXTRA_CALLKIT_NAME_CALLER, "")
    }

    private fun onHangUpPressed() {
        val activity = requireActivity()
        val data = activity.intent.extras?.getBundle(CallkitIncomingBroadcastReceiver.EXTRA_CALLKIT_INCOMING_DATA)
        val intent =
            CallkitIncomingBroadcastReceiver.getIntentEnded(activity, data)
        activity.sendBroadcast(intent)
        activity.finishAndRemoveTask()
    }

    private fun onMutePressed() {
        val activity = requireActivity()
        val audioManager = activity.getSystemService(Service.AUDIO_SERVICE) as AudioManager
        isMuted = !isMuted
        audioManager.isMicrophoneMute = isMuted
    }
}