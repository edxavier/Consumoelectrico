package com.nicrosoft.consumoelectrico.realm

import java.util.*

/**
 * Created by Eder Xavier Rojas on 20/09/2016.
 */
object UUIDGenerator {
    @JvmStatic
    fun nextUUID(): String {
        return UUID.randomUUID().toString()
    }
}