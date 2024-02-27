package com.example.shoppingcart.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.util.Patterns
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.example.shoppingcart.R
import com.example.shoppingcart.databinding.ActivityForgotPassBinding
import com.google.firebase.auth.FirebaseAuth

class ForgotPassActivity : AppCompatActivity() {
    private lateinit var binding: ActivityForgotPassBinding
    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityForgotPassBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window.statusBarColor = ContextCompat.getColor(this,R.color.sky_bg)
        auth = FirebaseAuth.getInstance()

        binding.onBack.setOnClickListener {
            startActivity(Intent(this,SignInActivity::class.java))
            finish()
        }



        binding.btnGetpass.setOnClickListener {
            val email = binding.edtEmail.text.toString().trim()
            if(!Patterns.EMAIL_ADDRESS.matcher(email).matches() && TextUtils.isEmpty(email)){
                Toast.makeText(this,"Vui lòng kiểm tra lại thông tin!", Toast.LENGTH_SHORT).show()
            }
            else{
                auth.sendPasswordResetEmail(email)
                    .addOnCompleteListener {
                        if(it.isSuccessful){
                            Toast.makeText(this,"Kiểm tra email của bạn", Toast.LENGTH_SHORT).show()
                        }
                        else{
                            Toast.makeText(this,"Error!", Toast.LENGTH_SHORT).show()
                        }
                    }
                    .addOnFailureListener { e->
                        Log.e("Error",e.message.toString())

                    }
            }
        }
    }
}
