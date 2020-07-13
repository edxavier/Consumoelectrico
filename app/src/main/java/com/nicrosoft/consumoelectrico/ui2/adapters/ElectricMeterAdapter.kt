package com.nicrosoft.consumoelectrico.ui2.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.nicrosoft.consumoelectrico.R
import com.nicrosoft.consumoelectrico.data.entities.ElectricMeter
import kotlinx.android.synthetic.main.electric_meter_item.view.*

class ElectricMeterAdapter: ListAdapter<ElectricMeter, ElectricMeterAdapter.ViewHolder>(EmeterDiffCallback()) {

    class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){

        fun bind(meter: ElectricMeter){
            itemView.apply {

            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context!!).inflate(R.layout.electric_meter_item, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}