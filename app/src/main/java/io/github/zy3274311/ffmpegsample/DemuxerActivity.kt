package io.github.zy3274311.ffmpegsample

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import io.github.zy3274311.ffmpegmedia.FFmpegEngine

class DemuxerActivity : AppCompatActivity() {
    private val demuxer = FFmpegEngine.createDemuxer()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_demuxer)
    }

    override fun onDestroy() {
        super.onDestroy()
        demuxer.release()
    }
}