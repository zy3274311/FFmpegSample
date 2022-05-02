package io.github.zy3274311.ffmpegsample

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import io.github.zy3274311.ffmpegmedia.NativeLib

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val txt = NativeLib().stringFromJNI()
        findViewById<TextView>(R.id.hello_tv).text = txt
    }
}