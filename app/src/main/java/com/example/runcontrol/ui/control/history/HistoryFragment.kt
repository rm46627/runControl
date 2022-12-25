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

//  TODO: history filtering

class HistoryFragment : Fragment() {

    private var _binding: FragmentHistoryListBinding? = null
    private val binding get() = _binding!!
    private lateinit var mainViewModel: MainViewModel
    private val mAdapter: HistoryAdapter by lazy { HistoryAdapter() }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentHistoryListBinding.inflate(layoutInflater, container, false)
        binding.lifecycleOwner = this
        mainViewModel = ViewModelProvider(requireActivity())[MainViewModel::class.java]

        setupRecyclerView(binding.historyRecyclerView)

        observeRuns()

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun observeRuns() {
        mainViewModel.readRuns.observe(viewLifecycleOwner) { runs ->
            mAdapter.setData(runs)
//            binding.itemCount = mAdapter.itemCount
        }
    }

    private fun setupRecyclerView(recyclerView: RecyclerView) {
        recyclerView.adapter = mAdapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
    }
}