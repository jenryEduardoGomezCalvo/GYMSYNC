package com.AppexSolutions.gymsync

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.AppexSolutions.gymsync.features.clients.presentation.screens.ClientsScreen
import com.AppexSolutions.gymsync.ui.theme.GymSyncTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContent {
            GymSyncTheme {
                ClientsScreen(
                    onAddClient = {
                        println("➕ Agregar nuevo cliente")
                        // TODO: Navegar a pantalla de agregar cliente
                    },
                    onClientClick = { clientId ->
                        println("👤 Click en cliente ID: $clientId")
                        // TODO: Navegar a detalle del cliente
                    },
                    onTabSelected = { tabIndex ->
                        println("📱 Tab seleccionado: $tabIndex")
                        // TODO: Navegar según el tab
                    }
                )
            }
        }
    }
}