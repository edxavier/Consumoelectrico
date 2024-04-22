package com.nicrosoft.consumoelectrico.ui2

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import com.nicrosoft.consumoelectrico.R

class ElectricLoadFragment : Fragment() {


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        setHasOptionsMenu(true)
        return inflater.inflate(R.layout.fragment_electric_load, container, false)
    }
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.electric_readings_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }
}