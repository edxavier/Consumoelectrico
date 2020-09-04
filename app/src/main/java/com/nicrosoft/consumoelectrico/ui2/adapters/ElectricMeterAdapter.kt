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
import com.nicrosoft.consumoelectrico.viewmodels.ElectricViewModel
import com.nicrosoft.consumoelectrico.utils.*
import kotlinx.android.synthetic.main.item_electric_meter.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.util.*
import kotlin.time.ExperimentalTime

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

        @ExperimentalTime
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
                        lastReadings?.let{
                            item_txt_meter_last_reading.text = "${lastReadings.readingValue.toInt()} kWh"
                            val previousHours = Date().hoursSinceDate(lastReadings.readingDate)
                            if(previousHours>=48)
                                item_txt_meter_readed_since.text = "Hace ${previousHours/24} dias"
                            else
                                item_txt_meter_readed_since.text = "Hace ${previousHours} horas"
                            item_txt_period_daily_avg.text = (lastReadings.kwAvgConsumption*24).toTwoDecimalPlace()
                            item_circular_progress.maxProgress = meter.maxKwLimit.toDouble()
                            item_circular_progress2.maxProgress = meter.periodLength.toDouble()

                            item_circular_progress.setCurrentProgress(lastReadings.kwAggConsumption.toDouble())
                            item_circular_progress2.setCurrentProgress((lastReadings.consumptionHours/24).toDouble())
                            val avgLimit = try { meter.maxKwLimit.toFloat() / meter.periodLength.toFloat() }catch (e:Exception){0f}
                            if(lastReadings.kwAvgConsumption*24 > avgLimit){
                                item_warning_msg.setVisible()
                                item_warning_icon.setVisible()
                            }else{
                                item_warning_msg.setHidden()
                                item_warning_icon.setHidden()
                            }
                            if(lastReadings.consumptionHours/24> meter.periodLength){
                                val v = context.getString(R.string.days_consumed_val, lastReadings.consumptionHours/24-meter.periodLength)
                                item_period_day_excess.setVisible()
                                item_period_day_excess.text = "+$v"
                            }else
                                item_period_day_excess.setHidden()

                            if(lastReadings.kwAggConsumption> meter.maxKwLimit){
                                val v = context.getString(R.string.initial_reading_val, (lastReadings.kwAggConsumption-meter.maxKwLimit).toTwoDecimalPlace())
                                item_consumption_excess.setVisible()
                                item_consumption_excess.text = "+$v"
                            }else
                                item_consumption_excess.setHidden()

                        }
                    }
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context!!).inflate(R.layout.item_electric_meter, parent, false))
    }

    @ExperimentalTime
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position), itemClickListener, viewModel, scope)
    }

    interface AdapterItemListener{
        fun onItemClickListener(meter:ElectricMeter)
        fun onItemDetailListener(meter:ElectricMeter)
        fun onItemNewReading(meter:ElectricMeter)
    }

}