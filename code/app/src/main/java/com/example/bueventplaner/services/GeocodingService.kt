package com.example.bueventplaner.services

import retrofit2.http.GET
import retrofit2.http.Query

interface GeocodingService {
    @GET("geocode/json")
    suspend fun getGeocode(
        @Query("address") address: String,
        @Query("key") apiKey: String
    ): GeocodingResponse
}

data class GeocodingResponse(
    val results: List<GeocodingResult>
)

data class GeocodingResult(
    val geometry: Geometry
)

data class Geometry(
    val location: LatLng
)

data class LatLng(
    val lat: Double,
    val lng: Double
)

