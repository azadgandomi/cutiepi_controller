package com.sazadgankar.picontroller

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity

class FirstActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_first)
    }

    fun startLocalNetworkActivity(view: View) {
        startActivity(Intent(this, LocalNetworkConnectionActivity::class.java))
    }

    fun startInternetActivity(view: View) {
        startActivity(Intent(this, InternetConnectionActivity::class.java))
    }
}
