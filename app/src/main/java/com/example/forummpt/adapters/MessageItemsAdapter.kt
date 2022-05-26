package com.example.forummpt.adapters

import android.content.Context
import android.content.DialogInterface
import android.content.res.Configuration
import android.graphics.Color
import android.os.Handler
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.view.ViewGroup
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.DrawableImageViewTarget
import com.example.forummpt.GlideLoad
import com.example.forummpt.Messages
import com.example.forummpt.R
import com.example.forummpt.dto.ApiResponse
import com.example.forummpt.dto.MessageAppDTO
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textview.MaterialTextView
import com.google.gson.GsonBuilder
import okhttp3.*
import org.json.JSONObject
import java.io.IOException
import java.text.SimpleDateFormat
import kotlin.concurrent.thread


class MessageItemsAdapter( private val context : Context, private val messages : List<MessageAppDTO>, val listener : (MessageAppDTO) -> Unit) : RecyclerView.Adapter<MessageItemsAdapter.ThreadViewHolder>() {

    class ThreadViewHolder(view : View) : RecyclerView.ViewHolder(view) {
        val parent = view
        val card = view.findViewById<MaterialCardView>(R.id.message_list_item)
        var currentNightMode : Int? = card.context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        var currentTheme : String? = Preferences(card.context).getTheme()
        val containerMessageReply = view.findViewById<ConstraintLayout>(R.id.containerMessageReply)
        val containerMessageImages = view.findViewById<GridLayout>(R.id.containerMessageImages)
        val containerMessageReplyImg = view.findViewById<GridLayout>(R.id.containerMessageReplyImg)
        val txt = view.findViewById<TextView>(R.id.tvMessageText)
        val date = view.findViewById<TextView>(R.id.tvMessageDate)
        val username = view.findViewById<TextView>(R.id.tvMessageUsername)
        val userImage = view.findViewById<ImageView>(R.id.ivMessageAvatar)
        val replyUsername = view.findViewById<TextView>(R.id.tvMessageReplyUsername)
        val replyTxt = view.findViewById<TextView>(R.id.tvMessageReplyText)
        val msgImage1 = view.findViewById<ImageView>(R.id.ivMessageImage1)
        val msgImage2 = view.findViewById<ImageView>(R.id.ivMessageImage2)
        val msgImage3 = view.findViewById<ImageView>(R.id.ivMessageImage3)
        val msgReplyImage1 = view.findViewById<ImageView>(R.id.ivMessageReplyImage1)
        val msgReplyImage2 = view.findViewById<ImageView>(R.id.ivMessageReplyImage2)
        val msgReplyImage3 = view.findViewById<ImageView>(R.id.ivMessageReplyImage3)
        val userId = Preferences(parent.context).getUserId()
        val userRole = Preferences(parent.context).getUserRole()
        val token = Preferences(parent.context).getAuthorizationToken()

        fun bindView(message : MessageAppDTO, listener: (MessageAppDTO) -> Unit) {
            //region Обработка_reply

            if (message.replyText != null && message.replyUsername != null) {
                // Установка заголовка и текста
                replyTxt.text = message.replyText
                replyUsername.text = "Re: "+message.replyUsername
                // Обработка интерфейса
                when (currentTheme) {
                    "Светлая" -> { containerMessageReply.background = containerMessageReply.context.getDrawable(R.drawable.black_frame) }
                    "Тёмная" -> { containerMessageReply.background = containerMessageReply.context.getDrawable(R.drawable.white_frame) }
                    "Системная" -> {
                        when (currentNightMode) {
                            Configuration.UI_MODE_NIGHT_NO -> { containerMessageReply.background = containerMessageReply.context.getDrawable(R.drawable.black_frame) }
                            Configuration.UI_MODE_NIGHT_YES -> { containerMessageReply.background = containerMessageReply.context.getDrawable(R.drawable.white_frame) }
                        }
                    }
                }
                // Загрузка изображений
                if (message.replyImages != null) {
                    var replyUrls : List<String> = message.replyImages.split(",")
                    var ivReplyList = mutableListOf<ImageView>()
                    ivReplyList.add(0, msgReplyImage1)
                    ivReplyList.add(1, msgReplyImage2)
                    ivReplyList.add(2, msgReplyImage3)
                    for (i in 0..replyUrls.size-1) {
                        var it = ivReplyList.get(i)
                        val site = it.context.getString(R.string.site)
                        var url = site + replyUrls.get(i).replace(" ", "")
                        Glide.with(it.context).load(url).into(it)
                        // Просмотр в окне
                        it.setOnClickListener {
                            val alertadd = AlertDialog.Builder(it.context)
                            val factory = LayoutInflater.from(it.context)
                            val view: View = factory.inflate(R.layout.image_zoom, null)
                            val imageView = view.findViewById<ZoomClass>(R.id.zoomedImage)
                            val target = DrawableImageViewTarget(imageView)
                            Glide.with(it.context).load(url).into(target)
                            alertadd.setView(view)
                            alertadd.setNeutralButton("Закрыть") { dlg, sumthin -> }
                            alertadd.show()
                        }
                    }
                }
                else { containerMessageReplyImg.visibility = View.GONE }
            }
            else { containerMessageReply.visibility = View.GONE }
            //endregion

            //region Обработка_основного_сообщения
            val site = userImage.context.getString(R.string.site)
            // Обработка изображений
            if (message.images != null) {
                var urls : List<String> = message.images.split(",")
                var ivList = mutableListOf<ImageView>()
                ivList.add(0, msgImage1)
                ivList.add(1, msgImage2)
                ivList.add(2, msgImage3)
                for (i in 0..urls.size-1) {
                    var it = ivList.get(i)
                    var url = site + urls.get(i).replace(" ", "")
                    Glide.with(it.context).load(url).into(it)
                    // Просмотр в окне
                    it.setOnClickListener {
                        val alertadd = AlertDialog.Builder(it.context)
                        val factory = LayoutInflater.from(it.context)
                        val view: View = factory.inflate(R.layout.image_zoom, null)
                        val imageView = view.findViewById<ZoomClass>(R.id.zoomedImage)
                        val target = DrawableImageViewTarget(imageView)
                        Glide.with(it.context).load(url).into(target)
                        alertadd.setView(view)
                        alertadd.setNeutralButton("Закрыть") { dlg, sumthin -> }
                        alertadd.show()
                    }
                }
            }
            else { containerMessageImages.visibility = View.GONE }
            // Установка основных свойств
            Glide.with(userImage.context).load(site+message.userImage).into(userImage)
            txt.text = message.messageText
            date.text = SimpleDateFormat("MM/dd/yyyy HH:mm:ss").format(message.messageDatetime)
            username.text = message.username
            itemView.setOnClickListener { listener(message) }
            //endregion

            //region Card_on_click
            card.setOnTouchListener(object : OnTouchListener {
                private val handler: Handler = Handler()
                private val runnable = Runnable {
                    if (card.isPressed) {
                        // Вызвать контекстное меню
                        // На кнопку ответить присвоить вызов функции addMessage(message.id)
                        lateinit var builder : AlertDialog.Builder
                        when (currentTheme) {
                            "Светлая" -> { builder = AlertDialog.Builder(card.context, R.style.DialogTheme_Light) }
                            "Тёмная" -> { builder = AlertDialog.Builder(card.context, R.style.DialogTheme_Dark) }
                            "Системная" -> {
                                when (currentNightMode) {
                                    Configuration.UI_MODE_NIGHT_NO -> { builder = AlertDialog.Builder(card.context, R.style.DialogTheme_Light) }
                                    Configuration.UI_MODE_NIGHT_YES -> { builder = AlertDialog.Builder(card.context, R.style.DialogTheme_Dark) }
                                }
                            }
                        }
                        builder.setTitle("Выберите действие").setItems(R.array.messages_actions, DialogInterface.OnClickListener { dialog, which ->
                            when (which) {
                                0 -> {
                                    dialog.cancel()
                                    if (parent.context is Messages) {
                                        (parent.context as Messages).addMessage(message.id)
                                    }
                                }
                                1 -> {
                                    if (!userRole.equals("ADMIN")) {
                                        if (parent.context is Messages) { (parent.context as Messages).adapterToast("У вас недостаточно прав") }
                                        return@OnClickListener;
                                    }
                                    val url = site+"/api/message/delete"
                                    var formBody : RequestBody = FormBody.Builder().add("token", token).add("messageId", message.id.toString()).build()
                                    var request: Request = Request.Builder().url(url).post(formBody).build()
                                    thread {
                                        val client = OkHttpClient()
                                        client.newCall(request).enqueue(object : Callback {
                                            override fun onFailure(call: Call?, e: IOException?) { throw e!! }

                                            override fun onResponse(call: Call?, response: Response) {
                                                val gson = GsonBuilder().create()
                                                val json = JSONObject(response.body()?.string())
                                                val resp = gson.fromJson(json.toString(), ApiResponse::class.java)
                                                if (resp.response.equals("success")) {
                                                    if (parent.context is Messages) { (parent.context as Messages).adapterToast("Сообщение удалено") }
                                                }
                                                else {
                                                    if (parent.context is Messages) { (parent.context as Messages).adapterToast("Не удалось удалить сообщение") }
                                                }
                                            }

                                        })
                                    }
                                }
                                2 -> {
                                    val url = site+"/api/local-ban/apply"
                                    var formBody : RequestBody = FormBody.Builder().add("token", token).add("messageId", message.id.toString()).build()
                                    var request: Request = Request.Builder().url(url).post(formBody).build()
                                    thread {
                                        val client = OkHttpClient()
                                        client.newCall(request).enqueue(object : Callback {
                                            override fun onFailure(call: Call?, e: IOException?) { throw e!! }

                                            override fun onResponse(call: Call?, response: Response) {
                                                val gson = GsonBuilder().create()
                                                val json = JSONObject(response.body()?.string())
                                                val resp = gson.fromJson(json.toString(), ApiResponse::class.java)
                                                if (resp.response.equals("success")) {
                                                    if (parent.context is Messages) { (parent.context as Messages).adapterToast("Пользователь заблокирован в данном обсуждении") }
                                                }
                                                else {
                                                    if (parent.context is Messages) { (parent.context as Messages).adapterToast("Не удалось заблокировать пользователя") }
                                                }
                                            }

                                        })
                                    }
                                }
                                3 -> {
                                    val url = site+"/api/local-ban/unban"
                                    var formBody : RequestBody = FormBody.Builder().add("token", token).add("messageId", message.id.toString()).build()
                                    var request: Request = Request.Builder().url(url).post(formBody).build()
                                    thread {
                                        val client = OkHttpClient()
                                        client.newCall(request).enqueue(object : Callback {
                                            override fun onFailure(call: Call?, e: IOException?) { throw e!! }

                                            override fun onResponse(call: Call?, response: Response) {
                                                val gson = GsonBuilder().create()
                                                val json = JSONObject(response.body()?.string())
                                                val resp = gson.fromJson(json.toString(), ApiResponse::class.java)
                                                if (resp.response.equals("success")) {
                                                    if (parent.context is Messages) { (parent.context as Messages).adapterToast("Пользователь разблокирован в данном обсуждении") }
                                                }
                                                else {
                                                    if (parent.context is Messages) { (parent.context as Messages).adapterToast("Не удалось разблокировать пользователя") }
                                                }
                                            }

                                        })
                                    }
                                }
                                4 -> {
                                    if (!userRole.equals("ADMIN")) {
                                        if (parent.context is Messages) { (parent.context as Messages).adapterToast("У вас недостаточно прав") }
                                        return@OnClickListener;
                                    }
                                    else { if (parent.context is Messages) { (parent.context as Messages).globalBanUser(message.id) } }
                                }
                                5 -> {
                                    if (!userRole.equals("ADMIN")) {
                                        if (parent.context is Messages) { (parent.context as Messages).adapterToast("У вас недостаточно прав") }
                                        return@OnClickListener;
                                    }
                                    else {
                                        val url = site+"/api/global-ban/unban"
                                        var formBody : RequestBody = FormBody.Builder().add("token", token).add("messageId", message.id.toString()).build()
                                        var request: Request = Request.Builder().url(url).post(formBody).build()
                                        thread {
                                            val client = OkHttpClient()
                                            client.newCall(request).enqueue(object : Callback {
                                                override fun onFailure(call: Call?, e: IOException?) { throw e!! }

                                                override fun onResponse(call: Call?, response: Response) {
                                                    val gson = GsonBuilder().create()
                                                    val json = JSONObject(response.body()?.string())
                                                    val resp = gson.fromJson(json.toString(), ApiResponse::class.java)
                                                    if (resp.response.equals("success")) {
                                                        if (parent.context is Messages) { (parent.context as Messages).adapterToast("Пользователь успешно разблокирован") }
                                                    }
                                                    else {
                                                        if (parent.context is Messages) { (parent.context as Messages).adapterToast("Не удалось разблокировать пользователя") }
                                                    }
                                                }

                                            })
                                        }
                                    }
                                }
                            }
                        })
                        builder.create().show()
                    }
                }

                override fun onTouch(v: View, event: MotionEvent): Boolean {
                    if (event.action == MotionEvent.ACTION_DOWN) {
                        handler.postDelayed(runnable, 700)
                        card.isPressed = true
                    }
                    if (event.action == MotionEvent.ACTION_UP) {
                        if (card.isPressed) {
                            card.isPressed = false
                            handler.removeCallbacks(runnable)
                        }
                    }
                    return false
                }
            })
            //endregion

            when (currentTheme) {
                "Светлая" -> { setTextViewsGroupColor(card, Color.parseColor("#000000")) }
                "Тёмная" -> { setTextViewsGroupColor(card, Color.parseColor("#FFFFFF")) }
                "Системная" -> {
                    when (currentNightMode) {
                        Configuration.UI_MODE_NIGHT_NO -> { setTextViewsGroupColor(card, Color.parseColor("#000000")) }
                        Configuration.UI_MODE_NIGHT_YES -> { setTextViewsGroupColor(card, Color.parseColor("#FFFFFF")) }
                    }
                }
            }
            card.setStrokeColor(Color.parseColor(Preferences(card.context).getAccentColor()))

        }

        fun setTextViewsGroupColor(v: View, color : Int) {
            try {
                if (v is ViewGroup) {
                    for (i in 0 until v.childCount) { setTextViewsGroupColor(v.getChildAt(i), color) }
                } else { if (v is MaterialTextView) { v.setTextColor(color) } }
            } catch (e: Exception) { e.printStackTrace() }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ThreadViewHolder = ThreadViewHolder (
        LayoutInflater.from(context).inflate(R.layout.message_list, parent, false)
    )

    override fun getItemCount(): Int = messages.size

    override fun onBindViewHolder(holder: ThreadViewHolder, position: Int) {
        holder.bindView(messages[position], listener)
    }

}
