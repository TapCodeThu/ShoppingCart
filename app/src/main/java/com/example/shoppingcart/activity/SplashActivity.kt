package com.example.shoppingcart.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.core.content.ContextCompat
import com.example.shoppingcart.R
import com.example.shoppingcart.databinding.ActivitySplashBinding
import com.google.firebase.auth.FirebaseAuth

class SplashActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySplashBinding
    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window.statusBarColor = ContextCompat.getColor(this,R.color.sky_bg)

        auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser
        Handler(Looper.myLooper()!!).postDelayed(Runnable {

            if(currentUser != null){
                startActivity(Intent(this,MainActivity::class.java))
                finish()
            }
            else{
                startActivity(Intent(this,SignInActivity::class.java))
                finish()
            }





        },2000)

    }
}