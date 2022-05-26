package com.example.forummpt

import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.forummpt.adapters.Preferences
import com.example.forummpt.dto.ApiSessionsDTO
import com.google.android.material.button.MaterialButton
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textview.MaterialTextView
import com.google.gson.GsonBuilder
import okhttp3.*
import org.json.JSONObject
import java.io.IOException
import java.lang.NullPointerException

class Authorization : AppCompatActivity() {
    private var currentNightMode : Int? = null
    private var currentTheme : String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_authorization)
        val tvAuthRestore : TextView = findViewById(R.id.tvAuthRestore)
        tvAuthRestore.setOnClickListener { startActivity(Intent(this, Restoration::class.java)) }
        val btnAuthOk : MaterialButton = findViewById(R.id.btnAuthOk)
        btnAuthOk.setOnClickListener { login() }
        currentTheme = Preferences(this@Authorization).getTheme()
        currentNightMode = this.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        checkTheme()
    }

    fun checkTheme() {
        val activity = findViewById<ConstraintLayout>(R.id.activityAuthorization)
        when (currentTheme) {
            "Светлая" -> {
                activity.background = ColorDrawable(Color.parseColor("#FFFFFF"))
                setTextViewsGroupColor(activity, Color.parseColor("#000000"))
            }
            "Тёмная" -> {
                activity.background = ColorDrawable(Color.parseColor("#000000"))
                setTextViewsGroupColor(activity, Color.parseColor("#FFFFFF"))
            }
            "Системная", "", null -> {
                when (currentNightMode) {
                    Configuration.UI_MODE_NIGHT_NO -> {
                        activity.background = ColorDrawable(Color.parseColor("#FFFFFF"))
                        setTextViewsGroupColor(activity, Color.parseColor("#000000"))
                    }
                    Configuration.UI_MODE_NIGHT_YES -> {
                        activity.background = ColorDrawable(Color.parseColor("#000000"))
                        setTextViewsGroupColor(activity, Color.parseColor("#FFFFFF"))
                    }
                }
            }
        }
    }

    fun setTextViewsGroupColor(v: View, color : Int) {
        try {
            if (v is ViewGroup) { for (i in 0 until v.childCount) { setTextViewsGroupColor(v.getChildAt(i), color) } }
            else {
                if (v is TextInputEditText) {
                    v.setTextColor(color)
                    v.setHintTextColor(Color.parseColor("#808080"))
                }
                if (v is MaterialTextView) { v.setTextColor(color) }
                if (v is MaterialCheckBox) { v.setTextColor(color) }
                if (v is MaterialButton) { v.setBackgroundColor(Color.parseColor(Preferences(this@Authorization).getAccentColor())) }
            }
        } catch (e: Exception) { e.printStackTrace() }
    }

    fun login() {
        val username = findViewById<TextInputEditText>(R.id.editAuthLogin)
        val password = findViewById<TextInputEditText>(R.id.editAuthPassword)
        if (username.text.toString() == "" || password.text.toString() == "") {
            Toast.makeText(this@Authorization, "Введите пароль и логин", Toast.LENGTH_SHORT).show()
            return
        }
        var formBody: RequestBody = FormBody.Builder().add("username", username.text.toString()).add("password", password.text.toString()).build()
        var request: Request = Request.Builder().url(getString(R.string.site)+"/api/login").post(formBody).build()
        val client = OkHttpClient()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call?, e: IOException?) {
                throw e!!
            }

            override fun onResponse(call: Call?, response: Response) {
                val gson = GsonBuilder().create()
                val jsonArray = JSONObject(response.body()?.string())
                val session = gson.fromJson(jsonArray.toString(), ApiSessionsDTO::class.java)
                var role : String = ""
                if (session.token != null && session.userId != null && session.userRole != null) {
                    val prefs = Preferences(this@Authorization)
                    if (session.userRole!!.contains("ADMIN")) { role = "ADMIN" } else { role = "USER" }
                    prefs.setUserId(session.userId!!)
                    prefs.setAuthorizationToken(session.token!!)
                    prefs.setUserRole(role)
                    runOnUiThread { startActivity(Intent(this@Authorization, Threads::class.java)) }
                }
                else { runOnUiThread { Toast.makeText(this@Authorization, "Не удалось авторизоваться", Toast.LENGTH_SHORT).show() } }
            }

        })
    }

    fun toastLong(text : String) { Toast.makeText(this@Authorization, text, Toast.LENGTH_LONG).show() }

}