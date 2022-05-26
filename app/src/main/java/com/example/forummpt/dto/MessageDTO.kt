package com.example.forummpt.dto

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import java.sql.Timestamp

@Parcelize
data class MessageDTO(
     var id: Long,
     val messageText: String,
     val messageDatetime: Timestamp,
     val username: String,
     val threadId: Long,
     val userId: Long,
     val repliedMessageId: Long?,
): Parcelable