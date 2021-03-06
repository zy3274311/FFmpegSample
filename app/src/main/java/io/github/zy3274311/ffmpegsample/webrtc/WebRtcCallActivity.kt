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
        val options = PeerConnectionFactory
            .InitializationOptions
            .builder(applicationContext)
            .createInitializationOptions()
        PeerConnectionFactory.initialize(options)

        localSurfaceView = findViewById(R.id.local_surface)
        localSurfaceView?.init(eglBase.eglBaseContext, null)
        videoSinkProxy.setTarget(localSurfaceView)

        findViewById<Button>(R.id.switchCameraBtn).setOnClickListener {
            videoCapturer?.switchCamera(null)
        }
        findViewById<Button>(R.id.joinSessionBtn).setOnClickListener {
            joinSession()
        }
        findViewById<Button>(R.id.leaveSessionBtn).setOnClickListener {
            leaveSession()
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

    private fun joinSession(){
    }

    private fun leaveSession() {
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
                        peerConnection?.setLocalDescription(MySdpObserver("Local SdpObserver"), offerSdp)
                        lifecycleScope.launch(Dispatchers.Main) {
                            val body = requestPublish(offerSdp.description)
                            if (body == null) {
                                Log.e(TAG, "requestPublish fail")
                            } else {
                                Log.e(TAG, "requestPublish success $body")
                                // offer sdp???m=xxx???answer sdp???m=xxx????????????????????????????????? offer sdp ???????????????????????????m=video?????????answer sdp ???????????????m=audio ???
                                // https://www.jianshu.com/p/f37009f625f9
//                                val remoteSessionDescription = SessionDescription(SessionDescription.Type.ANSWER, body.sdp)
//                                peerConnection?.setRemoteDescription(MySdpObserver(), remoteSessionDescription)
                                val remoteSdp = body.sdp
                                val remoteDescription = convertAnswerSdp(offerSdp.description, remoteSdp)
                                val remoteSessionDescription = SessionDescription(SessionDescription.Type.ANSWER, remoteDescription)
                                peerConnection?.setRemoteDescription(MySdpObserver("Remote SdpObserver"), remoteSessionDescription)
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
        // webrtc??????????????????????????????????????????h264??????
        // name.startsWith("OMX.qcom.")??????name.startsWith("OMX.Exynos.")
        // ??????????????????????????????remote sdp????????????
//        val videoEncoderFactory = DefaultVideoEncoderFactory(eglBase.eglBaseContext, true, true)
//        val hardwareVideoEncoderFactory = HardwareVideoEncoderFactory(eglBase.eglBaseContext, true, true)
//        val softwareVideoEncoderFactory = SoftwareVideoEncoderFactory()
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


        //??????audiosource???audiotrack
        val rtcConfig = PeerConnection.RTCConfiguration(emptyList())
        rtcConfig.sdpSemantics = PeerConnection.SdpSemantics.UNIFIED_PLAN

        val peerConnection = factory.createPeerConnection(rtcConfig, this)
//        val mediaStream = factory.createLocalMediaStream("local mediastream")
//        mediaStream.addTrack(videoTrack)
//        mediaStream.addTrack(audioTrack)
//        peerConnection?.addStream(mediaStream)

//        peerConnection?.addTrack(videoTrack)
//        peerConnection?.addTrack(audioTrack)

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
        //????????????
        audioConstraints.mandatory.add(
            MediaConstraints.KeyValuePair(
                "googEchoCancellation",
                "true"
            )
        )
        //????????????
        audioConstraints.mandatory.add(MediaConstraints.KeyValuePair("googAutoGainControl", "true"))
        //????????????
        audioConstraints.mandatory.add(MediaConstraints.KeyValuePair("googHighpassFilter", "true"))
        //????????????
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
     * ??????AnswerSdp
     * @param offerSdp offerSdp?????????offer????????????sdp
     * @param answerSdp answerSdp???????????????srs??????????????????sdp
     * @return ????????????AnswerSdp
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
            //????????????
            answerSdp
        } else {
            //??????????????????
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
        Log.i(TAG, "onFrameCaptured($p0)")
    }

    override fun onStandardizedIceConnectionChange(newState: PeerConnection.IceConnectionState?) {
        super.onStandardizedIceConnectionChange(newState)
        Log.i(TAG, "onStandardizedIceConnectionChange($newState)")
    }

    override fun onConnectionChange(newState: PeerConnection.PeerConnectionState?) {
        super.onConnectionChange(newState)
        Log.i(TAG, "onConnectionChange($newState)")
    }

    override fun onSelectedCandidatePairChanged(event: CandidatePairChangeEvent?) {
        super.onSelectedCandidatePairChanged(event)
        Log.i(TAG, "onSelectedCandidatePairChanged(local:${event?.local}, remote:${event?.remote})")
    }

    override fun onTrack(transceiver: RtpTransceiver?) {
        super.onTrack(transceiver)
        Log.i(TAG, "onTrack($transceiver)")
    }

    private class ProxyVideoSink : VideoSink {
        private var target: VideoSink? = null

        @Synchronized
        override fun onFrame(frame: VideoFrame) {
            if (target == null) {
                Log.e(TAG, "ProxyVideoSink tartget is null")
                return
            }
//            Log.e(TAG, "ProxyVideoSink onFrame(${frame.buffer})")
            target?.onFrame(frame)
        }

        @Synchronized
        fun setTarget(target: VideoSink?) {
            this.target = target
        }
    }

}