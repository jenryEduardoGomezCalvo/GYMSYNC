package com.AppexSolutions.gymsync.features.auth.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.AppexSolutions.gymsync.features.auth.presentation.components.LoginTextField
import com.AppexSolutions.gymsync.features.auth.presentation.components.PasswordTextField
import com.AppexSolutions.gymsync.features.auth.presentation.components.PrimaryButton
import com.AppexSolutions.gymsync.features.auth.presentation.viewmodels.GymViewModel
import com.AppexSolutions.gymsync.features.auth.presentation.viewmodels.GymLoginViewModelFactory

/**
 * Pantalla de Login
 *
 * Diseño inspirado en GymFlow
 */
@Composable
fun LoginScreen(
    factory: GymLoginViewModelFactory,
    onLoginSuccess: () -> Unit = {},
    onRegisterClick: () -> Unit = {},
    onForgotPasswordClick: () -> Unit = {}
) {
    val viewModel: GymViewModel = viewModel(factory = factory)
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Efecto para navegar cuando el login es exitoso
    LaunchedEffect(uiState.isLoginSuccessful) {
        if (uiState.isLoginSuccessful) {
            onLoginSuccess()
            viewModel.resetLoginSuccess()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0A1628),  // Azul oscuro superior
                        Color(0xFF1A2332)   // Azul oscuro inferior
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(80.dp))

            // Logo circular
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFD1D5DB)),  // Gris claro
                contentAlignment = Alignment.Center
            ) {
                // TODO: Reemplaza con tu logo
                // Image(
                //     painter = painterResource(id = R.drawable.logo_gymsync),
                //     contentDescription = "Logo GymSync",
                //     modifier = Modifier.size(80.dp),
                //     contentScale = ContentScale.Fit
                // )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Título
            Text(
                text = "GymSync",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Subtítulo
            Text(
                text = "Gestiona tu gimnasio al siguiente nivel",
                fontSize = 14.sp,
                color = Color(0xFF9CA3AF),  // Gris medio
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Campo Email
            LoginTextField(
                value = uiState.email,
                onValueChange = viewModel::onEmailChange,
                label = "Email",
                placeholder = "correo@ejemplo.com",
                enabled = !uiState.isLoading
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Campo Contraseña
            PasswordTextField(
                value = uiState.password,
                onValueChange = viewModel::onPasswordChange,
                label = "Contraseña",
                enabled = !uiState.isLoading
            )

            Spacer(modifier = Modifier.height(8.dp))

            // ¿Olvidaste tu contraseña?
            TextButton(
                onClick = onForgotPasswordClick,
                modifier = Modifier.align(Alignment.End),
                enabled = !uiState.isLoading
            ) {
                Text(
                    text = "¿Olvidaste tu contraseña?",
                    color = Color(0xFF60A5FA),  // Azul claro
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Botón Iniciar sesión
            PrimaryButton(
                text = if (uiState.isLoading) "Iniciando..." else "Iniciar sesión",
                onClick = viewModel::login,
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isLoading
            )

            Spacer(modifier = Modifier.height(24.dp))

            // ¿No tienes cuenta? Regístrate
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "¿No tienes cuenta? ",
                    color = Color(0xFF9CA3AF),
                    fontSize = 14.sp
                )
                TextButton(
                    onClick = onRegisterClick,
                    contentPadding = PaddingValues(0.dp),
                    enabled = !uiState.isLoading
                ) {
                    Text(
                        text = "Regístrate",
                        color = Color(0xFF60A5FA),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        // Diálogo de Error
        if (uiState.error != null) {
            ErrorDialog(
                message = uiState.error!!,
                onDismiss = viewModel::clearError
            )
        }

        // Indicador de carga (opcional, si quieres overlay)
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = Color(0xFF60A5FA)
                )
            }
        }
    }
}

/**
 * Diálogo de error
 */
@Composable
fun ErrorDialog(
    message: String,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Text(
                text = "⚠️",
                fontSize = 48.sp
            )
        },
        title = {
            Text(
                text = "Error",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Text(
                text = message,
                textAlign = TextAlign.Center
            )
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Aceptar")
            }
        },
        containerColor = Color(0xFF1F2937),
        titleContentColor = Color.White,
        textContentColor = Color(0xFF9CA3AF)
    )
}