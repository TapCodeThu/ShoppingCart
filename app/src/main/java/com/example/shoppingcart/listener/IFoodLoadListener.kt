package com.example.shoppingcart.listener

import com.example.shoppingcart.model.FoodModel

interface IFoodLoadListener {
    fun onFoodLoadSuccess(foodModelList: List<FoodModel>?)
    fun onFoodLoadFailed(message: String?)
}