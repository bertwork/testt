package com.example.studenttaskmanager.ui.tasks

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
import com.example.studenttaskmanager.databinding.ItemTaskBinding
import com.example.studenttaskmanager.util.toDateString
import com.example.studenttaskmanager.util.todayStart

class TaskAdapter(
    private val onToggle: (Task) -> Unit = {},
    private val onClick: (Task) -> Unit = {},
    private val onLongClick: (Task) -> Unit = {}
) : ListAdapter<Task, TaskAdapter.VH>(DIFF) {

    companion object DIFF : DiffUtil.ItemCallback<Task>() {
        override fun areItemsTheSame(a: Task, b: Task) = a.id == b.id
        override fun areContentsTheSame(a: Task, b: Task) = a == b
    }

    inner class VH(val b: ItemTaskBinding) : RecyclerView.ViewHolder(b.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH =
        VH(ItemTaskBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: VH, position: Int) {
        val task = getItem(position)
        val b = holder.b
        val ctx = b.root.context

        b.chkDone.setOnCheckedChangeListener(null)
        b.chkDone.isChecked = task.status == TaskStatus.COMPLETED
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

        if (done) {
            b.tvStatus.text = "DONE"
            b.tvStatus.setTextColor(ContextCompat.getColor(ctx, R.color.success))
        } else {
            b.tvStatus.text = "ACTIVE"
            b.tvStatus.setTextColor(ContextCompat.getColor(ctx, R.color.warning))
        }

        b.chkDone.setOnCheckedChangeListener { _, _ -> onToggle(task) }
        b.root.setOnClickListener { onClick(task) }
        b.root.setOnLongClickListener { onLongClick(task); true }
    }
}