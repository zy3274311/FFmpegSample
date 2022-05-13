package io.github.zy3274311.ffmpegsample

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import org.webrtc.*

class WebRtcCallActivity : AppCompatActivity(), PeerConnection.Observer,
    CameraVideoCapturer.CameraEventsHandler, CapturerObserver {
    companion object {
        private const val TAG = "WebRtcCallActivity"
    }

    private val eglBase = EglBase.create()
    private var localSurfaceView: SurfaceViewRenderer? = null
    private var peerConnectionFactory: PeerConnectionFactory? = null
    private var peerConnection: PeerConnection? = null
    private var videoCapturer: CameraVideoCapturer? = null
    private var videoTrack: VideoTrack? = null
    private var audioTrack: AudioTrack? = null
    private var surfaceTextureHelper: SurfaceTextureHelper? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_webrtc_call)
        PeerConnectionFactory.initialize(
            PeerConnectionFactory
                .InitializationOptions
                .builder(applicationContext)
                .createInitializationOptions()
        )
        localSurfaceView = findViewById(R.id.local_surface)
        localSurfaceView?.init(eglBase.eglBaseContext, null)

        initRTC()
    }

    override fun onDestroy() {
        super.onDestroy()
        localSurfaceView?.release()
        videoCapturer?.dispose()
        surfaceTextureHelper?.dispose()
        videoTrack?.dispose()
        audioTrack?.dispose()
        peerConnection?.dispose()
        peerConnectionFactory?.dispose()
    }

    private fun initRTC() {
        peerConnection = generatePeerConnection()
        peerConnection?.createOffer(object : SdpObserver {
            /* SdpObserver --------------------------------------------------------- */
            override fun onCreateSuccess(sdp: SessionDescription?) {
                if (sdp == null) {
                    return
                }
                Log.i(TAG, "onCreateSuccess(${sdp.description})")
                when (sdp.type) {
                    SessionDescription.Type.OFFER -> {
                        Handler(Looper.getMainLooper()).post {
                            peerConnection?.setLocalDescription(MySdpObserver(), sdp)
                        }
                    }
                    SessionDescription.Type.ANSWER -> {

                    }
                    SessionDescription.Type.PRANSWER -> {

                    }
                    else -> {}
                }
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
        }, MediaConstraints())
    }
    private fun freeRtc() {

    }

    private fun generatePeerConnectionFactory(): PeerConnectionFactory {
        val options = PeerConnectionFactory.Options()
        val videoEncoderFactory =DefaultVideoEncoderFactory(eglBase.eglBaseContext, true, true)
        val videoDecoderFactory = DefaultVideoDecoderFactory(eglBase.eglBaseContext)
        return PeerConnectionFactory.builder()
            .setOptions(options)
            .setVideoDecoderFactory(videoDecoderFactory)
            .setVideoEncoderFactory(videoEncoderFactory)
            .createPeerConnectionFactory()
    }

    private fun generatePeerConnection(): PeerConnection? {
        val factory = generatePeerConnectionFactory()
        peerConnectionFactory = factory
        val createAudioSource = factory.createAudioSource(createAudioConstraints())
        val aTrack = factory.createAudioTrack("local_audio_track", createAudioSource)
        audioTrack = aTrack
        val cameraVideoCapturer = createVideoCapture(applicationContext)
        videoCapturer = cameraVideoCapturer
        cameraVideoCapturer?.let { capture ->
            val videoSource = factory.createVideoSource(capture.isScreencast)
            val vTrack = factory.createVideoTrack("local_video_track", videoSource)
                .apply {
                    addSink(localSurfaceView)
                }
            videoTrack = vTrack
            val surfaceHelper =
                SurfaceTextureHelper.create("surface_texture_thread", eglBase.eglBaseContext)
            surfaceTextureHelper = surfaceHelper
            capture.initialize(surfaceTextureHelper, this, videoSource.capturerObserver)
            capture.startCapture(640, 480, 20)
        }


        //创建audiosource及audiotrack
        val rtcConfig = PeerConnection.RTCConfiguration(emptyList())
        rtcConfig.sdpSemantics = PeerConnection.SdpSemantics.UNIFIED_PLAN

        val peerConnection = factory.createPeerConnection(rtcConfig, this)
        peerConnection?.apply {
            addTransceiver(
                videoTrack,
                RtpTransceiver.RtpTransceiverInit(RtpTransceiver.RtpTransceiverDirection.SEND_ONLY)
            )
            addTransceiver(
                audioTrack,
                RtpTransceiver.RtpTransceiverInit(RtpTransceiver.RtpTransceiverDirection.SEND_ONLY)
            )
        }
        return peerConnection
    }


    private fun createAudioConstraints(): MediaConstraints {
        val audioConstraints = MediaConstraints()
        //回声消除
        audioConstraints.mandatory.add(
            MediaConstraints.KeyValuePair(
                "googEchoCancellation",
                "true"
            )
        )
        //自动增益
        audioConstraints.mandatory.add(MediaConstraints.KeyValuePair("googAutoGainControl", "true"))
        //高音过滤
        audioConstraints.mandatory.add(MediaConstraints.KeyValuePair("googHighpassFilter", "true"))
        //噪音处理
        audioConstraints.mandatory.add(
            MediaConstraints.KeyValuePair(
                "googNoiseSuppression",
                "true"
            )
        )
        return audioConstraints
    }

    private fun createVideoCapture(context: Context): CameraVideoCapturer? {
        val enumerator: CameraEnumerator = if (Camera2Enumerator.isSupported(context)) {
            Camera2Enumerator(context)
        } else {
            Camera1Enumerator()
        }
        for (name in enumerator.deviceNames) {
            if (enumerator.isFrontFacing(name)) {
                return enumerator.createCapturer(name, null)
            }
        }
        for (name in enumerator.deviceNames) {
            if (enumerator.isBackFacing(name)) {
                return enumerator.createCapturer(name, null)
            }
        }
        return null
    }

    /* PeerConnection.Observer --------------------------------------------------------- */
    override fun onSignalingChange(p0: PeerConnection.SignalingState?) {
        Log.i(TAG, "onSignalingChange($p0)")
    }

    override fun onIceConnectionChange(p0: PeerConnection.IceConnectionState?) {
        Log.i(TAG, "onIceConnectionChange($p0)")
    }

    override fun onIceConnectionReceivingChange(p0: Boolean) {
        Log.i(TAG, "onIceConnectionReceivingChange($p0)")
    }

    override fun onIceGatheringChange(p0: PeerConnection.IceGatheringState?) {
        Log.i(TAG, "onIceGatheringChange($p0)")
    }

    override fun onIceCandidate(p0: IceCandidate?) {
        Log.i(TAG, "onIceCandidate($p0)")
    }

    override fun onIceCandidatesRemoved(p0: Array<out IceCandidate>?) {
        Log.i(TAG, "onIceCandidatesRemoved($p0)")
    }

    override fun onAddStream(p0: MediaStream?) {
        Log.i(TAG, "onAddStream($p0)")
    }

    override fun onRemoveStream(p0: MediaStream?) {
        Log.i(TAG, "onRemoveStream($p0)")
    }

    override fun onDataChannel(p0: DataChannel?) {
        Log.i(TAG, "onDataChannel($p0)")
    }

    override fun onRenegotiationNeeded() {
        Log.i(TAG, "onRenegotiationNeeded()")
    }

    override fun onAddTrack(p0: RtpReceiver?, p1: Array<out MediaStream>?) {
        Log.i(TAG, "onAddTrack($p0, $p1)")
    }

    /* CameraEventsHandler --------------------------------------------------------- */
    override fun onCameraError(p0: String?) {
        Log.i(TAG, "onCameraError($p0)")
    }

    override fun onCameraDisconnected() {
        Log.i(TAG, "onCameraDisconnected()")
    }

    override fun onCameraFreezed(p0: String?) {
        Log.i(TAG, "onCameraFreezed($p0)")
    }

    override fun onCameraOpening(p0: String?) {
        Log.i(TAG, "onCameraOpening($p0)")
    }

    override fun onFirstFrameAvailable() {
        Log.i(TAG, "onFirstFrameAvailable()")
    }

    override fun onCameraClosed() {
        Log.i(TAG, "onCameraClosed()")
    }

    /* CapturerObserver --------------------------------------------------------- */
    override fun onCapturerStarted(p0: Boolean) {
        Log.i(TAG, "onCapturerStarted($p0)")
    }

    override fun onCapturerStopped() {
        Log.i(TAG, "onCapturerStopped()")
    }

    override fun onFrameCaptured(p0: VideoFrame?) {
//        Log.i(TAG, "onFrameCaptured($p0)")
    }

}