package de.beatbrot.screenshotassistant.util

import androidx.lifecycle.MutableLiveData

fun <T> liveDataOf(value: T) = MutableLiveData<T>().apply {
    this.value = value
}
