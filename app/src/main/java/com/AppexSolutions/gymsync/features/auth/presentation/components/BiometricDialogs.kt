package com.AppexSolutions.gymsync.features.auth.presentation.components

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun EnableBiometricDialog(onConfirm: () -> Unit, onSkip: () -> Unit) {
    AlertDialog(
        onDismissRequest = onSkip,
        icon = {
            Icon(
                imageVector = Icons.Filled.Fingerprint,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = Color(0xFF60A5FA)
            )
        },
        title = {
            Text(
                "Activar acceso con huella",
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        },
        text = {
            Text(
                "¿Deseas activar el acceso con huella dactilar o Face ID para futuros inicios de sesión?",
                textAlign = TextAlign.Center,
                color = Color(0xFF9CA3AF)
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Activar", color = Color(0xFF60A5FA), fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onSkip) {
                Text("Ahora no", color = Color(0xFF9CA3AF))
            }
        },
        containerColor = Color(0xFF1F2937),
        titleContentColor = Color.White,
        textContentColor = Color(0xFF9CA3AF)
    )
}
