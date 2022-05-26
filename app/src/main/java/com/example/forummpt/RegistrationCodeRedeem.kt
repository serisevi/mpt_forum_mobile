package com.example.forummpt

import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.forummpt.adapters.Preferences
import com.example.forummpt.dto.ApiResponse
import com.google.android.material.button.MaterialButton
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textview.MaterialTextView
import com.google.gson.GsonBuilder
import okhttp3.*
import org.json.JSONObject
import java.io.IOException

class RegistrationCodeRedeem : AppCompatActivity() {
    private var currentNightMode : Int? = null
    private var currentTheme : String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registration_code_redeem)
        currentTheme = Preferences(this@RegistrationCodeRedeem).getTheme()
        currentNightMode = this.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        checkTheme()
        val btnRegCodeOk = findViewById<MaterialButton>(R.id.btnRegCodeOk)
        btnRegCodeOk.setOnClickListener { redeemCode() }
    }

    fun checkTheme() {
        val activity = findViewById<ConstraintLayout>(R.id.activityRegistrationCodeRedeem)
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
                if (v is MaterialButton) { v.setBackgroundColor(Color.parseColor(Preferences(this@RegistrationCodeRedeem).getAccentColor())) }
            }
        } catch (e: Exception) { e.printStackTrace() }
    }

    fun redeemCode() {
        val code = findViewById<TextInputEditText>(R.id.editRegCode).text.toString()
        if (code.length == 8) {
            val url = getString(R.string.site)+"/api/registration/confirm"
            var formBody : RequestBody = FormBody.Builder().add("code", code).build()
            var request: Request = Request.Builder().url(url).post(formBody).build()
            val client = OkHttpClient()
            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call?, e: IOException?) { throw e!! }

                override fun onResponse(call: Call?, response: Response) {
                    val gson = GsonBuilder().create()
                    val json = JSONObject(response.body()?.string())
                    val resp = gson.fromJson(json.toString(), ApiResponse::class.java)
                    runOnUiThread {
                        if (resp.response.equals("success") == true) {
                            Toast.makeText(this@RegistrationCodeRedeem, "Вы успешно зарегистрировались", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this@RegistrationCodeRedeem, Authorization::class.java))
                        }
                        else { Toast.makeText(this@RegistrationCodeRedeem, "Неверный код", Toast.LENGTH_SHORT).show() }
                    }
                }

            })
        }
        else { Toast.makeText(this@RegistrationCodeRedeem, "Длина кода 8 символов", Toast.LENGTH_SHORT).show() }
    }

}