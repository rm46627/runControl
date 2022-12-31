package com.example.runcontrol.ui.control

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.runcontrol.databinding.FragmentControlBinding
import com.example.runcontrol.ui.MainViewModel
import com.example.runcontrol.ui.control.ChartUtil.daysOfLastWeek
import com.example.runcontrol.ui.control.ChartUtil.mapDistancesToLastWeek
import com.patrykandpatryk.vico.core.axis.formatter.AxisValueFormatter
import com.patrykandpatryk.vico.core.axis.horizontal.HorizontalAxis


class ControlFragment : Fragment() {

    private var _binding: FragmentControlBinding? = null
    private val binding get() = _binding!!
    private lateinit var mainViewModel: MainViewModel
    private lateinit var controlViewModel: ControlViewModel
    private val mAdapter: RecentAdapter by lazy { RecentAdapter() }

    private val lastWeek = daysOfLastWeek()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentControlBinding.inflate(layoutInflater, container, false)
        binding.lifecycleOwner = this
        mainViewModel = ViewModelProvider(requireActivity())[MainViewModel::class.java]
        controlViewModel = ViewModelProvider(requireActivity())[ControlViewModel::class.java]

        setupRecyclerView(binding.recentActivitiesRecyclerView)

        observeRuns()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.weekActivityChartView.entryProducer = controlViewModel.chartEntryModelProducer
//        binding.chartView.marker = marker
//        binding.chartView.chart?.addDecoration(decoration = thresholdLine)
        Log.d("lastWeek", lastWeek.toString())
        with(binding.weekActivityChartView.bottomAxis as HorizontalAxis) {
            this.valueFormatter = AxisValueFormatter { x, _ -> lastWeek[x.toInt() % lastWeek.size] }
        }
    }

    private fun observeRuns() {
        mainViewModel.readRuns.observe(viewLifecycleOwner) { runs ->
            mAdapter.setData(runs)
            val distList = mapDistancesToLastWeek(runs, lastWeek)
            Log.d("distList", distList.toString())
            controlViewModel.setChartData(distList)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupRecyclerView(recyclerView: RecyclerView) {
        recyclerView.adapter = mAdapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
    }
}