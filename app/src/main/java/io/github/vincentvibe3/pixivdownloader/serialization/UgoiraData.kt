package io.github.vincentvibe3.pixivdownloader.serialization

import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

@Serializable
data class UgoiraData(
    val error:Boolean,
    val message:String,
    val body:UgoiraDataBody)