package com.example.shoppingcart.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.example.shoppingcart.R
import com.example.shoppingcart.databinding.ActivitySignUpBinding
import com.google.firebase.auth.FirebaseAuth

class SignUpActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySignUpBinding
    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window.statusBarColor = ContextCompat.getColor(this,R.color.sky_bg)

        auth = FirebaseAuth.getInstance()



        actionButton()
    }

    private fun registerAccount(email: String, password: String) {
        auth.createUserWithEmailAndPassword(email,password)
            .addOnCompleteListener {
                binding.progressBar.visibility = View.GONE
                if(it.isSuccessful){
                   startActivity(Intent(this,MainActivity::class.java))
                    finish()
                    binding.btnSignup.visibility = View.VISIBLE
                }
                else{
                    binding.progressBar.visibility = View.GONE
                    binding.btnSignup.visibility = View.VISIBLE
                    Toast.makeText(this,"Lỗi tạo tài khoản!",Toast.LENGTH_SHORT).show()
                }
            }

    }

    private fun actionButton() {
        binding.tvLogin.setOnClickListener {
            startActivity(Intent(this,SignInActivity::class.java))
        }
        binding.btnSignup.setOnClickListener {
            binding.btnSignup.visibility = View.GONE
            binding.progressBar.visibility = View.VISIBLE
            val email = binding.edtEmail.text.toString().trim()
            val password = binding.edtPassword.text.toString().trim()
            val confirm_pass = binding.edtConfirm.text.toString().trim()
            if(TextUtils.isEmpty(email) || TextUtils.isEmpty(password) || TextUtils.isEmpty(confirm_pass)){
                Toast.makeText(this,"Vui lòng điền thông tin!",Toast.LENGTH_SHORT).show()
                binding.btnSignup.visibility = View.VISIBLE
                binding.progressBar.visibility = View.GONE
                return@setOnClickListener
            }
            else{
                if(password == confirm_pass){
                    registerAccount(email,password)
                }
                else{
                    Toast.makeText(this,"Sai mật khẩu!",Toast.LENGTH_SHORT).show()
                    binding.btnSignup.visibility = View.VISIBLE
                    binding.progressBar.visibility = View.GONE
                }
            }

        }
    }
}