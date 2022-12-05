package com.esatan.base.model.data

import com.google.gson.annotations.SerializedName

data class ApiResult<T>(
    val page: Int,
    @SerializedName("per_page")
    val perPage: Int,
    val total: Int,
    @SerializedName("total_pages")
    val totalPages: Int,
    val data: T?
)