package com.example.forummpt.dto

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class ApiSessionsDTO (
    var id : String?,
    var userId : String?,
    var userRole : String?,
    var token : String?,
) : Parcelable