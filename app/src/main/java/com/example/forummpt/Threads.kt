package com.example.forummpt

import android.content.DialogInterface
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.forummpt.adapters.Preferences
import com.example.forummpt.adapters.ThreadItemsAdapter
import com.example.forummpt.dto.ApiResponse
import com.example.forummpt.dto.ApiSessionsDTO
import com.example.forummpt.dto.ThreadDTO
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textview.MaterialTextView
import com.google.gson.GsonBuilder
import okhttp3.*
import org.json.JSONArray
import org.json.JSONObject
import org.jsoup.Jsoup
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import kotlin.concurrent.thread
import kotlin.math.absoluteValue


class Threads : AppCompatActivity() {
    var pagesCount : Int = 1
    var currentPage : Int = 1
    private var currentNightMode : Int? = null
    private var currentTheme : String? = null
    var userID : String? = null
    var token : String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_threads)
        userID = Preferences(this@Threads).getUserId()
        token = Preferences(this@Threads).getAuthorizationToken()
        currentTheme = Preferences(this@Threads).getTheme()
        currentNightMode = this.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        checkTheme()
        setListeners()
        thread {
            getPagesCount()
            getThreads(1)
        }
    }

    fun setListeners() {
        val ivMenuThreads : ImageView = findViewById(R.id.ivMenuThreads)
        ivMenuThreads.setOnClickListener {
            currentPage = 1
            getThreads(1)
        }
        val ivMenuAccount : ImageView = findViewById(R.id.ivMenuAccount)
        ivMenuAccount.setOnClickListener { startActivity(Intent(this, Account::class.java)) }
        val ivMenuSettings : ImageView = findViewById(R.id.ivMenuSettings)
        ivMenuSettings.setOnClickListener { startActivity(Intent(this, Settings::class.java)) }
        val btnThreadsPagesFirst : MaterialButton = findViewById(R.id.btnThreadsPagesFirst)
        btnThreadsPagesFirst.setOnClickListener { firstPage() }
        val btnThreadsPagesPrevious : MaterialButton = findViewById(R.id.btnThreadsPagesPrevious)
        btnThreadsPagesPrevious.setOnClickListener { previousPage() }
        val btnThreadsPagesCurrent : MaterialButton = findViewById(R.id.btnThreadsPagesCurrent)
        btnThreadsPagesCurrent.setOnClickListener { currentPage() }
        val btnThreadsPagesNext : MaterialButton = findViewById(R.id.btnThreadsPagesNext)
        btnThreadsPagesNext.setOnClickListener { nextPage() }
        val btnThreadsPagesLast : MaterialButton = findViewById(R.id.btnThreadsPagesLast)
        btnThreadsPagesLast.setOnClickListener { lastPage() }
        val btnThreadsSearch : MaterialButton = findViewById(R.id.btnThreadsSearch)
        val editThreadsSearch : TextInputEditText = findViewById(R.id.editThreadsSearch)
        btnThreadsSearch.setOnClickListener { searchThreads(editThreadsSearch.text.toString()) }
        val btnThreadsAdd : MaterialButton = findViewById(R.id.btnThreadsAdd)
        btnThreadsAdd.setOnClickListener {
            lateinit var alertadd : AlertDialog.Builder
            when (currentTheme) {
                "Светлая" -> { alertadd = AlertDialog.Builder(this@Threads, R.style.DialogTheme_Light) }
                "Тёмная" -> { alertadd = AlertDialog.Builder(this@Threads, R.style.DialogTheme_Dark) }
                "Системная" -> {
                    when (currentNightMode) {
                        Configuration.UI_MODE_NIGHT_NO -> { alertadd = AlertDialog.Builder(this@Threads, R.style.DialogTheme_Light) }
                        Configuration.UI_MODE_NIGHT_YES -> { alertadd = AlertDialog.Builder(this@Threads, R.style.DialogTheme_Dark) }
                    }
                }
            }
            val factory = LayoutInflater.from(this@Threads)
            val view: View = factory.inflate(R.layout.thread_add, null)
            val name = view.findViewById<TextInputEditText>(R.id.thread_add_name)
            val desc = view.findViewById<TextInputEditText>(R.id.thread_add_desc)
            when (currentTheme) {
                "Светлая" -> {
                    view.setBackgroundColor(Color.parseColor("#FFFFFF"))
                    name.setTextColor(Color.parseColor("#000000"))
                    desc.setTextColor(Color.parseColor("#000000"))
                }
                "Тёмная" -> {
                    view.setBackgroundColor(Color.parseColor("#000000"))
                    name.setTextColor(Color.parseColor("#FFFFFF"))
                    desc.setTextColor(Color.parseColor("#FFFFFF"))
                }
                "Системная" -> {
                    when (currentNightMode) {
                        Configuration.UI_MODE_NIGHT_NO -> {
                            view.setBackgroundColor(Color.parseColor("#FFFFFF"))
                            name.setTextColor(Color.parseColor("#000000"))
                            desc.setTextColor(Color.parseColor("#000000"))
                        }
                        Configuration.UI_MODE_NIGHT_YES -> {
                            view.setBackgroundColor(Color.parseColor("#000000"))
                            name.setTextColor(Color.parseColor("#FFFFFF"))
                            desc.setTextColor(Color.parseColor("#FFFFFF"))
                        }
                    }
                }
            }
            alertadd.setView(view)
            alertadd.setPositiveButton("Создать", DialogInterface.OnClickListener { dialog, which ->
                val tName : String = name.text.toString()
                val tDesc : String = desc.text.toString()
                val tDate : String = SimpleDateFormat("yyyy-MM-dd").format(Date())
                val userId : String = userID!!
                val client = OkHttpClient()
                val formBody: RequestBody = FormBody.Builder().add("name", tName).add("description", tDesc).add("creationTime", tDate)
                    .add("userId", userId).add("token", token).build()
                val request: Request = Request.Builder().url(getString(R.string.site)+"/api/threads/post").post(formBody).build()
                thread {
                    client.newCall(request).execute()
                    getPagesCount()
                    lastPage()
                }
            })
            alertadd.setNegativeButton("Отмена", DialogInterface.OnClickListener { dialog, which -> dialog.cancel() })
            alertadd.show()
        }
        val btnThreadsCancel : MaterialButton = findViewById(R.id.btnThreadsCancel)
        btnThreadsCancel.setOnClickListener {
            currentPage = 1
            getThreads(1)
        }
    }

    fun checkTheme() {
        val activity = findViewById<ConstraintLayout>(R.id.activityThreads)
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
            } else {
                if (v is MaterialTextView) { v.setTextColor(color) }
                if (v is TextInputEditText) { v.setTextColor(color) }
                if (v is MaterialButton) { v.setBackgroundColor(Color.parseColor(Preferences(this@Threads).getAccentColor())) }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getThreads(pageNumber : Int) {
        runOnUiThread {
            val btnThreadsPagesCurrent : MaterialButton = findViewById(R.id.btnThreadsPagesCurrent)
            btnThreadsPagesCurrent.setText(currentPage.toString()+"/"+pagesCount.toString())
        }
        val client = OkHttpClient()
        val url = getString(R.string.site)+"/api/threads/page/"+currentPage.toString()
        var formBody : RequestBody = FormBody.Builder().add("token", token).build()
        var request : Request = Request.Builder().url(url).post(formBody).build()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call?, e: IOException?) {
                throw e!!
            }

            override fun onResponse(call: Call?, response: Response) {
                val gson = GsonBuilder().setDateFormat("yyyy-MM-dd").create()
                val jsonArray = JSONArray(response.body()?.string())
                val threadList = mutableListOf<ThreadDTO>()
                for (i in 1..jsonArray.length()) { threadList.add(gson.fromJson(jsonArray.get(i-1).toString(), ThreadDTO::class.java)) }
                runOnUiThread {
                    val recyclerView = findViewById<RecyclerView>(R.id.threads_recycler)
                    recyclerView.layoutManager = LinearLayoutManager(this@Threads)
                    recyclerView.setHasFixedSize(true)
                    recyclerView.adapter = ThreadItemsAdapter(this@Threads, threadList) {
                        val intent = Intent(this@Threads, Messages::class.java)
                        intent.putExtra("Thread", it)
                        startActivity(intent)
                    }
                    val containerThreadsPageMenu : ConstraintLayout = findViewById(R.id.containerThreadsPageMenu)
                    containerThreadsPageMenu.visibility = View.VISIBLE
                }
            }

        })
    }

    fun searchThreads(str : String) {
        if (str.length > 0) {
            thread {
                val client = OkHttpClient()
                val formBody: RequestBody = FormBody.Builder().add("text", str).add("token", token).build()
                val request: Request = Request.Builder().url(getString(R.string.site)+"/api/threads/search").post(formBody).build()
                try {
                    val response = client.newCall(request).execute()
                    val gson = GsonBuilder().setDateFormat("yyyy-MM-dd").create()
                    val jsonArray = JSONArray(response.body()?.string())
                    val threadList = mutableListOf<ThreadDTO>()
                    for (i in 1..jsonArray.length()) { threadList.add(gson.fromJson(jsonArray.get(i-1).toString(), ThreadDTO::class.java)) }
                    runOnUiThread {
                        val recyclerView = findViewById<RecyclerView>(R.id.threads_recycler)
                        recyclerView.layoutManager = LinearLayoutManager(this@Threads)
                        recyclerView.setHasFixedSize(true)
                        recyclerView.adapter = ThreadItemsAdapter(this@Threads, threadList) {
                            val intent = Intent(this, Messages::class.java)
                            intent.putExtra("Thread", it)
                            startActivity(intent)
                        }
                        val containerThreadsPageMenu : ConstraintLayout = findViewById(R.id.containerThreadsPageMenu)
                        containerThreadsPageMenu.visibility = View.GONE
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
    }

    fun getPagesCount() {
        val site = getString(R.string.site)
        val url = site+"/api/threads/count-pages"
        var formBody : RequestBody = FormBody.Builder().add("token", token).build()
        var request: Request = Request.Builder().url(url).post(formBody).build()
        val client = OkHttpClient()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call?, e: IOException?) {
                throw e!!
            }

            override fun onResponse(call: Call?, response: Response) {
                val gson = GsonBuilder().create()
                val json = JSONObject(response.body()?.string())
                val resp = gson.fromJson(json.toString(), ApiResponse::class.java)
                if (resp.response == "error") {
                    Preferences(this@Threads).unauthorize(token!!)
                    startActivity(Intent(this@Threads, Authorization::class.java))
                    return
                }
                if (resp.response.toInt() == 0) { pagesCount = 1 }
                else { pagesCount = resp.response.toInt() }
            }

        })
    }

    fun firstPage() {
        if (currentPage != 1) {
            currentPage = 1
            getThreads(currentPage)
        }
    }

    fun previousPage() {
        if (currentPage != 1) {
            currentPage = currentPage - 1
            getThreads(currentPage)
        }
    }

    fun currentPage() {
        lateinit var builder : AlertDialog.Builder
        when (currentTheme) {
            "Светлая" -> { builder = AlertDialog.Builder(this@Threads, R.style.DialogTheme_Light) }
            "Тёмная" -> { builder = AlertDialog.Builder(this@Threads, R.style.DialogTheme_Dark) }
            "Системная" -> {
                when (currentNightMode) {
                    Configuration.UI_MODE_NIGHT_NO -> { builder = AlertDialog.Builder(this@Threads, R.style.DialogTheme_Light) }
                    Configuration.UI_MODE_NIGHT_YES -> { builder = AlertDialog.Builder(this@Threads, R.style.DialogTheme_Dark) }
                }
            }
        }
        val view = LayoutInflater.from(this@Threads).inflate(R.layout.search_page, null)
        val msg = view.findViewById<MaterialTextView>(R.id.search_page_tv)
        val input = view.findViewById<TextInputEditText>(R.id.search_page_edit)
        input.setHint("1-"+pagesCount.toString())
        input.inputType = InputType.TYPE_CLASS_NUMBER
        when (currentTheme) {
            "Светлая" -> {
                view.setBackgroundColor(Color.parseColor("#FFFFFF"))
                msg.setTextColor(Color.parseColor("#000000"))
                input.setTextColor(Color.parseColor("#000000"))
                input.setBackgroundColor(Color.parseColor("#FFFFFF"))
            }
            "Тёмная" -> {
                view.setBackgroundColor(Color.parseColor("#000000"))
                msg.setTextColor(Color.parseColor("#FFFFFF"))
                input.setTextColor(Color.parseColor("#FFFFFF"))
                input.setBackgroundColor(Color.parseColor("#000000"))
            }
            "Системная" -> {
                when (currentNightMode) {
                    Configuration.UI_MODE_NIGHT_NO -> {
                        view.setBackgroundColor(Color.parseColor("#FFFFFF"))
                        msg.setTextColor(Color.parseColor("#000000"))
                        input.setTextColor(Color.parseColor("#000000"))
                        input.setBackgroundColor(Color.parseColor("#FFFFFF"))
                    }
                    Configuration.UI_MODE_NIGHT_YES -> {
                        view.setBackgroundColor(Color.parseColor("#000000"))
                        msg.setTextColor(Color.parseColor("#FFFFFF"))
                        input.setTextColor(Color.parseColor("#FFFFFF"))
                        input.setBackgroundColor(Color.parseColor("#000000"))
                    }
                }
            }
        }
        builder.setView(view)
        builder.setPositiveButton("Oк", DialogInterface.OnClickListener{ dialog, which ->
            var number : Int? = input.text.toString().toIntOrNull()
            if (number != null) {
                if (number >= 1 && number <= pagesCount) { currentPage = number }
                else {
                    if (number < 1) { currentPage = 1 }
                    if (number > pagesCount) { currentPage = pagesCount }
                }
                getThreads(currentPage)
            }
        })
        builder.setNegativeButton("Отмена", DialogInterface.OnClickListener { dialog, which -> dialog.cancel() })
        builder.show()
    }

    fun nextPage() {
        if (currentPage != pagesCount) {
            currentPage = currentPage + 1
            getThreads(currentPage)
        }
    }

    fun lastPage() {
        if (currentPage != pagesCount) {
            currentPage = pagesCount
            getThreads(currentPage)
        }
    }

}