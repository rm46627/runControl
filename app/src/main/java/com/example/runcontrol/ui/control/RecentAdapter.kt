package com.example.runcontrol.ui.control

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.runcontrol.database.entities.RunEntity
import com.example.runcontrol.databinding.RowLayoutButtonBinding
import com.example.runcontrol.databinding.RowLayoutHistoryBinding
import com.example.runcontrol.extensions.NavController.safeNavigate
import com.example.runcontrol.ui.maps.MapsUtil
import com.example.runcontrol.utils.RunsDiffUtil


class RecentAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        const val CARD_TYPE = 1
        const val BTN_TYPE = 2
        var limit: Int = 0
    }

    private var runs = emptyList<RunEntity>()

    class RunCardViewHolder(private val binding: RowLayoutHistoryBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(runEntity: RunEntity) {
            binding.dateValueTextView.text = runEntity.date.takeLast(10)
            binding.timeValueTextView.text = MapsUtil.getTimerStringFromTime(runEntity.time)
            binding.distanceValueTextView.text = MapsUtil.formatDistance(runEntity.distanceMeters)
            binding.caloriesValueTextView.text = runEntity.burnedKcal.toString()
            binding.executePendingBindings()
        }

        companion object {
            fun from(parent: ViewGroup): RunCardViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = RowLayoutHistoryBinding.inflate(layoutInflater, parent, false)
                return RunCardViewHolder(binding)
            }
        }
    }

    class ButtonViewHolder(private val binding: RowLayoutButtonBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(adapter: RecentAdapter, position: Int) {
        binding.btn.setOnClickListener {
            limit += 3
//            it.hide()
            adapter.notifyDataSetChanged()
        }
            binding.executePendingBindings()
        }

        companion object {
            fun from(parent: ViewGroup): ButtonViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = RowLayoutButtonBinding.inflate(layoutInflater, parent, false)
                return ButtonViewHolder(binding)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (viewType == CARD_TYPE) {
            return RunCardViewHolder.from(parent)
        }
        return ButtonViewHolder.from(parent)
    }

    override fun getItemViewType(position: Int): Int {
        return if(position != limit-1) {
            CARD_TYPE
        } else {
            BTN_TYPE
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder.itemViewType) {
            CARD_TYPE -> initCardLayout(holder as RunCardViewHolder, position)
            BTN_TYPE -> initBtnLayout(holder as ButtonViewHolder, position)
            else -> {}
        }
    }

    private fun initBtnLayout(holder: ButtonViewHolder, position: Int) {
        holder.bind(this, position)
    }

    private fun initCardLayout(holder: RunCardViewHolder, position: Int) {
        clickAnimationForCard(holder as RunCardViewHolder)
        val currentRun = runs[position]
        holder.bind(currentRun)
        // single click
        holder.itemView.findViewById<ConstraintLayout>(com.example.runcontrol.R.id.runRowLayout)
            .setOnClickListener {
                val action =
                    ControlFragmentDirections.actionControlFragmentToDetailsFragment(currentRun)
                holder.itemView.findNavController().safeNavigate(action)
            }
    }

    private fun clickAnimationForCard(holder: RunCardViewHolder) {
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
        return if(limit < runs.size) limit else runs.size
    }

    fun setData(newRunsList: List<RunEntity>) {
        val runsDiffUtil = RunsDiffUtil(runs, newRunsList.subList(0, limit))
        val diffUtilResult = DiffUtil.calculateDiff(runsDiffUtil)
        runs = newRunsList
        diffUtilResult.dispatchUpdatesTo(this)
    }

}
