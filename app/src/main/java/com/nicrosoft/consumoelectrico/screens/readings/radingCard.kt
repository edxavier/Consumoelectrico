package com.nicrosoft.consumoelectrico.screens.readings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Divider
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nicrosoft.consumoelectrico.R
import com.nicrosoft.consumoelectrico.data.entities.ElectricReading
import com.nicrosoft.consumoelectrico.utils.*
import java.util.*


@Composable
fun ReadingCard(
    reading: ElectricReading,
    prevReading: ElectricReading?,
    modifier: Modifier,
    onClick: (() -> Unit)? = null
){
    val context = LocalContext.current
    ElevatedCard(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape = RoundedCornerShape(12.dp))
            .clickable {
                onClick?.let { it() }
            }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
        ){
            val annotatedString = buildAnnotatedString {
                append("${reading.readingValue.toInt()}".padStart(5, '0'))
                withStyle(
                    style = SpanStyle(fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                ) {
                    append("kWh")
                }
            }
            Column(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.secondaryContainer)
                    .padding(12.dp)
            ){
                Text(
                    text = annotatedString,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = reading.readingDate.formatDayMonth(context),
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.secondary
                )
                Text(
                    text = reading.readingDate.formatYear(context),
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 20.sp,
                    color = MaterialTheme.colorScheme.secondary
                )
                Text(
                    text = reading.readingDate.formatTimeAmPm(context),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier
                )
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(12.dp),
                horizontalAlignment = Alignment.End
            ) {
                val fontSize = 13.sp
                val consumptionLabel = if(reading.consumptionPreviousHours>72){
                    context.getString(R.string.label_consumption_since_last_reading, (reading.consumptionPreviousHours/24).toTwoDecimalPlace())
                }else{
                    context.getString(R.string.label_consumption_in_hours, (reading.consumptionPreviousHours).toTwoDecimalPlace())
                }
                val consumptionString = buildAnnotatedString {
                    append("${context.getString(R.string.consumption)}: ")
                    withStyle(
                        style = SpanStyle(
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    ){append(String.format(Locale.getDefault(), "%.0f", reading.kwAggConsumption))}
                    withStyle(
                        style = SpanStyle(
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        append(" kWh")
                    }
                }
                Text(
                    text = consumptionString,
                    color = MaterialTheme.colorScheme.outline,
                    fontWeight = FontWeight.SemiBold,  fontSize = fontSize
                )

                val consumption2String = buildAnnotatedString {
                    append( "$consumptionLabel: ")
                    withStyle(
                        style = SpanStyle(
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    ) { append(String.format(Locale.getDefault(), "%.0f", reading.kwConsumption)) }
                    withStyle(
                        style = SpanStyle(fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
                    ) { append(" kWh") }
                }
                Text(
                    text = consumption2String,
                    color = MaterialTheme.colorScheme.outline,
                    fontWeight = FontWeight.SemiBold,fontSize = fontSize
                )
                val avgBaseColor = MaterialTheme.colorScheme.primary
                var avgColor = avgBaseColor
                prevReading?.let {
                    avgColor = if(reading.kwAvgConsumption*24>it.kwAvgConsumption*24)
                        Color.Red
                    else
                        Color(0xff388e3c)
                }
                val dailyAvgStr = buildAnnotatedString {

                    append("${context.getString(R.string.daily_avg)}: ")
                    withStyle(
                        style = SpanStyle(
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = avgColor
                        )
                    ){append(String.format(Locale.getDefault(), "%.2f", (reading.kwAvgConsumption*24)))}
                    withStyle(
                        style = SpanStyle(fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
                    ) { append(" kWh") }
                }
                Text(
                    text = dailyAvgStr,
                    color = MaterialTheme.colorScheme.outline,
                    fontWeight = FontWeight.SemiBold,fontSize = fontSize
                )
                val hourlyAvgStr = buildAnnotatedString {

                    append("${context.getString(R.string.hourly_avg)}: ")
                    withStyle(
                        style = SpanStyle(
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = avgColor
                        )
                    ) { append(String.format(Locale.getDefault(), "%.2f", (reading.kwAvgConsumption))) }
                    withStyle(
                        style = SpanStyle(fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
                    ) { append(" kWh") }
                }
                Text(
                    text = hourlyAvgStr,
                    color = MaterialTheme.colorScheme.outline,
                    fontWeight = FontWeight.SemiBold,fontSize = fontSize
                )
            }
        }
        if(reading.comments.isNotEmpty()) {
            Divider(modifier = Modifier
                .height(1.dp)
                .fillMaxWidth())
            Text(
                text = reading.comments,
                color = MaterialTheme.colorScheme.outline,
                fontSize = 12.sp, lineHeight = 13.sp,
                fontStyle = FontStyle.Italic,
                modifier = Modifier.padding(8.dp)
            )
        }
    }

}

