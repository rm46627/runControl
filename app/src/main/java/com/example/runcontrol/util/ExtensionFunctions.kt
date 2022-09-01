package com.example.runcontrol.util

import android.view.View

object ExtensionFunctions {

    fun View.show(){
        this.visibility = View.VISIBLE
    }

    fun View.hide(){
        this.visibility = View.INVISIBLE
    }

    fun View.enable(){
        this.isEnabled = true
    }

    fun View.disable(){
        this.isEnabled = false
    }



}