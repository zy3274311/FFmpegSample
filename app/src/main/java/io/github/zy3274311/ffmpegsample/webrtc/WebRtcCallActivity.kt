package io.github.zy3274311.ffmpegsample.webrtc

import android.content.Context
import android.media.MediaCodecInfo
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import io.github.zy3274311.ffmpegsample.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.webrtc.*
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


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

    private val retrofit = generateRetrofit()
    private val srsWebService = getSrsWebService()
    private val videoSinkProxy = ProxyVideoSink()


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
        videoSinkProxy.setTarget(localSurfaceView)

        findViewById<Button>(R.id.switchCameraBtn).setOnClickListener {
            videoCapturer?.switchCamera(null)
        }

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
            override fun onCreateSuccess(offerSdp: SessionDescription?) {
                if (offerSdp == null) {
                    return
                }
                Log.i(TAG, "onCreateSuccess(${offerSdp.description})")
                when (offerSdp.type) {
                    SessionDescription.Type.OFFER -> {
                        peerConnection?.setLocalDescription(MySdpObserver(), offerSdp)
                        lifecycleScope.launch(Dispatchers.Main) {
                            val body = requestPublish(offerSdp.description)
                            if (body == null) {
                                Log.e(TAG, "requestPublish fail")
                            } else {
                                Log.e(TAG, "requestPublish success $body")
                                // offer sdp中m=xxx和answer sdp中m=xxx顺序不对，大概意思就是 offer sdp 中比如第一个顺序是m=video，但是answer sdp 中第一个是m=audio ；
                                // https://www.jianshu.com/p/f37009f625f9
//                                val remoteSessionDescription = SessionDescription(SessionDescription.Type.ANSWER, body.sdp)
//                                peerConnection?.setRemoteDescription(MySdpObserver(), remoteSessionDescription)
                                val remoteSdp = body.sdp
                                val remoteDescription = convertAnswerSdp(offerSdp.description, remoteSdp)
                                val answerSdp = SessionDescription(SessionDescription.Type.ANSWER, remoteDescription)
                                peerConnection?.setRemoteDescription(MySdpObserver(), answerSdp)
                            }
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

    private fun generateRetrofit(): Retrofit {
        val retrofit = Retrofit.Builder()
            .baseUrl("http://101.43.226.106:1985")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        return retrofit
    }

    private fun getSrsWebService(): SrsWebService {
        val srsWebService = retrofit.create(SrsWebService::class.java)
        return srsWebService
    }

    private suspend fun requestPublish(sdp: String): SrsPublishResponseBody? {
        return withContext(Dispatchers.Default) {
            val body = SrsPublishRequestBody(sdp, getStreamUrl())
            val call = srsWebService.requestPublish(body)
            val response = call.execute()
            if (response.isSuccessful) {
                response.body()
            } else {
                val code = response.code()
                val msg = response.message()
                null
            }
        }
    }

    private fun getStreamUrl(): String {
        return "webrtc://101.43.226.106/live/livestream"
    }

    private fun generatePeerConnectionFactory(): PeerConnectionFactory {
        val options = PeerConnectionFactory.Options()
        val encoderFactory = createCustomVideoEncoderFactory(eglBase.eglBaseContext, true, true,
            object : VideoEncoderSupportedCallback {
                override fun isSupportedVp8(info: MediaCodecInfo): Boolean {
                    return true
                }

                override fun isSupportedVp9(info: MediaCodecInfo): Boolean {
                    return true
                }

                override fun isSupportedH264(info: MediaCodecInfo): Boolean {
                    return true
                }

            })
        val videoEncoderFactory = DefaultVideoEncoderFactory(eglBase.eglBaseContext, false, true)
        val videoDecoderFactory = DefaultVideoDecoderFactory(eglBase.eglBaseContext)
        return PeerConnectionFactory.builder()
            .setOptions(options)
            .setVideoDecoderFactory(videoDecoderFactory)
            .setVideoEncoderFactory(encoderFactory)
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
                    addSink(videoSinkProxy)
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

    /**
     * 转换AnswerSdp
     * @param offerSdp offerSdp：创建offer时生成的sdp
     * @param answerSdp answerSdp：网络请求srs服务器返回的sdp
     * @return 转换后的AnswerSdp
     */
    private fun convertAnswerSdp(offerSdp: String, answerSdp: String?): String {
        if (answerSdp.isNullOrBlank()) {
            return ""
        }
        val indexOfOfferVideo = offerSdp.indexOf("m=video")
        val indexOfOfferAudio = offerSdp.indexOf("m=audio")
        if (indexOfOfferVideo == -1 || indexOfOfferAudio == -1) {
            return answerSdp
        }
        val indexOfAnswerVideo = answerSdp.indexOf("m=video")
        val indexOfAnswerAudio = answerSdp.indexOf("m=audio")
        if (indexOfAnswerVideo == -1 || indexOfAnswerAudio == -1) {
            return answerSdp
        }

        val isFirstOfferVideo = indexOfOfferVideo < indexOfOfferAudio
        val isFirstAnswerVideo = indexOfAnswerVideo < indexOfAnswerAudio
        return if (isFirstOfferVideo == isFirstAnswerVideo) {
            //顺序一致
            answerSdp
        } else {
            //需要调换顺序
            buildString {
                append(answerSdp.substring(0, indexOfAnswerVideo.coerceAtMost(indexOfAnswerAudio)))
                append(
                    answerSdp.substring(
                        indexOfAnswerVideo.coerceAtLeast(indexOfOfferVideo),
                        answerSdp.length
                    )
                )
                append(
                    answerSdp.substring(
                        indexOfAnswerVideo.coerceAtMost(indexOfAnswerAudio),
                        indexOfAnswerVideo.coerceAtLeast(indexOfOfferVideo)
                    )
                )
            }
        }
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
        Log.e(TAG, "onIceCandidate(${p0?.toString()})")

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

    private class ProxyVideoSink : VideoSink {
        private var target: VideoSink? = null

        @Synchronized
        override fun onFrame(frame: VideoFrame) {
            if (target == null) {
                Log.e(TAG, "ProxyVideoSink tartget is null")
                return
            }
            Log.e(TAG, "ProxyVideoSink onFrame($frame)")
            target?.onFrame(frame)
        }

        @Synchronized
        fun setTarget(target: VideoSink?) {
            this.target = target
        }
    }

}