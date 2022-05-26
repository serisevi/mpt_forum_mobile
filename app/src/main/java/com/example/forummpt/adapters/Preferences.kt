package com.example.forummpt.adapters

import android.content.Context
import com.example.forummpt.R
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import kotlin.concurrent.thread

class Preferences(context : Context) {

    val prefContext = context
    val preferencesName = "forum_settings"
    val sharedPreferences = context.getSharedPreferences(preferencesName, Context.MODE_PRIVATE)

    fun setTheme(themeMode : String) {
        sharedPreferences.edit().remove("THEME_MODE").apply()
        sharedPreferences.edit().putString("THEME_MODE", themeMode).apply()
    }

    fun setAccentColor(colorCode: String) {
        sharedPreferences.edit().remove("ACCENT_COLOR").apply()
        sharedPreferences.edit().putString("ACCENT_COLOR", colorCode).apply()
    }

    fun setUserId(userId : String) {
        sharedPreferences.edit().remove("USER_ID").apply()
        sharedPreferences.edit().putString("USER_ID", userId).apply()
    }

    fun setUserRole(role : String) {
        sharedPreferences.edit().remove("USER_ROLE").apply()
        sharedPreferences.edit().putString("USER_ROLE", role).apply()
    }

    fun setAuthorizationToken(token : String) {
        sharedPreferences.edit().remove("AUTH_TOKEN").apply()
        sharedPreferences.edit().putString("AUTH_TOKEN", token).apply()
    }

    fun getTheme() : String? {
        return sharedPreferences.getString("THEME_MODE", "")
    }

    fun getAccentColor() : String? {
        return sharedPreferences.getString("ACCENT_COLOR", "")
    }

    fun getUserId() : String? {
        return sharedPreferences.getString("USER_ID", "")
    }

    fun getUserRole() : String? {
        return sharedPreferences.getString("USER_ROLE", "")
    }

    fun getAuthorizationToken() : String? {
        return sharedPreferences.getString("AUTH_TOKEN", "")
    }

    fun unauthorize(token : String) {
        sharedPreferences.edit().remove("USER_ID").remove("AUTH_TOKEN").apply()
        var formBody: RequestBody = FormBody.Builder().add("token", token).build()
        var request: Request = Request.Builder().url(prefContext.getString(R.string.site)+"/api/logout").post(formBody).build()
        thread { OkHttpClient().newCall(request).execute() }
    }

    fun clearPrefs() { sharedPreferences.edit().clear().apply() }

}