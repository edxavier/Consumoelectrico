package com.nicrosoft.consumoelectrico.screens.readings

import android.provider.Settings.Global.getString
import android.util.Log
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.EaseInOutCubic
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.nicrosoft.consumoelectrico.R
import com.nicrosoft.consumoelectrico.data.entities.ElectricReading
import com.nicrosoft.consumoelectrico.screens.CircularProgress
import com.nicrosoft.consumoelectrico.screens.NoDataScreen
import com.nicrosoft.consumoelectrico.viewmodels.ElectricViewModel

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ReadingsScreen(
    readings: List<ElectricReading>,
    isLoading: Boolean = true,
    onItemClick: ((reading:ElectricReading) -> Unit)? = null
) {

    val listState = rememberLazyListState()

    if(isLoading){
      CircularProgress()
    }else if(readings.isNotEmpty()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 2.dp, horizontal = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            state = listState
        ) {
            items(items = readings, key = { it.code }) { reading ->
                val previousIndex = (readings.indexOfFirst { it.code == reading.code })
                val previousReading = if(previousIndex>=0 && (previousIndex+1)<readings.size)
                    readings[previousIndex+1]
                else
                    null
                ReadingCard(
                    reading = reading,
                    prevReading = previousReading,
                    onClick = { onItemClick?.let { it(reading) } },
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