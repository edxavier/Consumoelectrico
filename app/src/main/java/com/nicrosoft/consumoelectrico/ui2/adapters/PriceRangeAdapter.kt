package com.nicrosoft.consumoelectrico.ui2.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.nicrosoft.consumoelectrico.R
import com.nicrosoft.consumoelectrico.data.entities.PriceRange
import com.pixplicity.easyprefs.library.Prefs
import kotlinx.android.synthetic.main.item_price_range.view.*

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

        @SuppressLint("SetTextI18n")
        fun bind(price: PriceRange, listener: PriceItemListener?){
            itemView.apply {
                //Disparar evento para que la vista que lo implemente tenda el objeto al que se le dio click
                this.setOnClickListener { listener?.onPriceItemClickListener(price) }
                with(this){
                    label_from_kw.text = "${price.fromKw} kW"
                    label_to_kw.text = "${price.toKw} kW"
                    val simbol = Prefs.getString("price_simbol", "$")
                    label_kw_price.text = "$simbol${price.price}"
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