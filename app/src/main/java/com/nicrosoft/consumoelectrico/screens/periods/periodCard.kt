package com.nicrosoft.consumoelectrico.screens.periods

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    var cardModifier = modifier.fillMaxWidth().clip(shape = RoundedCornerShape(12.dp))
    if (period.active){
        cardModifier = cardModifier.clickable {
            onClick?.let { it() }
        }
    }
    ElevatedCard(
        modifier = cardModifier
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
        ){
            val bgColor = if(period.active) MaterialTheme.colorScheme.tertiaryContainer else MaterialTheme.colorScheme.secondaryContainer
            val fgColor = if(period.active) MaterialTheme.colorScheme.onTertiaryContainer else MaterialTheme.colorScheme.onSecondaryContainer
            Column(
                modifier = Modifier
                    .background(bgColor)
                    .padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ){
                if(period.active) {
                    Icon(imageVector = ImageVector.vectorResource(id = R.drawable.undo_left), contentDescription = null)
                }
                Text(
                    text = period.fromDate.formatDayMonth(context),
                    fontSize = 14.sp,
                    color = fgColor
                )
                Text(
                    text = period.fromDate.formatYear(context),
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 22.sp,
                    color = fgColor
                )
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(12.dp),
                horizontalAlignment = Alignment.End
            ){
                val annotatedString = buildAnnotatedString {
                    append("${period.totalKw.toInt()}".padStart(4, '0'))
                    withStyle(
                        style = SpanStyle(fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                    ) {
                        append("kWh")
                    }
                }
                Text(
                    text = annotatedString,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 20.sp,
                    color = MaterialTheme.colorScheme.secondary
                )
                Text(
                    text = "$coinSymbol${period.totalBill.toTwoDecimalPlace()}",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }
    }

}

