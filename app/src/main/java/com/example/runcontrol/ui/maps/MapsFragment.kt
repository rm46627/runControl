package com.example.runcontrol.ui.maps

import android.annotation.SuppressLint
import android.content.Intent
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
import com.example.runcontrol.R
import com.example.runcontrol.database.entities.RunEntity
import com.example.runcontrol.databinding.FragmentMapsBinding
import com.example.runcontrol.extensions.NavController.safeNavigate
import com.example.runcontrol.extensions.View.disable
import com.example.runcontrol.extensions.View.enable
import com.example.runcontrol.extensions.View.hide
import com.example.runcontrol.extensions.View.hideFadeOut
import com.example.runcontrol.extensions.View.show
import com.example.runcontrol.extensions.View.showFadeIn
import com.example.runcontrol.service.TrackerService
import com.example.runcontrol.ui.MainViewModel
import com.example.runcontrol.ui.maps.MapsUtil.formatAvgPace
import com.example.runcontrol.ui.maps.MapsUtil.formatDistance
import com.example.runcontrol.ui.maps.MapsUtil.getTimerStringFromTime
import com.example.runcontrol.ui.maps.MapsUtil.setCameraOnCurrentLocation
import com.example.runcontrol.ui.maps.MapsUtil.showBiggerPicture
import com.example.runcontrol.utils.Constants.TRACKER_SERVICE_START
import com.example.runcontrol.utils.Constants.TRACKER_SERVICE_STOP
import com.example.runcontrol.utils.Permissions.hasBackgroundLocationPermission
import com.example.runcontrol.utils.Permissions.requestBackgroundLocationPermission
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
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

    private var _binding: FragmentMapsBinding? = null
    private val binding get() = _binding!!
    private lateinit var map: GoogleMap
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    private var locationList = mutableListOf<LatLng>()
    private var polylineList = mutableListOf<Polyline>()
    private var markerList = mutableListOf<Marker>()

    private lateinit var mapsViewModel: MapsViewModel
    private lateinit var mainViewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mapsViewModel = ViewModelProvider(requireActivity())[MapsViewModel::class.java]
        mainViewModel = ViewModelProvider(requireActivity())[MainViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMapsBinding.inflate(inflater, container, false)

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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
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
        if (mapsViewModel.currentRunState == RunStatus.ENDED) {
            binding.hintTextView.hide()
            binding.resetBtn.show()
            binding.resultBtn.show()
            showBiggerPicture(false, locationList, markerList, resources, map)
        }
        else if (mapsViewModel.currentRunState == RunStatus.READY) {
            binding.hintTextView.hide()
            binding.startBtn.show()
            setCameraOnCurrentLocation(1, fusedLocationProviderClient, map)
        }
        else if (mapsViewModel.currentRunState == RunStatus.STARTED){
            binding.hintTextView.hide()
            setCameraOnCurrentLocation(1, fusedLocationProviderClient, map)
        }
    }

    private fun observeTrackerService() {
        TrackerService.locationList.observe(viewLifecycleOwner) {
            if (it != null) {
                locationList = it
                if (locationList.size == 1) {
                    binding.stopBtn.enable()
                }
                MapsUtil.drawPolyline(locationList, polylineList, map)
                MapsUtil.followPosition(locationList, map)
            }
        }
        TrackerService.runTime.observe(viewLifecycleOwner) {
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

    override fun onMyLocationButtonClick(): Boolean {
        if(mapsViewModel.currentRunState == RunStatus.CLEAN){
            mainViewModel.canChangeFragment.postValue(false)
            binding.hintTextView.hideFadeOut()
            lifecycleScope.launch {
                delay(1500)
                binding.startBtn.showFadeIn()
                delay(1000)
                mapsViewModel.ready()
                mainViewModel.canChangeFragment.postValue(true)
            }
        }
        return false
    }

    @SuppressLint("MissingPermission")
    private fun onStartButtonClicked() {
        mainViewModel.canChangeFragment.postValue(false)
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
                binding.stopBtn.enable()
                mainViewModel.canChangeFragment.postValue(true)
            }
        }
        timer.start()
    }

    @SuppressLint("MissingPermission")
    private fun onStopButtonClicked() {
        mainViewModel.canChangeFragment.postValue(false)
        stopForegroundService()
        binding.stopBtn.hide()
        showBiggerPicture(true, locationList, markerList, resources, map)
        displayResults(true)
        mapsViewModel.ended()
    }

    private fun onResetButtonClicked() {
        mainViewModel.canChangeFragment.postValue(false)
        mapReset()
        lifecycleScope.launch {
            binding.resetBtn.hideFadeOut()
            binding.resultBtn.hideFadeOut()
            delay(1000)
            binding.startBtn.showFadeIn()
            mapsViewModel.ready()
            mainViewModel.canChangeFragment.postValue(true)
        }
    }

    private fun mapReset() {
        setCameraOnCurrentLocation(2000, fusedLocationProviderClient, map)
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
            TrackerService.runTime.value!!,
            TrackerService.distanceMeters.value!!,
            TrackerService.avgPaceTime.value!!,
            TrackerService.paceTimes.value!!,
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
            binding.resetBtn.showFadeIn()
            binding.resultBtn.showFadeIn()
            mainViewModel.canChangeFragment.postValue(true)
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

//    override fun onCreateAnimation(transit: Int, enter: Boolean, nextAnim: Int): Animation? {
//        var animation = super.onCreateAnimation(transit, enter, nextAnim);
//        if (animation == null && nextAnim != 0) {
//            animation = AnimationUtils.loadAnimation(activity, nextAnim);
//        }
//        if (animation != null) {
//            view?.setLayerType(View.LAYER_TYPE_HARDWARE, null);
//            animation.setAnimationListener(object : Animation.AnimationListener {
//                override fun onAnimationStart(p0: Animation?) { }
//                override fun onAnimationEnd(p0: Animation?) {
//                    view?.setLayerType(View.LAYER_TYPE_NONE, null)
//                }
//                override fun onAnimationRepeat(p0: Animation?) {}
//            })
//        }
//        return animation
//    }

}