package io.github.zy3274311.ffmpegsample

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import io.github.zy3274311.ffmedia.FFMedia
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.nio.ByteBuffer

class DemuxerActivity : AppCompatActivity() {
    private val demuxer = FFMedia.createDemuxer()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_demuxer)
        findViewById<Button>(R.id.startRead).setOnClickListener {
            lifecycleScope.launch {
                startRead()
            }
        }
        lifecycleScope.launch {
            setDataSource()
        }
    }

    private suspend fun setDataSource() {
        withContext(Dispatchers.IO) {
            val file = File("/sdcard/1234.mp4")
            val uri = Uri.fromFile(file)
            demuxer.setDataSource(uri)
            val trackCount = demuxer.trackCount
            for (i in 0 until trackCount) {
                Log.e("zhangying", "i:$i")
                demuxer.getTrackFormat(i);
            }
            Log.e("zhangying", "trackCount:$trackCount")
        }
    }

    private suspend fun startRead():Int {
        return withContext(Dispatchers.IO) {
            val capacity = 1024 * 1024 * 2
            val buffer = ByteBuffer.allocateDirect(capacity)
            while (demuxer.readSampleData(buffer, 0) > 0) {
                buffer.clear()
            }
            1
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        demuxer.release()
    }

}