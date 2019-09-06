package de.beatbrot.screenshotassistant.util

import androidx.lifecycle.MutableLiveData

fun <T> liveDataOf(value: T): MutableLiveData<T> {
    val data = MutableLiveData<T>()
    data.postValue(value)
    return data
}