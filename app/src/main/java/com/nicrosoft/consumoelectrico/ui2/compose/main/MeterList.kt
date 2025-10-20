package com.nicrosoft.consumoelectrico.ui2.compose.main

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import com.nicrosoft.consumoelectrico.viewmodels.ElectricViewModel

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun MeterList(
    children: @Composable() (p: PaddingValues) -> Unit,
    onFabClick: (() -> Unit)? = null,
    viewModel: ElectricViewModel
){
    MaterialTheme{
        Scaffold(
            floatingActionButton = {
                AnimatedVisibility(visible = viewModel.expandedFab, enter = scaleIn(),
                    exit = scaleOut(),) {
                    FloatingActionButton(
                        onClick = {
                            onFabClick?.let { it() }
                        },
                        backgroundColor = Color(0xff3f51b5),
                        contentColor = Color.White
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null
                        )
                    }
                }
            },
            floatingActionButtonPosition = FabPosition.End,
            isFloatingActionButtonDocked = false
        ){ p ->
           children(p)
        }

    }

}