package com.example.forummpt.dto

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import java.sql.Date

@Parcelize
class ThreadDTO (
    var id : Integer,
    var threadName : String,
    var threadDescription : String,
    var threadCreationTime : Date,
    var userId : Integer,
    var username : String,
    var avatar : String,
) : Parcelable