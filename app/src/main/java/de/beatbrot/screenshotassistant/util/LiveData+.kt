package de.beatbrot.screenshotassistant.util

import androidx.lifecycle.MutableLiveData

fun <T> MutableLiveData<T>.init(value: T): MutableLiveData<T> {
    postValue(value)
    return this
}

fun <T> liveDataOf(value: T): MutableLiveData<T> {
    val data = MutableLiveData<T>()
    data.postValue(value)
    return data
}