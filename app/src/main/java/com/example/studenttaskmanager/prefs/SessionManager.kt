package com.example.studenttaskmanager.prefs

import android.content.Context
import android.content.SharedPreferences
import java.security.MessageDigest
import java.util.UUID

class SessionManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("session", Context.MODE_PRIVATE)

    val username: String? get() = prefs.getString(KEY_USER, null)
    val hasUser: Boolean get() = username != null
    val isPinEnabled: Boolean get() = prefs.getString(KEY_PIN, null) != null

    fun saveUser(name: String) {
        prefs.edit().putString(KEY_USER, name).apply()
    }

    fun setPin(pin: String) {
        val salt = UUID.randomUUID().toString()
        prefs.edit()
            .putString(KEY_SALT, salt)
            .putString(KEY_PIN, hash(salt, pin))
            .apply()
    }

    fun verifyPin(pin: String): Boolean {
        val salt = prefs.getString(KEY_SALT, null) ?: return false
        val stored = prefs.getString(KEY_PIN, null) ?: return false
        return stored == hash(salt, pin)
    }

    fun clear() = prefs.edit().clear().apply()

    private fun hash(salt: String, pin: String): String =
        MessageDigest.getInstance("SHA-256")
            .digest("$salt:$pin".toByteArray())
            .joinToString("") { "%02x".format(it) }

    companion object {
        private const val KEY_USER = "username"
        private const val KEY_PIN  = "pin_hash"
        private const val KEY_SALT = "pin_salt"
    }
}