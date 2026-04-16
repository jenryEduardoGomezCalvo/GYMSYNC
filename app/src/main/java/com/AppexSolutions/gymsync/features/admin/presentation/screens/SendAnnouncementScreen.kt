package com.AppexSolutions.gymsync.features.admin.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.AppexSolutions.gymsync.core.datastore.AnnouncementType
import com.AppexSolutions.gymsync.features.admin.presentation.viewmodels.SendAnnouncementViewModel
import com.AppexSolutions.gymsync.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SendAnnouncementScreen(
    onNavigateBack: () -> Unit,
    onOpenHistory: () -> Unit,
    viewModel: SendAnnouncementViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(state.successMessage, state.errorMessage) {
        state.successMessage?.let {
            snackbar.showSnackbar(it)
            viewModel.dismissMessages()
        }
        state.errorMessage?.let {
            snackbar.showSnackbar(it)
            viewModel.dismissMessages()
        }
    }

    Scaffold(
        containerColor = NavyBlue,
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = {
            TopAppBar(
                title = { Text("Enviar anuncio", color = TextPrimary, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, null, tint = TextPrimary)
                    }
                },
                actions = {
                    IconButton(onClick = onOpenHistory) {
                        Icon(Icons.Default.History, "Historial", tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkNavy)
            )
        }
    ) { pad ->
        Column(
            modifier = Modifier
                .padding(pad)
                .fillMaxSize()
                .background(NavyBlue)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header card
            Card(
                colors = CardDefaults.cardColors(containerColor = CardBackground),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(Icons.Default.Campaign, null, tint = ElectricBlue)
                    Column {
                        Text("Broadcast a clientes", color = TextPrimary, fontWeight = FontWeight.SemiBold)
                        Text(
                            text = if (state.isLoadingRecipients) "Cargando destinatarios…"
                            else "${state.eligibleCount} destinatario(s) con notificaciones activas",
                            color = TextSecondary,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            // Tipo
            Text("Tipo", color = TextSecondary, fontWeight = FontWeight.SemiBold)
            TypeSelector(
                selected = state.type,
                onSelect = viewModel::onTypeChange
            )

            // Título
            OutlinedTextField(
                value = state.title,
                onValueChange = viewModel::onTitleChange,
                label = { Text("Título") },
                supportingText = { Text("${state.title.length}/100", color = TextMuted) },
                isError = state.title.isNotEmpty() && !state.titleValid,
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = textFieldColors()
            )

            // Mensaje
            OutlinedTextField(
                value = state.message,
                onValueChange = viewModel::onMessageChange,
                label = { Text("Mensaje") },
                supportingText = { Text("${state.message.length}/500", color = TextMuted) },
                isError = state.message.isNotEmpty() && !state.messageValid,
                minLines = 4,
                modifier = Modifier.fillMaxWidth(),
                colors = textFieldColors()
            )

            // Preview
            if (state.title.isNotBlank() || state.message.isNotBlank()) {
                PreviewCard(
                    title = state.title.ifBlank { "(título)" },
                    message = state.message.ifBlank { "(mensaje)" },
                    type = state.type
                )
            }

            Spacer(Modifier.height(4.dp))

            // Botón enviar
            Button(
                onClick = viewModel::send,
                enabled = state.canSend,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ElectricBlue,
                    disabledContainerColor = SteelBlue
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (state.isSending) {
                    CircularProgressIndicator(
                        color = TextPrimary,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(20.dp)
                    )
                } else {
                    Icon(Icons.Default.Send, null)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "Enviar a ${state.eligibleCount} cliente(s)",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun TypeSelector(
    selected: AnnouncementType,
    onSelect: (AnnouncementType) -> Unit
) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        AnnouncementType.values().forEach { type ->
            val isSel = type == selected
            FilterChip(
                selected = isSel,
                onClick = { onSelect(type) },
                label = { Text(type.name) },
                colors = FilterChipDefaults.filterChipColors(
                    containerColor = CardBackground,
                    labelColor = TextSecondary,
                    selectedContainerColor = type.accentColor(),
                    selectedLabelColor = Color.White
                )
            )
        }
    }
}

@Composable
private fun PreviewCard(title: String, message: String, type: AnnouncementType) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MidnightBlue),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .size(10.dp)
                        .background(type.accentColor(), shape = RoundedCornerShape(5.dp))
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    type.name,
                    color = type.accentColor(),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(Modifier.height(6.dp))
            Text(title, color = TextPrimary, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(4.dp))
            Text(message, color = TextSecondary, fontSize = 13.sp)
        }
    }
}

@Composable
private fun textFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = TextPrimary,
    unfocusedTextColor = TextPrimary,
    focusedLabelColor = ElectricBlue,
    unfocusedLabelColor = TextMuted,
    focusedBorderColor = ElectricBlue,
    unfocusedBorderColor = SteelBlue,
    cursorColor = ElectricBlue,
    focusedContainerColor = CardBackground,
    unfocusedContainerColor = CardBackground
)

internal fun AnnouncementType.accentColor(): Color = when (this) {
    AnnouncementType.GENERAL -> ElectricBlue
    AnnouncementType.URGENTE -> ErrorRed
    AnnouncementType.PROMOCION -> SuccessGreen
}
