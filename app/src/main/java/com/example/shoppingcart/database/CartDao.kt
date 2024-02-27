package com.example.shoppingcart.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface CartDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCart(cartEntity: CartEntity)

    @Query("SELECT * FROM cart")
    suspend fun getAllCartItem() : List<CartEntity>

    @Query("DELETE FROM cart WHERE id=:id")
    suspend fun deleteCart(id: Int)
}