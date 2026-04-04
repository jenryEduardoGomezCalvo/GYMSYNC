package com.AppexSolutions.gymsync.features.clients.presentation.screens

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.AppexSolutions.gymsync.features.clients.presentation.components.ClientPhoneField
import com.AppexSolutions.gymsync.features.clients.presentation.components.ClientTextField
import com.AppexSolutions.gymsync.features.clients.presentation.components.DatePickerField
import com.AppexSolutions.gymsync.features.clients.presentation.components.ProfileAvatarPicker
import com.AppexSolutions.gymsync.features.clients.presentation.viewmodels.CreateUserViewModel

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateUserScreen(
    viewModel: CreateUserViewModel = hiltViewModel(),
    onSuccess: () -> Unit,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.successMessage) {
        if (uiState.successMessage != null) {
            kotlinx.coroutines.delay(1200)
            onSuccess()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Crear Usuario", fontWeight = FontWeight.Bold, fontSize = 20.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
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
        Box(Modifier.fillMaxSize().padding(paddingValues)) {
            if (uiState.isLoading) {
                CircularProgressIndicator(Modifier.align(Alignment.Center), color = Color(0xFF60A5FA))
            } else {
                Column(
                    Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Avatar de perfil
                    ProfileAvatarPicker(
                        imageUri = uiState.profileImageUri,
                        onImageSelected = viewModel::onProfileImageSelected
                    )
                    Spacer(Modifier.height(24.dp))

                    // Nombres y Apellidos
                    ClientTextField(value = uiState.nombres, onValueChange = viewModel::onNombresChange, label = "Nombres *", icon = Icons.Default.Person)
                    Spacer(Modifier.height(16.dp))
                    ClientTextField(value = uiState.apellidos, onValueChange = viewModel::onApellidosChange, label = "Apellidos *", icon = Icons.Default.Person)
                    Spacer(Modifier.height(16.dp))

                    // Email
                    ClientTextField(value = uiState.email, onValueChange = viewModel::onEmailChange, label = "Email *", icon = Icons.Default.Email)
                    Spacer(Modifier.height(16.dp))

                    // Password
                    PasswordField(value = uiState.password, onValueChange = viewModel::onPasswordChange, label = "Contraseña *")
                    Spacer(Modifier.height(16.dp))
                    PasswordField(value = uiState.confirmPassword, onValueChange = viewModel::onConfirmPasswordChange, label = "Confirmar contraseña *")
                    Spacer(Modifier.height(16.dp))

                    // Teléfono
                    ClientPhoneField(value = uiState.telefono, onValueChange = viewModel::onTelefonoChange)
                    Spacer(Modifier.height(16.dp))

                    // Fecha de nacimiento
                    DatePickerField(value = uiState.fechaNacimiento, onValueChange = viewModel::onFechaNacimientoChange, label = "Fecha de nacimiento")
                    Spacer(Modifier.height(16.dp))

                    // ── Rol Dropdown ──
                    Text("Rol *", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color.White, modifier = Modifier.padding(bottom = 8.dp))
                    DropdownSelector(
                        items = uiState.roles.map { it.id to it.nombre },
                        selectedId = uiState.selectedRolId,
                        onSelected = viewModel::onRolSelected,
                        placeholder = "Selecciona un rol"
                    )
                    Spacer(Modifier.height(16.dp))

                    // ── Gym Dropdown ──
                    Text("Gimnasio", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color.White, modifier = Modifier.padding(bottom = 8.dp))
                    DropdownSelector(
                        items = uiState.gyms.map { it.id to it.nombre },
                        selectedId = uiState.selectedGymId,
                        onSelected = { viewModel.onGymSelected(it) },
                        placeholder = "Selecciona un gimnasio (opcional)"
                    )
                    Spacer(Modifier.height(32.dp))

                    // Botón crear
                    Button(
                        onClick = viewModel::createUser,
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        enabled = !uiState.isSaving,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6))
                    ) {
                        if (uiState.isSaving) {
                            CircularProgressIndicator(Modifier.size(24.dp), Color.White, strokeWidth = 2.dp)
                        } else {
                            Text("Crear Usuario", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                    Spacer(Modifier.height(32.dp))
                }
            }

            // Error snackbar
            if (uiState.error != null) {
                Snackbar(
                    Modifier.align(Alignment.BottomCenter).padding(16.dp),
                    containerColor = Color(0xFFEF4444),
                    action = {
                        TextButton(onClick = viewModel::clearError) { Text("OK", color = Color.White) }
                    }
                ) { Text(uiState.error!!, color = Color.White) }
            }

            // Success snackbar
            if (uiState.successMessage != null) {
                Snackbar(
                    Modifier.align(Alignment.BottomCenter).padding(16.dp),
                    containerColor = Color(0xFF059669)
                ) { Text(uiState.successMessage!!, color = Color.White) }
            }
        }
    }
}

// ── Reusable PasswordField ──
@Composable
private fun PasswordField(value: String, onValueChange: (String) -> Unit, label: String) {
    Column(Modifier.fillMaxWidth()) {
        Text(label, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color.White, modifier = Modifier.padding(bottom = 8.dp))
        OutlinedTextField(
            value = value, onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = { Icon(Icons.Default.Lock, null, tint = Color(0xFF9CA3AF)) },
            visualTransformation = PasswordVisualTransformation(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                focusedContainerColor = Color(0xFF1A1F2E), unfocusedContainerColor = Color(0xFF1A1F2E),
                focusedBorderColor = Color(0xFF3B82F6), unfocusedBorderColor = Color(0xFF2D3748),
                cursorColor = Color(0xFF60A5FA)
            ),
            singleLine = true
        )
    }
}

// ── Reusable Dropdown ──
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DropdownSelector(
    items: List<Pair<Int, String>>,
    selectedId: Int?,
    onSelected: (Int) -> Unit,
    placeholder: String
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedLabel = items.firstOrNull { it.first == selectedId }?.second ?: placeholder

    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
        OutlinedTextField(
            value = selectedLabel,
            onValueChange = {},
            readOnly = true,
            modifier = Modifier.fillMaxWidth().menuAnchor(),
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                focusedContainerColor = Color(0xFF1A1F2E), unfocusedContainerColor = Color(0xFF1A1F2E),
                focusedBorderColor = Color(0xFF3B82F6), unfocusedBorderColor = Color(0xFF2D3748)
            )
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            containerColor = Color(0xFF1A1F2E)
        ) {
            items.forEach { (id, label) ->
                DropdownMenuItem(
                    text = { Text(label, color = Color.White) },
                    onClick = {
                        onSelected(id)
                        expanded = false
                    }
                )
            }
        }
    }
}
