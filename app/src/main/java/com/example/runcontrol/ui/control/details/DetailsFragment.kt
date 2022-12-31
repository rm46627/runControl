package com.example.runcontrol.ui.control.details

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.runcontrol.R
import com.example.runcontrol.databinding.FragmentDetailsBinding
import com.example.runcontrol.ui.maps.MapsUtil.drawPolyline
import com.example.runcontrol.ui.maps.MapsUtil.showBiggerPicture
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.patrykandpatryk.vico.core.axis.formatter.PercentageFormatAxisValueFormatter
import com.patrykandpatryk.vico.core.axis.vertical.VerticalAxis
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

//    TODO: sliding left right for other runs
//    TODO: on map click transition to new fragment allowing to view route with more details like zooming in and out

@AndroidEntryPoint
class DetailsFragment : Fragment(), OnMapReadyCallback {

    private val args: DetailsFragmentArgs by navArgs()

    private var _binding: FragmentDetailsBinding? = null
    private val binding get() = _binding!!
    private lateinit var map: GoogleMap
    private lateinit var detailsViewModel: DetailsViewModel

    private var locationList = mutableListOf<LatLng>()
    private var polylineList = mutableListOf<Polyline>()
    private var markerList = mutableListOf<Marker>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        detailsViewModel = ViewModelProvider(requireActivity())[DetailsViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDetailsBinding.inflate(inflater, container, false)
        locationList = args.run.locations as MutableList<LatLng>
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.details_map) as SupportMapFragment?
        mapFragment?.getMapAsync(this)

        binding.dateValue.text = args.run.dateToFullDateTime()

        binding.chartView.entryProducer = detailsViewModel.chartEntryModelProducer
//        binding.chartView.marker = marker
//        binding.chartView.chart?.addDecoration(decoration = thresholdLine)
        with(binding.chartView.startAxis as VerticalAxis) {
            this.maxLabelCount = 5
            this.valueFormatter = PercentageFormatAxisValueFormatter()
        }
        detailsViewModel.setChartData(args.run.paceTimes)

        binding.deleteBtn.setOnClickListener {
            lifecycleScope.launch {
                detailsViewModel.removeFromDb(args.run)
            }
            findNavController().navigateUp()
        }

    }

    @SuppressLint("MissingPermission")
    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        map.isMyLocationEnabled = false
        map.setOnMarkerClickListener { true }
        map.uiSettings.apply {
            isZoomControlsEnabled = false
            isZoomGesturesEnabled = false
            isRotateGesturesEnabled = false
            isTiltGesturesEnabled = false
            isCompassEnabled = false
            isScrollGesturesEnabled = false
            isMyLocationButtonEnabled = false
        }
        showRun()
    }

    private fun showRun() {
        showBiggerPicture(false, locationList, markerList, resources, map)
        drawPolyline(locationList, polylineList, map)
    }

}