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
import com.nicrosoft.consumoelectrico.databinding.ItemPriceRangeBinding
import com.pixplicity.easyprefs.library.Prefs

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

    class ViewHolder(itemView: View, _binding: ItemPriceRangeBinding): RecyclerView.ViewHolder(itemView){
        val binding: ItemPriceRangeBinding = _binding

        @SuppressLint("SetTextI18n")
        fun bind(price: PriceRange, listener: PriceItemListener?){
            itemView.apply {

                //Disparar evento para que la vista que lo implemente tenda el objeto al que se le dio click
                this.setOnClickListener { listener?.onPriceItemClickListener(price) }
                with(this){

                    binding.labelFromKw.text = "${price.fromKw} kW"
                    binding.labelToKw.text = "${price.toKw} kW"
                    val simbol = Prefs.getString("price_simbol", "$")
                    binding.labelKwPrice.text = "$simbol${price.price}"
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemPriceRangeBinding.inflate(LayoutInflater.from(parent.context!!))
        return ViewHolder(binding.root, binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position), itemClickListener)
    }

    interface PriceItemListener{
        fun onPriceItemClickListener(meter:PriceRange)
    }

}