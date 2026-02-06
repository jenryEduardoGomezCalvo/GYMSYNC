package com.AppexSolutions.gymsync.features.auth.presentation.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Campo de texto para Email
 */
@Composable
fun LoginTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true  // ✅ AÑADE ESTE PARÁMETRO
) {
    Column(modifier = modifier.fillMaxWidth()) {
        // Label
        Text(
            text = label,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = Color.White
        )

        Spacer(modifier = Modifier.height(8.dp))

        // TextField
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            enabled = enabled,  // ✅ USA EL PARÁMETRO AQUÍ
            placeholder = {
                Text(
                    text = placeholder,
                    color = Color(0xFF6B7280)
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Email,
                    contentDescription = "Email icon",
                    tint = Color(0xFF9CA3AF)
                )
            },
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                disabledTextColor = Color(0xFF6B7280),  // ✅ Color cuando está deshabilitado
                focusedContainerColor = Color(0xFF1F2937),
                unfocusedContainerColor = Color(0xFF1F2937),
                disabledContainerColor = Color(0xFF1F2937),  // ✅ Background cuando está deshabilitado
                focusedBorderColor = Color(0xFF3B82F6),
                unfocusedBorderColor = Color(0xFF374151),
                disabledBorderColor = Color(0xFF374151),  // ✅ Borde cuando está deshabilitado
                cursorColor = Color(0xFF60A5FA)
            ),
            singleLine = true
        )
    }
}