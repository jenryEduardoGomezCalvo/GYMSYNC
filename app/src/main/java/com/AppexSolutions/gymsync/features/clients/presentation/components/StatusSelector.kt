package com.AppexSolutions.gymsync.features.clients.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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

@Composable
fun StatusSelector(
    activo: Boolean,
    onToggle: () -> Unit,
    label: String = "Estado",
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = label, fontSize = 14.sp,
            fontWeight = FontWeight.Medium, color = Color.White,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatusOption("Activo", activo, Color(0xFF3B82F6), onClick = { if (!activo) onToggle() }, Modifier.weight(1f))
            StatusOption("Inactivo", !activo, Color(0xFFEF4444), onClick = { if (activo) onToggle() }, Modifier.weight(1f))
        }
    }
}

@Composable
private fun StatusOption(
    label: String, isSelected: Boolean, accent: Color,
    onClick: () -> Unit, modifier: Modifier = Modifier
) {
    val bg = if (isSelected) accent.copy(alpha = 0.15f) else Color.Transparent
    val border = if (isSelected) accent else Color(0xFF2D3748)
    val textColor = if (isSelected) accent else Color(0xFF9CA3AF)

    Box(
        modifier = modifier.height(48.dp)
            .background(bg, RoundedCornerShape(12.dp))
            .border(1.dp, border, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            RadioButton(
                selected = isSelected, onClick = onClick,
                colors = RadioButtonDefaults.colors(selectedColor = accent, unselectedColor = Color(0xFF6B7280))
            )
            Text(text = label, color = textColor, fontSize = 14.sp,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal)
        }
    }
}
