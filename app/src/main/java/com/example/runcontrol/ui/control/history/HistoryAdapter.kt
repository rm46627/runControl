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

    private lateinit var mRecyclerView: RecyclerView

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        mRecyclerView = recyclerView
    }

    class MyViewHolder(private val binding: RowLayoutHistoryBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(runEntity: RunEntity) {

            val dayTime = runEntity.dateToDayTime()
            val imageRes: Int = when(dayTime) {
                "Morning" -> R.drawable.ic_morning
                "Afternoon" -> R.drawable.ic_afternoon
                "Evening" -> R.drawable.ic_evening
                "Night" -> R.drawable.ic_night
                else -> R.drawable.ic_afternoon
            }
            binding.dayTimeImageView.setImageResource(imageRes)
            binding.dayTimeTextView.text = dayTime
            binding.dateValueTextView.text = runEntity.dateToCalendar()
            binding.timeValueTextView.text = MapsUtil.getTimerStringFromTime(runEntity.runTime)
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

        val recyclerViewState = mRecyclerView.layoutManager?.onSaveInstanceState()
        diffUtilResult.dispatchUpdatesTo(this)
        mRecyclerView.layoutManager?.onRestoreInstanceState(recyclerViewState)
    }
}