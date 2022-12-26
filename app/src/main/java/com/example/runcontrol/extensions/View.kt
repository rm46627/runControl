package com.example.runcontrol.extensions

import android.view.View
import android.view.animation.AccelerateInterpolator

import android.view.animation.AlphaAnimation
import android.view.animation.Animation


object View {

    fun View.show(){
        this.visibility = View.VISIBLE
    }

    fun View.showFadeIn() {
        this.alpha = 0f
        this.show()
        this.animate().alpha(1f).duration = 1000
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

    fun View.enable(){
        this.isEnabled = true
    }

    fun View.disable(){
        this.isEnabled = false
    }



}