package com.example.shoppingcart.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.example.shoppingcart.R
import com.example.shoppingcart.databinding.ActivitySignInBinding
import com.google.firebase.auth.FirebaseAuth

class SignInActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySignInBinding
    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignInBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window.statusBarColor = ContextCompat.getColor(this,R.color.sky_bg)

        auth = FirebaseAuth.getInstance()

        actionButton()
    }

    private fun actionButton() {
        binding.tvForgetpass.setOnClickListener {
            startActivity(Intent(this,ForgotPassActivity::class.java))
            finish()
        }
        binding.tvSignup.setOnClickListener {
            startActivity(Intent(this,SignUpActivity::class.java))
        }

        binding.btnLogin.setOnClickListener {
            binding.btnLogin.visibility = View.GONE
            binding.progressBar.visibility = View.VISIBLE
            val email = binding.edtEmail.text.toString().trim()
            val password = binding.edtPassword.text.toString().trim()
            if(TextUtils.isEmpty(email) || TextUtils.isEmpty(password)){
                Toast.makeText(this,"Vui lòng nhập thông tin!",Toast.LENGTH_SHORT).show()
                binding.btnLogin.visibility = View.VISIBLE
                binding.progressBar.visibility = View.GONE
            }
            else{
                signInAccount(email,password)
            }

        }
    }

    private fun signInAccount(email: String, password: String) {
        auth.signInWithEmailAndPassword(email,password)
            .addOnCompleteListener {

                if(it.isSuccessful){
                    binding.progressBar.visibility = View.GONE
                    binding.btnLogin.visibility = View.VISIBLE
                    startActivity(Intent(this,MainActivity::class.java))
                    finish()
                }
                else{
                    binding.progressBar.visibility = View.GONE
                    binding.btnLogin.visibility = View.VISIBLE

                    Toast.makeText(this,"Lỗi đăng nhập!",Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e->
                binding.progressBar.visibility = View.GONE
                binding.btnLogin.visibility = View.VISIBLE
            }

    }
}