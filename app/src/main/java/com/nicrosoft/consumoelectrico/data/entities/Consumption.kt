package com.nicrosoft.consumoelectrico.data.entities

// Si se desconoce el wattage de un equipo, podemos calcularlo sabiendo el amperaje y el voltaje, debemos multiplicarlo para obtener los watts

class Consumption (
        var name:String = "",
        var quantity:Int = 0,
        var watts: Float = 0f,
        var hoursMonthUsage: Float = 0f,
        var totalConsumption: Float = 0f
)