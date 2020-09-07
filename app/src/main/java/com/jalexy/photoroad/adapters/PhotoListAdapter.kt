package com.jalexy.photoroad.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.jalexy.photoroad.R
import com.jalexy.photoroad.room.Photo
import kotlinx.android.synthetic.main.photo_item.view.*

class PhotoListAdapter internal constructor(val context: Context) :
    RecyclerView.Adapter<PhotoListAdapter.PhotoItemHolder>() {

    private val inflater: LayoutInflater = LayoutInflater.from(context)
    private var photoList = emptyList<Photo>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoItemHolder {
        val itemView = inflater.inflate(R.layout.photo_item, parent, false)
        return PhotoItemHolder(itemView)
    }

    override fun onBindViewHolder(holder: PhotoItemHolder, position: Int) {
        holder.bind(photoList[position])
    }

    override fun getItemCount() = photoList.size

    internal fun setPhotos(photos: List<Photo>) {
        this.photoList = photos
        notifyDataSetChanged()
    }

    inner class PhotoItemHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val photoView = itemView.photo_view
        private val photoUri = itemView.photo_uri
        private val photoStatus = itemView.photo_status

        fun bind(photo: Photo) {
            Glide.with(context)
                .load(photo.photoUri)
                .placeholder(R.drawable.ic_launcher_foreground)
                .into(photoView)

            val photoName = photo.photoUri.substringAfterLast("/")
            photoUri.text = photoName

            photoStatus.text = if (photo.sent) "Отправлено" else "Ждет отправки"
        }
    }
}
