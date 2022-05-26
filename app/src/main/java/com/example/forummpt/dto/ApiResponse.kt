package com.example.forummpt.dto

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class ApiResponse (
    var response : String,
) : Parcelable