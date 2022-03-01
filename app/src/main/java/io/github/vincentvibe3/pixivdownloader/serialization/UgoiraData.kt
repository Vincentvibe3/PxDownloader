package io.github.vincentvibe3.pixivdownloader.serialization

import kotlinx.serialization.Serializable

@Serializable
data class UgoiraData (
    val error:Boolean,
    val message:String,
    val body:UgoiraDataBody?
)