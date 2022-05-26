package com.example.forummpt

import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.children
import com.example.forummpt.adapters.Preferences
import com.example.forummpt.dto.ApiResponse
import com.example.forummpt.dto.SpecializationDTO
import com.google.android.material.button.MaterialButton
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textview.MaterialTextView
import com.google.gson.GsonBuilder
import okhttp3.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.zip.Inflater
import kotlin.concurrent.thread


class Registration : AppCompatActivity() {
    private var currentNightMode : Int? = null
    private var currentTheme : String? = null
    private var attachedFile : File? = null
    private var pickerResult = 100
    var userID : String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registration)
        userID = Preferences(this).getUserId()
        val btnRegOk : MaterialButton = findViewById(R.id.btnRegOk)
        val btnAttech = findViewById<MaterialButton>(R.id.btnRegAttech)
        val cmbSpec : Spinner = findViewById(R.id.cmbRegSpec)
        val cmbCourse : Spinner = findViewById(R.id.cmbRegCourse)
        currentTheme = Preferences(this@Registration).getTheme()
        currentNightMode = this.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        checkTheme()
        getSpecializations()
        btnAttech.setOnClickListener {
            var intent = Intent(Intent.ACTION_PICK)
            intent.setType("image/*")
            startActivityForResult(intent, pickerResult)
        }
        btnRegOk.setOnClickListener { startActivity(Intent(this, RegistrationCodeRedeem::class.java)) }
        cmbSpec.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) { checkTheme() }
            override fun onNothingSelected(parent: AdapterView<*>?) {  }
        }
        cmbCourse.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) { checkTheme() }
            override fun onNothingSelected(parent: AdapterView<*>?) {  }
        }
        btnRegOk.setOnClickListener { register() }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && requestCode == pickerResult) {
            val inputStream = contentResolver.openInputStream(data!!.data!!)
            val tempFile = File.createTempFile("mpt", "forum")
            tempFile.deleteOnExit()
            val outputStream = FileOutputStream(tempFile)
            inputStream!!.copyTo(outputStream)
            attachedFile = tempFile
        }
    }

    fun checkTheme() {
        val activity = findViewById<ConstraintLayout>(R.id.activityRegistration)
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
                if (v is MaterialButton) { v.setBackgroundColor(Color.parseColor(Preferences(this@Registration).getAccentColor())) }
            }
        } catch (e: Exception) { e.printStackTrace() }
    }

    fun getSpecializations() {
        val client = OkHttpClient()
        var request = OkHttpRequest(client)
        val site = getString(R.string.site)
        val url = site+"/api/specializations"
        request.GET(url, object: Callback {
            override fun onResponse(call: Call?, response: Response) {
                thread {
                    val gson = GsonBuilder().create()
                    val jsonArray = JSONArray(response.body()?.string())
                    var stringArray : MutableList<String> = mutableListOf()
                    for (i in 1..jsonArray.length()) {
                        var spec : SpecializationDTO = gson.fromJson(jsonArray.get(i-1).toString(), SpecializationDTO::class.java)
                        stringArray.add(i-1, spec.specialization)
                    }
                    runOnUiThread {
                        val cmbSpec : Spinner = findViewById(R.id.cmbRegSpec)
                        val arrayAdapter = ArrayAdapter(this@Registration, R.layout.simple_spinner_item, stringArray)
                        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                        cmbSpec!!.setAdapter(arrayAdapter)
                    }
                }
            }

            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@Registration, e.toString(), Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    fun register() {
        val login = findViewById<TextInputEditText>(R.id.editRegLogin).text.toString()
        val email = findViewById<TextInputEditText>(R.id.editRegEmail).text.toString()
        val password = findViewById<TextInputEditText>(R.id.editRegPassword).text.toString()
        val lastname = findViewById<TextInputEditText>(R.id.editRegLastname).text.toString()
        val firstname = findViewById<TextInputEditText>(R.id.editRegFirstname).text.toString()
        val middlename = findViewById<TextInputEditText>(R.id.editRegMiddlename).text.toString()
        val desc = findViewById<TextInputEditText>(R.id.editRegDesc).text.toString()
        val cmbCourse = findViewById<Spinner>(R.id.cmbRegCourse)
        val cmbSpec = findViewById<Spinner>(R.id.cmbRegSpec)
        if (login.length > 0 && email.length > 0 && password.length > 0 && lastname.length > 0
            && firstname.length > 0 && middlename.length >= 0 && desc.length > 0) {
            val url = getString(R.string.site)+"/api/registration"
            var formBody : RequestBody = FormBody.Builder().add("username", login).add("email", email).add("password", password)
                .add("firstname", firstname).add("middlename", middlename).add("lastname", lastname).add("description", desc)
                .add("course", cmbCourse.selectedItem.toString()).add("specialization", cmbSpec.selectedItem.toString()).build()
            var request: Request = Request.Builder().url(url).post(formBody).build()
            val client = OkHttpClient()
            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call?, e: IOException?) { throw e!! }

                override fun onResponse(call: Call?, response: Response) {
                    val gson = GsonBuilder().create()
                    val json = JSONObject(response.body()?.string())
                    val resp = gson.fromJson(json.toString(), ApiResponse::class.java)
                    runOnUiThread {
                        if (resp.response.equals("error") == false) {
                            if (attachedFile != null) {thread{ UploadUtility.uploadAvatar(attachedFile, resp.response, getString(R.string.site)) }}
                            startActivity(Intent(this@Registration, RegistrationCodeRedeem::class.java))
                        }
                        else { Toast.makeText(this@Registration, "Логин или адрес электронной почты уже заняты", Toast.LENGTH_SHORT).show() }
                    }
                }

            })
        }
        else { Toast.makeText(this@Registration, "Заполните все поля", Toast.LENGTH_SHORT).show() }
    }

}