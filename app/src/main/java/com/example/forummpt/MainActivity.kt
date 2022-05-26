package com.example.forummpt

import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.forummpt.adapters.Preferences
import com.google.android.material.button.MaterialButton
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textview.MaterialTextView
import java.security.AccessController.getContext

class MainActivity : AppCompatActivity() {
    private var currentNightMode : Int? = null
    private var currentTheme : String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        checkPreferences()
        setContentView(R.layout.activity_main)
        currentTheme = Preferences(this@MainActivity).getTheme()
        currentNightMode = this.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        checkTheme()
        val btnAuth : MaterialButton = findViewById(R.id.btnAuth)
        val btnRegister : MaterialButton = findViewById(R.id.btnRegister)
        btnAuth.setOnClickListener {
            val intent = Intent(this@MainActivity, Authorization::class.java)
            startActivity(intent)
        }
        btnRegister.setOnClickListener {
            val intent = Intent(this@MainActivity, Registration::class.java)
            startActivity(intent)
        }
    }

    fun checkPreferences() {
        val prefs = Preferences(this@MainActivity)
        val theme : String? = prefs.getTheme()
        val accentColor : String? = prefs.getAccentColor()
        val token : String? = prefs.getAuthorizationToken()
        if (theme.equals("") || theme == null) { prefs.setTheme(resources.getStringArray(R.array.theme_modes).get(0)) }
        if (accentColor.equals("") || accentColor == null) { prefs.setAccentColor(resources.getStringArray(R.array.accent_colors).get(0)) }
        if (token != "" && token != null) { startActivity(Intent(this@MainActivity, Threads::class.java)) }
    }

    fun checkTheme() {
        val activity = findViewById<ConstraintLayout>(R.id.activityMain)
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
                if (v is MaterialButton) { v.setBackgroundColor(Color.parseColor(Preferences(this@MainActivity).getAccentColor())) }
            }
        } catch (e: Exception) { e.printStackTrace() }
    }

}