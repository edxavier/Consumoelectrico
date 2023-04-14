package com.nicrosoft.consumoelectrico.ui2.compose.main

import android.util.Log
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


@Composable
fun CircularProgressbar(
    size: Dp = 90.dp,
    foregroundIndicatorColor: Color = Color(0xFF00838f),
    shadowColor: Color = Color.LightGray,
    indicatorThickness: Dp = 4.dp,
    usage: Float = 0f,
    measureUnit: String = "",
    maxUsage: Float = 100f,
    animationDuration: Int = 1000,
    dataTextStyle: TextStyle = TextStyle(
        fontSize = 20.sp, fontWeight = FontWeight(700)
    ),
    remainingTextStyle: TextStyle = TextStyle(
        fontSize = 14.sp
    )
) {

    // It remembers the data usage value
    var dataUsageRemember by remember {
        mutableStateOf(0f)
    }

    // This is to animate the foreground indicator
    val dataUsageAnimate = animateFloatAsState(
        targetValue = dataUsageRemember,
        animationSpec = tween(
            durationMillis = animationDuration
        )
    )

    dataUsageRemember = usage

    Box(
        modifier = Modifier
            .size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier
                .size(size)
        ) {
            // For shadow
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(shadowColor, Color.White),
                    center = Offset(x = this.size.width / 2, y = this.size.height / 2),
                    radius = this.size.height / 2
                ),
                radius = this.size.height / 2,
                center = Offset(x = this.size.width / 2, y = this.size.height / 2)
            )

            // This is the white circle that appears on the top of the shadow circle
            drawCircle(
                color = Color.White,
                radius = (size / 2 - indicatorThickness).toPx(),
                center = Offset(x = this.size.width / 2, y = this.size.height / 2)
            )

            // Convert the dataUsage to angle
            val sweepAngle = (dataUsageAnimate.value) * 360 / maxUsage

            // Foreground indicator
            drawArc(
                color = foregroundIndicatorColor,
                startAngle = -90f,
                sweepAngle = sweepAngle,
                useCenter = false,
                style = Stroke(width = indicatorThickness.toPx(), cap = StrokeCap.Round),
                size = Size(
                    width = (size - indicatorThickness).toPx(),
                    height = (size - indicatorThickness).toPx()
                ),
                topLeft = Offset(
                    x = (indicatorThickness / 2).toPx(),
                    y = (indicatorThickness / 2).toPx()
                )
            )
        }

        // Display the data usage value
        DisplayText(
            measureUnit = measureUnit,
            animateNumber = dataUsageAnimate,
            dataTextStyle = dataTextStyle,
            remainingTextStyle = remainingTextStyle
        )
    }


}

@Composable
private fun DisplayText(
    animateNumber: State<Float>,
    dataTextStyle: TextStyle,
    remainingTextStyle: TextStyle,
    measureUnit: String = ""
) {
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Text that shows the number inside the circle
        Text(
            text = (animateNumber.value).toInt().toString().padStart(2, '0'),
            style = dataTextStyle
        )

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = measureUnit,
            style = remainingTextStyle
        )
    }
}

