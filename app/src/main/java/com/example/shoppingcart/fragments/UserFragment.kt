package com.example.shoppingcart.fragments

import android.app.Activity.RESULT_OK
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.navigation.fragment.findNavController
import com.example.shoppingcart.R
import com.example.shoppingcart.activity.ReplacePassActivity
import com.example.shoppingcart.activity.SignInActivity
import com.example.shoppingcart.databinding.FragmentUserBinding
import com.google.firebase.auth.FirebaseAuth


class UserFragment : Fragment() {
    private lateinit var binding: FragmentUserBinding
    private lateinit var auth: FirebaseAuth
    private var imgURI : Uri? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
       binding = FragmentUserBinding.inflate(inflater,container,false)
        return binding.root

    }

    val activityResultLanucher = registerForActivityResult(ActivityResultContracts.StartActivityForResult())
    {result ->
        if(result.resultCode == RESULT_OK){
            val data = result.data
            imgURI = data?.data
            binding.ivAvatar.setImageURI(imgURI)
        }

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        actionButton()

        binding.tvEmail.setText(auth.currentUser!!.email)


    }

    private fun actionButton() {
        binding.actionLogout.setOnClickListener {
            showDialogLogout()
        }
        binding.gotoReplacePass.setOnClickListener {
            gotoActivityReplacePassWord()
        }
        binding.btnBack.setOnClickListener {
            findNavController().navigate(R.id.action_userFragment_to_homeFragment)
        }
        binding.gotoHistory.setOnClickListener {
            findNavController().navigate(R.id.action_userFragment_to_historyFragment)
        }
        binding.actionUploadImage.setOnClickListener {
           val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            activityResultLanucher.launch(intent)
        }
    }

    private fun gotoActivityReplacePassWord() {
        startActivity(Intent(requireContext(),ReplacePassActivity::class.java))

    }

    private fun showDialogLogout() {
        val builder  = AlertDialog.Builder(requireContext())
        builder.setMessage("Đăng xuất ngay ?")
        builder.setPositiveButton("Đồng ý"){dialog, _ ->
            auth.signOut()
           startActivity(Intent(requireContext(),SignInActivity::class.java))
            dialog.dismiss()

        }
        builder.setNegativeButton("Hủy"){dialog, _ ->
            dialog.dismiss()

        }
        val alertDialog = builder.create()
        alertDialog.show()
    }


}