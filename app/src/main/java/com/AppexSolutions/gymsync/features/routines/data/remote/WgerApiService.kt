package com.AppexSolutions.gymsync.features.routines.data.remote

import com.AppexSolutions.gymsync.features.routines.data.remote.dto.WgerExerciseInfoDto
import com.AppexSolutions.gymsync.features.routines.data.remote.dto.WgerPaginatedResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface WgerApiService {
    @GET("exerciseinfo/")
    suspend fun getExercises(
        @Query("format") format: String = "json",
        @Query("language") language: Int = 4,
        @Query("limit") limit: Int = 100,
        @Query("offset") offset: Int = 0
    ): WgerPaginatedResponse<WgerExerciseInfoDto>
}
