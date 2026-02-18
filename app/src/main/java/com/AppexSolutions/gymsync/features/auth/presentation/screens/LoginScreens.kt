package com.AppexSolutions.gymsync.features.auth.presentation.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.AppexSolutions.gymsync.features.auth.presentation.components.LoginTextField
import com.AppexSolutions.gymsync.features.auth.presentation.components.PasswordTextField
import com.AppexSolutions.gymsync.features.auth.presentation.components.PrimaryButton
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.AppexSolutions.gymsync.features.auth.presentation.components.LoginTextField
import com.AppexSolutions.gymsync.features.auth.presentation.components.PasswordTextField
import com.AppexSolutions.gymsync.features.auth.presentation.components.PrimaryButton
import com.AppexSolutions.gymsync.features.auth.presentation.viewmodels.GymViewModel
import com.AppexSolutions.gymsync.features.auth.presentation.viewmodels.GymLoginViewModelFactory
import com.AppexSolutions.gymsync.features.auth.presentation.viewmodels.LoginViewModel
import com.AppexSolutions.gymsync.ui.theme.GradientStart
import com.AppexSolutions.gymsync.ui.theme.GradientEnd

// ─────────────────────────────────────────────────────────────────
// Pantalla de Login moderna con diseño profesional
// ─────────────────────────────────────────────────────────────────
@SuppressLint("ContextCastToActivity")
@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit = {},
    onForgotPasswordClick: () -> Unit = {},
    viewModel: LoginViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val activity = LocalContext.current as? FragmentActivity

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
                    colors = listOf(GradientStart, GradientEnd)
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

            // Logo moderno
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "GS",
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Título con tipografía moderna
            Text(
                "GymSync",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Subtítulo
            Text(
                "Plataforma premium para gestión de gimnasios",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Campos de formulario
            LoginTextField(
                value = uiState.email,
                onValueChange = viewModel::onEmailChange,
                label = "Correo electrónico",
                placeholder = "admin@gimnasio.com",
                enabled = !uiState.isLoading && !uiState.biometricLoginInProgress
            )

            Spacer(modifier = Modifier.height(16.dp))

            PasswordTextField(
                value = uiState.password,
                onValueChange = viewModel::onPasswordChange,
                label = "Contraseña",
                enabled = !uiState.isLoading && !uiState.biometricLoginInProgress
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Enlace de recuperación
            TextButton(
                onClick = onForgotPasswordClick,
                modifier = Modifier.align(Alignment.End),
                enabled = !uiState.isLoading
            ) {
                Text(
                    "¿Olvidaste tu contraseña?",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.secondary
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Botón principal
            PrimaryButton(
                text = if (uiState.isLoading) "Iniciando sesión..." else "Iniciar sesión",
                onClick = viewModel::login,
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isLoading && !uiState.biometricLoginInProgress
            )

            // Botón biométrico
            if (uiState.showBiometricButton) {
                Spacer(modifier = Modifier.height(20.dp))
                OutlinedButton(
                    onClick = {
                        activity?.let { viewModel.loginWithBiometric(it) }
                    },
                    enabled = !uiState.isLoading && !uiState.biometricLoginInProgress,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.secondary
                    ),
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        width = 1.dp
                    )
                ) {
                    if (uiState.biometricLoginInProgress) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = MaterialTheme.colorScheme.secondary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Filled.Fingerprint,
                            contentDescription = "Autenticación biométrica",
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Acceso biométrico",
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }
        }

        // Diálogos
        if (uiState.showEnableBiometricDialog) {
            EnableBiometricDialog(
                onConfirm = viewModel::confirmEnableBiometric,
                onSkip = viewModel::skipEnableBiometric
            )
        }

        if (uiState.error != null) {
            ErrorDialog(message = uiState.error!!, onDismiss = viewModel::clearError)
        }

        // Overlay de carga
        if (uiState.isLoading) {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.scrim),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

// Loading overlay

// ─────────────────────────────────────────────────────────────────
// Overload para compatibilidad con el sistema de navegacion manual
// (GymLoginViewModelFactory). No rompe el codigo existente.
// ─────────────────────────────────────────────────────────────────
@Composable
fun LoginScreen(
    factory: GymLoginViewModelFactory,
    onLoginSuccess: () -> Unit = {},
    onForgotPasswordClick: () -> Unit = {}
) {
    val viewModel: GymViewModel = viewModel(factory = factory)
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

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
                    colors = listOf(GradientStart, GradientEnd)
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

            // Logo moderno
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "GS",
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                "GymSync",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                "Plataforma premium para gestión de gimnasios",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(48.dp))

            LoginTextField(
                value = uiState.email,
                onValueChange = viewModel::onEmailChange,
                label = "Correo electrónico",
                placeholder = "admin@gimnasio.com",
                enabled = !uiState.isLoading
            )

            Spacer(modifier = Modifier.height(16.dp))

            PasswordTextField(
                value = uiState.password,
                onValueChange = viewModel::onPasswordChange,
                label = "Contraseña",
                enabled = !uiState.isLoading
            )

            Spacer(modifier = Modifier.height(8.dp))

            TextButton(
                onClick = onForgotPasswordClick,
                modifier = Modifier.align(Alignment.End),
                enabled = !uiState.isLoading
            ) {
                Text(
                    "¿Olvidaste tu contraseña?",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.secondary
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            PrimaryButton(
                text = if (uiState.isLoading) "Iniciando sesión..." else "Iniciar sesión",
                onClick = viewModel::login,
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isLoading
            )
        }

        if (uiState.error != null) {
            ErrorDialog(message = uiState.error!!, onDismiss = viewModel::clearError)
        }

        if (uiState.isLoading) {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.scrim),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@Composable
fun EnableBiometricDialog(onConfirm: () -> Unit, onSkip: () -> Unit) {
    AlertDialog(
        onDismissRequest = onSkip,
        icon = {
            Icon(
                imageVector = Icons.Filled.Fingerprint,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = Color(0xFF60A5FA)
            )
        },
        title = {
            Text(
                "Activar acceso con huella",
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        },
        text = {
            Text(
                "¿Deseas activar el acceso con huella dactilar o Face ID para futuros inicios de sesión?",
                textAlign = TextAlign.Center,
                color = Color(0xFF9CA3AF)
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Activar", color = Color(0xFF60A5FA), fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onSkip) {
                Text("Ahora no", color = Color(0xFF9CA3AF))
            }
        },
        containerColor = Color(0xFF1F2937),
        titleContentColor = Color.White,
        textContentColor = Color(0xFF9CA3AF)
    )
}

@Composable
fun ErrorDialog(message: String, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Text("⚠️", fontSize = 48.sp) },
        title = { Text("Error", fontWeight = FontWeight.Bold) },
        text = { Text(message, textAlign = TextAlign.Center) },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Aceptar") } },
        containerColor = Color(0xFF1F2937),
        titleContentColor = Color.White,
        textContentColor = Color(0xFF9CA3AF)
    )
}
