package com.example.runcontrol.ui.control.history

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.runcontrol.databinding.FragmentHistoryListBinding
import com.example.runcontrol.ui.MainViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HistoryFragment : Fragment() {

    private var _binding: FragmentHistoryListBinding? = null
    private val binding get() = _binding!!
    private lateinit var mainViewModel: MainViewModel
    private val mAdapter: HistoryAdapter by lazy { HistoryAdapter() }
    private lateinit var historyViewModel: HistoryViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        historyViewModel = ViewModelProvider(requireActivity())[HistoryViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentHistoryListBinding.inflate(layoutInflater, container, false)
        binding.lifecycleOwner = this
        mainViewModel = ViewModelProvider(requireActivity())[MainViewModel::class.java]

        setupRecyclerView(binding.historyRecyclerView)

        binding.sortTypeChipGroup.setOnCheckedStateChangeListener { _, selectedChipId ->
            historyViewModel.selectedChip = selectedChipId[0]
            observeRuns()
        }
        binding.sortDirImageView.setOnClickListener {
            historyViewModel.listDesc = !historyViewModel.listDesc
            observeRuns()
            it.animate().apply {
                duration = 300
                rotationXBy(180f)
            }
        }

        observeRuns()


        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun observeRuns() {
        val desc = historyViewModel.listDesc
        mainViewModel.readRuns.observe(viewLifecycleOwner) { runs ->
            mAdapter.setData(runs.sortedWith (
                when(historyViewModel.selectedChip){
                    binding.dateChip.id ->
                        if(desc) compareByDescending {it.date}
                        else compareBy {it.date}
                    binding.timeChip.id -> if(desc) compareByDescending {it.runTime}
                        else compareBy {it.runTime}
                    binding.distanceChip.id -> if(desc) compareByDescending {it.distanceMeters}
                        else compareBy {it.distanceMeters}
                    binding.caloriesChip.id -> if(desc) compareByDescending {it.burnedKcal}
                        else compareBy {it.burnedKcal}
                    else -> if(desc) compareByDescending {it.date} else compareBy {it.burnedKcal}
                }
            ))
        }
    }

    private fun setupRecyclerView(recyclerView: RecyclerView) {
        recyclerView.adapter = mAdapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
    }
}