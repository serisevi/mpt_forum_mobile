package com.example.forummpt.dto

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class NotificationDTO (
    var id : Long,
    var text : String,
    var notificationRead : Boolean,
    var userId : Long,
    var messageId : Long,
) : Parcelable