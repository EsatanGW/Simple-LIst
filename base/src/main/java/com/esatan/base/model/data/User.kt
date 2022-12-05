package com.esatan.base.model.data

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class UserData(
    val id: Int,
    val email: String,
    @SerializedName("first_name")
    val firstName: String,
    @SerializedName("last_name")
    val lastName: String,
    @SerializedName("avatar")
    val avatarUrl: String,
    var extra: UserExtraData? = null,
    var page: PageData? = null
): Parcelable

@Parcelize
data class UserExtraData(
    val id: Int,
    val name: String,
    val year: Int,
    val color: String,
    @SerializedName("pantone_value")
    val pantoneValue: String
): Parcelable

@Parcelize
data class PageData(
    val page: Int,
    val perPage: Int,
    val total: Int,
    val totalPages: Int
): Parcelable