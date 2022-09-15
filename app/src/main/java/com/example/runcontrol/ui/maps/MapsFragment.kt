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
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.runcontrol.R
import com.example.runcontrol.databinding.FragmentMapsBinding
import com.example.runcontrol.model.Result
import com.example.runcontrol.service.TrackerService
import com.example.runcontrol.ui.maps.MapsUtil.formatDistance
import com.example.runcontrol.ui.maps.MapsUtil.fromVectorToBitmap
import com.example.runcontrol.ui.maps.MapsUtil.getTimerStringFromTime
import com.example.runcontrol.ui.maps.MapsUtil.setCameraPosition
import com.example.runcontrol.util.Constants.TRACKER_SERVICE_START
import com.example.runcontrol.util.Constants.TRACKER_SERVICE_STOP
import com.example.runcontrol.util.ExtensionFunctions.disable
import com.example.runcontrol.util.ExtensionFunctions.enable
import com.example.runcontrol.util.ExtensionFunctions.hide
import com.example.runcontrol.util.ExtensionFunctions.show
import com.example.runcontrol.util.Permissions.hasBackgroundLocationPermission
import com.example.runcontrol.util.Permissions.requestBackgroundLocationPermission
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.vmadalin.easypermissions.EasyPermissions
import com.vmadalin.easypermissions.dialogs.SettingsDialog
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

//@AndroidEntryPoint
class MapsFragment : Fragment(), OnMapReadyCallback, GoogleMap.OnMyLocationButtonClickListener,
    GoogleMap.OnMarkerClickListener,
    EasyPermissions.PermissionCallbacks {

    private lateinit var binding: FragmentMapsBinding
    private lateinit var map: GoogleMap
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var mapsViewModel: MapsViewModel

    private var locationList = mutableListOf<LatLng>()
    private var polylineList = mutableListOf<Polyline>()
    private var markerList = mutableListOf<Marker>()
    private var time = 0.0
    val started = MutableLiveData(false)

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
        binding.tracking = this

        binding.startBtn.setOnClickListener {
            onStartButtonClicked()
        }
        binding.stopBtn.setOnClickListener {
            onStopButtonClicked()
        }
        binding.resetBtn.setOnClickListener {
            onResetButtonClicked()
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
            time = it
            binding.timerValueTextView.text = getTimerStringFromTime(time)
        }
        TrackerService.started.observe(viewLifecycleOwner) {
            started.value = it
        }
        TrackerService.distance.observe(viewLifecycleOwner) {
            mapsViewModel.distance = it
            mapsViewModel.currentKilometerDistance = it % 1000
            binding.distanceValueTextView.text = formatDistance(mapsViewModel.distance)
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
            map.isMyLocationEnabled = false
        } else {
            requestBackgroundLocationPermission(this)
        }
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
        map.isMyLocationEnabled = true
        showBiggerPicture()
        displayResults()
    }

    private fun showBiggerPicture() {
        val bounds = LatLngBounds.Builder()
        for (location in locationList) {
            bounds.include(location)
        }
        map.animateCamera(
            CameraUpdateFactory.newLatLngBounds(bounds.build(), 100),
            2000,
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
        binding.startBtn.show()
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

    private fun displayResults() {
        val result = Result(
            formatDistance(mapsViewModel.distance),
            getTimerStringFromTime(time),
            TrackerService.date.value!!
        )
        lifecycleScope.launch {
            delay(2500L)
            val directions = MapsFragmentDirections.actionMapsFragmentToResultFragment(result)
            findNavController().navigate(directions)
            binding.startBtn.apply {
                hide()
                enable()
            }
            binding.stopBtn.hide()
            binding.resetBtn.show()
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