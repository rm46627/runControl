package com.example.runcontrol.ui.control.history

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.runcontrol.R
import com.example.runcontrol.database.entities.RunEntity
import com.example.runcontrol.databinding.RowLayoutHistoryBinding
import com.example.runcontrol.extensions.NavController.safeNavigate
import com.example.runcontrol.ui.maps.MapsUtil
import com.example.runcontrol.utils.RunsDiffUtil

class HistoryAdapter : RecyclerView.Adapter<HistoryAdapter.MyViewHolder>() {

    private var runs = emptyList<RunEntity>()

    class MyViewHolder(private val binding: RowLayoutHistoryBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(runEntity: RunEntity) {
            binding.dateValueTextView.text = runEntity.date.takeLast(10)
            binding.timeValueTextView.text = MapsUtil.getTimerStringFromTime(runEntity.time)
            binding.distanceValueTextView.text = MapsUtil.formatDistance(runEntity.distanceMeters)
            binding.caloriesValueTextView.text = runEntity.burnedKcal.toString()
            binding.executePendingBindings()
        }

        companion object {
            fun from(parent: ViewGroup): MyViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = RowLayoutHistoryBinding.inflate(layoutInflater, parent, false)
                return MyViewHolder(binding)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        clickAnimationForCard(holder)

        val currentRun = runs[position]
        holder.bind(currentRun)

        // single click
        holder.itemView.findViewById<ConstraintLayout>(R.id.runRowLayout).setOnClickListener {
            val action =
                HistoryFragmentDirections.actionHistoryFragmentToDetailsFragment(currentRun)
            holder.itemView.findNavController().safeNavigate(action)
        }
    }

    private fun clickAnimationForCard(holder: MyViewHolder) {
        val attrs = intArrayOf(android.R.attr.selectableItemBackgroundBorderless)
        val typedArray = holder.itemView.context.obtainStyledAttributes(attrs)
        val selectableItemBackground = typedArray.getResourceId(0, 0)
        typedArray.recycle()
        holder.itemView.isClickable = true
        holder.itemView.isFocusable = true
//        holder.itemView.foreground = holder.itemView.context.getDrawable(selectableItemBackground)
        holder.itemView.foreground =
            AppCompatResources.getDrawable(holder.itemView.context, selectableItemBackground)
    }

    override fun getItemCount(): Int {
        return runs.size
    }

    fun setData(newRunsList: List<RunEntity>) {
        val runsDiffUtil = RunsDiffUtil(runs, newRunsList)
        val diffUtilResult = DiffUtil.calculateDiff(runsDiffUtil)
        runs = newRunsList
        diffUtilResult.dispatchUpdatesTo(this)
    }
}