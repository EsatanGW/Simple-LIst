package com.esatan.base.viewmodel

import androidx.lifecycle.ViewModelProvider
import com.esatan.base.model.ApiClient

abstract class BaseViewModelFactory : ViewModelProvider.NewInstanceFactory() {
    abstract val apiClient: ApiClient
}