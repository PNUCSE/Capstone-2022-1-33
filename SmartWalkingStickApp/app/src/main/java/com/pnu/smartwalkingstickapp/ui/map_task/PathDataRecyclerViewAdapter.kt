package com.pnu.smartwalkingstickapp.ui.map_task

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.pnu.smartwalkingstickapp.databinding.MenuRcvPathDataBinding
import com.pnu.smartwalkingstickapp.ui.map_task.response.path.Feature
import com.pnu.smartwalkingstickapp.utils.TTS

class PathDataRecyclerViewAdapter(private val context: Context) :
    RecyclerView.Adapter<PathDataRecyclerViewAdapter.ViewHolder>() {
    var dataSet = listOf<Feature>()
    val ttsSpeaker = TTS(context)

    inner class ViewHolder(private val binding: MenuRcvPathDataBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Feature) {
            with(binding) {
                feature = item
                layoutPoiData.setOnLongClickListener {
                    true
                }
            }

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = MenuRcvPathDataBinding.inflate(inflater, parent, false)

        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(dataSet[position])
    }

    override fun getItemCount(): Int {
        return dataSet.size
    }

    fun setData(newList: List<Feature>) {
        dataSet = newList
        notifyDataSetChanged()
    }
}