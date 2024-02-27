package com.example.shoppingcart.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.shoppingcart.databinding.RowItemImageBinding
import com.example.shoppingcart.model.ImageModel

class ImageAdapter(private val context: Context, private val imageList: List<ImageModel>)
    : RecyclerView.Adapter<ImageAdapter.ViewHolder>() {


        class ViewHolder(binding: RowItemImageBinding) : RecyclerView.ViewHolder(binding.root){
            val imageView = binding.imageView
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(RowItemImageBinding.inflate(LayoutInflater.from(parent.context),parent,false))
    }

    override fun getItemCount(): Int {
        return imageList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentImage = imageList[position]
        Glide.with(context)
            .load(currentImage.url).into(holder.imageView)
    }
}