package com.AppexSolutions.gymsync

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.AppexSolutions.gymsync.core.di.appContainer
import com.AppexSolutions.gymsync.core.navigation.AppNavigation
import com.AppexSolutions.gymsync.ui.theme.GymSyncTheme

class MainActivity : ComponentActivity() {

    lateinit var appContainer : appContainer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inicializar contenedor de dependencias
        appContainer = appContainer(this)

        enableEdgeToEdge()
        setContent {
            GymSyncTheme {
                // Sistema de navegación
                AppNavigation(appContainer = appContainer)
            }
        }
    }
}