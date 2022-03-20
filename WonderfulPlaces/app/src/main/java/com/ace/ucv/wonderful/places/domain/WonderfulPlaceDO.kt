package com.ace.ucv.wonderful.places.domain

import java.io.Serializable

data class WonderfulPlaceDO(
    val id: Int,
    val title: String,
    val image: String,
    val description: String,
    val date: String,
    val location: String,
    val latitude: Double,
    val longitude: Double
) : Serializable