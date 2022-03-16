package com.ebinumer.cameraxandcrop

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.ebinumer.cameraxandcrop.databinding.ListItemImgBinding
import java.io.File

class GalleryAdapter(private val fileArray: Array<File>,val Click:itemClickedInterface) :
    RecyclerView.Adapter<GalleryAdapter.ViewHolder>() {
    class ViewHolder(val binding: ListItemImgBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(file: File) {
            Glide.with(binding.root).load(file).into(binding.localImg)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return ViewHolder(ListItemImgBinding.inflate(layoutInflater, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(fileArray[position])
        holder.binding.localImg.setOnClickListener {
            Click.clicked(position)
        }
    }

    override fun getItemCount() = fileArray.size
}