package com.AppexSolutions.gymsync.features.routines.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
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
import com.AppexSolutions.gymsync.features.routines.domain.entities.RoutineHistory
import com.AppexSolutions.gymsync.features.routines.presentation.viewmodels.RoutineHistoryViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val HistBgDeep       = Color(0xFF0A1628)
private val HistBgCard       = Color(0xFF1F2937)
private val HistAccentLight  = Color(0xFF60A5FA)
private val HistTextSecondary = Color(0xFF9CA3AF)
private val HistTextMuted    = Color(0xFF6B7280)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoutineHistoryScreen(
    userId: Int,
    onBack: () -> Unit,
    viewModel: RoutineHistoryViewModel = hiltViewModel()
) {
    LaunchedEffect(userId) { viewModel.loadHistory(userId) }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Historial",
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = HistBgDeep,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        containerColor = HistBgDeep
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(HistBgDeep)
                .padding(paddingValues)
        ) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(
                        color = HistAccentLight,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                uiState.history.isEmpty() -> {
                    Text(
                        text = "Aún no has completado ninguna rutina",
                        color = HistTextSecondary,
                        fontSize = 16.sp,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(horizontal = 32.dp)
                    )
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(items = uiState.history, key = { it.id }) { entry ->
                            HistoryItem(entry = entry)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HistoryItem(entry: RoutineHistory) {
    val dateFormatter = remember {
        SimpleDateFormat("dd MMM yyyy · HH:mm", Locale.getDefault())
    }
    val formattedDate = remember(entry.completedAt) {
        dateFormatter.format(Date(entry.completedAt))
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = HistBgCard,
        shape = MaterialTheme.shapes.medium,
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.CheckCircle,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp)
            )
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = entry.routineName,
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = formattedDate,
                    style = MaterialTheme.typography.bodySmall,
                    color = HistTextMuted
                )
            }
        }
    }
}
