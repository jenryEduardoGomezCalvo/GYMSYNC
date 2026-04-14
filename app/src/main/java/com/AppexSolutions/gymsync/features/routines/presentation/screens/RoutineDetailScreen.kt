package com.AppexSolutions.gymsync.features.routines.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.ui.draw.clip
import coil.compose.AsyncImage
import coil.compose.AsyncImagePainter
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.outlined.AccessTime
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
import com.AppexSolutions.gymsync.features.routines.domain.entities.RoutineExercise
import com.AppexSolutions.gymsync.features.routines.presentation.viewmodels.RoutineDetailViewModel

private val DetailBgDeep       = Color(0xFF0A1628)
private val DetailBgCard       = Color(0xFF1F2937)
private val DetailAccent       = Color(0xFF3B82F6)
private val DetailAccentLight  = Color(0xFF60A5FA)
private val DetailTextSecondary = Color(0xFF9CA3AF)
private val DetailTextMuted    = Color(0xFF6B7280)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoutineDetailScreen(
    routineId: Int,
    userId: Int,
    onBack: () -> Unit,
    onAddExercises: (Int) -> Unit,
    viewModel: RoutineDetailViewModel = hiltViewModel()
) {
    LaunchedEffect(routineId) { viewModel.loadRoutine(routineId) }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }

    // Navigate back after showing the completion snackbar
    LaunchedEffect(uiState.completedSuccessfully) {
        if (uiState.completedSuccessfully) {
            snackbarHostState.showSnackbar("¡Rutina completada!")
            onBack()
        }
    }

    var showCompleteDialog by remember { mutableStateOf(false) }

    if (showCompleteDialog) {
        AlertDialog(
            onDismissRequest = { showCompleteDialog = false },
            title = {
                Text(
                    text = "Completar rutina",
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            },
            text = {
                Text(
                    text = "¿Marcar esta rutina como completada?",
                    color = DetailTextSecondary
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showCompleteDialog = false
                        viewModel.completeRoutine(userId)
                    }
                ) {
                    Text(
                        text = "Confirmar",
                        color = DetailAccentLight,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showCompleteDialog = false }) {
                    Text("Cancelar", color = DetailTextSecondary)
                }
            },
            containerColor = DetailBgCard,
            titleContentColor = Color.White,
            textContentColor = DetailTextSecondary
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = uiState.routine?.name ?: "Rutina",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
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
                    containerColor = DetailBgDeep,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { if (!uiState.isCompleting) showCompleteDialog = true },
                text = { Text("Terminar rutina", fontWeight = FontWeight.SemiBold) },
                icon = {
                    if (uiState.isCompleting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Completar rutina"
                        )
                    }
                },
                containerColor = DetailAccent,
                contentColor = Color.White
            )
        },
        containerColor = DetailBgDeep
    ) { paddingValues ->
        val routine = uiState.routine

        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(DetailBgDeep)
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = DetailAccentLight)
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(DetailBgDeep)
                .padding(paddingValues),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header card: day chips + notification time
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = DetailBgCard),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        // Day chips row
                        val dayLabels = mapOf(
                            1 to "L", 2 to "M", 3 to "X",
                            4 to "J", 5 to "V", 6 to "S", 7 to "D"
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            dayLabels.forEach { (dayNum, label) ->
                                val isAssigned = routine != null && dayNum in routine.days
                                SuggestionChip(
                                    onClick = {},
                                    label = {
                                        Text(
                                            text = label,
                                            fontSize = 11.sp,
                                            fontWeight = if (isAssigned) FontWeight.Bold else FontWeight.Normal
                                        )
                                    },
                                    modifier = Modifier.height(28.dp),
                                    colors = SuggestionChipDefaults.suggestionChipColors(
                                        containerColor = if (isAssigned) DetailAccent else Color(0xFF374151),
                                        labelColor = if (isAssigned) Color.White else DetailTextMuted
                                    ),
                                    border = SuggestionChipDefaults.suggestionChipBorder(
                                        enabled = true,
                                        borderColor = if (isAssigned) DetailAccent else Color(0xFF4B5563),
                                        borderWidth = 1.dp
                                    )
                                )
                            }
                        }

                        Spacer(Modifier.height(10.dp))

                        // Notification time row
                        if (routine != null) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Outlined.AccessTime,
                                    contentDescription = null,
                                    tint = DetailTextSecondary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    text = "${routine.notificationHour}:${
                                        routine.notificationMinute.toString().padStart(2, '0')
                                    }",
                                    color = DetailTextSecondary,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }
            }

            // Add exercises button row
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = { onAddExercises(routineId) }) {
                        Text(
                            text = "+ Añadir ejercicios",
                            color = DetailAccentLight,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            // Exercise list
            items(
                items = routine?.exercises ?: emptyList(),
                key = { it.id }
            ) { routineExercise ->
                RoutineExerciseItem(
                    routineExercise = routineExercise,
                    onDelete = { viewModel.removeExercise(routineExercise.id) }
                )
            }

            // Bottom padding so FAB does not overlap last card
            item { Spacer(Modifier.height(80.dp)) }
        }
    }
}

@Composable
private fun RoutineExerciseItem(
    routineExercise: RoutineExercise,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = DetailBgCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {

            // Exercise image
            val imageUrl = routineExercise.exercise.imageUrl
            if (imageUrl != null) {
                var isError by remember { mutableStateOf(false) }
                if (!isError) {
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = routineExercise.exercise.name,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF111827)),
                        onState = { state ->
                            isError = state is AsyncImagePainter.State.Error
                        }
                    )
                    Spacer(Modifier.height(10.dp))
                }
            }

            // Name + muscle group chip row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = routineExercise.exercise.name,
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )
                SuggestionChip(
                    onClick = {},
                    label = {
                        Text(
                            text = routineExercise.exercise.muscleGroup.displayName,
                            fontSize = 11.sp
                        )
                    },
                    modifier = Modifier.height(28.dp),
                    colors = SuggestionChipDefaults.suggestionChipColors(
                        containerColor = Color(0xFF374151),
                        labelColor = DetailTextSecondary
                    ),
                    border = SuggestionChipDefaults.suggestionChipBorder(
                        enabled = true,
                        borderColor = Color(0xFF4B5563),
                        borderWidth = 1.dp
                    )
                )
            }

            Spacer(Modifier.height(6.dp))

            // Sets × reps | rest
            Text(
                text = "${routineExercise.sets} series × ${routineExercise.reps} reps  |  " +
                        "Descanso: ${routineExercise.restSeconds}s",
                style = MaterialTheme.typography.bodySmall,
                color = DetailTextSecondary
            )

            Spacer(Modifier.height(4.dp))

            // Delete button row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Eliminar ejercicio",
                        tint = Color(0xFFEF4444),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
