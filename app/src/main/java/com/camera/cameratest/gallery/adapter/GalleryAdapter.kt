package com.camera.cameratest.gallery.adapter

import android.content.Context
import android.content.Intent
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.camera.cameratest.R
import com.camera.cameratest.database.PhotoLibrary
import com.camera.cameratest.gallery.GalleryActivity
import com.camera.cameratest.preview.PreviewActivity
import kotlinx.android.synthetic.main.photo_item.view.*

class GalleryAdapter : RecyclerView.Adapter<GalleryAdapter.ViewHolder> {

    private lateinit var context:Context
    private lateinit var photoLibarys:MutableList<PhotoLibrary>

    constructor(context: Context, photoLibarys: MutableList<PhotoLibrary>) : super() {
        this.context = context
        this.photoLibarys = photoLibarys
    }

    class ViewHolder(v:View):RecyclerView.ViewHolder(v){
        val view:View = v
    }

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): GalleryAdapter.ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.photo_item,p0,false)
        val holder = ViewHolder(view)
        holder.view.small_image.setOnClickListener(){
            val intent = Intent(context,PreviewActivity::class.java)
            val i = holder.layoutPosition
            intent.putExtra("file",photoLibarys[i].path)
            intent.putExtra("package",GalleryActivity::class.java.toString())
            context.startActivity(intent)
        }
        return holder
    }

    override fun getItemCount(): Int {
        return photoLibarys.size
    }

    override fun onBindViewHolder(p0: GalleryAdapter.ViewHolder, p1: Int) {
        val photoLibary = photoLibarys[p1]
        with(p0.itemView){
            val option = RequestOptions().override(200,200).centerCrop()
            Glide.with(context).load(photoLibary.path).apply(option).into(small_image)
        }
    }
}