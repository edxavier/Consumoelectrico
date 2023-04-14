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
import com.nicrosoft.consumoelectrico.databinding.ItemPeriodBinding
import com.nicrosoft.consumoelectrico.utils.formatDate
import com.nicrosoft.consumoelectrico.utils.toTwoDecimalPlace
import com.nicrosoft.consumoelectrico.viewmodels.ElectricViewModel
import com.pixplicity.easyprefs.library.Prefs
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class PeriodsAdapter(
        private val itemClickListener: PeriodItemListener,
        private val viewModel: ElectricViewModel
): ListAdapter<ElectricBillPeriod, PeriodsAdapter.ViewHolder>(DiffCallback()) {

    class DiffCallback: DiffUtil.ItemCallback<ElectricBillPeriod>() {
        override fun areItemsTheSame(oldItem: ElectricBillPeriod, newItem: ElectricBillPeriod): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: ElectricBillPeriod, newItem: ElectricBillPeriod): Boolean {
            return oldItem == newItem
        }
    }

    class ViewHolder(itemView: View, _binding: ItemPeriodBinding): RecyclerView.ViewHolder(itemView){
        val binding: ItemPeriodBinding = _binding

        @SuppressLint("SetTextI18n")
        fun bind(period: ElectricBillPeriod, listener: PeriodItemListener?,  viewModel: ElectricViewModel){
            itemView.apply {
                //Disparar evento para que la vista que lo implemente tenda el objeto al que se le dio click
                this.setOnClickListener { listener?.onPeriodItemClickListener(period) }
                with(this){
                    binding.itemLabelFromPeriod
                    binding.itemLabelFromPeriod.text = period.fromDate.formatDate(context)
                    binding.itemLabelToPeriod.text = period.toDate.formatDate(context)

                    GlobalScope.launch {
                        val lastR = viewModel.getLastPeriodReading(period.code)
                        val periods = viewModel.getMeterAllPeriods(period.meterCode)
                        val firstReading = if (periods.size > 1)
                            viewModel.getLastPeriodReading(periods[1].code)
                        else
                            viewModel.getFirstPeriodReading(period.code)
                        firstReading?.let { fr ->
                            binding.itemFirstReading.text = " ${fr.readingValue.toTwoDecimalPlace()} Kwh"
                        }
                        binding.itemLastReading.text = "${lastR?.readingValue?.toTwoDecimalPlace()} Kwh"
                    }
                    val coinSymbol = Prefs.getString("price_simbol", "$")
                    binding.itemLabelTotalKw.text = period.totalKw.toTwoDecimalPlace()
                    binding.itemLabelTotalSpend.text = "$coinSymbol${period.totalBill.toTwoDecimalPlace()}"
                    if(period.active)
                        binding.itemPeriodStatus.setBackgroundColor(ContextCompat.getColor(context, R.color.md_green_500))
                    else
                        binding.itemPeriodStatus.setBackgroundColor(ContextCompat.getColor(context, R.color.md_grey_500))

                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemPeriodBinding.inflate(LayoutInflater.from(parent.context!!))
        return ViewHolder(binding.root, binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position), itemClickListener, viewModel)
    }

    interface PeriodItemListener{
        fun onPeriodItemClickListener(period:ElectricBillPeriod)
    }

}