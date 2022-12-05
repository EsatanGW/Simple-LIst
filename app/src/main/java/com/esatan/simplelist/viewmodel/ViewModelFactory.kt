package com.esatan.simplelist.viewmodel

import androidx.lifecycle.ViewModel
import com.esatan.rxjava.model.service.UserService
import com.esatan.rxjava.viewmodel.RxViewModelFactory

object ViewModelFactory : RxViewModelFactory() {

    private val userService: UserService by lazy {
        apiClient.create(UserService::class.java)
    }

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        when {
            modelClass.isAssignableFrom(UserViewModel::class.java) -> {
                return modelClass.getDeclaredConstructor(UserService::class.java)
                    .newInstance(userService)
            }
        }
        return super.create(modelClass)
    }
}