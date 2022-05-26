package com.example.forummpt.dto

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class StatsAppDTO (
    var totalMessagesCount : String,
    var monthlyMessagesCount : String,
    var totalThreadsCreated : String,
    var monthlyThreadsCreated : String,
) : Parcelable