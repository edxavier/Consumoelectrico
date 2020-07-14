package com.nicrosoft.consumoelectrico.ui2.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.nicrosoft.consumoelectrico.R
import com.nicrosoft.consumoelectrico.data.entities.ElectricMeter
import kotlinx.android.synthetic.main.electric_meter_item.view.*

class ElectricMeterAdapter(
        private val itemClickListener: AdapterItemListener
): ListAdapter<ElectricMeter, ElectricMeterAdapter.ViewHolder>(DiffCallback()) {

    class DiffCallback: DiffUtil.ItemCallback<ElectricMeter>() {
        override fun areItemsTheSame(oldItem: ElectricMeter, newItem: ElectricMeter): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: ElectricMeter, newItem: ElectricMeter): Boolean {
            return oldItem == newItem
        }
    }

    class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){

        fun bind(meter: ElectricMeter, listener: AdapterItemListener?){
            itemView.apply {
                //Disparar evento para que la vista que lo implemente tenda el objeto al que se le dio click
                this.setOnClickListener { listener?.onItemClickListener(meter) }
                with(this){
                    button_item_details.setOnClickListener { listener?.onItemDetailListener(meter) }
                    txt_meter_name.text = meter.name
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context!!).inflate(R.layout.electric_meter_item, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position), itemClickListener)
    }

    interface AdapterItemListener{
        fun onItemClickListener(meter:ElectricMeter)
        fun onItemDetailListener(meter:ElectricMeter)
    }

}