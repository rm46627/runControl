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
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.MutableLiveData
import com.example.runcontrol.ui.maps.MapsUtil
import com.example.runcontrol.ui.maps.MapsUtil.formatDistance
import com.example.runcontrol.ui.maps.MapsUtil.getTimerStringFromTime
import com.example.runcontrol.utils.Constants.LOCATION_FASTEST_UPDATE_INTERVAL
import com.example.runcontrol.utils.Constants.LOCATION_UPDATE_INTERVAL
import com.example.runcontrol.utils.Constants.NOTIFICATION_CHANNEL_ID
import com.example.runcontrol.utils.Constants.NOTIFICATION_CHANNEL_NAME
import com.example.runcontrol.utils.Constants.NOTIFICATION_ID
import com.example.runcontrol.utils.Constants.TRACKER_SERVICE_START
import com.example.runcontrol.utils.Constants.TRACKER_SERVICE_STOP
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.AndroidEntryPoint
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
        val distanceMeters = MutableLiveData<Double>()
        val runTime = MutableLiveData<Int>()
        val date = MutableLiveData<Long>()
        val locationList = MutableLiveData<MutableList<LatLng>>()
        val kilometerReached = MutableLiveData<Boolean>()
        val paceTimes = MutableLiveData<MutableList<Int>>()
        val avgPaceTime = MutableLiveData<Double>()
        val burnedKcal = MutableLiveData<Int>()

        fun timerReset() {
            runTime.postValue(0)
        }
    }

    private fun setInitialValues() {
        started.postValue(false)
        locationList.postValue(mutableListOf())
        distanceMeters.postValue((0.0))
        runTime.postValue((0))
        kilometerReached.postValue(false)
        paceTimes.postValue(mutableListOf())
        avgPaceTime.postValue(0.0)
    }

    private val locationCallback = object : LocationCallback(){
        override fun onLocationResult(result: LocationResult) {
            super.onLocationResult(result)
            result.locations.let { locations ->
                for(location in locations){
                    updateLocationList(location)
                    updateDistance()
                    updateNotificationPeriodically()
                    updateKcal()
                    Log.d("dist", "$distanceMeters")
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
                    runTime.value?.apply{
                        val tt = runTime.value!! + 1
                        runTime.postValue(tt)
                    }
                }
            },
            0,
            1000
        )
    }

    private fun setDate() {
        date.postValue(System.currentTimeMillis())
    }

    private fun updateDistance() {
        distanceMeters.postValue(locationList.value?.let {
            MapsUtil.calculateDistance(it, distanceMeters.value!!)
        })
        val meters = (distanceMeters.value!! % 1000).toInt()
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
                val t = runTime.value!!
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
                add(runTime.value!! - tsum)
                paceTimes.postValue(this)
                avgPaceTime.postValue((tsum.toDouble() + this.last().toDouble())/this.size)
            }
        }
    }

    private fun updateKcal() {
        burnedKcal.postValue((distanceMeters.value!! * 0.001 * 62 * 1.036).toInt())
    }

    private fun updateNotificationPeriodically() {
        notification.apply {
            setContentTitle("Your running session")
            setContentText(formatDistance(distanceMeters.value!!) + "\n" + getTimerStringFromTime(runTime.value!!))
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
