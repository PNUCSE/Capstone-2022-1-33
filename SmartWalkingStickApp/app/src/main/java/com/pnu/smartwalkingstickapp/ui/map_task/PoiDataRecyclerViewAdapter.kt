package com.pnu.smartwalkingstickapp.ui.map_task

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.pnu.smartwalkingstickapp.databinding.MenuRcvPoiDataBinding
import com.pnu.smartwalkingstickapp.ui.map_task.response.search.Poi

class PoiDataRecyclerViewAdapter : RecyclerView.Adapter<PoiDataRecyclerViewAdapter.ViewHolder>() {
    private var listener : OnPoiDataItemClick? = null
    var dataSet = mutableListOf<Poi>()


    fun setData(newList : MutableList<Poi>){
        dataSet = newList
        notifyDataSetChanged()
    }

    inner class ViewHolder(private val binding : MenuRcvPoiDataBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Poi){
            with(binding){
                poi = item
                executePendingBindings() // 강제 결합시키기
                layoutPoiData.setOnClickListener {
                    listener!!.sendData(item)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = MenuRcvPoiDataBinding.inflate(inflater, parent, false)

        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(dataSet[position])
    }

    override fun getItemCount(): Int {
        return dataSet.size
    }

    fun setOnStateInterface(mlistener: OnPoiDataItemClick) {
        listener = mlistener
    }

}