package com.droid_app_dev.network_module

import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {

    @GET("/iranjith4/ad-assignment/db")
    suspend fun getfacilities(
    ): List<Facilities>
}