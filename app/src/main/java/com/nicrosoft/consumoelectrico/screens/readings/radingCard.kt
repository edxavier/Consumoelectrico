package com.nicrosoft.consumoelectrico.screens.readings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
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

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        onClick = { onClick?.invoke() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min),
            verticalAlignment = Alignment.CenterVertically
        ){
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(70.dp)
                    .background(MaterialTheme.colorScheme.primary)
                    .padding(vertical = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ){
                Text(
                    text = reading.readingDate.formatDayMonth(context).uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                )
                Text(
                    text = reading.readingDate.formatYear(context),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${reading.readingValue.toInt()}".padStart(5, '0'),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        letterSpacing = 1.sp
                    )

                    val deltaString = buildAnnotatedString {
                        withStyle(style = SpanStyle(fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.tertiary, fontSize = 18.sp)) {
                            append("+${String.format(Locale.getDefault(), "%.1f", reading.kwConsumption)}")
                        }
                        withStyle(style = SpanStyle(fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)) {
                            append(" kWh")
                        }
                    }
                    Text(text = deltaString)
                }

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
                Spacer(modifier = Modifier.height(12.dp))

                Row(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.total_period),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${String.format(Locale.getDefault(), "%.1f", reading.kwAggConsumption)} kWh",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    var avgColor = MaterialTheme.colorScheme.onSurfaceVariant
                    prevReading?.let {
                        if(reading.kwAvgConsumption*24 > it.kwAvgConsumption*24) avgColor = MaterialTheme.colorScheme.error
                        else if (reading.kwAvgConsumption*24 < it.kwAvgConsumption*24) avgColor = MaterialTheme.colorScheme.tertiary
                    }

                    Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                        Text(
                            text = stringResource(R.string.daily_avg_label),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${String.format(Locale.getDefault(), "%.2f", (reading.kwAvgConsumption*24))} kWh/d",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = avgColor
                        )
                    }
                }
            }
        }
    }
}
