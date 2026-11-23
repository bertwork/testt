package com.example.studenttaskmanager.ui.settings

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.studenttaskmanager.R
import com.example.studenttaskmanager.data.AppDatabase
import com.example.studenttaskmanager.databinding.DialogChangePinBinding
import com.example.studenttaskmanager.databinding.FragmentSettingsBinding
import com.example.studenttaskmanager.prefs.SessionManager
import com.example.studenttaskmanager.ui.login.LoginActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class SettingsFragment : Fragment(R.layout.fragment_settings) {

    private var _b: FragmentSettingsBinding? = null
    private val b get() = _b!!

    private lateinit var prefs: SessionManager

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        _b = FragmentSettingsBinding.bind(view)
        prefs = SessionManager(requireContext())

        b.tvUsername.text = prefs.username ?: "—"
        b.btnChangePin.isVisible = prefs.isPinEnabled

        b.btnChangePin.setOnClickListener { showChangePinDialog() }
        b.btnReset.setOnClickListener { confirmReset() }
    }

    private fun showChangePinDialog() {
        val d = DialogChangePinBinding.inflate(layoutInflater)

        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle("Change PIN")
            .setView(d.root)
            .setPositiveButton("Save", null)
            .setNegativeButton("Cancel", null)
            .create()

        dialog.setOnShowListener {
            dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE)
                .setOnClickListener {
                    val current = d.etCurrent.text?.toString().orEmpty()
                    val newPin  = d.etNew.text?.toString().orEmpty()
                    val confirm = d.etConfirm.text?.toString().orEmpty()

                    when {
                        !prefs.verifyPin(current) -> {
                            d.etCurrent.error = "Incorrect PIN"
                        }
                        newPin.length != 4 -> {
                            d.etNew.error = "Must be exactly 4 digits"
                        }
                        newPin != confirm -> {
                            d.etConfirm.error = "PINs do not match"
                        }
                        else -> {
                            prefs.setPin(newPin)
                            toast("PIN updated")
                            dialog.dismiss()
                        }
                    }
                }
        }
        dialog.show()
    }

    private fun confirmReset() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Reset app?")
            .setMessage("This will delete your username, PIN and all tasks. This cannot be undone.")
            .setPositiveButton("Reset") { _, _ -> performReset() }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun performReset() {
        viewLifecycleOwner.lifecycleScope.launch {
            // Clear DB on IO thread
            withContext(Dispatchers.IO) {
                AppDatabase.get(requireContext()).taskDao().deleteAll()
            }
            prefs.clear()

            // Restart the whole app stack so nothing stale is left in memory
            val i = Intent(requireContext(), LoginActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            startActivity(i)
            requireActivity().finish()
        }
    }

    private fun toast(m: String) =
        Toast.makeText(requireContext(), m, Toast.LENGTH_SHORT).show()

    override fun onDestroyView() {
        super.onDestroyView()
        _b = null
    }
}