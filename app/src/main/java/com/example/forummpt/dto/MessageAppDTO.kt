package com.example.forummpt.dto

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import java.sql.Timestamp

@Parcelize
data class MessageAppDTO(
    var id: Long,
    val threadId: Long,
    val username: String,
    val userImage: String,
    val messageDatetime: Timestamp,
    val messageText: String,
    val replyText: String,
    val replyUsername: String,
    val images: String?,
    val replyImages: String?,
): Parcelable