package io.github.vincentvibe3.pixivdownloader

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.github.vincentvibe3.pixivdownloader.utils.checkCookies
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AppViewModel: ViewModel() {

    val loginStatus = MutableLiveData(checkCookies())

    suspend fun startDownload(id:String){
        withContext(Dispatchers.IO){

        }
    }

}