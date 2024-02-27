package com.example.shoppingcart.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.shoppingcart.R
import com.example.shoppingcart.adapter.CartAdapter
import com.example.shoppingcart.databinding.BottomSheetOrderingBinding
import com.example.shoppingcart.databinding.FragmentCartBinding
import com.example.shoppingcart.eventbus.UpdateCart
import com.example.shoppingcart.listener.ICartLoadListener
import com.example.shoppingcart.model.CartModel
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.lang.StringBuilder


class CartFragment : Fragment(), ICartLoadListener {
    private lateinit var binding: FragmentCartBinding
    private lateinit var cartLoadListener: ICartLoadListener
    private lateinit var auth: FirebaseAuth


    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    fun onUpdateCart(event: UpdateCart) {
        // Cập nhật dữ liệu trong RecyclerView của CartActivity
        loadCartFromFirebase()
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
       binding = FragmentCartBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()

        binding.btnBack.setOnClickListener {
            findNavController().navigate(R.id.action_cartFragment_to_homeFragment)
        }
        binding.btnContinue.setOnClickListener {
            findNavController().navigate(R.id.action_cartFragment_to_homeFragment)
        }


        iNit()
        loadCartFromFirebase()

        //action button mua
        binding.btnMua.setOnClickListener {
            showBottomSheetOrdering()
        }


    }

    private fun showBottomSheetOrdering() {
        val bottomSheetBinding  = BottomSheetOrderingBinding.inflate(layoutInflater)
        val bottomSheetDialog = BottomSheetDialog(requireContext())
        bottomSheetDialog.setContentView(bottomSheetBinding.root)

        bottomSheetBinding.btnOrder.setOnClickListener {
            val name = bottomSheetBinding.edtName.text.toString().trim()
            val address = bottomSheetBinding.edtAddress.text.toString().trim()
            val phone_number = bottomSheetBinding.edtSdt.text.toString().trim()
            if(name.isEmpty()){
                bottomSheetBinding.edtName.error = "Thông tin bắt buộc (*)"
            }
            if(address.isEmpty()){
                bottomSheetBinding.edtAddress.error = "Thông tin bắt buộc (*)"
            }
            if(phone_number.isEmpty()){
                bottomSheetBinding.edtSdt.error = "Thông tin bắt buộc (*)"
            }
        }


        bottomSheetDialog.show()
    }

    private fun loadCartFromFirebase() {
        val cartModels : MutableList<CartModel> = ArrayList()
        FirebaseDatabase.getInstance().getReference("Cart")
            .child(auth.currentUser!!.uid)
            .addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                  cartModels.clear()
                    for(cartSnapshot in snapshot.children){
                        val cartModel = cartSnapshot.getValue(CartModel::class.java)
                        cartModel!!.key = cartSnapshot.key
                        cartModels.add(cartModel)
                    }
                    if(cartModels.isEmpty()){
                        binding.layoutCartempty.visibility = View.VISIBLE
                        binding.llBottomLayout.visibility = View.GONE
                    }
                    cartLoadListener.onCartLoadSuccess(cartModels)
                }

                override fun onCancelled(error: DatabaseError) {
                   cartLoadListener.onCartLoadFailed(error.message)
                    binding.layoutCartempty.visibility = View.VISIBLE
                    binding.llBottomLayout.visibility = View.GONE
                }

            })
    }

    private fun iNit() {
        auth = FirebaseAuth.getInstance()
        cartLoadListener = this
        binding.recyclerview.layoutManager = LinearLayoutManager(requireContext())

    }

    override fun onCartLoadSuccess(cartModelList: List<CartModel>?) {
       var sum = 0.0
        if(cartModelList != null){
            for(cartModel in cartModelList){
                sum += cartModel.totalPrice

            }
            binding.tvTotalprice.text = StringBuilder("").append(sum).append("00")
            val adapter = CartAdapter(cartModelList,requireContext())
            adapter.notifyDataSetChanged()
            binding.recyclerview.adapter = adapter
        }
    }

    override fun onCartLoadFailed(message: String?) {
       Toast.makeText(requireContext(),message,Toast.LENGTH_SHORT).show()
    }


}