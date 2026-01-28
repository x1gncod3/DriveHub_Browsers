package com.drivehub.browser

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.progressindicator.LinearProgressIndicator

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContentView(R.layout.splash_activity)
        
        val splashProgress = findViewById<LinearProgressIndicator>(R.id.splashProgress)
        val progressText = findViewById<android.widget.TextView>(R.id.progressText)
        val loadingText = findViewById<android.widget.TextView>(R.id.loadingText)
        
        // Animate progress from 0 to 100% over 2 seconds
        var progress = 0
        val totalDuration = 2000L // 2 seconds
        val updateInterval = 20L // Update every 20ms for smooth animation
        val totalSteps = (totalDuration / updateInterval).toInt()
        val progressIncrement = 100.0 / totalSteps
        
        val handler = Handler(Looper.getMainLooper())
        val runnable = object : Runnable {
            override fun run() {
                if (progress <= 100) {
                    splashProgress.setProgressCompat(progress, true)
                    progressText.text = "${progress}%"
                    
                    // Update loading text based on progress
                    loadingText.text = when {
                        progress < 30 -> "Initializing engine..."
                        progress < 60 -> "Loading components..."
                        progress < 90 -> "Almost ready..."
                        progress < 100 -> "Finalizing..."
                        else -> "Launching DriveHub Browser..."
                    }
                    
                    progress += 1
                    handler.postDelayed(this, updateInterval)
                } else {
                    // Navigate to main activity when complete
                    startActivity(Intent(this@SplashActivity, MainActivity::class.java))
                    finish()
                }
            }
        }
        
        handler.post(runnable)
    }
}
