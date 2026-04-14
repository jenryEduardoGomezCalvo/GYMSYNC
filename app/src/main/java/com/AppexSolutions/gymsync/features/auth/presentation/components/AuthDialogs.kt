package com.AppexSolutions.gymsync.features.auth.presentation.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp

@Composable
fun ErrorDialog(message: String, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Text("⚠️", fontSize = 48.sp) },
        title = { Text("Error", fontWeight = FontWeight.Bold) },
        text = { Text(message, textAlign = TextAlign.Center) },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Aceptar") } },
        containerColor = Color(0xFF1F2937),
        titleContentColor = Color.White,
        textContentColor = Color(0xFF9CA3AF)
    )
}
