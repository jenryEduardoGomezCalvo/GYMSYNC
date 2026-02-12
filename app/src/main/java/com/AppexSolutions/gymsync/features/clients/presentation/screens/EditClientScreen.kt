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

    // Navegar de vuelta cuando se elimine o guarde
    LaunchedEffect(uiState.successMessage) {
        if (uiState.successMessage != null) {
            kotlinx.coroutines.delay(1500)
            onNavigateBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Editar Cliente",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF0A1628),
                    titleContentColor = Color.White
                )
            )
        },
        containerColor = Color(0xFF0A1628)
    ) { paddingValues ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = Color(0xFF60A5FA)
                )
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Avatar
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF1E40AF)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = Color(0xFF60A5FA),
                            modifier = Modifier.size(64.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Toca para cambiar avatar",
                        color = Color(0xFF9CA3AF),
                        fontSize = 12.sp
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    // Nombre completo
                    ClientTextField(
                        value = uiState.name,
                        onValueChange = viewModel::onNameChange,
                        label = "Nombre"
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    ClientTextField(
                        value = uiState.last_name,
                        onValueChange = viewModel::onNameChange,
                        label = "apellidos"
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Teléfono
                    ClientPhoneField(
                        value = uiState.phone,
                        onValueChange = viewModel::onPhoneChange
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Spacer(modifier = Modifier.height(16.dp))

                    // Fecha de inscripción
                    DatePickerField(
                        value = uiState.registrationDate,
                        onValueChange = viewModel::onRegistrationDateChange
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Estado
                    StatusSelector(
                        selectedStatus = uiState.status,
                        onStatusSelected = viewModel::onStatusChange
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    // Botón Guardar
                    Button(
                        onClick = viewModel::saveChanges,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        enabled = !uiState.isSaving,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF3B82F6),
                            contentColor = Color.White
                        )
                    ) {
                        if (uiState.isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = "Guardar cambios",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Botón Eliminar
                    DeleteButton(onClick = viewModel::showDeleteDialog)

                    Spacer(modifier = Modifier.height(32.dp))
                }
            }

            // Diálogo de confirmación de eliminación
            if (uiState.showDeleteDialog) {
                DeleteConfirmationDialog(
                    onConfirm = viewModel::deleteClient,
                    onDismiss = viewModel::hideDeleteDialog
                )
            }

            // Snackbar de éxito
            if (uiState.successMessage != null) {
                Snackbar(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp),
                    containerColor = Color(0xFF059669)
                ) {
                    Text(
                        text = uiState.successMessage!!,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun DeleteConfirmationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Text("⚠️", fontSize = 48.sp)
        },
        title = {
            Text(
                text = "¿Eliminar cliente?",
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        },
        text = {
            Text(
                text = "Esta acción no se puede deshacer. ¿Estás seguro de que deseas eliminar este cliente?",
                textAlign = TextAlign.Center,
                color = Color(0xFF9CA3AF)
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFEF4444)
                )
            ) {
                Text("Eliminar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = Color(0xFF9CA3AF))
            }
        },
        containerColor = Color(0xFF1A1F2E),
        titleContentColor = Color.White,
        textContentColor = Color(0xFF9CA3AF)
    )
}