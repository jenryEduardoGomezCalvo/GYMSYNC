package com.AppexSolutions.gymsync.features.clients.presentation.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog

/**
 * Diálogo para seleccionar la fuente de la imagen de perfil:
 * galería del dispositivo o cámara.
 */
@Composable
fun ImageSourceDialog(
    onGallerySelected: () -> Unit,
    onCameraSelected: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFF1A1F2E)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Seleccionar imagen",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Botón Galería
                Button(
                    onClick = onGallerySelected,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6))
                ) {
                    Icon(
                        Icons.Default.AccountBox,
                        contentDescription = null,
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Galería", color = Color.White, fontWeight = FontWeight.Medium)
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Botón Cámara
                Button(
                    onClick = onCameraSelected,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E40AF))
                ) {
                    Icon(
                        Icons.Default.AddAPhoto,
                        contentDescription = null,
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Cámara", color = Color.White, fontWeight = FontWeight.Medium)
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Botón Cancelar
                TextButton(onClick = onDismiss) {
                    Text("Cancelar", color = Color(0xFF9CA3AF))
                }
            }
        }
    }
}
