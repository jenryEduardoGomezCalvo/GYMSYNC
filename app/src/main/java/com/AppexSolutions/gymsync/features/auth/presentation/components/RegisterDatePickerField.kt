package com.AppexSolutions.gymsync.features.auth.presentation.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Campo de fecha con DatePicker integrado
 *
 * Muestra un campo de solo lectura que al hacer clic abre un DatePicker de Material3.
 * El valor se devuelve en formato "YYYY-MM-DD" (como espera la API).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterDatePickerField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()

    Column(modifier = modifier.fillMaxWidth()) {
        // Label
        Text(
            text = label,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = Color.White
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Campo que abre el DatePicker al hacer clic
        OutlinedTextField(
            value = value,
            onValueChange = {},  // Solo lectura
            modifier = Modifier.fillMaxWidth(),
            enabled = enabled,
            readOnly = true,
            placeholder = {
                Text(
                    text = "YYYY-MM-DD",
                    color = Color(0xFF6B7280)
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.CalendarMonth,
                    contentDescription = "Calendario",
                    tint = Color(0xFF9CA3AF)
                )
            },
            trailingIcon = {
                IconButton(
                    onClick = { showDatePicker = true },
                    enabled = enabled
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = "Abrir calendario",
                        tint = Color(0xFF60A5FA)
                    )
                }
            },
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                disabledTextColor = Color(0xFF6B7280),
                focusedContainerColor = Color(0xFF1F2937),
                unfocusedContainerColor = Color(0xFF1F2937),
                disabledContainerColor = Color(0xFF1F2937),
                focusedBorderColor = Color(0xFF3B82F6),
                unfocusedBorderColor = Color(0xFF374151),
                disabledBorderColor = Color(0xFF374151),
                cursorColor = Color(0xFF60A5FA)
            ),
            singleLine = true
        )
    }

    // DatePicker Dialog
    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                            val formattedDate = sdf.format(Date(millis))
                            onValueChange(formattedDate)
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("Aceptar", color = Color(0xFF60A5FA))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancelar", color = Color(0xFF9CA3AF))
                }
            },
            colors = DatePickerDefaults.colors(
                containerColor = Color(0xFF1F2937)
            )
        ) {
            DatePicker(
                state = datePickerState,
                colors = DatePickerDefaults.colors(
                    containerColor = Color(0xFF1F2937),
                    titleContentColor = Color.White,
                    headlineContentColor = Color.White,
                    weekdayContentColor = Color(0xFF9CA3AF),
                    subheadContentColor = Color(0xFF9CA3AF),
                    yearContentColor = Color.White,
                    currentYearContentColor = Color(0xFF60A5FA),
                    selectedYearContainerColor = Color(0xFF3B82F6),
                    selectedYearContentColor = Color.White,
                    dayContentColor = Color.White,
                    selectedDayContainerColor = Color(0xFF3B82F6),
                    selectedDayContentColor = Color.White,
                    todayContentColor = Color(0xFF60A5FA),
                    todayDateBorderColor = Color(0xFF3B82F6),
                    navigationContentColor = Color.White
                )
            )
        }
    }
}
