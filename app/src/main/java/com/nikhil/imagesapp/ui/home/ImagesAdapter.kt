package com.nikhil.imagesapp.ui.home

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.nikhil.imagesapp.R
import com.nikhil.imagesapp.extensions.loadImageUrl
import com.nikhil.imagesapp.models.Image
import kotlinx.android.synthetic.main.item_image_thumbnail.view.*
import timber.log.Timber

class ImagesAdapter(private var data: MutableList<Image>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    interface Callbacks {
        fun onItemClick(imageUrl: String)
    }

    private var mCallbacks: Callbacks? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ImageViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_image_thumbnail, parent, false))
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        bindItem(holder as ImageViewHolder, data[position], position)
    }

    override fun getItemCount(): Int {
        return data.size
    }

    private fun bindItem(holder: ImageViewHolder, image: Image, position: Int) {
        holder.imageThumbnail.loadImageUrl(imageUrl = image.imageUrl, resourceId = R.color.colorGrey)
        holder.imageThumbnail.setOnClickListener {
            mCallbacks?.onItemClick(image.imageUrl)
        }
    }

    fun setCalbacks(callbacks: Callbacks) {
        mCallbacks = callbacks;
    }

    fun refreshData(list: List<Image>) {
        data.clear()
        data.addAll(list)
        notifyDataSetChanged()
        Timber.d("LoadingFooterRVAdapter$data")
    }

    class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageThumbnail = itemView.image_thumbnail
    }

}