package com.example.shoppingcart.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView.OnItemClickListener
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.shoppingcart.activity.DetailItemActivity
import com.example.shoppingcart.databinding.RowFoodItemBinding
import com.example.shoppingcart.eventbus.UpdateCart
import com.example.shoppingcart.listener.ICartLoadListener
import com.example.shoppingcart.listener.IRecyclerView
import com.example.shoppingcart.model.CartModel
import com.example.shoppingcart.model.FoodModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import org.greenrobot.eventbus.EventBus
import java.lang.StringBuilder

class FoodAdapter(private val context: Context,
                  private var list: List<FoodModel>,
                  private val cartLoadListener: ICartLoadListener)
    : RecyclerView.Adapter<FoodAdapter.ViewHolder>() {

    private lateinit var auth: FirebaseAuth


    fun setFilterList(list: List<FoodModel>){
        this.list = list
        notifyDataSetChanged()

    }



        class ViewHolder(binding: RowFoodItemBinding) : RecyclerView.ViewHolder(binding.root),
            View.OnClickListener {
            val imageView = binding.image
            val tv_price = binding.tvPrice
            val tv_name  = binding.tvName

            private var clickListener: IRecyclerView? = null
            fun setClickListener(clickListener: IRecyclerView){
                this.clickListener = clickListener
            }
            init {
                itemView.setOnClickListener(this)
            }

            override fun onClick(v: View?) {
                clickListener!!.onItemClickListener(v,adapterPosition)
            }
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(RowFoodItemBinding.inflate(LayoutInflater.from(parent.context),parent,false))

    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentList = list[position]
        Glide.with(context).load(currentList.image).into(holder.imageView)
        holder.tv_price.text = StringBuilder("Giá: ").append(currentList.price).append(" 000 VND")
        holder.tv_name.text = StringBuilder("").append(currentList.name)


        holder.setClickListener(object : IRecyclerView{
            override fun onItemClickListener(view: View?, position: Int) {
                val intent = Intent(context,DetailItemActivity::class.java)
                val bundle = Bundle()
                bundle.putString("food_key",currentList.key)
                bundle.putString("food_name",currentList.name)
                bundle.putString("food_price",currentList.price)
                bundle.putString("food_image",currentList.image)
                bundle.putString("food_descript",currentList.description)
                intent.putExtras(bundle)
                context.startActivity(intent)


                   // addToCart(list[position])
            }

        })
    }

    @SuppressLint("SuspiciousIndentation")
    private fun addToCart(foodModel: FoodModel) {
        auth = FirebaseAuth.getInstance()
      val userCart =   FirebaseDatabase.getInstance().getReference("Cart")
            .child(auth.currentUser!!.uid)
            userCart.child(foodModel.key!!)
            .addListenerForSingleValueEvent(object :ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                   if(snapshot.exists()){
                       //if exist item cart, update now
                       val cartModel = snapshot.getValue(CartModel::class.java)
                       val updateCart : MutableMap<String,Any> = HashMap()
                       cartModel!!.quantity = cartModel.quantity + 1
                       updateCart["quantity"] = cartModel.quantity
                       updateCart["totalPrice"] = cartModel.quantity * cartModel.price!!.toFloat()
                       userCart.child(foodModel.key!!)
                           .updateChildren(updateCart)
                           .addOnSuccessListener {

                               //EventBus
                               EventBus.getDefault().postSticky(UpdateCart())


                           }
                           .addOnFailureListener { e ->
                               cartLoadListener.onCartLoadFailed(e.message)

                           }
                   }
                    else{
                        //add new cart
                        val cartModel = CartModel()
                       cartModel.key = foodModel.key
                       cartModel.name = foodModel.name
                       cartModel.price = foodModel.price
                       cartModel.image = foodModel.image
                       cartModel.quantity = 1
                       cartModel.totalPrice = foodModel.price!!.toFloat()
                       userCart.child(foodModel.key!!)
                           .setValue(cartModel)
                           .addOnSuccessListener {
                               //EventBus
                               EventBus.getDefault().postSticky(UpdateCart())

                           }
                           .addOnFailureListener { e->
                               cartLoadListener.onCartLoadFailed(e.message)

                           }


                   }
                }

                override fun onCancelled(error: DatabaseError) {
                   cartLoadListener.onCartLoadFailed(error.message)
                }

            })



    }
}