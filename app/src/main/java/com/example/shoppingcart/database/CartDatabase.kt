package com.example.shoppingcart.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [CartEntity::class], version = 1)
abstract class CartDatabase : RoomDatabase() {

    abstract fun cartDao() : CartDao

    companion object{
        @Volatile
        private var INSTANCE : CartDatabase? = null

        fun getInstance(context: Context) : CartDatabase{
            return INSTANCE ?: synchronized(this){
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    CartDatabase::class.java,
                    "cart_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}