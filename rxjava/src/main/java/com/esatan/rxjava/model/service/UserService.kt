package com.esatan.rxjava.model.service

import com.esatan.base.model.data.ApiResult
import com.esatan.base.model.data.UserData
import com.esatan.base.model.data.UserExtraData
import io.reactivex.rxjava3.core.Single
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface UserService {
    @GET("users")
    fun queryUserInformation(
        @Query("page") page: Int
    ): Single<Response<ApiResult<List<UserData>>>>

    @GET("unknown")
    fun queryUserNameColor(
        @Query("page") page: Int
    ): Single<Response<ApiResult<List<UserExtraData>>>>
}