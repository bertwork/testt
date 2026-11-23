package com.example.studenttaskmanager.ui.tasks

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.studenttaskmanager.R
import com.example.studenttaskmanager.data.Task
import com.example.studenttaskmanager.data.TaskStatus
import com.example.studenttaskmanager.databinding.DialogTaskBinding
import com.example.studenttaskmanager.databinding.FragmentTasksBinding
import com.example.studenttaskmanager.util.toDateString
import com.example.studenttaskmanager.util.todayStart
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch
import java.util.Calendar

class TasksFragment : Fragment(R.layout.fragment_tasks) {

    private var _b: FragmentTasksBinding? = null
    private val b get() = _b!!

    private val vm: TaskViewModel by viewModels()
    private lateinit var adapter: TaskAdapter

    // Deadline currently chosen in the open dialog (epoch millis)
    private var pendingDeadline: Long = todayStart()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        _b = FragmentTasksBinding.bind(view)

        adapter = TaskAdapter(
            onToggle = { vm.toggleStatus(it) },
            onClick = { showTaskDialog(it) },
            onLongClick = { confirmDelete(it) }
        )
        b.rvTasks.layoutManager = LinearLayoutManager(requireContext())
        b.rvTasks.adapter = adapter

        b.etSearch.doAfterTextChanged { vm.setQuery(it?.toString().orEmpty()) }

        b.chipGroup.setOnCheckedStateChangeListener { _, checked ->
            vm.setFilter(
                when (checked.firstOrNull()) {
                    R.id.chipActive    -> TaskFilter.ACTIVE
                    R.id.chipCompleted -> TaskFilter.COMPLETED
                    else               -> TaskFilter.ALL
                }
            )
        }

        b.fabAdd.setOnClickListener { showTaskDialog(null) }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                vm.tasks.collect {
                    adapter.submitList(it)
                    b.tvEmpty.isVisible = it.isEmpty()
                }
            }
        }
    }

    /**
     * Shows the add/edit dialog.
     * Pass null to add a new task, or an existing Task to edit it.
     * The dialog stays open if validation fails.
     */
    private fun showTaskDialog(task: Task?) {
        val d = DialogTaskBinding.inflate(layoutInflater)
        pendingDeadline = task?.deadline ?: todayStart()

        d.etTitle.setText(task?.title.orEmpty())
        d.btnDeadline.text = pendingDeadline.toDateString()
        d.swCompleted.isChecked = task?.status == TaskStatus.COMPLETED

        d.btnDeadline.setOnClickListener {
            pickDate(pendingDeadline) { picked ->
                pendingDeadline = picked
                d.btnDeadline.text = picked.toDateString()
            }
        }

        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle(if (task == null) "New task" else "Edit task")
            .setView(d.root)
            .setPositiveButton("Save", null)
            .setNegativeButton("Cancel", null)
            .create()

        // Override the Save click so we can keep the dialog open on bad input
        dialog.setOnShowListener {
            dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE)
                .setOnClickListener {
                    val title = d.etTitle.text?.toString()?.trim().orEmpty()
                    if (title.isEmpty()) {
                        d.etTitle.error = "Title cannot be empty"
                        return@setOnClickListener
                    }
                    val done = d.swCompleted.isChecked
                    if (task == null) {
                        vm.addTask(title, pendingDeadline, done)
                    } else {
                        vm.updateTask(
                            task.copy(
                                title = title,
                                deadline = pendingDeadline,
                                status = if (done) TaskStatus.COMPLETED
                                else TaskStatus.IN_PROGRESS
                            )
                        )
                    }
                    dialog.dismiss()
                }
        }
        dialog.show()
    }

    private fun confirmDelete(task: Task) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Delete task")
            .setMessage("Delete \"${task.title}\"?")
            .setPositiveButton("Delete") { _, _ -> vm.deleteTask(task) }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun pickDate(initial: Long, onPick: (Long) -> Unit) {
        val c = Calendar.getInstance().apply { timeInMillis = initial }
        DatePickerDialog(
            requireContext(),
            { _, y, m, d ->
                val picked = Calendar.getInstance().apply {
                    set(y, m, d, 0, 0, 0)
                    set(Calendar.MILLISECOND, 0)
                }.timeInMillis
                onPick(picked)
            },
            c.get(Calendar.YEAR),
            c.get(Calendar.MONTH),
            c.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        b.rvTasks.adapter = null
        _b = null
    }
}