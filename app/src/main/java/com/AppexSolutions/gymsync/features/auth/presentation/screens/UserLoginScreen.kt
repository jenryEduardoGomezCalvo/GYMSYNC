package com.AppexSolutions.gymsync.features.auth.presentation.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.FitnessCenter
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
import com.AppexSolutions.gymsync.features.auth.presentation.components.EnableBiometricDialog
import com.AppexSolutions.gymsync.features.auth.presentation.components.ErrorDialog
import com.AppexSolutions.gymsync.features.auth.presentation.components.LoginTextField
import com.AppexSolutions.gymsync.features.auth.presentation.components.PasswordTextField
import com.AppexSolutions.gymsync.features.auth.presentation.components.PrimaryButton
import com.AppexSolutions.gymsync.features.users.presentation.viewmodels.UserLoginViewModel
import com.AppexSolutions.gymsync.ui.theme.*

@SuppressLint("ContextCastToActivity")
@Composable
fun UserLoginScreen(
    viewModel: UserLoginViewModel = hiltViewModel(),
    onUserSelected: (Int) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val activity = LocalContext.current as? FragmentActivity

    LaunchedEffect(uiState.loggedClientId) {
        uiState.loggedClientId?.let { clientId ->
            onUserSelected(clientId)
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
            Spacer(Modifier.height(80.dp))

            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF8B5CF6).copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.FitnessCenter,
                    contentDescription = null,
                    tint = Color(0xFF8B5CF6),
                    modifier = Modifier.size(48.dp)
                )
            }

            Spacer(Modifier.height(24.dp))

            Text(
                "Acceso Miembro",
                style = MaterialTheme.typography.headlineLarge,
                color = TextPrimary
            )

            Spacer(Modifier.height(8.dp))

            Text(
                "Ingresa con tu cuenta de miembro del gimnasio",
                style = MaterialTheme.typography.bodyMedium,
                color = TextMuted,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(48.dp))

            LoginTextField(
                value = uiState.email,
                onValueChange = viewModel::onEmailChange,
                label = "Correo electrónico",
                placeholder = "tu_correo@gym.com",
                enabled = !uiState.isLoading
            )

            Spacer(Modifier.height(16.dp))

            PasswordTextField(
                value = uiState.password,
                onValueChange = viewModel::onPasswordChange,
                label = "Contraseña",
                enabled = !uiState.isLoading
            )

            Spacer(Modifier.height(32.dp))

            PrimaryButton(
                text = if (uiState.isLoading) "Iniciando sesión..." else "Iniciar sesión",
                onClick = viewModel::login,
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isLoading
            )

            if (uiState.showBiometricButton) {
                Spacer(Modifier.height(16.dp))
                OutlinedButton(
                    onClick = { activity?.let { viewModel.loginWithBiometric(it) } },
                    enabled = !uiState.biometricLoginInProgress && !uiState.isLoading,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFF8B5CF6)
                    )
                ) {
                    if (uiState.biometricLoginInProgress) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        Spacer(Modifier.width(8.dp))
                    } else {
                        Icon(Icons.Default.Fingerprint, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                    }
                    Text("Entrar con huella")
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
            ErrorDialog(
                message = uiState.error!!,
                onDismiss = viewModel::clearError
            )
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
