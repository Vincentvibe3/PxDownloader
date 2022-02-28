package io.github.vincentvibe3.pixivdownloader.serialization

import kotlinx.serialization.Serializable

@Serializable
data class UgoiraFrame(
    val file:String,
    val delay:Int
)