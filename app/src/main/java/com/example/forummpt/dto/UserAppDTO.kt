package com.example.forummpt.dto

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import java.sql.Date

@Parcelize
data class UserAppDTO (
    var username : String,
    var email : String,
    var creationTime : Date,
    var firstname : String,
    var middlename : String,
    var lastname : String,
    var description : String,
    var avatar : String,
    var course : Int,
    var specialization : String,
) : Parcelable