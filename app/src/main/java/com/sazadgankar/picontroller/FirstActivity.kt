package com.sazadgankar.picontroller

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class FirstActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_first)
    }

    fun startLocalNetworkActivity() {
        startActivity(Intent(this, LocalNetworkConnectionActivity::class.java))
    }

    fun startInternetActivity() {
        startActivity(Intent(this, InternetConnectionActivity::class.java))
    }
}
