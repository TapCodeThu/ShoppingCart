package com.example.shoppingcart.activity

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.Glide
import com.example.shoppingcart.R
import com.example.shoppingcart.adapter.ImageAdapter
import com.example.shoppingcart.database.CartDatabase
import com.example.shoppingcart.database.CartEntity
import com.example.shoppingcart.databinding.ActivityDetailItemBinding
import com.example.shoppingcart.databinding.BottomDialogDetailBinding
import com.example.shoppingcart.listener.ICartLoadListener
import com.example.shoppingcart.model.CartModel
import com.example.shoppingcart.model.FoodModel
import com.example.shoppingcart.model.ImageModel
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class DetailItemActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDetailItemBinding
    private lateinit var imageAdapter: ImageAdapter
    private lateinit var auth: FirebaseAuth
    lateinit var cartLoadListener: ICartLoadListener
    private var isActionButton: Boolean = false
    private var foodKey: String? = null
    private var foodImage: String? = null
    private var foodName: String? = null
    private var foodPrice: String? = null
    private var foodDescription: String? = null
    private var quantity: Int = 1
    private lateinit var foodModel: FoodModel

    private lateinit var cartDatabase: CartDatabase


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailItemBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window.statusBarColor = ContextCompat.getColor(this, R.color.sky_bg)

        auth = FirebaseAuth.getInstance()
        //khoi tao cartdatabae
        cartDatabase = CartDatabase.getInstance(this)


//******************GET DATA FROM HOMEFRAGMENT****************************//
        val bundle: Bundle? = intent.extras
        foodKey = bundle?.getString("food_key")
        foodName = bundle?.getString("food_name")
        foodPrice = bundle?.getString("food_price")
        foodImage = bundle?.getString("food_image")
        foodDescription = bundle?.getString("food_descript")

        //hien thi len UI
        binding.tvName.text = foodName
        binding.tvPrice.text = StringBuilder("").append(foodPrice).append(" 000 VND")
        binding.tvDescription.text = foodDescription
        Glide.with(this).load(foodImage).into(binding.imageMain)

        //********************ACTION BUTTON*****************************//
        actionButton()
        binding.recyclerview.layoutManager = GridLayoutManager(this@DetailItemActivity, 2)

        //load image from firebase
        loadImageFromFirebase()



        // kiem tra cart in firebase

        val userCart = FirebaseDatabase.getInstance().getReference("Cart")
            .child(auth.currentUser!!.uid)
        userCart.child(foodKey!!)
            .addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    if(snapshot.exists()){
                        binding.btnAdd.setBackgroundColor(ContextCompat.getColor(this@DetailItemActivity,R.color.grey_color))
                        binding.btnAdd.text = "Đã thêm vào giỏ hàng"
                        binding.btnAdd.isEnabled = false

                    }
                    else{
                        binding.btnAdd.setOnClickListener {
                            showBottomSheet()
                        }
                    }

                }

                override fun onCancelled(error: DatabaseError) {

                }

            })
    }

    private fun loadImageFromFirebase() {
        val images: MutableList<ImageModel> = ArrayList()
        FirebaseDatabase.getInstance().getReference("image")
            .child(foodKey!!).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (imageSnapshot in snapshot.children) {
                        val imageUrl = imageSnapshot.child("url")
                            .getValue(String::class.java) // Lấy giá trị URL từ Firebase
                        imageUrl?.let {
                            val imageModel =
                                ImageModel(url = it) // Tạo một đối tượng ImageModel với URL
                            images.add(imageModel) // Thêm vào danh sách images
                        }
                    }

                    //khoi tao adapter
                    imageAdapter = ImageAdapter(this@DetailItemActivity, images)
                    binding.recyclerview.adapter = imageAdapter
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@DetailItemActivity, error.message, Toast.LENGTH_SHORT)
                        .show()
                }

            })
    }

    private fun actionButton() {
        binding.btnBack.setOnClickListener {
            onBackPressed()
        }




        binding.btnAddCartToolbar.setOnClickListener {
            val userCart = FirebaseDatabase.getInstance().getReference("Cart")
                .child(auth.currentUser!!.uid)
            userCart.child(foodKey!!)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            binding.btnAddCartToolbar.isEnabled = false
                        } else {
                            showBottomSheet()
                        }

                    }

                    override fun onCancelled(error: DatabaseError) {

                    }

                })
        }


    }

    @SuppressLint("SuspiciousIndentation")
    private fun showBottomSheet() {
        val bottomSheetBinding = BottomDialogDetailBinding.inflate(layoutInflater)
        val bottomSheetDialog = BottomSheetDialog(this)
        bottomSheetDialog.setContentView(bottomSheetBinding.root)

        //gan data cho view
        bottomSheetBinding.tvFoodName.text = foodName
        bottomSheetBinding.tvFoodPrice.text =
            StringBuilder("Giá: ").append(quantity * foodPrice!!.toFloat()).append(" 000 VND")
        Glide.with(this).load(foodImage).into(bottomSheetBinding.imageView)


        //action button giam
        bottomSheetBinding.actionMinus.setOnClickListener {
            if (quantity > 1) {
                quantity--
                bottomSheetBinding.tvFoodQuantity.text = quantity.toString()
                bottomSheetBinding.tvFoodPrice.text =
                    StringBuilder("Giá: ").append(quantity * foodPrice!!.toFloat()).append("00 VND")
            }
        }
        //action button plus
        bottomSheetBinding.btnPlus.setOnClickListener {
            quantity++
            bottomSheetBinding.tvFoodQuantity.text = quantity.toString()
            bottomSheetBinding.tvFoodPrice.text =
                StringBuilder("Giá: ").append(quantity * foodPrice!!.toFloat()).append("00 VND")

        }
//action button in bottomsheet
        bottomSheetBinding.btnAddtocart.setOnClickListener {
            val userCart = FirebaseDatabase.getInstance().getReference("Cart")
                .child(auth.currentUser!!.uid)
                userCart.child(foodKey!!)
                    .addListenerForSingleValueEvent(object : ValueEventListener{
                        override fun onDataChange(snapshot: DataSnapshot) {
                            if(!snapshot.exists()){
                                val cartModel = CartModel()
                                cartModel.key = foodKey
                                cartModel.image = foodImage
                                cartModel.name = foodName
                                cartModel.price = foodPrice
                                cartModel.quantity = quantity
                                cartModel.totalPrice = quantity * foodPrice!!.toFloat()

                                val cartItem = CartEntity(
                                    id = foodKey!!.toInt(),
                                    name = foodName!!,
                                    image = foodImage!!,
                                    price = foodPrice!!,
                                    quantity = quantity,
                                    totalPrice = quantity * foodPrice!!.toFloat()
                                )


                                GlobalScope.launch(Dispatchers.IO){
                                    cartDatabase.cartDao().insertCart(cartItem)
                                }

                                userCart.child(foodKey!!)
                                    .setValue(cartModel)
                                    .addOnSuccessListener {
                                        //Event bus
                                        bottomSheetDialog.dismiss()
                                        binding.btnAdd.setBackgroundColor(ContextCompat.getColor(this@DetailItemActivity,R.color.grey_color))
                                        binding.btnAdd.text = "Đã thêm vào giỏ hàng"
                                        binding.btnAdd.isEnabled = false
                                    }
                                    .addOnFailureListener { e->
                                        cartLoadListener.onCartLoadFailed(e.message)
                                        bottomSheetDialog.dismiss()
                                    }
                            }
                            else{

                                //no code my bug

                            }

                        }

                        override fun onCancelled(error: DatabaseError) {
                          cartLoadListener.onCartLoadFailed(error.message)
                        }


                    })
        }
        bottomSheetBinding.btnHuy.setOnClickListener {
            bottomSheetDialog.dismiss()
        }




        bottomSheetDialog.show()


    }

}