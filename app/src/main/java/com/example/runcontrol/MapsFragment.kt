package com.example.runcontrol

import android.annotation.SuppressLint
import androidx.fragment.app.Fragment

import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.runcontrol.databinding.FragmentMapsBinding
import com.example.runcontrol.util.ExtensionFunctions.disable
import com.example.runcontrol.util.ExtensionFunctions.hide
import com.example.runcontrol.util.ExtensionFunctions.show
import com.example.runcontrol.util.Permissions.hasBackgroundLocationPermission
import com.example.runcontrol.util.Permissions.requestBackgroundLocationPermission

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.vmadalin.easypermissions.EasyPermissions
import com.vmadalin.easypermissions.dialogs.SettingsDialog
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MapsFragment : Fragment(), OnMapReadyCallback, GoogleMap.OnMyLocationButtonClickListener, EasyPermissions.PermissionCallbacks {

    private lateinit var binding: FragmentMapsBinding
    private lateinit var map: GoogleMap

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMapsBinding.inflate(inflater, container, false)

        binding.startBtn.setOnClickListener {
            onStartButtonClicked()
        }
        binding.stopBtn.setOnClickListener {  }
        binding.resetBtn.setOnClickListener {  }

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
        map.uiSettings.apply {
            isZoomControlsEnabled = false
            isZoomGesturesEnabled = false
            isRotateGesturesEnabled = false
            isTiltGesturesEnabled = false
            isCompassEnabled = false
            isScrollGesturesEnabled = false
        }
    }

    private fun onStartButtonClicked() {
        if(hasBackgroundLocationPermission(requireContext())){
            startCountDown()
            binding.startBtn.disable()
            binding.startBtn.hide()
            binding.stopBtn.show()

        } else {
            requestBackgroundLocationPermission(this)
        }
    }

    private fun startCountDown() {
        binding.timerTextView.show()
        binding.stopBtn.disable()
        val timer: CountDownTimer = object: CountDownTimer(4000, 1000) {
            override fun onTick(milisUntilFinished: Long) {
                val currentSecond = milisUntilFinished / 1000
                if(currentSecond.toString() == "0"){
                    binding.timerTextView.text = getString(R.string.go_count_down_timer)
                    binding.timerTextView.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
                } else {
                    binding.timerTextView.text = currentSecond.toString()
                    binding.timerTextView.setTextColor(ContextCompat.getColor(requireContext(), R.color.red))
                }
            }

            override fun onFinish() {
                binding.timerTextView.hide()
            }
        }
        timer.start()
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

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    override fun onPermissionsDenied(requestCode: Int, perms: List<String>) {
        if(EasyPermissions.somePermissionPermanentlyDenied(this, perms)){
            SettingsDialog.Builder(requireActivity()).build().show()
        } else {
            requestBackgroundLocationPermission(this)
        }
    }

    override fun onPermissionsGranted(requestCode: Int, perms: List<String>) {
        onStartButtonClicked()
    }
}