package com.nicrosoft.consumoelectrico.ui2.compose.main

import android.text.Spannable
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nicrosoft.consumoelectrico.R
import com.nicrosoft.consumoelectrico.data.entities.ElectricMeter
import com.nicrosoft.consumoelectrico.data.entities.ElectricReading
import com.nicrosoft.consumoelectrico.utils.*
import com.nicrosoft.consumoelectrico.viewmodels.ElectricViewModel
import kotlinx.coroutines.launch
import android.text.SpannableStringBuilder
import android.text.style.RelativeSizeSpan
import android.text.style.SubscriptSpan
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import java.util.*


@Composable
fun MeterPreviewCard(
    onClick: (() -> Unit)? = null,
    onDetailsClick: ((meter:ElectricMeter) -> Unit)? = null,
    meter:ElectricMeter,
    viewModel:ElectricViewModel,
    modifier: Modifier
){

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape = RoundedCornerShape(16.dp))
            .background(
                brush = Brush.verticalGradient(
                    colors = grayGradient
                )
            )
            .clickable {
                onDetailsClick?.let { it(meter) }
            },
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        var lastRead by remember { mutableStateOf(ElectricReading()) }
        val scope = rememberCoroutineScope()
        SideEffect {
            scope.launch {
                meter.getLastReading(viewModel)?.let {
                    lastRead = it
                }
            }
        }
        val previousHours = Date().hoursSinceDate(lastRead.readingDate)
        var timeSince = ""
        timeSince = if(previousHours>=48)
            stringResource(R.string.since, (previousHours/24))
        else
            stringResource(R.string.hours, previousHours)
        Column(
            Modifier
                .fillMaxWidth()
                .background(brush = Brush.verticalGradient(colors = pinkGradient))
                .padding(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(text = meter.name.replaceFirstChar { it.uppercase() }, fontSize = 22.sp,
                    color = Color.White, fontWeight = FontWeight(500), modifier = Modifier.weight(1f))
                Image(
                    painter = painterResource(id = R.drawable.more_vert),
                    contentDescription = null,
                    modifier = Modifier.clickable {
                        onClick?.let { it() }
                    }
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                // Creating a string span
                val myString = "${lastRead.readingValue.toInt()} kWh"
                val start = myString.length - 4
                val end = start + 3
                val mStringSpan = SpannableStringBuilder(myString)
                mStringSpan.setSpan(SubscriptSpan(), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                mStringSpan.setSpan(RelativeSizeSpan(0.5f),start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                val annotatedString = buildAnnotatedString {
                    append("${lastRead.readingValue.toInt()} ")
                    withStyle(style = SpanStyle(fontSize = 12.sp)) {
                        append("kWh")
                    }
                }
                Text(
                    text = annotatedString, color = Color.White, fontSize = 16.sp,
                    modifier = Modifier.weight(1f)
                )
                Image(painter = painterResource(id = R.drawable.access_time),
                    contentDescription = null, modifier = Modifier.size(15.dp) )
                Text(text = timeSince, color = Color.White, fontSize = 13.sp, modifier = Modifier.padding(start = 4.dp))
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val kwhColor = if (lastRead.kwAggConsumption > meter.maxKwLimit) Color(0xffe91e63) else Color(0xff009688)
            val daysColor = if (lastRead.consumptionHours/24 > meter.periodLength) Color(0xff9c27b0) else Color(0xff2196f3)

            CircularProgressbar(
                maxUsage = meter.maxKwLimit.toFloat(),
                usage = lastRead.kwAggConsumption,
                measureUnit = "kWh",
                foregroundIndicatorColor = kwhColor
            )
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val dailyAvg = lastRead.kwAvgConsumption*24
                val avgLimit =  meter.maxKwLimit / meter.periodLength
                val color = if (dailyAvg > avgLimit) Color(0xffb71c1c) else Color(0xff455a64)
                Text(
                    text = stringResource(id = R.string.consumption).uppercase(), fontSize = 12.sp,
                    fontWeight = FontWeight(600),
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = (dailyAvg).toTwoDecimalPlace(), fontSize = 28.sp,
                    fontWeight = FontWeight(600),
                    color = color
                )
                Text(
                    text = stringResource(id = R.string.daily_avg_unit), fontSize = 14.sp,
                    color = color
                )
            }
            CircularProgressbar(
                maxUsage = meter.periodLength.toFloat(),
                usage = lastRead.consumptionHours/24,
                measureUnit = stringResource(id = R.string.label_days),
                foregroundIndicatorColor = daysColor
            )
        }

       Spacer(modifier = Modifier.height(8.dp))


    }
}
