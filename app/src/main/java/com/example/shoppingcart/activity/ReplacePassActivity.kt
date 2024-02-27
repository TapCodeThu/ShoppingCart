package com.example.shoppingcart.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.findNavController
import com.example.shoppingcart.R
import com.example.shoppingcart.databinding.ActivityReplacePassBinding
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth

class ReplacePassActivity : AppCompatActivity() {
    private lateinit var binding: ActivityReplacePassBinding
    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReplacePassBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window.statusBarColor = ContextCompat.getColor(this,R.color.sky_bg)
        auth = FirebaseAuth.getInstance()

        binding.actionBack.setOnClickListener {
            onBackPressed()
        }


        binding.btnReplace.setOnClickListener {
            binding.progress.visibility = View.VISIBLE
            binding.btnReplace.visibility = View.GONE
            val email = binding.edtEmail.text.toString().trim()
            val oldPass = binding.edtPassold.text.toString().trim()
            val newPass = binding.edtPassnew.text.toString().trim()

            if(email.isNotEmpty() && oldPass.isNotEmpty() && newPass.isNotEmpty()){
                val user = auth.currentUser
                if(user != null && user.email != null){
                    //tao redit voi email va old pass
                    val credital = EmailAuthProvider.getCredential(email,oldPass)

                        //authen user with email created
                    user.reauthenticate(credital)
                        .addOnCompleteListener {creditalTask ->
                            if(creditalTask.isSuccessful){
                                user.updatePassword(newPass)
                                    .addOnCompleteListener {
                                        if(it.isSuccessful){
                                            binding.progress.visibility = View.GONE
                                            binding.btnReplace.visibility = View.VISIBLE
                                            Toast.makeText(this,"Đổi mật khẩu thành công",Toast.LENGTH_SHORT).show()
                                        }
                                        else{
                                            binding.progress.visibility = View.GONE
                                            binding.btnReplace.visibility = View.VISIBLE
                                            Toast.makeText(this,"Đổi mật khẩu thất bại! Vui lòng thử lại sau.",Toast.LENGTH_SHORT).show()
                                        }

                                    }
                            }
                            else{
                                binding.progress.visibility = View.GONE
                                binding.btnReplace.visibility = View.VISIBLE
                                Toast.makeText(this,"Xác thực thất bại!. Vui lòng kiểm tra email",Toast.LENGTH_SHORT).show()
                            }

                        }
                }
            }
            else{
                binding.progress.visibility = View.GONE
                binding.btnReplace.visibility = View.VISIBLE
                Toast.makeText(this,"Vui lòng nhập đủ thông tin!",Toast.LENGTH_SHORT).show()
            }
        }


    }

}