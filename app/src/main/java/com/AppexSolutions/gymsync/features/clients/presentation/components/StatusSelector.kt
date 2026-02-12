package com.AppexSolutions.gymsync.features.clients.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.AppexSolutions.gymsync.features.clients.domain.entities.ClientStatus

@Composable
fun StatusSelector(
    selectedStatus: ClientStatus,
    onStatusSelected: (ClientStatus) -> Unit,
    label: String = "Estado",
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = label,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = Color.White,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Botón Activo
            StatusOption(
                status = ClientStatus.ACTIVO,
                isSelected = selectedStatus == ClientStatus.ACTIVO,
                onClick = { onStatusSelected(ClientStatus.ACTIVO) },
                modifier = Modifier.weight(1f)
            )

            // Botón Inactivo
            StatusOption(
                status = ClientStatus.INACTIVO,
                isSelected = selectedStatus == ClientStatus.INACTIVO,
                onClick = { onStatusSelected(ClientStatus.INACTIVO) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun StatusOption(
    status: ClientStatus,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val (backgroundColor, borderColor, textColor) = when {
        isSelected && status == ClientStatus.ACTIVO -> Triple(
            Color(0xFF1E40AF),
            Color(0xFF3B82F6),
            Color(0xFF60A5FA)
        )
        isSelected && status == ClientStatus.INACTIVO -> Triple(
            Color(0xFF7F1D1D),
            Color(0xFFEF4444),
            Color(0xFFEF4444)
        )
        else -> Triple(
            Color.Transparent,
            Color(0xFF2D3748),
            Color(0xFF9CA3AF)
        )
    }

    Box(
        modifier = modifier
            .height(48.dp)
            .background(backgroundColor, RoundedCornerShape(12.dp))
            .border(1.dp, borderColor, RoundedCornerShape(12.dp)),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            RadioButton(
                selected = isSelected,
                onClick = onClick,
                colors = RadioButtonDefaults.colors(
                    selectedColor = textColor,
                    unselectedColor = Color(0xFF6B7280)
                )
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = status.displayName,
                color = textColor,
                fontSize = 14.sp,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
            )
        }
    }
}