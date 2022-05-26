package com.example.forummpt.adapters

import android.content.Context
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.forummpt.R
import com.example.forummpt.dto.ThreadDTO
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.textview.MaterialTextView

class ThreadItemsAdapter(
    private val context : Context,
    private val threads : List<ThreadDTO>,
    val listener : (ThreadDTO) -> Unit
) : RecyclerView.Adapter<ThreadItemsAdapter.ThreadViewHolder>() {

    class ThreadViewHolder(view : View) : RecyclerView.ViewHolder(view) {
        val item = view.findViewById<MaterialCardView>(R.id.thread_list_item)
        var currentNightMode : Int? = item.context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        var currentTheme : String? = Preferences(item.context).getTheme()
        val name = view.findViewById<TextView>(R.id.tvThreadName)
        val desc = view.findViewById<TextView>(R.id.tvThreadDesc)
        val username = view.findViewById<TextView>(R.id.tvThreadUsername)
        val date = view.findViewById<TextView>(R.id.tvThreadDate)
        val image = view.findViewById<ShapeableImageView>(R.id.ivThreadAvatar)

        fun bindView(thread : ThreadDTO, listener: (ThreadDTO) -> Unit) {
            var site = image.context.getString(R.string.site)
            Glide.with(image.context).load(site+thread.avatar).into(image)
            name.text = thread.threadName
            desc.text = thread.threadDescription
            username.text = thread.username
            date.text = thread.threadCreationTime.toString()
            itemView.setOnClickListener { listener(thread) }
            when (currentTheme) {
                "Светлая" -> {
                    setTextViewsGroupColor(item, Color.parseColor("#000000"))
                }
                "Тёмная" -> {
                    setTextViewsGroupColor(item, Color.parseColor("#FFFFFF"))
                }
                "Системная" -> {
                    when (currentNightMode) {
                        Configuration.UI_MODE_NIGHT_NO -> { setTextViewsGroupColor(item, Color.parseColor("#000000")) }
                        Configuration.UI_MODE_NIGHT_YES -> { setTextViewsGroupColor(item, Color.parseColor("#FFFFFF")) }
                    }
                }
            }
            item.setStrokeColor(Color.parseColor(Preferences(item.context).getAccentColor()))
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
        LayoutInflater.from(context).inflate(R.layout.thread_list, parent, false)
    )

    override fun getItemCount(): Int = threads.size

    override fun onBindViewHolder(holder: ThreadViewHolder, position: Int) {
        holder.bindView(threads[position], listener)
    }

}