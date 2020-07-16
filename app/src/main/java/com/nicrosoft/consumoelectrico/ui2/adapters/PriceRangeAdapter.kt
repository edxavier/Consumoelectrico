package com.nicrosoft.consumoelectrico.ui2.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.nicrosoft.consumoelectrico.R
import com.nicrosoft.consumoelectrico.data.entities.PriceRange
import kotlinx.android.synthetic.main.item_electric_meter.view.*

class PriceRangeAdapter(
        private val itemClickListener: PriceItemListener
): ListAdapter<PriceRange, PriceRangeAdapter.ViewHolder>(DiffCallback()) {

    class DiffCallback: DiffUtil.ItemCallback<PriceRange>() {
        override fun areItemsTheSame(oldItem: PriceRange, newItem: PriceRange): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: PriceRange, newItem: PriceRange): Boolean {
            return oldItem == newItem
        }
    }

    class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){

        fun bind(meter: PriceRange, listener: PriceItemListener?){
            itemView.apply {
                //Disparar evento para que la vista que lo implemente tenda el objeto al que se le dio click
                this.setOnClickListener { listener?.onPriceItemClickListener(meter) }
                with(this){
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context!!).inflate(R.layout.item_price_range, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position), itemClickListener)
    }

    interface PriceItemListener{
        fun onPriceItemClickListener(meter:PriceRange)
    }

}