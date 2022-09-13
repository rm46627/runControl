package com.example.runcontrol.ui.maps

import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.util.Log
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.DrawableCompat
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.SphericalUtil
import java.text.DecimalFormat
import kotlin.math.roundToInt

object MapUtil {

    fun setCameraPosition(location: LatLng): CameraPosition {
        return CameraPosition.Builder()
            .target(location)
            .zoom(18f)
            .build()
    }

    fun calculateElapsedTime(startTime: Long, stopTime: Long): String {
        val elapsedTime = stopTime - startTime
        val seconds = (elapsedTime / 1000).toInt() % 60
        val minutes = (elapsedTime / (1000 * 60) % 60)
        val hours = (elapsedTime / (1000 * 60 * 60) % 24)
        return String.format("%02d:%02d:%02d", hours, minutes, seconds)
//        return "$hours:$minutes:$seconds"
    }

    fun getTimerStringFromTime(time: Double): String {
        val resultInt = time.roundToInt()
        val hours = resultInt / (60 * 60)
        val minutes = (resultInt / 60) % 60
        val seconds = resultInt % 60
        val out = String.format("%02d:%02d:%02d", hours, minutes, seconds)
        Log.d("Timer", out)
        return out
    }

    fun calculateDistance(locationList: MutableList<LatLng>, distance: Double): Double {
        if (locationList.size > 1) {
            return distance + SphericalUtil.computeDistanceBetween(
                locationList[locationList.size - 2],
                locationList.last()
            )
        }
        return 0.0
    }

    fun formatDistance(distance: Double): String {
        if (distance > 999) {
            return "${DecimalFormat("#.##").format(distance / 1000)} km"
        }
        return "${distance.toInt()} m"
    }

    fun fromVectorToBitmap(resources: Resources, id: Int, color: Int): BitmapDescriptor {
        val vectorDrawable: Drawable = ResourcesCompat.getDrawable(resources, id, null)
            ?: return BitmapDescriptorFactory.defaultMarker()
        val bitmap = Bitmap.createBitmap(
            vectorDrawable.intrinsicWidth,
            vectorDrawable.intrinsicHeight,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        vectorDrawable.setBounds(0, 0, canvas.width, canvas.height)
        DrawableCompat.setTint(vectorDrawable, color)
        vectorDrawable.draw(canvas)
        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }
}