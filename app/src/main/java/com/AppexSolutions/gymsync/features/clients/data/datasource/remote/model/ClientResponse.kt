package com.AppexSolutions.gymsync.features.clients.data.datasource.remote.model

import com.google.gson.annotations.SerializedName

data class ClientsListResponse(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("message")
    val message: String,

    @SerializedName("data")
    val data: List<ClientDto>
)

data class ClientDto(
    @SerializedName("id")
    val id: Int,

    @SerializedName("nombres")
    val nombres: String,

    @SerializedName("apellidos")
    val apellidos: String,

    @SerializedName("activo")
    val activo: Boolean
)
