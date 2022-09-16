package com.example.runcontrol.service

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_LOW
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Build
import android.os.Looper
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.MutableLiveData
import com.example.runcontrol.ui.maps.MapsUtil
import com.example.runcontrol.ui.maps.MapsUtil.formatDistance
import com.example.runcontrol.ui.maps.MapsUtil.getTimerStringFromTime
import com.example.runcontrol.util.Constants.LOCATION_FASTEST_UPDATE_INTERVAL
import com.example.runcontrol.util.Constants.LOCATION_UPDATE_INTERVAL
import com.example.runcontrol.util.Constants.NOTIFICATION_CHANNEL_ID
import com.example.runcontrol.util.Constants.NOTIFICATION_CHANNEL_NAME
import com.example.runcontrol.util.Constants.NOTIFICATION_ID
import com.example.runcontrol.util.Constants.TRACKER_SERVICE_START
import com.example.runcontrol.util.Constants.TRACKER_SERVICE_STOP
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.AndroidEntryPoint
import java.text.DateFormat.getDateTimeInstance
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class TrackerService: LifecycleService() {

    @Inject
    lateinit var notification: NotificationCompat.Builder
    @Inject
    lateinit var notificationManager: NotificationManager

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private val timer = Timer()

    private var currentMeters = 0

    companion object {
        val started = MutableLiveData<Boolean>()
        val distance = MutableLiveData<Double>()
        val time = MutableLiveData<Int>()
        val date = MutableLiveData<String>()
        val locationList = MutableLiveData<MutableList<LatLng>>()
        val kilometerReached = MutableLiveData<Boolean>()
        val paceTimes = MutableLiveData<MutableList<Int>>()
        val avgPaceTime = MutableLiveData<Double>()
    }

    private fun setInitialValues() {
        started.postValue(false)
        locationList.postValue(mutableListOf())
        distance.postValue((0.0))
        time.postValue((0))
        kilometerReached.postValue(false)
        paceTimes.postValue(mutableListOf())
    }

    private val locationCallback = object : LocationCallback(){
        override fun onLocationResult(result: LocationResult) {
            super.onLocationResult(result)
            result.locations.let { locations ->
                for(location in locations){
                    updateLocationList(location)
                    updateDistance()
                    updateNotificationPeriodically()
                }
            }
        }

        private fun updateLocationList(location: Location){
            val newLatLng = LatLng(location.latitude, location.longitude)
            locationList.value?.apply{
                add(newLatLng)
                locationList.postValue(this )
            }
        }

    }

    override fun onCreate() {
        setInitialValues()
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        super.onCreate()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let{
            when(it.action){
                TRACKER_SERVICE_START -> {
                    started.postValue(true)
                    startForegroundService()
                }
                TRACKER_SERVICE_STOP -> {
                    started.postValue(false)
                    stopForegroundService()
                }
                else -> {}
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun startForegroundService() {
        startLocationUpdates()
        startTimer()
        setDate()
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, notification.build())
    }

    private fun stopForegroundService() {
        removeLocationUpdates()
        timer.cancel()
        (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).cancel(
            NOTIFICATION_ID
        )
        stopForeground(true)
        stopSelf()
    }

    private fun removeLocationUpdates() {
        fusedLocationProviderClient.removeLocationUpdates(locationCallback)
    }

    @SuppressLint("MissingPermission")
    private fun startLocationUpdates(){
        val locationRequest = LocationRequest.create().apply{
            interval = LOCATION_UPDATE_INTERVAL
            fastestInterval = LOCATION_FASTEST_UPDATE_INTERVAL
            priority = Priority.PRIORITY_HIGH_ACCURACY
        }
        fusedLocationProviderClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
    }

    private fun startTimer() {
        timer.scheduleAtFixedRate(
            object : TimerTask() {
                override fun run() {
                    time.value?.apply{
                        val tt = time.value!! + 1
                        time.postValue(tt)
                    }
                }
            },
            0,
            1000
        )
    }

    private fun setDate() {
        val dateTime = Date(System.currentTimeMillis())
        date.postValue(getDateTimeInstance().format(dateTime))
    }

    private fun updateDistance() {
        distance.postValue(locationList.value?.let {
            MapsUtil.calculateDistance(it, distance.value!!)
        })
        val meters = (distance.value!! % 1000).toInt()
        if (meters >= currentMeters){
            currentMeters = meters
        } else {
            currentMeters = 0
            nextKilometer()
        }
    }

    private fun nextKilometer() {
        updateAvgPace()
        kilometerReached.postValue(true)
    }

    private fun updateAvgPace() {
        paceTimes.value?.apply {
            if (this.isEmpty()) {
                val t = time.value!!
                add(t)
                paceTimes.postValue(this)
                avgPaceTime.postValue(t.toDouble())
            } else {
                var tsum = 0
                this.let { times ->
                    for (t in times) {
                        tsum += t
                    }
                }
                add(time.value!! - tsum)
                paceTimes.postValue(this)
                avgPaceTime.postValue((tsum.toDouble() + this.last().toDouble())/this.size)
            }
        }
    }

    private fun updateNotificationPeriodically() {
        notification.apply {
            setContentTitle("Distance\tTime")
//            distance.postValue(locationList.value?.let { calculateDistance(it, distance.value!!) })
            setContentText(formatDistance(distance.value!!) + "\t" + getTimerStringFromTime(time.value!!))
        }
        notificationManager.notify(NOTIFICATION_ID, notification.build())
    }

    private fun createNotificationChannel(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                NOTIFICATION_CHANNEL_NAME,
                IMPORTANCE_LOW
            )
            notificationManager.createNotificationChannel(channel)
        }
    }

}
