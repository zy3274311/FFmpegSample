package io.github.zy3274311.ffmpegsample.webrtc

import android.util.Log
import org.webrtc.SdpObserver
import org.webrtc.SessionDescription

open class MySdpObserver(label:String) : SdpObserver {
    private val TAG = label

    override fun onCreateSuccess(sdp: SessionDescription?) {
        Log.i(TAG, "onCreateSuccess(${sdp?.description})")

    }

    override fun onSetSuccess() {
        Log.i(TAG, "onSetSuccess()")
    }

    override fun onCreateFailure(p0: String?) {
        Log.i(TAG, "onCreateFailure($p0)")
    }

    override fun onSetFailure(p0: String?) {
        Log.i(TAG, "onSetFailure($p0)")
    }
}