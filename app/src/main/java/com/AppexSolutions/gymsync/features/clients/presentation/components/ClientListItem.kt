package com.AppexSolutions.gymsync.features.clients.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.AppexSolutions.gymsync.features.clients.domain.entities.Client

@Composable
fun ClientListItem(
    client: Client,
    modifier: Modifier = Modifier,
    photoLocalPath: String? = null,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(12.dp), // Menos redondeado
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp,
            pressedElevation = 4.dp
        ),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar: muestra foto de perfil si existe, sino la inicial
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(getAvatarColor(client.id)),
                contentAlignment = Alignment.Center
            ) {
                if (photoLocalPath != null) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(java.io.File(photoLocalPath))
                            .crossfade(true)
                            .build(),
                        contentDescription = "Foto de ${client.nombreCompleto}",
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Text(
                        text = client.inicial.uppercase(),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Información del cliente
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = client.nombreCompleto,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = client.email,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Badge de estado
            StatusBadge(activo = client.activo)
        }
    }
}

@Composable
fun StatusBadge(activo: Boolean) {
    val (bgColor, textColor, label) = if (activo) {
        Triple(
            MaterialTheme.colorScheme.primaryContainer,
            MaterialTheme.colorScheme.onPrimaryContainer,
            "Activo"
        )
    } else {
        Triple(
            MaterialTheme.colorScheme.errorContainer,
            MaterialTheme.colorScheme.onErrorContainer,
            "Inactivo"
        )
    }

    Box(
        modifier = Modifier
            .background(bgColor, RoundedCornerShape(6.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = textColor
        )
    }
}

@Composable
private fun getAvatarColor(id: Int): Color {
    // Paleta más coherente y profesional
    val colors = listOf(
        MaterialTheme.colorScheme.primary,
        MaterialTheme.colorScheme.secondary,
        MaterialTheme.colorScheme.tertiary,
        androidx.compose.ui.graphics.Color(0xFF2563EB), // Azul
        androidx.compose.ui.graphics.Color(0xFF059669), // Verde
        androidx.compose.ui.graphics.Color(0xFFDC2626), // Rojo
        androidx.compose.ui.graphics.Color(0xFF7C3AED), // Púrpura
        androidx.compose.ui.graphics.Color(0xFFEA580C)  // Naranja
    )
    return colors[id % colors.size]
}
