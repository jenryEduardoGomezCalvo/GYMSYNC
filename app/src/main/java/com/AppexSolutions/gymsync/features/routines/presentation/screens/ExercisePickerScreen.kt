package com.AppexSolutions.gymsync.features.routines.presentation.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import coil.compose.AsyncImage
import coil.compose.AsyncImagePainter
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.AppexSolutions.gymsync.features.routines.domain.entities.Exercise
import com.AppexSolutions.gymsync.features.routines.domain.entities.MuscleGroup
import com.AppexSolutions.gymsync.features.routines.presentation.viewmodels.ExercisePickerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExercisePickerScreen(
    routineId: Int,
    onBack: () -> Unit,
    viewModel: ExercisePickerViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        // ViewModel init already handles refresh via init block
    }

    val muscleGroups = MuscleGroup.entries.toTypedArray()
    val selectedTabIndex = muscleGroups.indexOf(uiState.selectedGroup).coerceAtLeast(0)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Agregar ejercicios",
                        fontWeight = FontWeight.Bold,
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
                    containerColor = Color(0xFF0A1628),
                    titleContentColor = Color.White
                )
            )
        },
        bottomBar = {
            BottomAppBar(
                containerColor = Color(0xFF0D1B2E),
                tonalElevation = 8.dp
            ) {
                Button(
                    onClick = {
                        viewModel.confirmSelection(routineId)
                        onBack()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .height(48.dp),
                    enabled = uiState.selectedIds.isNotEmpty() && !uiState.isSaving,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF3B82F6),
                        disabledContainerColor = Color(0xFF1E3A5F)
                    )
                ) {
                    Text(
                        text = "Agregar seleccionados (${uiState.selectedIds.size})",
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                }
            }
        },
        containerColor = Color(0xFF0A1628)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            ScrollableTabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = Color(0xFF0D1B2E),
                contentColor = Color.White,
                edgePadding = 8.dp,
                indicator = { tabPositions ->
                    if (selectedTabIndex < tabPositions.size) {
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                            color = Color(0xFF3B82F6)
                        )
                    }
                },
                divider = {}
            ) {
                muscleGroups.forEachIndexed { index, group ->
                    Tab(
                        selected = index == selectedTabIndex,
                        onClick = { viewModel.selectGroup(group) },
                        text = {
                            Text(
                                text = group.displayName,
                                color = if (index == selectedTabIndex) Color(0xFF3B82F6)
                                else Color(0xFF9CA3AF)
                            )
                        }
                    )
                }
            }

            if (uiState.isRefreshing) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color(0xFF3B82F6),
                    trackColor = Color(0xFF1A1F2E)
                )
            }

            if (uiState.error != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = uiState.error!!,
                        color = Color(0xFFEF4444),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                items(uiState.exercises) { exercise ->
                    ExercisePickerItem(
                        exercise = exercise,
                        isSelected = exercise.id in uiState.selectedIds,
                        config = uiState.exerciseConfigs[exercise.id]
                            ?: ExercisePickerViewModel.ExerciseConfig(),
                        onToggle = { viewModel.toggleExercise(exercise.id) },
                        onConfigChange = { sets, reps, rest ->
                            viewModel.updateConfig(exercise.id, sets, reps, rest)
                        }
                    )
                }
                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }
}

@Composable
private fun ExercisePickerItem(
    exercise: Exercise,
    isSelected: Boolean,
    config: ExercisePickerViewModel.ExerciseConfig,
    onToggle: () -> Unit,
    onConfigChange: (sets: Int, reps: Int, restSeconds: Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = isSelected,
                onCheckedChange = { onToggle() },
                colors = CheckboxDefaults.colors(
                    checkedColor = Color(0xFF3B82F6),
                    uncheckedColor = Color(0xFF4B5563),
                    checkmarkColor = Color.White
                )
            )
            Spacer(modifier = Modifier.width(8.dp))

            // Imagen del ejercicio
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF1A1F2E)),
                contentAlignment = Alignment.Center
            ) {
                if (exercise.imageUrl != null) {
                    var isError by remember { mutableStateOf(false) }
                    if (!isError) {
                        AsyncImage(
                            model = exercise.imageUrl,
                            contentDescription = exercise.name,
                            modifier = Modifier.size(64.dp).clip(RoundedCornerShape(8.dp)),
                            onState = { state ->
                                isError = state is AsyncImagePainter.State.Error
                            }
                        )
                    }
                    if (isError) {
                        Icon(
                            imageVector = Icons.Default.FitnessCenter,
                            contentDescription = null,
                            tint = Color(0xFF4B5563),
                            modifier = Modifier.size(28.dp)
                        )
                    }
                } else {
                    Icon(
                        imageVector = Icons.Default.FitnessCenter,
                        contentDescription = null,
                        tint = Color(0xFF4B5563),
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = exercise.name,
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Medium
                )
                if (exercise.description.isNotBlank()) {
                    Text(
                        text = exercise.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF9CA3AF),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        AnimatedVisibility(
            visible = isSelected,
            enter = expandVertically(),
            exit = shrinkVertically()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 48.dp, bottom = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = if (config.sets == 0) "" else config.sets.toString(),
                    onValueChange = { raw ->
                        val parsed = raw.filter { it.isDigit() }.toIntOrNull() ?: 0
                        onConfigChange(parsed, config.reps, config.restSeconds)
                    },
                    label = { Text("Series", color = Color(0xFF9CA3AF)) },
                    modifier = Modifier.width(80.dp),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedContainerColor = Color(0xFF1A1F2E),
                        unfocusedContainerColor = Color(0xFF1A1F2E),
                        focusedBorderColor = Color(0xFF3B82F6),
                        unfocusedBorderColor = Color(0xFF2D3748),
                        cursorColor = Color(0xFF60A5FA),
                        focusedLabelColor = Color(0xFF60A5FA),
                        unfocusedLabelColor = Color(0xFF9CA3AF)
                    )
                )
                OutlinedTextField(
                    value = if (config.reps == 0) "" else config.reps.toString(),
                    onValueChange = { raw ->
                        val parsed = raw.filter { it.isDigit() }.toIntOrNull() ?: 0
                        onConfigChange(config.sets, parsed, config.restSeconds)
                    },
                    label = { Text("Reps", color = Color(0xFF9CA3AF)) },
                    modifier = Modifier.width(80.dp),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedContainerColor = Color(0xFF1A1F2E),
                        unfocusedContainerColor = Color(0xFF1A1F2E),
                        focusedBorderColor = Color(0xFF3B82F6),
                        unfocusedBorderColor = Color(0xFF2D3748),
                        cursorColor = Color(0xFF60A5FA),
                        focusedLabelColor = Color(0xFF60A5FA),
                        unfocusedLabelColor = Color(0xFF9CA3AF)
                    )
                )
                OutlinedTextField(
                    value = if (config.restSeconds == 0) "" else config.restSeconds.toString(),
                    onValueChange = { raw ->
                        val parsed = raw.filter { it.isDigit() }.toIntOrNull() ?: 0
                        onConfigChange(config.sets, config.reps, parsed)
                    },
                    label = { Text("Descanso(s)", color = Color(0xFF9CA3AF)) },
                    modifier = Modifier.width(80.dp),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedContainerColor = Color(0xFF1A1F2E),
                        unfocusedContainerColor = Color(0xFF1A1F2E),
                        focusedBorderColor = Color(0xFF3B82F6),
                        unfocusedBorderColor = Color(0xFF2D3748),
                        cursorColor = Color(0xFF60A5FA),
                        focusedLabelColor = Color(0xFF60A5FA),
                        unfocusedLabelColor = Color(0xFF9CA3AF)
                    )
                )
            }
        }
    }
}
