package com.AppexSolutions.gymsync.features.clients.presentation.components

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String = "Fecha de inscripción",
    modifier: Modifier = Modifier
) {
    var showDialog by remember { mutableStateOf(false) }

    val initialMillis = remember(value) {
        if (value.isNotBlank()) {
            try {
                java.time.LocalDate.parse(value)
                    .atStartOfDay(java.time.ZoneOffset.UTC)
                    .toInstant()
                    .toEpochMilli()
            } catch (_: Exception) { null }
        } else null
    }

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialMillis
    )

    // 3. Lógica del Diálogo
    if (showDialog) {
        DatePickerDialog(
            onDismissRequest = { showDialog = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        // Convertimos milisegundos a formato YYYY-MM-DD
                        val date = java.time.Instant.ofEpochMilli(millis)
                            .atZone(java.time.ZoneId.of("UTC"))
                            .toLocalDate()
                        onValueChange(date.toString())
                    }
                    showDialog = false
                }) {
                    Text("OK", color = Color(0xFF3B82F6))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Cancelar", color = Color(0xFF9CA3AF))
                }
            },
            colors = DatePickerDefaults.colors(
                containerColor = Color(0xFF1A1F2E) // Color oscuro para el fondo
            )
        ) {
            DatePicker(
                state = datePickerState,
                colors = DatePickerDefaults.colors(
                    titleContentColor = Color.White,
                    headlineContentColor = Color.White,
                    weekdayContentColor = Color.White,
                    dayContentColor = Color.White,
                    selectedDayContainerColor = Color(0xFF3B82F6),
                    selectedDayContentColor = Color.White,
                    todayContentColor = Color(0xFF60A5FA),
                    todayDateBorderColor = Color(0xFF60A5FA)
                )
            )
        }
    }

    // 4. El campo visual (TextField)
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = label,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = Color.White,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        OutlinedTextField(
            value = value,
            onValueChange = { }, // No permitimos edición manual
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showDialog = true }, // Abre el diálogo al hacer clic
            enabled = false, // Desactivamos el foco para que el clic funcione siempre
            readOnly = true,
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = null,
                    tint = Color(0xFF9CA3AF)
                )
            },
            shape = RoundedCornerShape(12.dp),
            // Usamos colores para estado "disabled" porque enabled = false
            colors = OutlinedTextFieldDefaults.colors(
                disabledTextColor = Color.White,
                disabledContainerColor = Color(0xFF1A1F2E),
                disabledBorderColor = Color(0xFF2D3748),
                disabledLeadingIconColor = Color(0xFF9CA3AF),
                disabledPlaceholderColor = Color(0xFF6B7280)
            ),
            singleLine = true,
            placeholder = {
                Text(text = "Seleccionar fecha", color = Color(0xFF6B7280))
            }
        )
    }
}