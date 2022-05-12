package io.github.zy3274311.ffmpegsample

import android.net.Uri
import android.os.Bundle
import android.os.SharedMemory
import android.os.Trace
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
import java.nio.MappedByteBuffer

class DemuxerActivity : AppCompatActivity() {
    private val http_flv = "http://101.43.226.106:8080/live/livestream.flv"
    private val http_mp4 = "http://wos.58cdn.com.cn/gHeDcBazgLk/videotransform/4eacb36946404ee39d96f7d80f33343d.m204.mp4"
    private val demuxer = FFMedia.createDemuxer()
    override fun onCreate(savedInstanceState: Bundle?) {
        Trace.beginSection("DemuxerActivity onCreate")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_demuxer)
        findViewById<Button>(R.id.startRead).setOnClickListener {
            lifecycleScope.launch {
                readNextFrame()
            }
        }
        lifecycleScope.launch {
            setDataSource()
        }
        Trace.endSection()
    }

    private suspend fun setDataSource() {
        withContext(Dispatchers.Default) {
            Log.e("zhangying", "setDataSource start")
            val currThread = Thread.currentThread()
            Log.e("zhangying", "setDataSource threadId:${currThread.id}  threadName:${currThread.name}")

            val file = File("/sdcard/1234.mp4")
            val uri = Uri.fromFile(file)
            val flvUri = Uri.parse(http_flv)
            val mp4Uri = Uri.parse(http_mp4)

            demuxer.setDataSource(mp4Uri)
//            val trackCount = demuxer.trackCount
//            for (i in 0 until trackCount) {
//                Log.e("zhangying", "i:$i")
//                demuxer.getTrackFormat(i);
//            }
//            Log.e("zhangying", "trackCount:$trackCount")


            Log.e("zhangying", "setDataSource end")
        }
    }

    private suspend fun readNextFrame():Int {
        return withContext(Dispatchers.Default) {
            Log.e("zhangying", "readNextFrame start")
            val capacity = 1024 * 1024 * 2
            val buffer = ByteBuffer.allocateDirect(capacity)
            val ret = demuxer.readSampleData(buffer, 0)
            Log.e("zhangying", "readNextFrame:$ret")
            buffer.clear()
            ret
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        freeDemuxer()
    }

    private fun freeDemuxer(){
        demuxer.release()
    }

}