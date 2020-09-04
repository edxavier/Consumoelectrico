package com.nicrosoft.consumoelectrico.utils

import java.lang.Exception

sealed class AppResult {
    object OK : AppResult()
    data class AppException(val exception: Exception):AppResult()
}