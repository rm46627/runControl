package com.example.runcontrol.ui.control

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.appcompat.content.res.AppCompatResources
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.runcontrol.R
import com.example.runcontrol.database.entities.RunEntity
import com.example.runcontrol.databinding.RowLayoutButtonBinding
import com.example.runcontrol.databinding.RowLayoutRecentBinding
import com.example.runcontrol.extensions.NavController.safeNavigate
import com.example.runcontrol.ui.maps.MapsUtil
import com.example.runcontrol.utils.RunsDiffUtil

// TODO: animate new added recent runs on more button click instead of animate all of them

class RecentAdapter() : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    companion object {
        const val CARD_TYPE = 1
        const val BTN_TYPE = 2
        const val LIMIT_ADDITION = 3
        var displayedRunsLimit: Int = 3
        private var showMoreClicked = false
    }
    private var runs = emptyList<RunEntity>()
    private lateinit var recycler: RecyclerView

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        recycler = recyclerView
    }

    class RunCardViewHolder(private val binding: RowLayoutRecentBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(runEntity: RunEntity) {
            binding.dateValueTextView.text = runEntity.dateToCalendar()
            binding.timeValueTextView.text = MapsUtil.getTimerStringFromTime(runEntity.runTime)
            binding.distanceValueTextView.text = MapsUtil.formatDistance(runEntity.distanceMeters)
            binding.caloriesValueTextView.text = runEntity.burnedKcal.toString()
            binding.executePendingBindings()
        }

        companion object {
            fun from(parent: ViewGroup): RunCardViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = RowLayoutRecentBinding.inflate(layoutInflater, parent, false)
                return RunCardViewHolder(binding)
            }
        }
    }

    class ButtonViewHolder(private val binding: RowLayoutButtonBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(adapter: RecentAdapter, recyclerView: RecyclerView, btnClicked: Boolean) {
            if(!btnClicked) {
                binding.btn.setOnClickListener {
                    displayedRunsLimit += LIMIT_ADDITION
                    adapter.notifyItemRangeInserted(displayedRunsLimit - LIMIT_ADDITION, displayedRunsLimit)
                    showMoreClicked = true
                }
            } else {
                binding.btn.text = this.itemView.context.getString(R.string.show_all)
                binding.btn.setOnClickListener {
                    val action = ControlFragmentDirections.actionControlFragmentToHistoryFragment()
                    this.itemView.findNavController().safeNavigate(action)
                }
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
        return if(position != displayedRunsLimit-1) {
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
        holder.bind(this, recycler, showMoreClicked)

    }

    private fun initCardLayout(holder: RunCardViewHolder, position: Int) {
        val currentRun = runs[position]
        holder.bind(currentRun)
        setAnimation(holder.itemView, position)
        // single click
        holder.itemView.findViewById<ConstraintLayout>(R.id.runRowLayout)
            .setOnClickListener {
                val action =
                    ControlFragmentDirections.actionControlFragmentToDetailsFragment(currentRun)
                holder.itemView.findNavController().safeNavigate(action)
            }
        clickAnimationForCard(holder)
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
        return if(displayedRunsLimit < runs.size) displayedRunsLimit else runs.size
    }

    fun setData(newRunsList: List<RunEntity>) {
        val runsDiffUtil = RunsDiffUtil(runs, newRunsList)
        val diffUtilResult = DiffUtil.calculateDiff(runsDiffUtil)
        runs = newRunsList
        diffUtilResult.dispatchUpdatesTo(this)
    }

    private fun setAnimation(view: View, position: Int) {
        val anim = AnimationUtils.loadAnimation(view.context, R.anim.scale_up)
        view.startAnimation(anim)
    }
}
