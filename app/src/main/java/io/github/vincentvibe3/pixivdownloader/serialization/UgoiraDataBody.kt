package io.github.vincentvibe3.pixivdownloader.serialization

import kotlinx.serialization.Serializable

@Serializable
data class UgoiraDataBody(
    val src:String,
    val originalSrc:String,
    val mime_type:String,
    val frames:List<UgoiraFrame>)