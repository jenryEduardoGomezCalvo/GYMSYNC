package com.AppexSolutions.gymsync.features.clients.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.AppexSolutions.gymsync.features.clients.domain.entities.Client
import com.AppexSolutions.gymsync.features.clients.domain.entities.ClientStatus

/**
 * Item de la lista de clientes
 */
@Composable
fun ClientListItem(
    client: Client,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1A1F2E)
        ),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar circular con inicial
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(getAvatarColor(client.id)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = client.name.first().uppercase(),
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Información del cliente
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = client.name,
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = client.membershipType.displayName,
                    color = Color(0xFF9CA3AF),
                    fontSize = 14.sp
                )
            }

            // Badge de estado
            StatusBadge(status = client.status)
        }
    }
}

/**
 * Badge de estado (Activo/Inactivo)
 */
@Composable
fun StatusBadge(status: ClientStatus) {
    val (backgroundColor, textColor) = when (status) {
        ClientStatus.ACTIVO -> Color(0xFF1E40AF) to Color(0xFF60A5FA)
        ClientStatus.INACTIVO -> Color(0xFF7F1D1D) to Color(0xFFEF4444)
    }

    Box(
        modifier = Modifier
            .background(
                color = backgroundColor,
                shape = RoundedCornerShape(8.dp)
            )
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = status.displayName,
            color = textColor,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

/**
 * Colores de avatar según ID
 */
private fun getAvatarColor(id: Int): Color {
    val colors = listOf(
        Color(0xFFDB2777),  // Rosa
        Color(0xFFEA580C),  // Naranja
        Color(0xFF9333EA),  // Morado
        Color(0xFF0891B2),  // Cyan
        Color(0xFF7C3AED),  // Violeta
    )
    return colors[id % colors.size]
}