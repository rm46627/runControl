package com.example.runcontrol.ui.result

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.runcontrol.databinding.FragmentResultBinding
import com.example.runcontrol.ui.MainViewModel
import com.example.runcontrol.ui.maps.MapsUtil.formatDistance
import com.example.runcontrol.ui.maps.MapsUtil.getTimerStringFromTime
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ResultFragment : BottomSheetDialogFragment() {

    private val args: ResultFragmentArgs by navArgs()
    private lateinit var binding: FragmentResultBinding
    private val mainViewModel: MainViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentResultBinding.inflate(inflater, container, false)

        binding.distanceValueTextView.text = formatDistance(args.result.distanceMeters)
        binding.timeValueTextView.text = getTimerStringFromTime(args.result.time)
        binding.dateValueTextView.text = args.result.date

        binding.shareBtn.setOnClickListener {
            shareResult()
        }
        binding.saveBtn.setOnClickListener{
            saveResult()
        }

        return binding.root
    }

    private fun saveResult() {
        mainViewModel.insertRun(args.result)
        val action = ResultFragmentDirections.actionResultFragmentToMapsFragment()
        findNavController().navigate(action)
        Toast.makeText(
            requireContext(),
            "Run saved!",
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun shareResult() {
        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            type = "text/plain"
            putExtra(
                Intent.EXTRA_TEXT,
                "I went ${formatDistance(args.result.distanceMeters)} in ${args.result.time} on ${args.result.date}!"
            )
        }
        startActivity(shareIntent)
    }

}