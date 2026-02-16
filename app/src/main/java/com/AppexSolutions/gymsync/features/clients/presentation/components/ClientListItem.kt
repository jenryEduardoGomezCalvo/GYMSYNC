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
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1F2E)),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar
            Box(
                modifier = Modifier.size(48.dp).clip(CircleShape)
                    .background(getAvatarColor(client.id)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = client.inicial,
                    color = Color.White, fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = client.nombreCompleto,
                    color = Color.White, fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = client.email,
                    color = Color(0xFF9CA3AF), fontSize = 14.sp
                )
            }

            // Badge activo/inactivo
            StatusBadge(activo = client.activo)
        }
    }
}

@Composable
fun StatusBadge(activo: Boolean) {
    val (bg, fg, label) = if (activo) {
        Triple(Color(0xFF1E40AF), Color(0xFF60A5FA), "Activo")
    } else {
        Triple(Color(0xFF7F1D1D), Color(0xFFEF4444), "Inactivo")
    }
    Box(
        modifier = Modifier
            .background(bg, RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(text = label, color = fg, fontSize = 12.sp, fontWeight = FontWeight.Medium)
    }
}

private fun getAvatarColor(id: Int): Color {
    val colors = listOf(
        Color(0xFFDB2777), Color(0xFFEA580C), Color(0xFF9333EA),
        Color(0xFF0891B2), Color(0xFF7C3AED)
    )
    return colors[id % colors.size]
}
