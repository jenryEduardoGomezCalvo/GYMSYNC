package com.AppexSolutions.gymsync.features.clients.data.datasource.remote.model

import com.google.gson.annotations.SerializedName

/**
 * Respuesta de la API para un cliente
 *
 * Representa el JSON que viene del backend
 */
data class ClientResponse(
    @SerializedName("id")
    val id: Int?,

    @SerializedName("nombre")
    val nombre: String,

    @SerializedName("status")
    val status: String,

    @SerializedName("plan")
    val plan: String,

    @SerializedName("avatar_url")
    val avatarUrl: String? = null
)

/**
 * Respuesta de la API con lista de clientes
 */
data class ClientsListResponse(
    @SerializedName("data")
    val data: List<ClientResponse>
)