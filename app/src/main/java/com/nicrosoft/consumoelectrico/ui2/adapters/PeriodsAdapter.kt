package com.nicrosoft.consumoelectrico.ui2.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.nicrosoft.consumoelectrico.R
import com.nicrosoft.consumoelectrico.data.entities.ElectricBillPeriod
import com.nicrosoft.consumoelectrico.data.entities.PriceRange
import com.nicrosoft.consumoelectrico.utils.formatDate
import com.nicrosoft.consumoelectrico.utils.toTwoDecimalPlace
import com.pixplicity.easyprefs.library.Prefs
import kotlinx.android.synthetic.main.item_period.view.*
import kotlinx.android.synthetic.main.item_price_range.view.*

class PeriodsAdapter(
        private val itemClickListener: PeriodItemListener
): ListAdapter<ElectricBillPeriod, PeriodsAdapter.ViewHolder>(DiffCallback()) {

    class DiffCallback: DiffUtil.ItemCallback<ElectricBillPeriod>() {
        override fun areItemsTheSame(oldItem: ElectricBillPeriod, newItem: ElectricBillPeriod): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: ElectricBillPeriod, newItem: ElectricBillPeriod): Boolean {
            return oldItem == newItem
        }
    }

    class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){

        @SuppressLint("SetTextI18n")
        fun bind(period: ElectricBillPeriod, listener: PeriodItemListener?){
            itemView.apply {
                //Disparar evento para que la vista que lo implemente tenda el objeto al que se le dio click
                this.setOnClickListener { listener?.onPeriodItemClickListener(period) }
                with(this){
                    item_label_from_period.text = period.fromDate.formatDate(context)
                    item_label_to_period.text = period.toDate.formatDate(context)
                    item_label_total_kw.text = period.totalKw.toTwoDecimalPlace()
                    item_label_total_spend.text = period.totalBill.toTwoDecimalPlace()
                    if(period.active)
                        item_period_status.setColorFilter(ContextCompat.getColor(context, R.color.md_green_500))
                    else
                        item_period_status.setColorFilter(ContextCompat.getColor(context, R.color.md_grey_500))

                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context!!).inflate(R.layout.item_period, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position), itemClickListener)
    }

    interface PeriodItemListener{
        fun onPeriodItemClickListener(period:ElectricBillPeriod)
    }

}