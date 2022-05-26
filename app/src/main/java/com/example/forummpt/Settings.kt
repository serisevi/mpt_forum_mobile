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
import com.google.android.material.textview.MaterialTextView

class Settings : AppCompatActivity() {
    private var currentNightMode : Int? = null
    private var currentTheme : String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        var themes : Array<String> = resources.getStringArray(R.array.theme_modes)
        var colors : Array<String> = resources.getStringArray(R.array.accent_colors)
        findViewById<ImageView>(R.id.ivMenuThreads).setOnClickListener { startActivity(Intent(this, Threads::class.java)) }
        findViewById<ImageView>(R.id.ivMenuAccount).setOnClickListener { startActivity(Intent(this, Account::class.java)) }
        val cmbThemes = findViewById<Spinner>(R.id.cmbSettingsTheme)
        val cmbAccent = findViewById<Spinner>(R.id.cmbSettingsAccent)
        cmbThemes.setSelection(themes.indexOf(Preferences(this@Settings).getTheme()))
        cmbAccent.setSelection(colors.indexOf(Preferences(this@Settings).getAccentColor()))
        cmbThemes.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                Preferences(this@Settings).setTheme(themes[position])
                currentTheme = Preferences(this@Settings).getTheme()
                checkTheme()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {  }
        }
        cmbAccent.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                Preferences(this@Settings).setAccentColor(colors[position])
                checkTheme()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {  }
        }
        currentTheme = Preferences(this@Settings).getTheme()
        currentNightMode = this.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        checkTheme()
    }

    fun checkTheme() {
        val activity = findViewById<ConstraintLayout>(R.id.activitySettings)
        val ivMenuThreads : ImageView = findViewById(R.id.ivMenuThreads)
        val ivMenuAccount : ImageView = findViewById(R.id.ivMenuAccount)
        val ivMenuSettings : ImageView = findViewById(R.id.ivMenuSettings)
        when (currentTheme) {
            "Светлая" -> {
                activity.background = ColorDrawable(Color.parseColor("#FFFFFF"))
                setTextViewsGroupColor(activity, Color.parseColor("#000000"))
                ivMenuThreads.setImageDrawable(getDrawable(R.drawable.threads_black))
                ivMenuAccount.setImageDrawable(getDrawable(R.drawable.account_black))
                ivMenuSettings.setImageDrawable(getDrawable(R.drawable.gear_black))
            }
            "Тёмная" -> {
                activity.background = ColorDrawable(Color.parseColor("#000000"))
                ivMenuThreads.setImageDrawable(getDrawable(R.drawable.threads_white))
                ivMenuAccount.setImageDrawable(getDrawable(R.drawable.account_white))
                ivMenuSettings.setImageDrawable(getDrawable(R.drawable.gear_white))
                setTextViewsGroupColor(activity, Color.parseColor("#FFFFFF"))
            }
            "Системная" -> {
                when (currentNightMode) {
                    Configuration.UI_MODE_NIGHT_NO -> {
                        activity.background = ColorDrawable(Color.parseColor("#FFFFFF"))
                        ivMenuThreads.setImageDrawable(getDrawable(R.drawable.threads_black))
                        ivMenuAccount.setImageDrawable(getDrawable(R.drawable.account_black))
                        ivMenuSettings.setImageDrawable(getDrawable(R.drawable.gear_black))
                        setTextViewsGroupColor(activity, Color.parseColor("#000000"))
                    }
                    Configuration.UI_MODE_NIGHT_YES -> {
                        activity.background = ColorDrawable(Color.parseColor("#000000"))
                        ivMenuThreads.setImageDrawable(getDrawable(R.drawable.threads_white))
                        ivMenuAccount.setImageDrawable(getDrawable(R.drawable.account_white))
                        ivMenuSettings.setImageDrawable(getDrawable(R.drawable.gear_white))
                        setTextViewsGroupColor(activity, Color.parseColor("#FFFFFF"))
                    }
                }
            }
        }
    }

    fun setTextViewsGroupColor(v: View, color : Int) {
        try {
            if (v is ViewGroup) {
                for (i in 0 until v.childCount) {
                    val child = v.getChildAt(i)
                    setTextViewsGroupColor(child, color)
                }
            } else if (v is MaterialTextView) {
                v.setTextColor(color)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

}