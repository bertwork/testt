package com.example.studenttaskmanager.ui.dashboard

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.studenttaskmanager.R
import com.example.studenttaskmanager.databinding.FragmentDashboardBinding
import com.example.studenttaskmanager.prefs.SessionManager
import com.example.studenttaskmanager.ui.tasks.TaskViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DashboardFragment : Fragment(R.layout.fragment_dashboard) {

    private var _b: FragmentDashboardBinding? = null
    private val b get() = _b!!

    private val vm: TaskViewModel by viewModels()
    private lateinit var adapter: RecentTaskAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        _b = FragmentDashboardBinding.bind(view)

        val prefs = SessionManager(requireContext())
        b.tvWelcome.text = "Hi, ${prefs.username ?: "Student"}"
        b.tvDate.text = SimpleDateFormat(
            "EEEE, dd MMMM", Locale.getDefault()
        ).format(Date())

        adapter = RecentTaskAdapter()
        b.rvRecent.layoutManager = LinearLayoutManager(requireContext())
        b.rvRecent.adapter = adapter

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { vm.total.collect      { b.tvTotal.text      = it.toString() } }
                launch { vm.inProgress.collect { b.tvInProgress.text = it.toString() } }
                launch { vm.completed.collect  { b.tvCompleted.text  = it.toString() } }
                launch {
                    vm.recent.collect {
                        adapter.submitList(it)
                        val empty = it.isEmpty()
                        b.tvEmptyRecent.isVisible = empty
                        b.rvRecent.isVisible = !empty
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        b.rvRecent.adapter = null
        _b = null
    }
}