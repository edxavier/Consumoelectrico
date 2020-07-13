package com.nicrosoft.consumoelectrico.utils

import android.content.Context
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.nicrosoft.consumoelectrico.R
import kotlinx.android.synthetic.main.emeter_list_fragment.*
import java.text.SimpleDateFormat
import java.util.*


fun SwipeRefreshLayout.setAppColors(){
    this.setColorSchemeResources(
        R.color.md_amber_500,
        R.color.secondaryColor,
        R.color.md_blue_500,
        R.color.primaryColor)
}

fun RecyclerView.hideFabButtonOnScroll(fab:FloatingActionButton){
    this.addOnScrollListener(object: RecyclerView.OnScrollListener(){
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            if (dy > 0 && fab.visibility == View.VISIBLE) {
                fab.hide()
            } else if (dy < 0 && fab.visibility != View.VISIBLE) {
                fab.show()
            }
        }
    })
}

fun View.setHidden(){this.visibility = View.GONE}

fun View.setVisible(){this.visibility = View.VISIBLE}

fun View.fadeIn(){
    val animation: Animation = AnimationUtils.loadAnimation(context, R.anim.fade_in)
    this.startAnimation(animation)
}

fun View.fadeZoomIn(){
    val animation: Animation = AnimationUtils.loadAnimation(context, R.anim.fade_zoom_in)
    this.startAnimation(animation)
}

fun View.slideIn(){
    val animation: Animation = AnimationUtils.loadAnimation(context, R.anim.slide_in)
    this.startAnimation(animation)
}


fun Date.formatDate(context: Context): String{
    val myFormat = context.getString(R.string.date_format)
    val sdf = SimpleDateFormat(myFormat, Locale.getDefault())
    return sdf.format(this.time)
}

fun String.removeZeroDecimal():String {
    return if(this.endsWith(".0") or this.endsWith(".00" ))
        this.split(".")[0]
    else
        this
}

fun Float.toTwoDecimalPlace(): String{
    return String.format(Locale.US, "%.2f", this).replace(",", ".").removeZeroDecimal()
}
fun PopupMenu.enableIcons(){
    try {
        val fMenuHelper = PopupMenu::class.java.getDeclaredField("mPopup")
        val menuHelper: Any
        val argTypes: Class<*>

        fMenuHelper.isAccessible = true
        menuHelper = fMenuHelper.get(this)!!
        argTypes = Boolean::class.javaPrimitiveType!!
        menuHelper.javaClass.getDeclaredMethod("setForceShowIcon", argTypes).invoke(menuHelper, true)
    }catch (e:Exception){}
}