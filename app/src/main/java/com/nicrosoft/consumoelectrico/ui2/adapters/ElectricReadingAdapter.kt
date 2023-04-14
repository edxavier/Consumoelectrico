package com.nicrosoft.consumoelectrico.ui2.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.nicrosoft.consumoelectrico.R
import com.nicrosoft.consumoelectrico.data.entities.ElectricReading
import com.nicrosoft.consumoelectrico.databinding.ItemElectricReadingBinding
import com.nicrosoft.consumoelectrico.utils.*
import java.util.*
import kotlin.time.ExperimentalTime

class ElectricReadingAdapter(
        private val itemClickListener: AdapterItemListener
): ListAdapter<ElectricReading, ElectricReadingAdapter.ViewHolder>(DiffCallback()) {

    class DiffCallback: DiffUtil.ItemCallback<ElectricReading>() {
        override fun areItemsTheSame(oldItem: ElectricReading, newItem: ElectricReading): Boolean {
            return oldItem.code == newItem.code
        }

        override fun areContentsTheSame(oldItem: ElectricReading, newItem: ElectricReading): Boolean {
            //Log.e("EDER", oldItem.readingValue.toString())
            //Log.e("EDER", newItem.readingValue.toString())
            return false
        }
    }

    class ViewHolder(itemView: View, binding: ItemElectricReadingBinding): RecyclerView.ViewHolder(itemView){
        val b = binding
        @ExperimentalTime
        fun bind(reading: ElectricReading, prev:ElectricReading?, listener: AdapterItemListener?){
            itemView.apply {
                //Disparar evento para que la vista que lo implemente tenda el objeto al que se le dio click
                this.setOnClickListener { listener?.onItemClickListener(reading) }
                b.rTxtReading.text = reading.readingValue.toInt().toString()
                b.rTxtHourlyAvg.text = String.format(Locale.getDefault(), "%.3f kWh", reading.kwAvgConsumption)
                b.rTxtDailyAvg.text = String.format(Locale.getDefault(), "%.2f kWh", (reading.kwAvgConsumption*24))
                
                b.rTxtConsumption.text = String.format(Locale.getDefault(), "%.2f kWh", reading.kwConsumption)
                b.rTxtAggConsumption.text = String.format(Locale.getDefault(), "%.2f kWh", reading.kwAggConsumption)

                b.rTxtDayMonth.text = reading.readingDate.formatDayMonth(context)
                b.rTxtYear.text = reading.readingDate.formatYear(context)
                b.rTxtTime.text = reading.readingDate.formatTimeAmPm(context)
                if(reading.consumptionPreviousHours>48){
                    b.rLabelConsumption.text = context.getString(R.string.label_consumption_since_last_reading, (reading.consumptionPreviousHours/24).toTwoDecimalPlace())
                }else{
                    b.rLabelConsumption.text = context.getString(R.string.label_consumption_in_hours, (reading.consumptionPreviousHours).toTwoDecimalPlace())
                }
                if (reading.comments.isEmpty())
                    b.rTxtObservations.setHidden()
                else{
                    b.rTxtObservations.setVisible()
                    b.rTxtObservations.text = reading.comments
                }

                if(prev!=null){
                    when{
                        reading.kwAvgConsumption > prev.kwAvgConsumption ->{
                            b.rTxtHourlyTrend.rotation = 270f
                            b.rTxtHourlyTrend.setColorFilter(ContextCompat.getColor(context, R.color.md_red_700))
                            b.rTxtHourlyAvg.setTextColor(ContextCompat.getColor(context, R.color.md_red_700))
                        }
                        reading.kwAvgConsumption < prev.kwAvgConsumption ->{
                            b.rTxtHourlyTrend.rotation = 90f
                            b.rTxtHourlyTrend.setColorFilter(ContextCompat.getColor(context, R.color.md_green_700))
                            b.rTxtHourlyAvg.setTextColor(ContextCompat.getColor(context, R.color.md_green_700))
                        }
                        else ->{
                            b.rTxtHourlyTrend.rotation = 0f
                            b.rTxtHourlyTrend.setColorFilter(ContextCompat.getColor(context, R.color.md_black_1000_75))
                            b.rTxtHourlyAvg.setTextColor(ContextCompat.getColor(context, R.color.md_black_1000_75))
                        }
                    }
                    when{
                        reading.kwAvgConsumption*24 > prev.kwAvgConsumption*24 ->{
                            b.rTxtDailyTrend.rotation = 270f
                            b.rTxtDailyTrend.setColorFilter(ContextCompat.getColor(context, R.color.md_red_700))
                            b.rTxtDailyAvg.setTextColor(ContextCompat.getColor(context, R.color.md_red_700))
                        }
                        reading.kwAvgConsumption*24 < prev.kwAvgConsumption*24 ->{
                            b.rTxtDailyTrend.rotation = 90f
                            b.rTxtDailyTrend.setColorFilter(ContextCompat.getColor(context, R.color.md_green_700))
                            b.rTxtDailyAvg.setTextColor(ContextCompat.getColor(context, R.color.md_green_700))
                        }
                        else ->{
                            b.rTxtDailyTrend.rotation = 0f
                            b.rTxtDailyTrend.setColorFilter(ContextCompat.getColor(context, R.color.md_black_1000_75))
                            b.rTxtDailyAvg.setTextColor(ContextCompat.getColor(context, R.color.md_black_1000_75))
                        }
                    }
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemElectricReadingBinding.inflate(LayoutInflater.from(parent.context!!))
        return ViewHolder(binding.root, binding)
    }

    @OptIn(ExperimentalTime::class)
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val previous = if(position<itemCount-1)
            getItem(position+1)
        else
            null
        holder.bind(getItem(position), previous, itemClickListener)
    }

    interface AdapterItemListener{
        fun onItemClickListener(reading:ElectricReading)
    }

}