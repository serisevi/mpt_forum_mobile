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
import java.io.IOException

class Notifications : AppCompatActivity() {
    private var currentNightMode : Int? = null
    private var currentTheme : String? = null
    private var token : String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notifications)
        val btnNotificationClearUnread = findViewById<MaterialButton>(R.id.btnNotificationClearUnread)
        btnNotificationClearUnread.setOnClickListener { clearNotifications("unread") }
        val btnNotificationClearRead = findViewById<MaterialButton>(R.id.btnNotificationClearRead)
        btnNotificationClearRead.setOnClickListener { clearNotifications("read") }
        currentTheme = Preferences(this@Notifications).getTheme()
        token = Preferences(this@Notifications).getAuthorizationToken()
        currentNightMode = this.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        getNotifications()
        checkTheme()
    }

    fun getNotifications() {
        val lvNotificationsRead = findViewById<ListView>(R.id.lvNotificationsRead)
        val lvNotificationsUnread = findViewById<ListView>(R.id.lvNotificationsUnread)
        val client = OkHttpClient()
        var url = getString(R.string.site)+"/api/notifications/unread"
        val formBody : RequestBody = FormBody.Builder().add("token", token).build()
        var request : Request = Request.Builder().url(url).post(formBody).build()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call?, e: IOException?) { throw e!! }

            override fun onResponse(call: Call?, response: Response) {
                val gson = GsonBuilder().create()
                val jsonArray = JSONArray(response.body()?.string())
                val list = mutableListOf<String>()
                val dtoList = mutableListOf<NotificationDTO>()
                for (i in 0..jsonArray.length()-1) {
                    list.add(gson.fromJson(jsonArray.get(i).toString(), NotificationDTO::class.java).text)
                    dtoList.add(gson.fromJson(jsonArray.get(i).toString(), NotificationDTO::class.java))
                }
                runOnUiThread {
                    val adapter : ArrayAdapter<String> = ArrayAdapter<String>(this@Notifications, R.layout.simple_list_item, list)
                    lvNotificationsUnread.adapter = adapter
                    lvNotificationsUnread.setOnItemClickListener { parent, view, position, id ->
                        goTo(dtoList.get(position))
                    }
                    //
                }
            }
        })
        url = getString(R.string.site)+"/api/notifications/read"
        request = Request.Builder().url(url).post(formBody).build()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call?, e: IOException?) { throw e!! }

            override fun onResponse(call: Call?, response: Response) {
                val gson = GsonBuilder().create()
                val jsonArray = JSONArray(response.body()?.string())
                val list = mutableListOf<String>()
                val dtoList = mutableListOf<NotificationDTO>()
                for (i in 0..jsonArray.length()-1) {
                    list.add(gson.fromJson(jsonArray.get(i).toString(), NotificationDTO::class.java).text)
                    dtoList.add(gson.fromJson(jsonArray.get(i).toString(), NotificationDTO::class.java))
                }
                runOnUiThread {
                    val adapter : ArrayAdapter<String> = ArrayAdapter<String>(this@Notifications, R.layout.simple_list_item, list)
                    lvNotificationsRead.adapter = adapter
                    lvNotificationsRead.setOnItemClickListener { parent, view, position, id ->
                        goTo(dtoList.get(position))
                    }
                }
            }
        })
    }

    fun checkTheme() {
        val activity = findViewById<ConstraintLayout>(R.id.activityNotifications)
        when (currentTheme) {
            "Светлая" -> {
                activity.background = ColorDrawable(Color.parseColor("#FFFFFF"))
                setTextViewsGroupColor(activity, Color.parseColor("#000000"))
            }
            "Тёмная" -> {
                activity.background = ColorDrawable(Color.parseColor("#000000"))
                setTextViewsGroupColor(activity, Color.parseColor("#FFFFFF"))
            }
            "Системная" -> {
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
                if (v is MaterialTextView) { v.setTextColor(color) }
                if (v is MaterialButton) { v.setBackgroundColor(Color.parseColor(Preferences(this@Notifications).getAccentColor())) }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun goTo(notification : NotificationDTO) {
        var threadId : Int = -1
        var pageNumber : Int = -1
        val client = OkHttpClient()
        var url = getString(R.string.site)+"/api/notifications/go-to"
        var formBody : RequestBody = FormBody.Builder().add("token", token).add("notificationId", notification.id.toString()).build()
        var request : Request = Request.Builder().url(url).post(formBody).build()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call?, e: IOException?) { throw e!! }

            override fun onResponse(call: Call?, response: Response) {
                val gson = GsonBuilder().create()
                val json = JSONObject(response.body()?.string())
                var result = gson.fromJson(json.toString(), ApiResponse::class.java).response
                if (result.equals("error") == true) { runOnUiThread{ Toast.makeText(this@Notifications, "Не удалось перейти к сообщению", Toast.LENGTH_SHORT).show() } }
                else {
                    val list : List<String> = result.split(",")
                    goTo2(list.get(0).toInt(), list.get(1).toInt())
                }
            }
        })
    }

    fun goTo2(threadId : Int, pageNumber : Int) {
        val client = OkHttpClient()
        val url = getString(R.string.site)+"/api/threads/"+threadId
        val formBody = FormBody.Builder().add("token", token).build()
        val request = Request.Builder().url(url).post(formBody).build()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call?, e: IOException?) { throw e!! }

            override fun onResponse(call: Call?, response: Response) {
                val gson = GsonBuilder().setDateFormat("yyyy-MM-dd").create()
                val json = JSONObject(response.body()?.string())
                val thread = gson.fromJson(json.toString(), ThreadDTO::class.java)
                runOnUiThread {
                    var intent : Intent = Intent(this@Notifications, Messages::class.java)
                    intent.putExtra("Thread", thread)
                    intent.putExtra("ThreadPage", pageNumber)
                    startActivity(intent)
                }
            }
        })
    }

    fun clearNotifications(type : String) {
        val client = OkHttpClient()
        var url = getString(R.string.site)+"/api/notifications/"+type+"/clear"
        var formBody : RequestBody = FormBody.Builder().add("token", token).build()
        var request : Request = Request.Builder().url(url).post(formBody).build()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call?, e: IOException?) { throw e!! }

            override fun onResponse(call: Call?, response: Response) {
                val gson = GsonBuilder().create()
                val json = JSONObject(response.body()?.string())
                val result = gson.fromJson(json.toString(), ApiResponse::class.java)
                runOnUiThread {
                    if (result.equals("error") == true) { Toast.makeText(this@Notifications, "Не удалось очистить уведомления", Toast.LENGTH_SHORT).show() }
                    else { Toast.makeText(this@Notifications, "Уведомления очищены", Toast.LENGTH_SHORT).show() }
                    getNotifications()
                }
            }
        })
    }

}