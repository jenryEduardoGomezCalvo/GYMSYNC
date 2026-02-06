package com.AppexSolutions.gymsync

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import com.AppexSolutions.gymsync.core.di.appContainer
import com.AppexSolutions.gymsync.features.auth.di.GymModule
import com.AppexSolutions.gymsync.features.auth.presentation.screens.LoginScreen
import com.AppexSolutions.gymsync.features.auth.presentation.screens.RegisterScreen
import com.AppexSolutions.gymsync.ui.theme.GymSyncTheme

class MainActivity : ComponentActivity() {

    // ✅ Contenedor de dependencias
    lateinit var appContainer: appContainer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ✅ Inicializar contenedor de dependencias
        appContainer = appContainer(this)

        // ✅ Crear módulo de Auth
        val authModule = GymModule(appContainer)

        enableEdgeToEdge()
        setContent {
            GymSyncTheme {
                // Estado simple de navegación
                // TODO: Reemplazar con NavController cuando se implemente navegación completa
                var currentScreen by remember { mutableStateOf("login") }

                when (currentScreen) {
                    "login" -> {
                        LoginScreen(
                            factory = authModule.providerLoginviewModelFactory(),
                            onLoginSuccess = {
                                println("✅ Login exitoso! Navegando a Home...")
                                // TODO: Navegar a la pantalla principal
                                // currentScreen = "home"
                            },
                            onRegisterClick = {
                                println("📝 Navegando a Registro...")
                                currentScreen = "register"
                            },
                            onForgotPasswordClick = {
                                println("🔑 Navegando a Recuperar Contraseña...")
                                // TODO: Navegar a recuperar contraseña
                            }
                        )
                    }
                    "register" -> {
                        RegisterScreen(
                            factory = authModule.provideRegisterViewModelFactory(),
                            onRegisterSuccess = {
                                println("✅ Registro exitoso! Volviendo a Login...")
                                currentScreen = "login"
                            },
                            onBackClick = {
                                currentScreen = "login"
                            }
                        )
                    }
                }
            }
        }
    }
}