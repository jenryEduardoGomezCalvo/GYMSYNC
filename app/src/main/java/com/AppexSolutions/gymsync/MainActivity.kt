package com.AppexSolutions.gymsync

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.AppexSolutions.gymsync.core.di.appContainer
import com.AppexSolutions.gymsync.features.auth.di.GymModule
import com.AppexSolutions.gymsync.features.auth.presentation.screens.LoginScreen
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
                LoginScreen(
                    factory = authModule.providerLoginviewModelFactory(),  // ✅ Pasar el Factory
                    onLoginSuccess = {
                        // ✅ Cuando el login es exitoso
                        println("✅ Login exitoso! Navegando a Home...")
                        // TODO: Navegar a la pantalla principal
                        // navController.navigate("home")
                    },
                    onRegisterClick = {
                        println("📝 Navegando a Registro...")
                        // TODO: Navegar a pantalla de registro
                        // navController.navigate("register")
                    },
                    onForgotPasswordClick = {
                        println("🔑 Navegando a Recuperar Contraseña...")
                        // TODO: Navegar a recuperar contraseña
                        // navController.navigate("forgot_password")
                    }
                )
            }
        }
    }
}