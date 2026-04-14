package com.AppexSolutions.gymsync.features.admin.navigation

import kotlinx.serialization.Serializable

@Serializable object AdminDashboard
@Serializable object AdminScanner
@Serializable object AdminClients
@Serializable object AdminCreateUser
@Serializable data class AdminEditClient(val clientId: Int)
