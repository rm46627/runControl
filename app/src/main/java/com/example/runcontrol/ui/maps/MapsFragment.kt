package com.example.runcontrol.ui.maps

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.runcontrol.Constants.TRACKER_SERVICE_START
import com.example.runcontrol.Constants.TRACKER_SERVICE_STOP
import com.example.runcontrol.Permissions.hasBackgroundLocationPermission
import com.example.runcontrol.Permissions.requestBackgroundLocationPermission
import com.example.runcontrol.R
import com.example.runcontrol.database.entities.RunEntity
import com.example.runcontrol.databinding.FragmentMapsBinding
import com.example.runcontrol.extensionFunctions.NavController.safeNavigate
import com.example.runcontrol.extensionFunctions.View.disable
import com.example.runcontrol.extensionFunctions.View.enable
import com.example.runcontrol.extensionFunctions.View.hide
import com.example.runcontrol.extensionFunctions.View.show
import com.example.runcontrol.service.TrackerService
import com.example.runcontrol.ui.maps.MapsUtil.formatAvgPace
import com.example.runcontrol.ui.maps.MapsUtil.formatDistance
import com.example.runcontrol.ui.maps.MapsUtil.fromVectorToBitmap
import com.example.runcontrol.ui.maps.MapsUtil.getTimerStringFromTime
import com.example.runcontrol.ui.maps.MapsUtil.setCameraPosition
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.vmadalin.easypermissions.EasyPermissions
import com.vmadalin.easypermissions.dialogs.SettingsDialog
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MapsFragment : Fragment(), OnMapReadyCallback, GoogleMap.OnMyLocationButtonClickListener,
    GoogleMap.OnMarkerClickListener,
    EasyPermissions.PermissionCallbacks {

    private lateinit var binding: FragmentMapsBinding
    private lateinit var map: GoogleMap
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    private var locationList = mutableListOf<LatLng>()
    private var polylineList = mutableListOf<Polyline>()
    private var markerList = mutableListOf<Marker>()

    private lateinit var mapsViewModel: MapsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mapsViewModel = ViewModelProvider(requireActivity())[MapsViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMapsBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this

        binding.startBtn.setOnClickListener {
            onStartButtonClicked()
        }
        binding.stopBtn.setOnClickListener {
            onStopButtonClicked()
        }
        binding.resetBtn.setOnClickListener {
            onResetButtonClicked()
        }
        binding.resultBtn.setOnClickListener {
            displayResults(false)
        }

        fusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(requireActivity())

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(this)
    }

    override fun onStart() {
        super.onStart()
    }

    @SuppressLint("MissingPermission")
    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        map.isMyLocationEnabled = true
        map.setOnMyLocationButtonClickListener(this)
        map.setOnMarkerClickListener(this)
        map.uiSettings.apply {
            isZoomControlsEnabled = false
            isZoomGesturesEnabled = false
            isRotateGesturesEnabled = false
            isTiltGesturesEnabled = false
            isCompassEnabled = false
            isScrollGesturesEnabled = false
        }
        observeTrackerService()
        if (mapsViewModel.currentRunState == RunStatus.ENDED) {
            binding.hintTextView.hide()
            binding.resetBtn.show()
            binding.resultBtn.show()
            showBiggerPicture(false)
        }
    }

    private fun observeTrackerService() {
        TrackerService.locationList.observe(viewLifecycleOwner) {
            if (it != null) {
                locationList = it
                if (locationList.size == 1) {
                    binding.stopBtn.enable()
                }
                drawPolyline()
                followPosition()
            }
        }
        TrackerService.time.observe(viewLifecycleOwner) {
            binding.timerValueTextView.text = getTimerStringFromTime(it)
        }
        TrackerService.started.observe(viewLifecycleOwner) {
            if(it) binding.stopBtn.show()
            else binding.stopBtn.hide()
        }
        TrackerService.distanceMeters.observe(viewLifecycleOwner) {
            binding.distanceValueTextView.text = formatDistance(it)
        }
        TrackerService.avgPaceTime.observe(viewLifecycleOwner) {
            binding.paceValueTextView.text = formatAvgPace(it)
        }
        TrackerService.burnedKcal.observe(viewLifecycleOwner) {
            binding.caloriesValueTextView.text = it.toString()
        }
    }

    private fun drawPolyline() {
        val polyline = map.addPolyline(
            PolylineOptions().apply {
                width(10f)
                color(Color.BLUE)
                jointType(JointType.ROUND)
                startCap(ButtCap())
                endCap(ButtCap())
                addAll(locationList)
            }
        )
        polylineList.add(polyline)
    }

    private fun followPosition() {
        if (locationList.isNotEmpty()) {
            map.animateCamera(
                CameraUpdateFactory.newCameraPosition(setCameraPosition(locationList.last())),
                1000,
                null
            )
        }
    }

    override fun onMyLocationButtonClick(): Boolean {
        binding.hintTextView.animate().alpha(0f).duration = 1500
        lifecycleScope.launch {
            delay(2500)
            binding.hintTextView.hide()
            binding.startBtn.show()
        }
        return false
    }

    @SuppressLint("MissingPermission")
    private fun onStartButtonClicked() {
        if (hasBackgroundLocationPermission(requireContext())) {
            startCountDown()
            binding.startBtn.disable()
            binding.startBtn.hide()
            binding.stopBtn.show()
        } else {
            requestBackgroundLocationPermission(this)
        }
        mapsViewModel.started()
    }

    private fun startCountDown() {
        binding.timerTextView.show()
        binding.stopBtn.disable()
        val timer: CountDownTimer = object : CountDownTimer(4000, 1000) {
            override fun onTick(milisUntilFinished: Long) {
                val currentSecond = milisUntilFinished / 1000
                if (currentSecond.toString() == "0") {
                    binding.timerTextView.text = getString(R.string.go_count_down_timer)
                    binding.timerTextView.setTextColor(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.black
                        )
                    )
                } else {
                    binding.timerTextView.text = currentSecond.toString()
                    binding.timerTextView.setTextColor(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.red
                        )
                    )
                }
            }

            override fun onFinish() {
                sendActionCommandToService(TRACKER_SERVICE_START)
                binding.timerTextView.hide()
            }
        }
        timer.start()
    }

    @SuppressLint("MissingPermission")
    private fun onStopButtonClicked() {
        stopForegroundService()
        binding.stopBtn.hide()
        binding.startBtn.show()
        showBiggerPicture(true)
        displayResults(true)
        mapsViewModel.ended()
    }

    private fun showBiggerPicture(animate: Boolean) {
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
        addMarker(locationList.first(), R.drawable.ic_start)
        addMarker(locationList.last(), R.drawable.ic_finish)
    }

    private fun addMarker(position: LatLng, drawable: Int) {
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

    private fun onResetButtonClicked() {
        mapReset()
        binding.resetBtn.hide()
        binding.resultBtn.hide()
        binding.startBtn.show()
        mapsViewModel.clean()
    }

    @SuppressLint("MissingPermission")
    private fun mapReset() {
        fusedLocationProviderClient.lastLocation.addOnCompleteListener {
            val lastKnownLocation = LatLng(
                it.result.latitude,
                it.result.longitude
            )
            map.animateCamera(
                CameraUpdateFactory.newCameraPosition(
                    setCameraPosition(lastKnownLocation)
                )
            )
        }
        for (polyline in polylineList) {
            polyline.remove()
        }
        for (marker in markerList) {
            marker.remove()
        }
        locationList.clear()
        markerList.clear()
        TrackerService.timerReset()
        binding.distanceValueTextView.text = "0 m"
        binding.paceValueTextView.text = getString(R.string.pace_zero_text_value)
        binding.timerValueTextView.text = getString(R.string.timer_zero_text_value)
        binding.caloriesValueTextView.text = "0"
    }

    private fun stopForegroundService() {
        binding.startBtn.disable()
        sendActionCommandToService(TRACKER_SERVICE_STOP)
    }

    private fun sendActionCommandToService(action: String) {
        Intent(
            requireContext(),
            TrackerService::class.java
        ).apply {
            this.action = action
            requireContext().startService(this)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    private fun displayResults(delay: Boolean) {
        val runEntity = RunEntity(
            0,
            TrackerService.date.value!!,
            TrackerService.time.value!!,
            TrackerService.distanceMeters.value!!,
            TrackerService.avgPaceTime.value!!,
            TrackerService.burnedKcal.value!!,
            locationList
        )
        lifecycleScope.launch {
            delay( if (delay) 2500L else 0L )
            val directions = MapsFragmentDirections.actionMapsFragmentToResultFragment(runEntity)
            findNavController().safeNavigate(directions)
            binding.startBtn.apply {
                hide()
                enable()
            }
            binding.stopBtn.hide()
            binding.resetBtn.show()
            binding.resultBtn.show()
        }
    }

    override fun onPermissionsDenied(requestCode: Int, perms: List<String>) {
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            SettingsDialog.Builder(requireActivity()).build().show()
        } else {
            requestBackgroundLocationPermission(this)
        }
    }

    override fun onPermissionsGranted(requestCode: Int, perms: List<String>) {
        onStartButtonClicked()
    }

    override fun onMarkerClick(p0: Marker): Boolean {
        return true
    }
}