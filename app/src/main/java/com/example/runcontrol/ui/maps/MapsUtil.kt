package com.example.runcontrol.ui.maps

import android.annotation.SuppressLint
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.util.Log
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.DrawableCompat
import com.example.runcontrol.R
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.*
import com.google.maps.android.SphericalUtil
import java.text.DecimalFormat

object MapsUtil {

    private fun setCameraPosition(location: LatLng): CameraPosition {
        return CameraPosition.Builder()
            .target(location)
            .zoom(18f)
            .build()
    }

    fun getTimerStringFromTime(time: Int): String {
        val hours = time / (60 * 60)
        val minutes = (time / 60) % 60
        val seconds = time % 60
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

    fun formatAvgPace(pace: Double): String {
        val minutes = ((pace / 60) % 60).toInt()
        val seconds = (pace % 60).toInt()
        return String.format("%02d:%02d", minutes, seconds)
    }

    private fun fromVectorToBitmap(resources: Resources, id: Int, color: Int): BitmapDescriptor {
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

    fun drawPolyline(
        locationList: MutableList<LatLng>,
        polylineList: MutableList<Polyline>,
        map: GoogleMap
    ) {
        val polyline = map.addPolyline(
            PolylineOptions().apply {
                width(10f)
                jointType(JointType.ROUND)
                startCap(ButtCap())
                endCap(ButtCap())
                addAll(locationList)
            }
        )
        polylineList.add(polyline)
    }

    fun followPosition(locationList: MutableList<LatLng>, map: GoogleMap) {
        if (locationList.isNotEmpty()) {
            map.animateCamera(
                CameraUpdateFactory.newCameraPosition(setCameraPosition(locationList.last())),
                1000,
                null
            )
        }
    }

    fun showBiggerPicture(
        animate: Boolean,
        locationList: MutableList<LatLng>,
        markerList: MutableList<Marker>,
        resources: Resources,
        map: GoogleMap
    ) {
        val bounds = LatLngBounds.Builder()
        for (location in locationList) {
            bounds.include(location)
        }
        val duration = if (animate) 2000 else 1
        map.animateCamera(
            CameraUpdateFactory.newLatLngBounds(bounds.build(), 100),
            duration,
            null
        )
        addMarker(locationList.first(), R.drawable.ic_start, markerList, map, resources)
        addMarker(locationList.last(), R.drawable.ic_finish, markerList, map, resources)
    }

    private fun addMarker(
        position: LatLng,
        drawable: Int,
        markerList: MutableList<Marker>,
        map: GoogleMap,
        resources: Resources
    ) {
        val marker = map.addMarker(
            MarkerOptions()
                .position(position)
                .zIndex(1f)
                .icon(
                    fromVectorToBitmap(
                        resources,
                        drawable,
                        Color.parseColor("#000000")
                    )
                )
        )
        markerList.add(marker!!)
    }

    @SuppressLint("MissingPermission")
    fun setCameraOnCurrentLocation(
        duration: Int,
        fusedLocationProviderClient: FusedLocationProviderClient,
        map: GoogleMap
    ) {
        fusedLocationProviderClient.lastLocation.addOnCompleteListener {
            val lastKnownLocation = LatLng(
                it.result.latitude,
                it.result.longitude
            )
            map.animateCamera(
                CameraUpdateFactory.newCameraPosition(
                    setCameraPosition(lastKnownLocation)
                ),
                duration,
                null
            )
        }
    }
}