package com.AppexSolutions.gymsync.features.auth.presentation.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Dropdown selector genérico
 *
 * Reutilizable para: selección de rol, selección de gimnasio, etc.
 * Recibe una lista de opciones como Pair<Int, String> (id, nombre)
 *
 * TODO: Cuando se conecte la API, las opciones vendrán del servidor
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DropdownField(
    selectedValue: String,
    onValueChange: (Int, String) -> Unit,  // Devuelve (id, nombre)
    label: String,
    placeholder: String,
    options: List<Pair<Int, String>>,  // Lista de (id, nombre)
    leadingIcon: ImageVector,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxWidth()) {
        // Label
        Text(
            text = label,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = Color.White
        )

        Spacer(modifier = Modifier.height(8.dp))

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { if (enabled) expanded = !expanded }
        ) {
            OutlinedTextField(
                value = selectedValue,
                onValueChange = {},
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                enabled = enabled,
                readOnly = true,
                placeholder = {
                    Text(
                        text = placeholder,
                        color = Color(0xFF6B7280)
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = leadingIcon,
                        contentDescription = "$label icon",
                        tint = Color(0xFF9CA3AF)
                    )
                },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
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

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                containerColor = Color(0xFF1F2937)
            ) {
                options.forEach { (id, nombre) ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = nombre,
                                color = Color.White
                            )
                        },
                        onClick = {
                            onValueChange(id, nombre)
                            expanded = false
                        },
                        colors = MenuDefaults.itemColors(
                            textColor = Color.White
                        )
                    )
                }
            }
        }
    }
}
