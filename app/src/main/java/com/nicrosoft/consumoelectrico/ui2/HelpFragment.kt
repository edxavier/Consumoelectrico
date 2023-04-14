package com.nicrosoft.consumoelectrico.ui2

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.NavController
import com.nicrosoft.consumoelectrico.R
import com.nicrosoft.consumoelectrico.ScopeFragment

class HelpFragment : ScopeFragment() {

    private lateinit var navController: NavController

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_help, container, false)
    }

}
