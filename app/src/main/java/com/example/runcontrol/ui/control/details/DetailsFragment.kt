package com.example.runcontrol.ui.control.details

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.navArgs
import com.example.runcontrol.R
import com.example.runcontrol.databinding.FragmentDetailsBinding
import com.example.runcontrol.ui.maps.MapsUtil.fromVectorToBitmap
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.patrykandpatryk.vico.core.axis.formatter.PercentageFormatAxisValueFormatter
import com.patrykandpatryk.vico.core.axis.vertical.VerticalAxis
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DetailsFragment : Fragment(), OnMapReadyCallback {

    private val args: DetailsFragmentArgs by navArgs()

    private lateinit var binding: FragmentDetailsBinding
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
        binding = FragmentDetailsBinding.inflate(inflater, container, false)
        locationList = args.run.locations as MutableList<LatLng>
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.details_map) as SupportMapFragment?
        mapFragment?.getMapAsync(this)

        binding.dateValue.text = args.run.date

        binding.chartView.entryProducer = detailsViewModel.chartEntryModelProducer
//        binding.chartView.marker = marker
//        binding.chartView.chart?.addDecoration(decoration = thresholdLine)
        with(binding.chartView.startAxis as VerticalAxis) {
            this.maxLabelCount = 5
            this.valueFormatter = PercentageFormatAxisValueFormatter()
        }
        detailsViewModel.setChartData(args.run.paceTimes)

    }

    override fun onStart() {
        super.onStart()
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

    private fun drawPolylines() {
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

    private fun showRun() {
        val bounds = LatLngBounds.Builder()
        for (location in locationList) {
            bounds.include(location)
        }
        map.animateCamera(
            CameraUpdateFactory.newLatLngBounds(bounds.build(), 100),
            1,
            null
        )
        drawPolylines()
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

}