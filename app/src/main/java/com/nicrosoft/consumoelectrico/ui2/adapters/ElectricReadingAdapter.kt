package com.nicrosoft.consumoelectrico.ui2.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.nicrosoft.consumoelectrico.R
import com.nicrosoft.consumoelectrico.data.entities.ElectricReading
import com.nicrosoft.consumoelectrico.utils.*
import kotlinx.android.synthetic.main.item_electric_reading.view.*
import org.joda.time.LocalDate
import org.joda.time.Period
import org.joda.time.PeriodType
import java.util.*
import kotlin.time.DurationUnit
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

    class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){

        @ExperimentalTime
        fun bind(reading: ElectricReading, prev:ElectricReading?, listener: AdapterItemListener?){
            itemView.apply {
                //Disparar evento para que la vista que lo implemente tenda el objeto al que se le dio click
                this.setOnClickListener { listener?.onItemClickListener(reading) }
                with(this){
                    this.r_txt_reading.text = reading.readingValue.toInt().toString()
                    this.r_txt_hourly_avg.text = String.format(Locale.getDefault(), "%.3f kWh", reading.kwAvgConsumption)
                    this.r_txt_daily_avg.text = String.format(Locale.getDefault(), "%.2f kWh", (reading.kwAvgConsumption*24))

                    this.r_txt_consumption.text = String.format(Locale.getDefault(), "%.2f kWh", reading.kwConsumption)
                    this.r_txt_agg_consumption.text = String.format(Locale.getDefault(), "%.2f kWh", reading.kwAggConsumption)

                    this.r_txt_day_month.text = reading.readingDate.formatDayMonth(context)
                    this.r_txt_year.text = reading.readingDate.formatYear(context)
                    this.r_txt_time.text = reading.readingDate.formatTimeAmPm(context)
                    if(reading.consumptionPreviousHours>48){
                        this.r_label_consumption.text = context.getString(R.string.label_consumption_since_last_reading, (reading.consumptionPreviousHours/24).toTwoDecimalPlace())
                    }else{
                        this.r_label_consumption.text = context.getString(R.string.label_consumption_in_hours, (reading.consumptionPreviousHours).toTwoDecimalPlace())
                    }
                    if (reading.comments.isEmpty())
                        this.r_txt_observations.setHidden()
                    else{
                        this.r_txt_observations.setVisible()
                        this.r_txt_observations.text = reading.comments
                    }

                    if(prev!=null){
                        when{
                            reading.kwAvgConsumption > prev.kwAvgConsumption ->{
                                r_txt_hourly_trend.rotation = 270f
                                r_txt_hourly_trend.setColorFilter(ContextCompat.getColor(context, R.color.md_red_700))
                                r_txt_hourly_avg.setTextColor(ContextCompat.getColor(context, R.color.md_red_700))
                            }
                            reading.kwAvgConsumption < prev.kwAvgConsumption ->{
                                r_txt_hourly_trend.rotation = 90f
                                r_txt_hourly_trend.setColorFilter(ContextCompat.getColor(context, R.color.md_green_700))
                                r_txt_hourly_avg.setTextColor(ContextCompat.getColor(context, R.color.md_green_700))
                            }
                            else ->{
                                r_txt_hourly_trend.rotation = 0f
                                r_txt_hourly_trend.setColorFilter(ContextCompat.getColor(context, R.color.md_black_1000_75))
                                r_txt_hourly_avg.setTextColor(ContextCompat.getColor(context, R.color.md_black_1000_75))
                            }
                        }
                        when{
                            reading.kwAvgConsumption*24 > prev.kwAvgConsumption*24 ->{
                                r_txt_daily_trend.rotation = 270f
                                r_txt_daily_trend.setColorFilter(ContextCompat.getColor(context, R.color.md_red_700))
                                r_txt_daily_avg.setTextColor(ContextCompat.getColor(context, R.color.md_red_700))
                            }
                            reading.kwAvgConsumption*24 < prev.kwAvgConsumption*24 ->{
                                r_txt_daily_trend.rotation = 90f
                                r_txt_daily_trend.setColorFilter(ContextCompat.getColor(context, R.color.md_green_700))
                                r_txt_daily_avg.setTextColor(ContextCompat.getColor(context, R.color.md_green_700))
                            }
                            else ->{
                                r_txt_daily_trend.rotation = 0f
                                r_txt_daily_trend.setColorFilter(ContextCompat.getColor(context, R.color.md_black_1000_75))
                                r_txt_daily_avg.setTextColor(ContextCompat.getColor(context, R.color.md_black_1000_75))
                            }
                        }
                    }

                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context!!).inflate(R.layout.item_electric_reading, parent, false))
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