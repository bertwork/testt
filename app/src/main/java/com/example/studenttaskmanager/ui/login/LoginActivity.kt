package com.example.studenttaskmanager.ui.login

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.example.studenttaskmanager.databinding.ActivityLoginBinding
import com.example.studenttaskmanager.prefs.SessionManager
import com.example.studenttaskmanager.ui.MainActivity

class LoginActivity : AppCompatActivity() {

    private lateinit var b: ActivityLoginBinding
    private lateinit var prefs: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(b.root)
        prefs = SessionManager(this)

        setupMode()

        b.swPin.setOnCheckedChangeListener { _, checked ->
            if (!prefs.hasUser) b.tilPin.isVisible = checked
        }

        b.btnContinue.setOnClickListener { submit() }
    }

    /** Two modes: first-run (create account) vs returning (unlock). */
    private fun setupMode() {
        if (prefs.hasUser) {
            // Returning user
            b.etUsername.setText(prefs.username)
            b.etUsername.isEnabled = false
            b.swPin.isVisible = false
            b.tilPin.isVisible = prefs.isPinEnabled
            b.btnContinue.text = if (prefs.isPinEnabled) "Unlock" else "Continue"
        } else {
            // First run
            b.tilPin.isVisible = false
            b.btnContinue.text = "Continue"
        }
    }

    private fun submit() {
        hideError()

        if (!prefs.hasUser) {
            // First run — create account
            val name = b.etUsername.text?.toString()?.trim().orEmpty()
            if (name.length < 2) return error("Enter a username (min 2 characters)")

            prefs.saveUser(name)

            if (b.swPin.isChecked) {
                val pin = b.etPin.text?.toString().orEmpty()
                if (pin.length != 4) return error("PIN must be exactly 4 digits")
                prefs.setPin(pin)
            }
            goToMain()
        } else {
            // Returning user — verify PIN if set
            if (prefs.isPinEnabled) {
                val pin = b.etPin.text?.toString().orEmpty()
                if (pin.length != 4 || !prefs.verifyPin(pin)) return error("Incorrect PIN")
            }
            goToMain()
        }
    }

    private fun goToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    private fun error(msg: String) {
        b.tvError.text = msg
        b.tvError.isVisible = true
    }

    private fun hideError() {
        b.tvError.isVisible = false
    }
}