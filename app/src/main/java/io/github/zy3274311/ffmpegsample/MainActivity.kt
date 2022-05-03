package io.github.zy3274311.ffmpegsample

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import io.github.zy3274311.ffmpegmedia.FFmpegEngine

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val txt = FFmpegEngine().stringFromJNI()
        findViewById<TextView>(R.id.hello_tv).text = txt
        findViewById<Button>(R.id.startDemuxerBtn).setOnClickListener {
            startActivity(Intent(this, DemuxerActivity::class.java))
        }
    }
}