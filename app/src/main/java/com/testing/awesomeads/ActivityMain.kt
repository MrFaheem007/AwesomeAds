package com.testing.awesomeads

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.testing.awesomeads.databinding.ActivityMainBinding

class ActivityMain:AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding=ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


    }
}