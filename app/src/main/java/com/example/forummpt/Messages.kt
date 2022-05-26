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
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.forummpt.adapters.MessageItemsAdapter
import com.example.forummpt.adapters.Preferences
import com.example.forummpt.dto.ApiResponse
import com.example.forummpt.dto.MessageAppDTO
import com.example.forummpt.dto.MessageDTO
import com.example.forummpt.dto.ThreadDTO
import com.google.android.material.button.MaterialButton
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textview.MaterialTextView
import com.google.gson.GsonBuilder
import okhttp3.*
import org.json.JSONArray
import org.json.JSONObject
import org.jsoup.Jsoup
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.sql.Date
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import kotlin.concurrent.thread


class Messages : AppCompatActivity() {
    var pagesCount : Int = 1
    var currentPage : Int = 1
    var threadDTO : ThreadDTO? = null
    var intentPage : Int = -1
    var pickerResult = 100
    var attachedFiles = mutableListOf<File>()
    var userID : String? = null
    var token : String? = null
    private var currentNightMode : Int? = null
    private var currentTheme : String? = null
    lateinit var recyclerView : RecyclerView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_messages)
        userID = Preferences(this@Messages).getUserId()
        token = Preferences(this@Messages).getAuthorizationToken().toString()
        currentTheme = Preferences(this@Messages).getTheme()
        currentNightMode = this.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        thread {
            threadDTO = getIntent().getParcelableExtra<ThreadDTO>("Thread")
            intentPage = getIntent().getIntExtra("ThreadPage", -1)
            if (intentPage != -1) { currentPage = intentPage }
            else { currentPage = 1 }
            checkTheme()
            getPagesCount()
            getMessages(currentPage)
        }
        recyclerView = findViewById<RecyclerView>(R.id.messages_recycler)
        setListeners()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && requestCode == pickerResult) {
            val inputStream = contentResolver.openInputStream(data!!.data!!)
            val tempFile = File.createTempFile("mpt", "forum")
            tempFile.deleteOnExit()
            val outputStream = FileOutputStream(tempFile)
            inputStream!!.copyTo(outputStream)
            attachedFiles.add(tempFile)
        }
    }

    fun setListeners() {
        val ivMenuThreads : ImageView = findViewById(R.id.ivMenuThreads)
        ivMenuThreads.setOnClickListener { startActivity(Intent(this, Threads::class.java)) }
        val ivMenuAccount : ImageView = findViewById(R.id.ivMenuAccount)
        ivMenuAccount.setOnClickListener { startActivity(Intent(this, Account::class.java)) }
        val ivMenuSettings : ImageView = findViewById(R.id.ivMenuSettings)
        ivMenuSettings.setOnClickListener { startActivity(Intent(this, Settings::class.java)) }
        val editMessagesSearch : TextInputEditText = findViewById(R.id.editMessagesSearch)
        val btnMessagesSearch : MaterialButton = findViewById(R.id.btnMessagesSearch)
        btnMessagesSearch.setOnClickListener { searchMessages(editMessagesSearch.text.toString()) }
        val btnMessagesPagesFirst : MaterialButton = findViewById(R.id.btnMessagesPagesFirst)
        btnMessagesPagesFirst.setOnClickListener { firstPage() }
        val btnMessagesPagesPrevious : MaterialButton = findViewById(R.id.btnMessagesPagesPrevious)
        btnMessagesPagesPrevious.setOnClickListener { previousPage() }
        val btnMessagesPagesCurrent : MaterialButton = findViewById(R.id.btnMessagesPagesCurrent)
        btnMessagesPagesCurrent.setOnClickListener { currentPage() }
        val btnMessagesPagesNext : MaterialButton = findViewById(R.id.btnMessagesPagesNext)
        btnMessagesPagesNext.setOnClickListener { nextPage() }
        val btnMessagesPagesLast : MaterialButton = findViewById(R.id.btnMessagesPagesLast)
        btnMessagesPagesLast.setOnClickListener { lastPage() }
        val btnMessagesAdd : MaterialButton = findViewById(R.id.btnMessagesAdd)
        btnMessagesAdd.setOnClickListener {
            addMessage(null)
        }
        val btnMessagesCancel : MaterialButton = findViewById(R.id.btnMessagesCancel)
        btnMessagesCancel.setOnClickListener {
            currentPage = 1
            getMessages(currentPage)
        }
    }

    fun checkTheme() {
        val activity = findViewById<ConstraintLayout>(R.id.activityMessages)
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
                if (v is MaterialButton) { v.setBackgroundColor(Color.parseColor(Preferences(this@Messages).getAccentColor())) }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getMessages(pageNumber : Int) {
        getPagesCount()
        runOnUiThread {
            val btnMessagesPagesCurrent : MaterialButton = findViewById(R.id.btnMessagesPagesCurrent)
            btnMessagesPagesCurrent.setText(currentPage.toString()+"/"+pagesCount.toString())
        }
        val client = OkHttpClient()
        var formBody : RequestBody = FormBody.Builder().add("token", token).build()
        val url = getString(R.string.site)+"/api/messages/app/thread/"+threadDTO?.id.toString()+"/page/"+pageNumber.toString()
        val request : Request = Request.Builder().url(url).post(formBody).build()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call?, e: IOException?) { throw e!! }

            override fun onResponse(call: Call?, response: Response) {
                val gson = GsonBuilder().setDateFormat("yyyy-MM-dd hh:mm:ss.S").create()
                val jsonArray = JSONArray(response.body()?.string())
                val messagesList = mutableListOf<MessageAppDTO>()
                for (i in 1..jsonArray.length()) { messagesList.add(gson.fromJson(jsonArray.get(i-1).toString(), MessageAppDTO::class.java)) }
                runOnUiThread {
                    val recyclerView = findViewById<RecyclerView>(R.id.messages_recycler)
                    recyclerView.layoutManager = LinearLayoutManager(this@Messages)
                    recyclerView.setHasFixedSize(false)
                    recyclerView.adapter = MessageItemsAdapter(this@Messages, messagesList) {
                        //TO-DO
                    }
                    val containerMessagesPageMenu : ConstraintLayout = findViewById(R.id.containerMessagesPageMenu)
                    containerMessagesPageMenu.visibility = View.VISIBLE
                }
            }

        })
    }

    fun searchMessages(str : String) {
        if (str.length > 0) {
            thread {
                val client = OkHttpClient()
                val formBody: RequestBody = FormBody.Builder().add("text", str).add("token", token).build()
                val request: Request = Request.Builder().url(getString(R.string.site)+"/api/messages/app/thread/"+threadDTO?.id.toString()+"/search").post(formBody).build()
                try {
                    val response = client.newCall(request).execute()
                    val gson = GsonBuilder().setDateFormat("yyyy-MM-dd hh:mm:ss.S").create()
                    val jsonArray = JSONArray(response.body()?.string())
                    val messagesList = mutableListOf<MessageAppDTO>()
                    for (i in 1..jsonArray.length()) { messagesList.add(gson.fromJson(jsonArray.get(i-1).toString(), MessageAppDTO::class.java)) }
                    runOnUiThread {
                        val recyclerView = findViewById<RecyclerView>(R.id.messages_recycler)
                        recyclerView.layoutManager = LinearLayoutManager(this@Messages)
                        recyclerView.setHasFixedSize(false)
                        recyclerView.adapter = MessageItemsAdapter(this@Messages, messagesList) {
                            //TO-DO
                        }
                        val containerMessagesPageMenu : ConstraintLayout = findViewById(R.id.containerMessagesPageMenu)
                        containerMessagesPageMenu.visibility = View.GONE
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
    }

    fun getPagesCount() {
        val url = getString(R.string.site)+"/api/messages/thread/"+ threadDTO?.id.toString()+"/count-pages"
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
                if (resp.response.toInt() == 0) { pagesCount = 1 }
                else { pagesCount = resp.response.toInt() }
            }

        })
    }

    fun firstPage() {
        if (currentPage != 1) {
            currentPage = 1
            getMessages(currentPage)
        }
    }

    fun previousPage() {
        if (currentPage != 1) {
            currentPage = currentPage - 1
            getMessages(currentPage)
        }
    }

    fun currentPage() {
        lateinit var builder : AlertDialog.Builder
        when (currentTheme) {
            "Светлая" -> { builder = AlertDialog.Builder(this@Messages, R.style.DialogTheme_Light) }
            "Тёмная" -> { builder = AlertDialog.Builder(this@Messages, R.style.DialogTheme_Dark) }
            "Системная" -> {
                when (currentNightMode) {
                    Configuration.UI_MODE_NIGHT_NO -> { builder = AlertDialog.Builder(this@Messages, R.style.DialogTheme_Light) }
                    Configuration.UI_MODE_NIGHT_YES -> { builder = AlertDialog.Builder(this@Messages, R.style.DialogTheme_Dark) }
                }
            }
        }
        val view = LayoutInflater.from(this@Messages).inflate(R.layout.search_page, null)
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
                getMessages(currentPage)
            }
        })
        builder.setNegativeButton("Отмена", DialogInterface.OnClickListener { dialog, which -> dialog.cancel() })
        builder.show()
    }

    fun nextPage() {
        if (currentPage != pagesCount) {
            currentPage = currentPage + 1
            getMessages(currentPage)
        }
    }

    fun lastPage() {
        if (currentPage != pagesCount) {
            currentPage = pagesCount
            getMessages(currentPage)
        }
    }

    public fun addMessage(replyId : Long?) {
        attachedFiles.clear()
        lateinit var alertadd : AlertDialog.Builder
        when (currentTheme) {
            "Светлая" -> { alertadd = AlertDialog.Builder(this@Messages, R.style.DialogTheme_Light) }
            "Тёмная" -> { alertadd = AlertDialog.Builder(this@Messages, R.style.DialogTheme_Dark) }
            "Системная" -> {
                when (currentNightMode) {
                    Configuration.UI_MODE_NIGHT_NO -> { alertadd = AlertDialog.Builder(this@Messages, R.style.DialogTheme_Light) }
                    Configuration.UI_MODE_NIGHT_YES -> { alertadd = AlertDialog.Builder(this@Messages, R.style.DialogTheme_Dark) }
                }
            }
        }
        val view: View = LayoutInflater.from(this@Messages).inflate(R.layout.message_add, null)
        val text = view.findViewById<TextInputEditText>(R.id.message_add_text)
        val attach = view.findViewById<MaterialButton>(R.id.message_add_attach)
        attach.setBackgroundColor(Color.parseColor(Preferences(this@Messages).getAccentColor()))
        when (currentTheme) {
            "Светлая" -> {
                view.setBackgroundColor(Color.parseColor("#FFFFFF"))
                text.setTextColor(Color.parseColor("#000000"))
            }
            "Тёмная" -> {
                view.setBackgroundColor(Color.parseColor("#000000"))
                text.setTextColor(Color.parseColor("#FFFFFF"))
            }
            "Системная" -> {
                when (currentNightMode) {
                    Configuration.UI_MODE_NIGHT_NO -> {
                        view.setBackgroundColor(Color.parseColor("#FFFFFF"))
                        text.setTextColor(Color.parseColor("#000000"))
                    }
                    Configuration.UI_MODE_NIGHT_YES -> {
                        view.setBackgroundColor(Color.parseColor("#000000"))
                        text.setTextColor(Color.parseColor("#FFFFFF"))
                    }
                }
            }
        }
        attach.setOnClickListener {
            var intent = Intent(Intent.ACTION_PICK)
            intent.setType("image/*")
            startActivityForResult(intent, pickerResult)
        }
        alertadd.setView(view)
        alertadd.setPositiveButton("Отправить", DialogInterface.OnClickListener { dialog, which ->
            if (text.text?.equals("") == false) {
                // 1. Отправить сообщение, считать ответ (id)
                var messageId : Int = -1
                var msgText = text.text.toString()
                var msgTime = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.S").withZone(ZoneOffset.UTC).format(Instant.now()).toString()
                var msgThread = threadDTO?.id.toString()
                var msgAuthor = userID
                var msgReply : String?
                if (replyId != null) { msgReply = replyId.toString() }
                else { msgReply = "" }

                var formBody: RequestBody = FormBody.Builder().add("text", msgText).add("datetime", msgTime).add("threadId", msgThread)
                    .add("userId", msgAuthor).add("replyId", msgReply).add("token", token).build()
                var request: Request = Request.Builder().url(getString(R.string.site)+"/api/messages/post").post(formBody).build()
                thread {
                    val client = OkHttpClient()
                    client.newCall(request).enqueue(object : Callback {
                        override fun onFailure(call: Call?, e: IOException?) {
                            val a = e
                            throw e!!
                        }

                        override fun onResponse(call: Call?, response: Response) {
                            val gson = GsonBuilder().setDateFormat("yyyy-MM-dd hh:mm:ss.S").create()
                            val json = JSONObject(response.body()?.string())
                            val message : MessageDTO = gson.fromJson(json.toString(), MessageDTO::class.java)
                            messageId = message.id.toInt()
                            for (i in 1..attachedFiles.size) {
                                if (messageId != -1) {
                                    val file = attachedFiles.get(i-1)
                                    UploadUtility.uploadFile(file, messageId, getString(R.string.site), token)
                                }
                            }
                            getPagesCount()
                            currentPage = pagesCount
                            getMessages(currentPage)
                            runOnUiThread { recyclerView.scrollToPosition(recyclerView.childCount) }
                        }

                    })
                }
            }
        })
        alertadd.setNegativeButton("Отмена", DialogInterface.OnClickListener { dialog, which -> dialog.cancel() })
        alertadd.show()
    }

    fun globalBanUser(messageId : Long) {
        lateinit var alertadd : AlertDialog.Builder
        val view: View = LayoutInflater.from(this@Messages).inflate(R.layout.global_ban, null)
        val chGlobalBanAuth = view.findViewById<MaterialCheckBox>(R.id.chGlobalBanAuth)
        val chGlobalBanWrite = view.findViewById<MaterialCheckBox>(R.id.chGlobalBanWrite)
        val editGlobalBanDate = view.findViewById<TextInputEditText>(R.id.editGlobalBanDate)
        val tvGlobalBanDate = view.findViewById<MaterialTextView>(R.id.tvGlobalBanDate)
        when (currentTheme) {
            "Светлая" -> {
                alertadd = AlertDialog.Builder(this@Messages, R.style.DialogTheme_Light)
                chGlobalBanAuth.setTextColor(Color.parseColor("#000000"))
                chGlobalBanWrite.setTextColor(Color.parseColor("#000000"))
                editGlobalBanDate.setTextColor(Color.parseColor("#000000"))
                tvGlobalBanDate.setTextColor(Color.parseColor("#000000"))
            }
            "Тёмная" -> {
                alertadd = AlertDialog.Builder(this@Messages, R.style.DialogTheme_Dark)
                chGlobalBanAuth.setTextColor(Color.parseColor("#FFFFFF"))
                chGlobalBanWrite.setTextColor(Color.parseColor("#FFFFFF"))
                editGlobalBanDate.setTextColor(Color.parseColor("#FFFFFF"))
                tvGlobalBanDate.setTextColor(Color.parseColor("#FFFFFF"))
            }
            "Системная" -> {
                when (currentNightMode) {
                    Configuration.UI_MODE_NIGHT_NO -> {
                        alertadd = AlertDialog.Builder(this@Messages, R.style.DialogTheme_Light)
                        chGlobalBanAuth.setTextColor(Color.parseColor("#000000"))
                        chGlobalBanWrite.setTextColor(Color.parseColor("#000000"))
                        editGlobalBanDate.setTextColor(Color.parseColor("#000000"))
                        tvGlobalBanDate.setTextColor(Color.parseColor("#000000"))
                    }
                    Configuration.UI_MODE_NIGHT_YES -> {
                        alertadd = AlertDialog.Builder(this@Messages, R.style.DialogTheme_Dark)
                        chGlobalBanAuth.setTextColor(Color.parseColor("#FFFFFF"))
                        chGlobalBanWrite.setTextColor(Color.parseColor("#FFFFFF"))
                        editGlobalBanDate.setTextColor(Color.parseColor("#FFFFFF"))
                        tvGlobalBanDate.setTextColor(Color.parseColor("#FFFFFF"))
                    }
                }
            }
        }
        alertadd.setView(view)
        alertadd.setPositiveButton("Отправить", DialogInterface.OnClickListener { dialog, which ->
            val date = editGlobalBanDate.text.toString()
            if (date.length == 10 && date[4].toString().equals("-") && date[7].toString().equals("-") && (chGlobalBanAuth.isChecked || chGlobalBanWrite.isChecked)) {
                val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
                val prefs = Preferences(this@Messages)
                val start : String = SimpleDateFormat("yyyy-MM-dd").format(java.util.Date())
                val end = editGlobalBanDate.text.toString()
                val loginBan : Boolean = chGlobalBanAuth.isChecked
                val writeBan : Boolean = chGlobalBanWrite.isChecked
                val token = prefs.getAuthorizationToken()
                var formBody: RequestBody = FormBody.Builder().add("token", token).add("messageId", messageId.toString()).add("start", start.toString())
                    .add("end", end.toString()).add("loginBan", loginBan.toString()).add("writeBan", writeBan.toString()).build()
                var request: Request = Request.Builder().url(getString(R.string.site)+"/api/global-ban/apply").post(formBody).build()
                thread {
                    val client = OkHttpClient()
                    client.newCall(request).enqueue(object : Callback {
                        override fun onFailure(call: Call?, e: IOException?) { throw e!! }

                        override fun onResponse(call: Call?, response: Response) {
                            val gson = GsonBuilder().create()
                            val json = JSONObject(response.body()?.string())
                            val resp = gson.fromJson(json.toString(), ApiResponse::class.java)
                            runOnUiThread {
                                if (resp.response.equals("success")) { Toast.makeText(this@Messages, "Пользователь успешно заблокирован", Toast.LENGTH_SHORT).show() }
                                else { Toast.makeText(this@Messages, "Не удалось заблокировать пользователя", Toast.LENGTH_SHORT).show() }
                            }
                        }
                    })
                }
            }
            else { Toast.makeText(this@Messages, "Правильно укажите дату и время", Toast.LENGTH_SHORT).show() }
        })
        alertadd.setNegativeButton("Отмена", DialogInterface.OnClickListener { dialog, which -> dialog.cancel() })
        alertadd.show()
    }

    fun adapterToast(text : String) { runOnUiThread { Toast.makeText(this@Messages, text, Toast.LENGTH_SHORT).show() } }

}
