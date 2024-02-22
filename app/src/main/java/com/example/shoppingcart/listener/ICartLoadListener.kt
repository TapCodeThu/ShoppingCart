package com.example.shoppingcart.listener

import com.example.shoppingcart.model.CartModel

interface ICartLoadListener {
    fun onCartLoadSuccess(cartModelList: List<CartModel>?)
    fun onCartLoadFailed(message: String?)
}