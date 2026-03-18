package com.example.topics

import kotlinx.serialization.Serializable

@Serializable
data class Topic(val id: Int, val topic: String)
