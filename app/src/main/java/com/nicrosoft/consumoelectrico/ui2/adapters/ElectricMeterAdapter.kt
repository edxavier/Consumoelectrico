package com.nicrosoft.consumoelectrico.ui2.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.nicrosoft.consumoelectrico.R
import com.nicrosoft.consumoelectrico.data.entities.ElectricMeter
import com.nicrosoft.consumoelectrico.ui2.ElectricViewModel
import com.nicrosoft.consumoelectrico.utils.formatDate
import com.nicrosoft.consumoelectrico.utils.getLastReading
import com.nicrosoft.consumoelectrico.utils.toTwoDecimalPlace
import kotlinx.android.synthetic.main.item_electric_meter.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.joda.time.LocalDate
import org.joda.time.Period
import org.joda.time.PeriodType
import java.util.*

class ElectricMeterAdapter(
        private val itemClickListener: AdapterItemListener,
        private val viewModel: ElectricViewModel, private val scope:CoroutineScope
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

        @SuppressLint("SetTextI18n")
        fun bind(meter: ElectricMeter, listener: AdapterItemListener?,
                 viewModel: ElectricViewModel,
                 scope:CoroutineScope){
            itemView.apply {
                //Disparar evento para que la vista que lo implemente tenda el objeto al que se le dio click
                this.setOnClickListener { listener?.onItemClickListener(meter) }
                with(this){
                    button_item_details.setOnClickListener { listener?.onItemDetailListener(meter) }
                    button_new_reading.setOnClickListener {  listener?.onItemNewReading(meter) }
                    item_txt_meter_name.text = meter.name
                    scope.launch {
                        val lastReadings = meter.getLastReading(viewModel)
                        if(lastReadings.isNotEmpty()){
                            val last = lastReadings.first()
                            item_txt_meter_last_reading.text = "${last.readingValue} kWh"
                            val previousHours = Period(LocalDate(last.readingDate), LocalDate(Date()), PeriodType.hours())
                            if(previousHours.hours>=48)
                                item_txt_meter_readed_since.text = "Hace ${previousHours.hours/24} dias"
                            else
                                item_txt_meter_readed_since.text = "Hace ${previousHours.hours} horas"
                            item_txt_period_daily_avg.text = (last.kwAvgConsumption*24).toTwoDecimalPlace()
                            item_circular_progress.maxProgress = meter.maxKwLimit.toDouble()
                            item_circular_progress2.maxProgress = meter.periodLength.toDouble()

                            item_circular_progress.setCurrentProgress(last.kwAggConsumption.toDouble())
                            item_circular_progress2.setCurrentProgress((last.consumptionHours/24).toDouble())
                        }
                    }
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context!!).inflate(R.layout.item_electric_meter, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position), itemClickListener, viewModel, scope)
    }

    interface AdapterItemListener{
        fun onItemClickListener(meter:ElectricMeter)
        fun onItemDetailListener(meter:ElectricMeter)
        fun onItemNewReading(meter:ElectricMeter)
    }

}