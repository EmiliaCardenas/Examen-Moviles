package com.example.examen.data.remote.api

import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ExamenApi {
    /**
     * Ejemplos
     *
     *     @GET/POST/DELETE/ETC("ruta")
     *     suspend fun nombre(
     *         @Path
     *         @Body
     *     ): nombreDto
     *
     *     @GET("workshop/{id}")
     *     suspend fun getWorkshop(
     *         @Path("id") id: String,
     *     ): WorkshopResponseDto
     *
     *     @POST("workshop/add")
     *     suspend fun addWorkshop(
     *         @Body requestBody: WorkshopDto,
     *     ): AddNewWorkshopDto
     *
     *     @GET("beneficiary/search")
     *     suspend fun searchBeneficiaries(
     *         @Query("term") searchTerm: String,
     *     ): List<BeneficiaryDto>
     *
     * */
}