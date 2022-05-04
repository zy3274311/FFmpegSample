package io.github.zy3274311.ffmpegsample

import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import io.github.zy3274311.ffmedia.FFMedia
import java.io.File

class DemuxerActivity : AppCompatActivity() {
    private val demuxer = FFMedia.createDemuxer()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_demuxer)
        val file = File("/sdcard/1234.mp4")
        val uri = Uri.fromFile(file)
        demuxer.setDataSource(uri)
        val trackCount = demuxer.trackCount
        for (i in 0 until trackCount){
            Log.e("zhangying", "i:$i")
            demuxer.getTrackFormat(i);
        }
        Log.e("zhangying", "trackCount:$trackCount")
    }

    override fun onDestroy() {
        super.onDestroy()
        demuxer.release()
    }
}