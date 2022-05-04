package io.github.zy3274311.ffmpegsample

import android.Manifest.permission.*
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import io.github.zy3274311.ffmedia.FFMedia


class MainActivity : AppCompatActivity() {
    private val permissions = arrayOf(
        WRITE_EXTERNAL_STORAGE,
        READ_EXTERNAL_STORAGE,
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val txt = FFMedia().stringFromJNI()
        findViewById<TextView>(R.id.hello_tv).text = txt
        findViewById<Button>(R.id.startDemuxerBtn).setOnClickListener {
            startActivity(Intent(this, DemuxerActivity::class.java))
        }
        ActivityCompat.requestPermissions(this, permissions, 0)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }
}