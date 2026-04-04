package com.AppexSolutions.gymsync.features.auth.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.AppexSolutions.gymsync.features.auth.presentation.components.ErrorDialog
import com.AppexSolutions.gymsync.features.auth.presentation.components.PasswordTextField
import com.AppexSolutions.gymsync.features.auth.presentation.components.PrimaryButton
import com.AppexSolutions.gymsync.features.auth.presentation.components.RegisterDatePickerField
import com.AppexSolutions.gymsync.features.auth.presentation.components.RegisterTextField
import com.AppexSolutions.gymsync.features.auth.presentation.viewmodels.RegisterViewModel
import com.AppexSolutions.gymsync.features.auth.presentation.viewmodels.RegisterViewModelFactory

/**
 * Pantalla de Registro
 *
 * Diseño inspirado en GymFlow (mismo estilo que LoginScreen)
 * Fondo oscuro con gradiente, inputs redondeados con iconos, botón azul
 *
 * Consume: POST /auth/register-super-admin
 */
@Composable
fun RegisterScreen(
    factory: RegisterViewModelFactory,
    onRegisterSuccess: () -> Unit = {},
    onBackClick: () -> Unit = {}
) {
    val viewModel: RegisterViewModel = viewModel(factory = factory)
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Navegar cuando el registro es exitoso
    LaunchedEffect(uiState.isRegisterSuccessful) {
        if (uiState.isRegisterSuccessful) {
            onRegisterSuccess()
            viewModel.resetRegisterSuccess()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0A1628),
                        Color(0xFF1A2332)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // ── Botón de regreso ──
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start
            ) {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF1F2937)),
                    enabled = !uiState.isLoading
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Regresar",
                        tint = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ── Título ──
            Text(
                text = "Crear cuenta",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(4.dp))

            // ── Subtítulo ──
            Text(
                text = "Únete a GymSync y empieza a gestionar.",
                fontSize = 14.sp,
                color = Color(0xFF9CA3AF),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(28.dp))

            // ── Campo: Nombres ──
            RegisterTextField(
                value = uiState.nombres,
                onValueChange = viewModel::onNombresChange,
                label = "Nombres",
                placeholder = "María",
                leadingIcon = Icons.Default.Person,
                enabled = !uiState.isLoading
            )

            Spacer(modifier = Modifier.height(16.dp))

            // ── Campo: Apellidos ──
            RegisterTextField(
                value = uiState.apellidos,
                onValueChange = viewModel::onApellidosChange,
                label = "Apellidos",
                placeholder = "Hernández Ruiz",
                leadingIcon = Icons.Default.Person,
                enabled = !uiState.isLoading
            )

            Spacer(modifier = Modifier.height(16.dp))

            // ── Campo: Email ──
            RegisterTextField(
                value = uiState.email,
                onValueChange = viewModel::onEmailChange,
                label = "Email",
                placeholder = "correo@ejemplo.com",
                leadingIcon = Icons.Default.Email,
                enabled = !uiState.isLoading,
                keyboardType = KeyboardType.Email
            )

            Spacer(modifier = Modifier.height(16.dp))

            // ── Campo: Teléfono (opcional) ──
            RegisterTextField(
                value = uiState.telefono,
                onValueChange = viewModel::onTelefonoChange,
                label = "Teléfono (opcional)",
                placeholder = "9613456789",
                leadingIcon = Icons.Default.Phone,
                enabled = !uiState.isLoading,
                keyboardType = KeyboardType.Phone
            )

            Spacer(modifier = Modifier.height(16.dp))

            // ── Campo: Fecha de nacimiento (opcional) ──
            RegisterDatePickerField(
                value = uiState.fechaNacimiento,
                onValueChange = viewModel::onFechaNacimientoChange,
                label = "Fecha de nacimiento (opcional)",
                enabled = !uiState.isLoading
            )

            Spacer(modifier = Modifier.height(16.dp))

            // ── Campo: Contraseña ──
            PasswordTextField(
                value = uiState.password,
                onValueChange = viewModel::onPasswordChange,
                label = "Contraseña",
                enabled = !uiState.isLoading
            )

            Spacer(modifier = Modifier.height(16.dp))

            // ── Campo: Confirmar contraseña ──
            PasswordTextField(
                value = uiState.confirmPassword,
                onValueChange = viewModel::onConfirmPasswordChange,
                label = "Confirmar contraseña",
                enabled = !uiState.isLoading
            )

            Spacer(modifier = Modifier.height(32.dp))

            // ── Botón: Crear cuenta ──
            PrimaryButton(
                text = if (uiState.isLoading) "Registrando..." else "Crear cuenta",
                onClick = viewModel::register,
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isLoading
            )

            Spacer(modifier = Modifier.height(32.dp))
        }

        // ── Diálogo de Error ──
        if (uiState.error != null) {
            ErrorDialog(
                message = uiState.error!!,
                onDismiss = viewModel::clearError
            )
        }

        // ── Indicador de carga ──
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
