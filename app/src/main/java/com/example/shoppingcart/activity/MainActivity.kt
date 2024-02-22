package com.example.shoppingcart.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI
import com.example.shoppingcart.R
import com.example.shoppingcart.databinding.ActivityMainBinding
import com.example.shoppingcart.eventbus.UpdateCart
import com.example.shoppingcart.listener.ICartLoadListener
import com.example.shoppingcart.model.CartModel
import com.google.android.material.navigation.NavigationBarView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class MainActivity : AppCompatActivity(),  NavigationBarView.OnItemSelectedListener,
    ICartLoadListener {
    private lateinit var binding: ActivityMainBinding
    private lateinit var cartLoadListener: ICartLoadListener
    private lateinit var auth: FirebaseAuth


    override fun onStart() {
        super.onStart()

        EventBus.getDefault().register(this)
        countCartFromFirebase()
    }

    override fun onStop() {
        super.onStop()
        if(EventBus.getDefault().hasSubscriberForEvent(UpdateCart::class.java))
            EventBus.getDefault().removeStickyEvent(UpdateCart::class.java)
        EventBus.getDefault().unregister(this)
    }
    @Subscribe(threadMode = ThreadMode.MAIN,sticky = true)
    public fun onUpdateCartEvent(event: UpdateCart){
        countCartFromFirebase()

    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window.statusBarColor = ContextCompat.getColor(this, R.color.sky_bg)
        cartLoadListener = this
        auth = FirebaseAuth.getInstance()



       val naviController = findNavController(R.id.fragmentContainer)
        NavigationUI.setupWithNavController(binding.bottomNavView,naviController)


        binding.bottomNavView.setOnItemSelectedListener(this)
        countCartFromFirebase()


    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.homeFragment ->{
                findNavController(R.id.fragmentContainer).navigate(R.id.homeFragment)
            }
            R.id.cartFragment ->{
                findNavController(R.id.fragmentContainer).navigate(R.id.cartFragment)
            }
            R.id.userFragment ->{
                findNavController(R.id.fragmentContainer).navigate(R.id.userFragment)
            }
        }
        return true
    }

    override fun onCartLoadSuccess(cartModelList: List<CartModel>?) {
        var cartSum = 0
        for(cartModel in cartModelList!!) cartSum += cartModel.quantity
       if(cartSum >0){ binding.bottomNavView.getOrCreateBadge(R.id.cartFragment).number = cartSum}
    }

    override fun onCartLoadFailed(message: String?) {
        Toast.makeText(this,message,Toast.LENGTH_SHORT).show()
    }

    private fun countCartFromFirebase() {
        val cartModels : MutableList<CartModel> = ArrayList()
        FirebaseDatabase.getInstance().getReference("Cart")
            .child(auth.currentUser!!.uid)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    cartModels.clear()
                    for(cartSnapshot in snapshot.children){
                        val cartModel = cartSnapshot.getValue(CartModel::class.java)
                        cartModel!!.key = cartSnapshot.key
                        cartModels.add(cartModel)
                    }
                    cartLoadListener.onCartLoadSuccess(cartModels)
                }

                override fun onCancelled(error: DatabaseError) {
                    cartLoadListener.onCartLoadFailed(error.message)
                }

            })

    }
}

