package com.nicrosoft.consumoelectrico.screens.periods

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.nicrosoft.consumoelectrico.R
import com.nicrosoft.consumoelectrico.data.entities.ElectricBillPeriod
import com.nicrosoft.consumoelectrico.utils.formatDayMonth
import com.nicrosoft.consumoelectrico.utils.formatYear
import com.nicrosoft.consumoelectrico.utils.toTwoDecimalPlace
import com.pixplicity.easyprefs.library.Prefs


@Composable
fun PeriodCard(
    period: ElectricBillPeriod,
    modifier: Modifier,
    onClick: (() -> Unit)? = null
){
    val context = LocalContext.current
    val coinSymbol = Prefs.getString("price_simbol", "$")

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (period.active) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        ),
        onClick = { if (period.active) onClick?.invoke() }
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min)
            ){
                val sideBgColor = if (period.active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
                val sideFgColor = if (period.active) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)

                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(80.dp)
                        .background(sideBgColor)
                        .padding(vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ){
                    Text(
                        text = period.fromDate.formatDayMonth(context).uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = sideFgColor.copy(alpha = 0.8f)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = period.fromDate.formatYear(context),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = sideFgColor
                    )
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 16.dp, top = 14.dp, end = 16.dp, bottom = 14.dp),
                    verticalArrangement = Arrangement.Center
                ){
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = if (period.active) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f),
                        ){
                            Text(
                                text = if(period.active) stringResource(R.string.active_period) else stringResource(R.string.closed_period),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = if(period.active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                            )
                        }

                        if(period.active) {
                            Icon(
                                imageVector = ImageVector.vectorResource(id = R.drawable.ic_play_arrow),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Column {
                            Text(
                                text = "${period.totalKw.toInt()}".padStart(4, '0'),
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.tertiary
                            )
                            Text(
                                text = "kWh",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                            )
                        }

                        Column(
                            horizontalAlignment = Alignment.End
                        ) {
                            Text(
                                text = "$coinSymbol${period.totalBill.toTwoDecimalPlace()}",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = stringResource(R.string.total),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                            )
                        }
                    }

                    if(period.active && period.totalKw > 0) {
                        Spacer(modifier = Modifier.height(12.dp))
                        val progress = (period.totalKw / 1000f).coerceIn(0f, 1f)
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = MaterialTheme.colorScheme.tertiary,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant,
                        )
                    }
                }
            }

            if (!period.active) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                )
            }
        }
    }
}
