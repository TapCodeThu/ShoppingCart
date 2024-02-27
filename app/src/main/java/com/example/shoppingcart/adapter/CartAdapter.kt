package com.example.shoppingcart.adapter

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.shoppingcart.database.CartDatabase
import com.example.shoppingcart.databinding.RowCartItemBinding
import com.example.shoppingcart.eventbus.UpdateCart
import com.example.shoppingcart.model.CartModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import java.lang.StringBuilder

class CartAdapter(private var list: List<CartModel>,
                  private val context: Context)
    : RecyclerView.Adapter<CartAdapter.ViewHolder>() {

    private lateinit var cartDatabase: CartDatabase

        class ViewHolder(binding : RowCartItemBinding) : RecyclerView.ViewHolder(binding.root){
            val imageView = binding.imageView
            val tv_name = binding.tvName
            val tv_price = binding.tvPrice
            val tv_quantiy = binding.tvQuantity
            val iv_minus = binding.btnMinus
            val iv_plus = binding.btnPlus
            val iv_delete = binding.btnDelete
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(RowCartItemBinding.inflate(LayoutInflater.from(parent.context),parent,false))
    }

    override fun getItemCount(): Int {
       return list.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
       Glide.with(context)
           .load(list[position].image)
           .into(holder.imageView)
        holder.tv_name.text = list[position].name
        holder.tv_price.text = StringBuilder("Giá: ").append(list[position].price).append(" 000 VND")
        holder.tv_quantiy.text = StringBuilder("").append(list[position].quantity)
        holder.iv_minus.setOnClickListener { _ ->
            minusQuantity(holder,list[position])
        }
        holder.iv_plus.setOnClickListener { _, ->
            plusQuantity(holder,list[position])
        }
        holder.iv_delete.setOnClickListener { _, ->
            deleteItem(holder,list[position])

        }
    }

    private fun deleteItem(holder: ViewHolder, cartModel: CartModel) {
        val builder = AlertDialog.Builder(context)
        builder.setMessage("Bạn có muốn xóa ?")
        builder.setPositiveButton("Đồng ý"){dialog,_ ->
            notifyItemRemoved(holder.adapterPosition)
            FirebaseDatabase.getInstance()
                .getReference("Cart")
                .child(FirebaseAuth.getInstance().currentUser!!.uid)
                .child(cartModel.key!!)
                .removeValue()
                .addOnSuccessListener {
                    EventBus.getDefault().postSticky(UpdateCart())
                    refreshList(list)

                }

            GlobalScope.launch(Dispatchers.IO){
                cartDatabase = CartDatabase.getInstance(context)
                cartDatabase.cartDao().deleteCart(cartModel.key!!.toInt())
            }
        }
        builder.setNegativeButton("Không"){dialog,_ ->
            dialog.dismiss()
        }
        val dialogs = builder.create()
        dialogs.show()

    }

    private fun plusQuantity(holder: ViewHolder, cartModel: CartModel) {
        cartModel.quantity += 1
        cartModel.totalPrice = cartModel.quantity * cartModel.price!!.toFloat()
        holder.tv_quantiy.text = StringBuilder("").append(cartModel.quantity)
        updateCartFirebase(cartModel)


    }

    private fun minusQuantity(holder: ViewHolder, cartModel: CartModel) {
        if(cartModel.quantity > 1){
            cartModel.quantity -= 1
            cartModel.totalPrice = cartModel.quantity * cartModel.price!!.toFloat()
            holder.tv_quantiy.text = StringBuilder("").append(cartModel.quantity)
            updateCartFirebase(cartModel)
        }
        else{
            val builder = AlertDialog.Builder(context)
            builder.setMessage("Bạn có muốn xóa ?")
            builder.setPositiveButton("Đồng ý"){dialog,_ ->
                notifyItemRemoved(holder.adapterPosition)
                FirebaseDatabase.getInstance()
                    .getReference("Cart")
                    .child(FirebaseAuth.getInstance().currentUser!!.uid)
                    .child(cartModel.key!!)
                    .removeValue()
                    .addOnSuccessListener {
                        EventBus.getDefault().postSticky(UpdateCart())
                        refreshList(list)

                    }
            }
            builder.setNegativeButton("Không"){dialog,_ ->
                dialog.dismiss()
            }
            val dialogs = builder.create()
            dialogs.show()
        }


    }

     fun updateCartFirebase(cartModel: CartModel) {
     val cartRef =    FirebaseDatabase.getInstance()
            .getReference("Cart")
            .child(FirebaseAuth.getInstance().currentUser!!.uid)
            .child(cartModel.key!!)

            val updateCart =HashMap<String,Any>()
                updateCart["quantity"] = cartModel.quantity
                    updateCart["totalPrice"] = cartModel.totalPrice
            cartRef.updateChildren(updateCart)
                .addOnSuccessListener {
                    EventBus.getDefault().postSticky(UpdateCart())
                }

    }
    fun refreshList(newList: List<CartModel>){
        list = newList
        notifyDataSetChanged()

    }


}




