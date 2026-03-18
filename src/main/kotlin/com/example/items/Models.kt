package com.example.items

import kotlinx.serialization.Serializable

@Serializable
data class Item(val id: Int, val name: String)

@Serializable
data class CreateItemRequest(val name: String)

@Serializable
data class UpdateItemRequest(val name: String)
