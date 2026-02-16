package com.AppexSolutions.gymsync.features.clients.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.AppexSolutions.gymsync.features.clients.presentation.components.*
import com.AppexSolutions.gymsync.features.clients.presentation.viewmodels.EditClientViewModel
import com.AppexSolutions.gymsync.features.clients.presentation.viewmodels.EditClientViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditClientScreen(
    factory: EditClientViewModelFactory,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val viewModel: EditClientViewModel = viewModel(factory = factory)
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.successMessage) {
        if (uiState.successMessage != null) {
            kotlinx.coroutines.delay(1500)
            onNavigateBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Editar Usuario", fontWeight = FontWeight.Bold, fontSize = 20.sp) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF0A1628), titleContentColor = Color.White
                )
            )
        },
        containerColor = Color(0xFF0A1628)
    ) { paddingValues ->
        Box(modifier.fillMaxSize().padding(paddingValues)) {
            if (uiState.isLoading) {
                CircularProgressIndicator(Modifier.align(Alignment.Center), color = Color(0xFF60A5FA))
            } else {
                Column(
                    Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Avatar
                    Box(
                        Modifier.size(100.dp).clip(CircleShape).background(Color(0xFF1E40AF)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Person, null, tint = Color(0xFF60A5FA), modifier = Modifier.size(56.dp))
                    }
                    Spacer(Modifier.height(24.dp))

                    // Campos
                    ClientTextField(value = uiState.nombres, onValueChange = viewModel::onNombresChange, label = "Nombres")
                    Spacer(Modifier.height(16.dp))
                    ClientTextField(value = uiState.apellidos, onValueChange = viewModel::onApellidosChange, label = "Apellidos")
                    Spacer(Modifier.height(16.dp))
                    ClientTextField(value = uiState.email, onValueChange = viewModel::onEmailChange, label = "Email")
                    Spacer(Modifier.height(16.dp))
                    ClientPhoneField(value = uiState.telefono, onValueChange = viewModel::onTelefonoChange)
                    Spacer(Modifier.height(16.dp))
                    DatePickerField(value = uiState.fechaNacimiento, onValueChange = viewModel::onFechaNacimientoChange, label = "Fecha de nacimiento")
                    Spacer(Modifier.height(16.dp))

                    // Status toggle (usa PATCH toggle-active)
                    StatusSelector(
                        activo = uiState.activo,
                        onToggle = viewModel::toggleActive
                    )
                    Spacer(Modifier.height(32.dp))

                    // Guardar
                    Button(
                        onClick = viewModel::saveChanges,
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        enabled = !uiState.isSaving,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6))
                    ) {
                        if (uiState.isSaving) {
                            CircularProgressIndicator(Modifier.size(24.dp), Color.White, strokeWidth = 2.dp)
                        } else {
                            Text("Guardar cambios", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                    Spacer(Modifier.height(16.dp))

                    // Eliminar
                    DeleteButton(onClick = viewModel::showDeleteDialog)
                    Spacer(Modifier.height(32.dp))
                }
            }

            // Error snackbar
            if (uiState.error != null) {
                Snackbar(
                    Modifier.align(Alignment.BottomCenter).padding(16.dp),
                    containerColor = Color(0xFFEF4444)
                ) { Text(uiState.error!!, color = Color.White) }
            }

            // Success snackbar
            if (uiState.successMessage != null) {
                Snackbar(
                    Modifier.align(Alignment.BottomCenter).padding(16.dp),
                    containerColor = Color(0xFF059669)
                ) { Text(uiState.successMessage!!, color = Color.White) }
            }

            // Delete dialog
            if (uiState.showDeleteDialog) {
                DeleteConfirmationDialog(
                    onConfirm = viewModel::deleteClient,
                    onDismiss = viewModel::hideDeleteDialog
                )
            }
        }
    }
}

@Composable
fun DeleteConfirmationDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Text("⚠️", fontSize = 48.sp) },
        title = { Text("¿Eliminar usuario?", fontWeight = FontWeight.Bold, textAlign = TextAlign.Center) },
        text = { Text("Esta acción no se puede deshacer.", textAlign = TextAlign.Center, color = Color(0xFF9CA3AF)) },
        confirmButton = {
            Button(onClick = onConfirm, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))) {
                Text("Eliminar")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar", color = Color(0xFF9CA3AF)) } },
        containerColor = Color(0xFF1A1F2E),
        titleContentColor = Color.White
    )
}
