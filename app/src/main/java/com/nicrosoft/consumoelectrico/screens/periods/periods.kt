package com.nicrosoft.consumoelectrico.screens.periods

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.EaseInOutCubic
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.nicrosoft.consumoelectrico.R
import com.nicrosoft.consumoelectrico.data.entities.ElectricBillPeriod
import com.nicrosoft.consumoelectrico.screens.CircularProgress
import com.nicrosoft.consumoelectrico.screens.NoDataScreen


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PeriodsScreen(
    periods: List<ElectricBillPeriod>,
    isLoading: Boolean = true,
    onItemClick: ((reading:ElectricBillPeriod) -> Unit)? = null
) {

    val listState = rememberLazyListState()

    if(isLoading){
      CircularProgress()
    }else if(periods.isNotEmpty()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 2.dp, horizontal = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            state = listState
        ) {
            items(items = periods, key = { it.code }) { period ->
                PeriodCard(
                    period = period,
                    onClick = {
                           onItemClick?.let { it(period) }
                    },
                    modifier = Modifier.animateContentSize(
                        animationSpec = tween(
                            durationMillis = 350,
                            easing = EaseInOutCubic
                        )
                    )
                )
            }
        }
    }else{
        val context = LocalContext.current
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            NoDataScreen(
                message = context.getString(R.string.main_empty_suggest),
                imageId = R.raw.bulb_animation,
                title = context.getString(R.string.main_emty_title)
            )
        }

    }
}