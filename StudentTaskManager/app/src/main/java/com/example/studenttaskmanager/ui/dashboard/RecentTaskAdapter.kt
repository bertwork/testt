package com.example.studenttaskmanager.ui.dashboard

import android.content.res.ColorStateList
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.studenttaskmanager.R
import com.example.studenttaskmanager.data.Task
import com.example.studenttaskmanager.data.TaskStatus
import com.example.studenttaskmanager.databinding.ItemRecentTaskBinding
import com.example.studenttaskmanager.util.toDateString
import com.example.studenttaskmanager.util.todayStart

class RecentTaskAdapter : ListAdapter<Task, RecentTaskAdapter.VH>(DIFF) {

    companion object DIFF : DiffUtil.ItemCallback<Task>() {
        override fun areItemsTheSame(a: Task, b: Task) = a.id == b.id
        override fun areContentsTheSame(a: Task, b: Task) = a == b
    }

    inner class VH(val b: ItemRecentTaskBinding) : RecyclerView.ViewHolder(b.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH =
        VH(ItemRecentTaskBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: VH, position: Int) {
        val task = getItem(position)
        val b = holder.b
        val ctx = b.root.context

        b.tvTitle.text = task.title

        val done = task.status == TaskStatus.COMPLETED
        b.tvTitle.paintFlags = if (done)
            b.tvTitle.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
        else
            b.tvTitle.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()

        val overdue = !done && task.deadline < todayStart()
        b.tvDeadline.text = if (overdue) "Overdue · ${task.deadline.toDateString()}"
        else "Due ${task.deadline.toDateString()}"
        b.tvDeadline.setTextColor(
            ContextCompat.getColor(ctx, if (overdue) R.color.danger else R.color.text_muted)
        )

        val dotColor = when {
            done -> R.color.success
            overdue -> R.color.danger
            else -> R.color.warning
        }
        b.statusDot.backgroundTintList =
            ColorStateList.valueOf(ContextCompat.getColor(ctx, dotColor))
    }
}