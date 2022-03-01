package io.github.vincentvibe3.pixivdownloader

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class AppViewModel: ViewModel() {

    val loginStatus = MutableLiveData(false)
}