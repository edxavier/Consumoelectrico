package com.nicrosoft.consumoelectrico.ui2.adapters

import androidx.recyclerview.widget.DiffUtil
import com.nicrosoft.consumoelectrico.data.entities.ElectricMeter

class EmeterDiffCallback2: DiffUtil.ItemCallback<ElectricMeter>() {
    override fun areItemsTheSame(oldItem: ElectricMeter, newItem: ElectricMeter): Boolean {
        return false
    }

    override fun areContentsTheSame(oldItem: ElectricMeter, newItem: ElectricMeter): Boolean {
        return true
    }
}