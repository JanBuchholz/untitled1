package com.example.common

import kotlinx.serialization.Serializable

@Serializable
data class HealthResponse(val status: String)

@Serializable
data class HelloResponse(val message: String)

@Serializable
data class ErrorResponse(val message: String)
