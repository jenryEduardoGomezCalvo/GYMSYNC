package com.AppexSolutions.gymsync.features.auth.presentation.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.AppexSolutions.gymsync.features.auth.presentation.components.EnableBiometricDialog
import com.AppexSolutions.gymsync.features.auth.presentation.components.ErrorDialog
import com.AppexSolutions.gymsync.features.auth.presentation.components.LoginTextField
import com.AppexSolutions.gymsync.features.auth.presentation.components.PasswordTextField
import com.AppexSolutions.gymsync.features.auth.presentation.components.PrimaryButton
import com.AppexSolutions.gymsync.features.auth.presentation.viewmodels.LoginViewModel
import com.AppexSolutions.gymsync.ui.theme.GradientEnd
import com.AppexSolutions.gymsync.ui.theme.GradientStart

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
                enabled = !uiState.isLoading && !uiState.biometricLoginInProgress
            )

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
                    border = ButtonDefaults.outlinedButtonBorder.copy(width = 1.dp)
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

        if (uiState.showEnableBiometricDialog) {
            EnableBiometricDialog(
                onConfirm = viewModel::confirmEnableBiometric,
                onSkip = viewModel::skipEnableBiometric
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
