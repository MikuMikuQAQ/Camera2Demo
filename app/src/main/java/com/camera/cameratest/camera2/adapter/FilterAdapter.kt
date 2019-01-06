package com.camera.cameratest.camera2.adapter

import android.content.Context
import android.graphics.Color
import android.support.v7.widget.RecyclerView
import android.util.Log.e
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.camera.cameratest.R
import com.camera.cameratest.camera2.Camera2Fragment
import com.camera.cameratest.database.FilterLibrary
import kotlinx.android.synthetic.main.filter_select_item.view.*

class FilterAdapter : RecyclerView.Adapter<FilterAdapter.ViewHolder> {

    private lateinit var context: Context
    private lateinit var filterLibarys:MutableList<FilterLibrary>
    private var num:MutableList<Int> = ArrayList()

    constructor(context: Context, filterLibarys: MutableList<FilterLibrary>) : super() {
        this.context = context
        this.filterLibarys = filterLibarys
    }

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): FilterAdapter.ViewHolder {
        val v = LayoutInflater.from(context).inflate(R.layout.filter_select_item,p0,false)
        val holder = ViewHolder(v)
        holder.view.setOnClickListener {
            it.filter_name.setTextColor(Color.argb(255,86,255,227))
            Camera2Fragment.fitlerNum = filterLibarys[holder.layoutPosition].filterNum
            num.add(holder.layoutPosition)
            e("holder",holder.layoutPosition.toString())
            notifyDataSetChanged()
        }
        return holder
    }

    override fun getItemCount(): Int {
        return filterLibarys.size
    }

    override fun onBindViewHolder(p0: FilterAdapter.ViewHolder, p1: Int) {
        with(p0.itemView){
            filter_name.text = filterLibarys[p1].filterName
            filter_name.setTextColor(Color.rgb(0,0,0))
            if (num.size > 0){
                if (num[0] == p1){
                    e("num",num[0].toString())
                    e("p1",p1.toString())
                    filter_name.setTextColor(Color.argb(255,86,255,227))
                    num.clear()
                }
            }
        }
    }

    class ViewHolder(v: View):RecyclerView.ViewHolder(v){
        val view = v
    }
}