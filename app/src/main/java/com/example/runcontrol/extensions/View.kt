package com.example.runcontrol.extensions

import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import com.example.runcontrol.R


object View {

    fun View.show(){
        this.visibility = View.VISIBLE
    }

    fun View.showFadeIn() {
        this.alpha = 0f
        this.show()
        this.animate().alpha(1f).duration = 1000
    }

    fun View.showSlideUp() {
        val anim: Animation = AnimationUtils.loadAnimation(this.context, R.anim.slide_to_up)
        this.startAnimation(anim)
        this.show()
    }

    fun View.hide(){
        this.visibility = View.INVISIBLE
    }

    fun View.hideFadeOut() {
        val anim: Animation = AlphaAnimation(1f, 0f).apply {
            interpolator = AccelerateInterpolator()
            duration = 1000
        }
        this.startAnimation(anim)
        this.hide()
    }

    fun View.gone(){
        this.visibility = View.GONE
    }

    fun View.goneSlideDown(){
        val anim: Animation = AnimationUtils.loadAnimation(this.context, R.anim.slide_to_down)
        this.startAnimation(anim)
        this.visibility = View.GONE
    }

    fun View.enable(){
        this.isEnabled = true
    }

    fun View.disable(){
        this.isEnabled = false
    }



}