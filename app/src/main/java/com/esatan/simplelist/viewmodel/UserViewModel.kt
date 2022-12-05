package com.esatan.simplelist.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.paging.PagingData
import com.esatan.rxjava.model.paging.PagingModel
import com.esatan.rxjava.model.service.UserService
import com.esatan.rxjava.viewmodel.UserRxViewModel

class UserViewModel(userService: UserService) : UserRxViewModel(userService) {
    private val _errorLiveData = MutableLiveData<Throwable>()
    val errorLiveData: LiveData<Throwable> = _errorLiveData

    private val _userPagingDataLiveData = MutableLiveData<PagingData<PagingModel>>()
    val userPagingDataLiveData: LiveData<PagingData<PagingModel>> = _userPagingDataLiveData

    override fun createUserPagerSuccess(pagingData: PagingData<PagingModel>) {
        _userPagingDataLiveData.postValue(pagingData)
    }

    override fun createUserPagerError(throwable: Throwable) {
        _errorLiveData.postValue(throwable)
    }
}