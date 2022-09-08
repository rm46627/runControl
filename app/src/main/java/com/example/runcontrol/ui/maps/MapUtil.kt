package com.example.runcontrol.ui.maps

import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.SphericalUtil
import java.text.DecimalFormat

object MapUtil {

    fun setCameraPosition(location: LatLng): CameraPosition {
        return CameraPosition.Builder()
            .target(location)
            .zoom(18f)
            .build()
    }

    fun calculateElapsedTime(startTime: Long, stopTime: Long): String {
        val elapsedTime = startTime - stopTime
        val seconds = (elapsedTime / 1000).toInt() % 60
        val minutes = (elapsedTime / (1000*60) % 60)
        val hours = (elapsedTime / (1000*60*60) % 24)
        return "$hours:$minutes:$seconds"
    }

    fun calculateDistance(locationList: MutableList<LatLng>): String {
        if(locationList.size > 1) {
            var meters = 0.0
            for (i in 1..locationList.size) {
                meters += SphericalUtil.computeDistanceBetween(locationList[i-1], locationList[i])
            }
            if(meters > 999){
                val kilometers = meters/1000
                val strKm = DecimalFormat("#.##").format(kilometers)
                return "$strKm km"
            }
            return "$meters m"
        }
        return "0.00 m"
    }
}