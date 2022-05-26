package com.example.forummpt

import android.content.DialogInterface
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.forummpt.adapters.Preferences
import com.example.forummpt.dto.*
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textview.MaterialTextView
import com.google.gson.GsonBuilder
import okhttp3.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import kotlin.concurrent.thread


class Account : AppCompatActivity() {
    private var currentNightMode : Int? = null
    private var currentTheme : String? = null
    private var attachedFile : File? = null
    private var pickerResult = 100
    var userID : String? = null
    var token : String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account)
        userID = Preferences(this@Account).getUserId()
        token = Preferences(this@Account).getAuthorizationToken()
        currentTheme = Preferences(this@Account).getTheme()
        currentNightMode = this.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        checkTheme()
        thread { loadData() }
        findViewById<MaterialButton>(R.id.btnAccountEdit).setOnClickListener { editData() }
        findViewById<MaterialButton>(R.id.btnAccountChangePassword).setOnClickListener { changePassword() }
        findViewById<ImageView>(R.id.ivMenuThreads).setOnClickListener { startActivity(Intent(this, Threads::class.java)) }
        findViewById<ImageView>(R.id.ivMenuAccount).setOnClickListener { thread { loadData() } }
        findViewById<ImageView>(R.id.ivMenuSettings).setOnClickListener { startActivity(Intent(this, Settings::class.java)) }
        findViewById<MaterialButton>(R.id.btnAccountExit).setOnClickListener {
            Preferences(this).unauthorize(token!!)
            startActivity(Intent(this, Authorization::class.java))
        }
        findViewById<MaterialButton>(R.id.btnAccountNotifications).setOnClickListener {
            startActivity(Intent(this, Notifications::class.java))
        }
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
        val activity = findViewById<ConstraintLayout>(R.id.activityAccount)
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
            if (v is ViewGroup) { for (i in 0 until v.childCount) { setTextViewsGroupColor(v.getChildAt(i), color) } }
            else {
                if (v is MaterialTextView) { v.setTextColor(color) }
                if (v is TextInputEditText) { v.setTextColor(color) }
                if (v is MaterialButton) { v.setBackgroundColor(Color.parseColor(Preferences(this@Account).getAccentColor())) }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun loadData() {
        val client = OkHttpClient()
        val userId : String = userID!!
        val site = getString(R.string.site)
        var url = site+"/api/users/app/"+userId
        var formBody : RequestBody = FormBody.Builder().add("token", token).build()
        var request : Request = Request.Builder().url(url).post(formBody).build()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call?, e: IOException?) { throw e!! }

            override fun onResponse(call: Call?, response: Response) {
                val gson = GsonBuilder().setDateFormat("dd.MM.yyyy").create()
                val json = JSONObject(response.body()?.string())
                var user : UserAppDTO = gson.fromJson(json.toString(), UserAppDTO::class.java)
                runOnUiThread {
                    findViewById<MaterialTextView>(R.id.tvAccountPreviewName).text = user.lastname+" "+user.firstname+" "+user.middlename
                    findViewById<MaterialTextView>(R.id.tvAccountPreviewUsername).text = user.username
                    findViewById<MaterialTextView>(R.id.tvAccountPreviewEmail).text = user.email
                    findViewById<MaterialTextView>(R.id.tvAccountPreviewCourse).text = user.course.toString()+" курс"
                    findViewById<MaterialTextView>(R.id.tvAccountPreviewSpec).text = user.specialization
                    findViewById<MaterialTextView>(R.id.tvAccountPreviewDesc).text = user.description
                    Glide.with(this@Account).load(site+user.avatar).diskCacheStrategy(DiskCacheStrategy.NONE)
                        .skipMemoryCache(true).into(findViewById(R.id.ivAccountAvatar))
                    findViewById<MaterialTextView>(R.id.tvStatRegDate).text = user.creationTime.toString()
                }
            }

        })
        val newUrl = site+"/api/stats/user/"+userId
        val newClient = OkHttpClient()
        val newRequest : Request = Request.Builder().url(newUrl).post(formBody).build()
        newClient.newCall(newRequest).enqueue(object : Callback {
            override fun onFailure(call: Call?, e: IOException?) { throw e!! }

            override fun onResponse(call: Call?, response: Response) {
                val gson = GsonBuilder().create()
                val json = JSONObject(response.body()?.string())
                var stats : StatsAppDTO = gson.fromJson(json.toString(), StatsAppDTO::class.java)
                runOnUiThread {
                    findViewById<MaterialTextView>(R.id.tvStatMessagesTotal).text = stats.totalMessagesCount
                    findViewById<MaterialTextView>(R.id.tvStatMessagesMonthly).text = stats.monthlyMessagesCount
                    findViewById<MaterialTextView>(R.id.tvStatThreadsCreatedTotal).text = stats.totalThreadsCreated
                    findViewById<MaterialTextView>(R.id.tvStatThreadsCreatedMonthly).text = stats.monthlyThreadsCreated
                }
            }

        })
    }

    fun editData() {
        attachedFile = null
        var userId = userID!!
        lateinit var alert : AlertDialog.Builder
        var spinnerTheme : Int? = null
        when (currentTheme) {
            "Светлая" -> {
                alert = AlertDialog.Builder(this@Account, R.style.DialogTheme_Light)
                spinnerTheme = R.layout.spinner_light
            }
            "Тёмная" -> {
                alert = AlertDialog.Builder(this@Account, R.style.DialogTheme_Dark)
                spinnerTheme = R.layout.spinner_dark
            }
            "Системная" -> {
                when (currentNightMode) {
                    Configuration.UI_MODE_NIGHT_NO -> {
                        alert = AlertDialog.Builder(this@Account, R.style.DialogTheme_Light)
                        spinnerTheme = R.layout.spinner_light
                    }
                    Configuration.UI_MODE_NIGHT_YES -> {
                        alert = AlertDialog.Builder(this@Account, R.style.DialogTheme_Dark)
                        spinnerTheme = R.layout.spinner_dark
                    }
                }
            }
        }
        var view = LayoutInflater.from(this@Account).inflate(R.layout.edit_user, null)
        var firstname = view.findViewById<TextInputEditText>(R.id.editUserEditFirstname)
        var middlename = view.findViewById<TextInputEditText>(R.id.editUserEditMiddlename)
        var lastname = view.findViewById<TextInputEditText>(R.id.editUserEditLastname)
        var desc = view.findViewById<TextInputEditText>(R.id.editUserEditDesc)
        var course = view.findViewById<Spinner>(R.id.cmbUserEditCourse)
        var arrayAdapter = ArrayAdapter(this@Account, spinnerTheme!!, resources.getStringArray(R.array.courses))
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        course.setAdapter(arrayAdapter)
        course.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) { checkTheme() }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
        var spec = view.findViewById<Spinner>(R.id.cmbUserEditSpec)
        spec.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) { checkTheme() }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
        var attech = view.findViewById<MaterialButton>(R.id.btnUserEditAttech)
        when (currentTheme) {
            "Светлая" -> {
                view.setBackgroundColor(Color.parseColor("#FFFFFF"))
                setTextViewsGroupColor(view, Color.parseColor("#000000"))
            }
            "Тёмная" -> {
                view.setBackgroundColor(Color.parseColor("#000000"))
                setTextViewsGroupColor(view, Color.parseColor("#FFFFFF"))
            }
            "Системная" -> {
                when (currentNightMode) {
                    Configuration.UI_MODE_NIGHT_NO -> {
                        view.setBackgroundColor(Color.parseColor("#FFFFFF"))
                        setTextViewsGroupColor(view, Color.parseColor("#000000"))
                    }
                    Configuration.UI_MODE_NIGHT_YES -> {
                        view.setBackgroundColor(Color.parseColor("#000000"))
                        setTextViewsGroupColor(view, Color.parseColor("#FFFFFF"))
                    }
                }
            }
        }
        //region Prepare
        firstname.setText(findViewById<MaterialTextView>(R.id.tvAccountPreviewName).text.toString().split(" ").get(1).replace(" ", ""))
        middlename.setText(findViewById<MaterialTextView>(R.id.tvAccountPreviewName).text.toString().split(" ").get(0).replace(" ", ""))
        lastname.setText(findViewById<MaterialTextView>(R.id.tvAccountPreviewName).text.toString().split(" ").get(2).replace(" ", ""))
        desc.setText(findViewById<MaterialTextView>(R.id.tvAccountPreviewDesc).text.toString())
        course.setSelection(resources.getStringArray(R.array.courses).indexOf(findViewById<MaterialTextView>(R.id.tvAccountPreviewCourse).text.toString().replace(" курс", "")))
        val client = OkHttpClient()
        var okrequest = OkHttpRequest(client)
        val site = getString(R.string.site)
        var url = site+"/api/specializations"
        okrequest.GET(url, object: Callback {
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
                        arrayAdapter = ArrayAdapter(this@Account, spinnerTheme!!, stringArray)
                        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                        spec.setAdapter(arrayAdapter)
                        spec.setSelection(stringArray.indexOf(findViewById<MaterialTextView>(R.id.tvAccountPreviewSpec).text.toString()))
                    }
                }
            }

            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread { Toast.makeText(this@Account, e.toString(), Toast.LENGTH_SHORT).show() }
            }
        })
        attech.setOnClickListener {
            var intent = Intent(Intent.ACTION_PICK)
            intent.setType("image/*")
            startActivityForResult(intent, pickerResult)
        }
        //endregion
        alert.setView(view)
        alert.setPositiveButton("Изменить", DialogInterface.OnClickListener { dialog, which ->
            url = site+"/api/users/app/put"
            var formBody: RequestBody = FormBody.Builder().add("userId", userId).add("firstname", firstname.text.toString())
                .add("middlename", middlename.text.toString()).add("lastname", lastname.text.toString()).add("description", desc.text.toString())
                .add("course", course.selectedItem.toString()).add("specialization", spec.selectedItem.toString()).add("token", token).build()
            var request = Request.Builder().url(url).put(formBody).build()
            thread {
                var response = client.newCall(request).execute()
                if (attachedFile != null) { UploadUtility.uploadAvatar(attachedFile, userId, site) }
                loadData()
            }
        })
        alert.setNegativeButton("Отмена", DialogInterface.OnClickListener { dialog, which -> dialog.cancel() })
        alert.show()
    }

    fun changePassword() {
        var userId = userID
        var alert = AlertDialog.Builder(this@Account)
        when (currentTheme) {
            "Светлая" -> { alert = AlertDialog.Builder(this@Account, R.style.DialogTheme_Light) }
            "Тёмная" -> { alert = AlertDialog.Builder(this@Account, R.style.DialogTheme_Dark) }
            "Системная" -> {
                when (currentNightMode) {
                    Configuration.UI_MODE_NIGHT_NO -> { alert = AlertDialog.Builder(this@Account, R.style.DialogTheme_Light) }
                    Configuration.UI_MODE_NIGHT_YES -> { alert = AlertDialog.Builder(this@Account, R.style.DialogTheme_Dark) }
                }
            }
        }
        var view = LayoutInflater.from(this@Account).inflate(R.layout.change_password, null)
        var oldPass = view.findViewById<TextInputEditText>(R.id.editChangePasswordOld)
        var newPass = view.findViewById<TextInputEditText>(R.id.editChangePasswordNew)
        when (currentTheme) {
            "Светлая" -> {
                view.setBackgroundColor(Color.parseColor("#FFFFFF"))
                setTextViewsGroupColor(view, Color.parseColor("#000000"))
            }
            "Тёмная" -> {
                view.setBackgroundColor(Color.parseColor("#000000"))
                setTextViewsGroupColor(view, Color.parseColor("#FFFFFF"))
            }
            "Системная" -> {
                when (currentNightMode) {
                    Configuration.UI_MODE_NIGHT_NO -> {
                        view.setBackgroundColor(Color.parseColor("#FFFFFF"))
                        setTextViewsGroupColor(view, Color.parseColor("#000000"))
                    }
                    Configuration.UI_MODE_NIGHT_YES -> {
                        view.setBackgroundColor(Color.parseColor("#000000"))
                        setTextViewsGroupColor(view, Color.parseColor("#FFFFFF"))
                    }
                }
            }
        }
        alert.setView(view)
        alert.setPositiveButton("Сменить", DialogInterface.OnClickListener { dialog, which ->
            if (oldPass.text?.equals("") == false && newPass.text?.equals("") == false) {
                val client = OkHttpClient()
                val formBody: RequestBody = FormBody.Builder().add("oldPassword", oldPass.text.toString())
                    .add("newPassword", newPass.text.toString()).add("userId", userId).add("token", token).build()
                val request: Request = Request.Builder().url(getString(R.string.site)+"/api/change-password/app").post(formBody).build()
                thread {
                    val response = client.newCall(request).execute()
                    val gson = GsonBuilder().create()
                    val json = JSONObject(response.body()?.string())
                    val result = gson.fromJson(json.toString(), ApiResponse::class.java).response
                    runOnUiThread {
                        if (result.equals("success") == true) { Toast.makeText(this@Account, "Пароль успешно сменён", Toast.LENGTH_SHORT).show() }
                        else { Toast.makeText(this@Account, "При смене пароля произошла ошибка", Toast.LENGTH_SHORT).show() }
                    }
                }
            }
        })
        alert.setNegativeButton("Отмена", DialogInterface.OnClickListener{ dialog, which -> dialog.cancel() })
        alert.show()
    }

}