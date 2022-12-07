package com.example.runcontrol.extensionFunctions

import android.view.View

object View {

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