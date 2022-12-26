package com.example.runcontrol.extensions

import android.view.View

object View {

    fun View.show(){
        this.visibility = View.VISIBLE
    }

    fun View.hide(){
        this.visibility = View.INVISIBLE
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