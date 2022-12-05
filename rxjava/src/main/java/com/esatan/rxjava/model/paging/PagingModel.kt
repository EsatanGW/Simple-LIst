package com.esatan.rxjava.model.paging

sealed class PagingModel {
    object PageNumber : PagingModel()
    class Content<T>(val data: T): PagingModel()
}