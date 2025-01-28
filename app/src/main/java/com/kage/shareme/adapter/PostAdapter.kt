package com.kage.shareme.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.kage.shareme.databinding.FragmentFeedBinding
import com.kage.shareme.databinding.RecyclerRowBinding
import com.kage.shareme.model.Post

class PostAdapter (private val postList : ArrayList<Post>) : RecyclerView.Adapter<PostAdapter.PostHolder>(){
    class PostHolder (val binding : RecyclerRowBinding) : RecyclerView.ViewHolder(binding.root){

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostHolder {
       val binding = RecyclerRowBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return PostHolder(binding)
    }

    override fun getItemCount(): Int {
        return postList.size
    }

    override fun onBindViewHolder(holder: PostHolder, position: Int) {
        holder.binding.commentText.text = postList[position].comment
        holder.binding.recyclerEmailText.text = postList[position].email
        
        // Görseli yükle
        Glide.with(holder.itemView.context)
            .load(postList[position].downloadUrl)
            .into(holder.binding.recyclerImageView)
    }
}