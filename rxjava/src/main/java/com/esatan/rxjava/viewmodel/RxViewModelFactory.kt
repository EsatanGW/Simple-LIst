package com.esatan.rxjava.viewmodel

import com.esatan.base.model.ApiClient
import com.esatan.base.viewmodel.BaseViewModelFactory
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory

abstract class RxViewModelFactory : BaseViewModelFactory() {
    override val apiClient: ApiClient by lazy {
        ApiClient(RxJava3CallAdapterFactory.create())
    }
}