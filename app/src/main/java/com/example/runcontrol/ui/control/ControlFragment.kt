package com.example.runcontrol.ui.control

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.runcontrol.databinding.FragmentControlBinding
import com.example.runcontrol.ui.MainViewModel
import com.patrykandpatryk.vico.core.axis.formatter.PercentageFormatAxisValueFormatter
import com.patrykandpatryk.vico.core.axis.vertical.VerticalAxis


class ControlFragment : Fragment() {

    lateinit var binding: FragmentControlBinding
    private lateinit var mainViewModel: MainViewModel
    private lateinit var controlViewModel: ControlViewModel
    private val mAdapter: RecentAdapter by lazy { RecentAdapter() }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentControlBinding.inflate(layoutInflater, container, false)
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
        with(binding.weekActivityChartView.startAxis as VerticalAxis) {
            this.maxLabelCount = 5
            this.valueFormatter = PercentageFormatAxisValueFormatter()
        }
//        controlViewModel.setChartData(args.run.paceTimes)
    }

    private fun observeRuns() {
//        mainViewModel.readRecentRuns(3).observe(viewLifecycleOwner) { runs ->
//            mAdapter.setData(runs)
////            binding.itemCount = mAdapter.itemCount
//        }
        mainViewModel.readRuns.observe(viewLifecycleOwner) { runs ->
            RecentAdapter.limit = 3
            mAdapter.setData(runs)
//            binding.itemCount = mAdapter.itemCount
        }
    }

    private fun setupRecyclerView(recyclerView: RecyclerView) {
        recyclerView.adapter = mAdapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
    }
}