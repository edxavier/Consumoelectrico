package com.nicrosoft.consumoelectrico

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import kotlinx.android.synthetic.main.emeter_list_fragment.*


class EmeterListFragment : Fragment() {

    companion object {
        fun newInstance() = EmeterListFragment()
    }

    private lateinit var viewModel: EmetertViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.emeter_list_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(EmetertViewModel::class.java)
        val animation: Animation = AnimationUtils.loadAnimation(context, R.anim.fade_in)
        val slidein: Animation = AnimationUtils.loadAnimation(context, R.anim.slide_in)
        animation_view.startAnimation(animation)
        message_title.startAnimation(slidein)
        message_body.startAnimation(slidein)
    }

}