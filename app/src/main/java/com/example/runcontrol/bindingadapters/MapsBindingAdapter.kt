package com.example.runcontrol.bindingadapters

import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.example.runcontrol.service.TrackerService.Companion.started
import com.example.runcontrol.ui.maps.MapsViewModel

//@BindingAdapter("observeTracking")
//fun observeTracking(view: View, status: MapsViewModel.RunStatus){
//    if (started && view is Button) {
//        view.visibility = View.VISIBLE
//    } else if (started && view is TextView) {
//        view.visibility = View.INVISIBLE
//
//    }
//}