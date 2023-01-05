package com.example.runcontrol.ui.control

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.runcontrol.database.entities.RunEntity
import com.example.runcontrol.databinding.FragmentControlBinding
import com.example.runcontrol.extensions.View.show
import com.example.runcontrol.extensions.View.showFadeIn
import com.example.runcontrol.ui.MainViewModel
import com.example.runcontrol.ui.control.StatsUtil.bestPace
import com.example.runcontrol.ui.control.StatsUtil.daysOfLastWeek
import com.example.runcontrol.ui.control.StatsUtil.mapDistancesToLastWeek
import com.example.runcontrol.ui.control.StatsUtil.sumBurned
import com.example.runcontrol.ui.control.StatsUtil.sumKilometers
import com.example.runcontrol.ui.maps.MapsUtil
import com.patrykandpatryk.vico.core.axis.formatter.AxisValueFormatter
import com.patrykandpatryk.vico.core.axis.formatter.DecimalFormatAxisValueFormatter
import com.patrykandpatryk.vico.core.axis.horizontal.HorizontalAxis
import com.patrykandpatryk.vico.core.axis.vertical.VerticalAxis

//    TODO:
//        binding.chartView.marker = marker
//        binding.chartView.chart?.addDecoration(decoration = thresholdLine)
//    TODO: global stats like
//              best avg pace time for 5km workout
//              days of month/year with workout (pie chart)
//              kilometers run this week/month/year
//              avg kilometers run in week/month/year
//              kilometers run in week/month/year

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

        observeRuns()
        setupRecyclerView(binding.recentActivitiesRecyclerView)


        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setChart()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setChart() {
        binding.weekActivityChartView.entryProducer = controlViewModel.chartEntryModelProducer
        with(binding.weekActivityChartView.startAxis as VerticalAxis) {
            this.valueFormatter = DecimalFormatAxisValueFormatter("##")
        }
        with(binding.weekActivityChartView.bottomAxis as HorizontalAxis) {
            this.valueFormatter = AxisValueFormatter { x, _ -> lastWeek[x.toInt() % lastWeek.size] }
        }
    }

    private fun observeRuns() {
        mainViewModel.readRuns.observe(viewLifecycleOwner) { runs ->
            mAdapter.setData(runs)
            val distList = mapDistancesToLastWeek(runs, lastWeek)
            if(distList.any { it != 0.0 }) {
                showWeekCharts(distList)
            }
            if(runs.isNotEmpty()) {
                showStats(runs)
            }
        }
    }

    private fun showWeekCharts(distList: List<Double>) {
        binding.weekActivityCardView.show()
        binding.weekActivityTextView.showFadeIn()
        binding.weekActivityChartView.showFadeIn()
        controlViewModel.setChartData(distList)
    }

    private fun showStats(runs: List<RunEntity>) {
        binding.globalStatsCardView.show()
        binding.globalStatsTextView.showFadeIn()

        binding.workoutsImageView.showFadeIn()
        binding.workoutsTextView.showFadeIn()
        binding.workoutsValueTextView.text = runs.size.toString()
        binding.workoutsValueTextView.showFadeIn()

        binding.allKilometersImageView.showFadeIn()
        binding.allKilometersTextView.showFadeIn()
        binding.allKilometersValueTextView.text = MapsUtil.formatDistance(sumKilometers(runs))
        binding.allKilometersValueTextView.showFadeIn()

        binding.bestPaceImageView.showFadeIn()
        binding.bestPaceTextView.showFadeIn()
        binding.bestPaceValueTextView.text = MapsUtil.formatPace(bestPace(runs))
        binding.bestPaceValueTextView.showFadeIn()

        binding.allBurnedImageView.showFadeIn()
        binding.allBurnedTextView.showFadeIn()
        binding.allBurnedValueTextView.text = sumBurned(runs).toString()
        binding.allBurnedValueTextView.showFadeIn()
    }

    private fun setupRecyclerView(recyclerView: RecyclerView) {
        recyclerView.adapter = mAdapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
    }
}