package com.example.shoppingcart.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cart")
data class CartEntity (
    @PrimaryKey(autoGenerate = true)
    val id : Int =0,
    val name : String,
    val image : String,
    val price : String,
    val quantity : Int,
    val totalPrice : Float


    )