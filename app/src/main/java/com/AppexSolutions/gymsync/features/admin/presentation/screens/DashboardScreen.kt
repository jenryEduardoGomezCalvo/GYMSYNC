package com.AppexSolutions.gymsync.features.admin.presentation.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.AppexSolutions.gymsync.core.datastore.AttendanceByDate
import com.AppexSolutions.gymsync.core.datastore.AttendanceEntity
import com.AppexSolutions.gymsync.features.admin.presentation.viewmodels.DashboardUiState
import com.AppexSolutions.gymsync.features.admin.presentation.viewmodels.DashboardViewModel
import com.AppexSolutions.gymsync.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    if (uiState.isLoading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(NavyBlue),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = ElectricBlue)
        }
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(NavyBlue),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item {
            Text(
                text = "Dashboard",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                text = "Resumen de asistencias",
                style = MaterialTheme.typography.bodyMedium,
                color = TextMuted
            )
        }

        item { SummaryCardsRow(uiState) }

        item { BarChartSection(uiState.asistenciasPorDia) }

        item { LineChartSection(uiState.asistenciasPorDia) }

        item {
            Text(
                text = "ASISTENCIAS RECIENTES",
                style = MaterialTheme.typography.labelMedium,
                color = TextMuted,
                letterSpacing = 1.sp
            )
        }

        items(uiState.asistenciasRecientes) { attendance ->
            AttendanceItemRow(attendance)
        }

        item { Spacer(Modifier.height(16.dp)) }
    }
}

// ── Tarjetas de resumen ────────────────────────────────────────────────────────
@Composable
private fun SummaryCardsRow(uiState: DashboardUiState) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SummaryCard(
            modifier = Modifier.weight(1f),
            icon = Icons.Default.Today,
            value = uiState.totalHoy.toString(),
            label = "Hoy",
            color = ElectricBlue
        )
        SummaryCard(
            modifier = Modifier.weight(1f),
            icon = Icons.Default.DateRange,
            value = uiState.totalSemana.toString(),
            label = "Semana",
            color = BrightBlue
        )
        SummaryCard(
            modifier = Modifier.weight(1f),
            icon = Icons.Default.CalendarMonth,
            value = uiState.totalMes.toString(),
            label = "Mes",
            color = SuccessGreen
        )
    }
}

@Composable
private fun SummaryCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    value: String,
    label: String,
    color: Color
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted
            )
        }
    }
}

// ── Gráfica de barras: últimos 7 días ─────────────────────────────────────────
@Composable
private fun BarChartSection(asistenciasPorDia: List<AttendanceByDate>) {
    val data = remember(asistenciasPorDia) { getLast7DaysData(asistenciasPorDia) }
    val textMeasurer = rememberTextMeasurer()

    ChartCard(title = "Asistencias últimos 7 días") {
        if (data.isEmpty()) {
            EmptyChartMessage()
            return@ChartCard
        }

        val maxValue = data.maxOf { it.total }.coerceAtLeast(1)
        val labelStyle = TextStyle(color = TextMuted, fontSize = 10.sp)

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
        ) {
            val bottomPad = 28.dp.toPx()
            val topPad = 12.dp.toPx()
            val chartHeight = size.height - bottomPad - topPad
            val barCount = data.size
            val slotWidth = size.width / barCount
            val barWidth = slotWidth * 0.55f

            // Líneas guía horizontales
            val guideColor = Color.White.copy(alpha = 0.07f)
            for (i in 0..3) {
                val y = topPad + chartHeight * (1f - i / 3f)
                drawLine(guideColor, Offset(0f, y), Offset(size.width, y), strokeWidth = 1.dp.toPx())
            }

            data.forEachIndexed { index, item ->
                val barHeight = (item.total.toFloat() / maxValue) * chartHeight
                val x = index * slotWidth + (slotWidth - barWidth) / 2f
                val y = topPad + chartHeight - barHeight


                // Barra
                drawRoundRect(
                    color = ElectricBlue,
                    topLeft = Offset(x, y),
                    size = Size(barWidth, barHeight.coerceAtLeast(2.dp.toPx())),
                    cornerRadius = CornerRadius(4.dp.toPx())
                )

                // Etiqueta fecha debajo
                val label = formatAxisDate(item.fecha)
                val measured = textMeasurer.measure(label, labelStyle)
                val labelX = (index * slotWidth + slotWidth / 2f - measured.size.width / 2f)
                    .coerceIn(0f, size.width - measured.size.width)
                drawText(
                    textMeasurer = textMeasurer,
                    text = label,
                    topLeft = Offset(labelX, size.height - measured.size.height),
                    style = labelStyle
                )

                // Valor encima de la barra
                if (item.total > 0) {
                    val valueLabel = item.total.toString()
                    val valueMeasured = textMeasurer.measure(
                        valueLabel,
                        TextStyle(color = TextSecondary, fontSize = 9.sp)
                    )
                    val valueX = (index * slotWidth + slotWidth / 2f - valueMeasured.size.width / 2f)
                        .coerceIn(0f, size.width - valueMeasured.size.width)
                    val valueY = (y - valueMeasured.size.height - 2.dp.toPx()).coerceAtLeast(0f)
                    drawText(
                        textMeasurer = textMeasurer,
                        text = valueLabel,
                        topLeft = Offset(valueX, valueY),
                        style = TextStyle(color = TextSecondary, fontSize = 9.sp)
                    )
                }
            }
        }
    }
}

// ── Gráfica de línea: últimos 30 días ─────────────────────────────────────────
@Composable
private fun LineChartSection(asistenciasPorDia: List<AttendanceByDate>) {
    val textMeasurer = rememberTextMeasurer()
    val labelStyle = TextStyle(color = TextMuted, fontSize = 10.sp)

    ChartCard(title = "Histórico mensual") {
        if (asistenciasPorDia.isEmpty()) {
            EmptyChartMessage()
            return@ChartCard
        }

        val maxValue = asistenciasPorDia.maxOf { it.total }.coerceAtLeast(1)

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
        ) {
            val bottomPad = 28.dp.toPx()
            val topPad = 12.dp.toPx()
            val chartHeight = size.height - bottomPad - topPad
            val count = asistenciasPorDia.size
            val stepX = if (count > 1) size.width / (count - 1).toFloat() else size.width

            // Líneas guía
            val guideColor = Color.White.copy(alpha = 0.07f)
            for (i in 0..3) {
                val y = topPad + chartHeight * (1f - i / 3f)
                drawLine(guideColor, Offset(0f, y), Offset(size.width, y), strokeWidth = 1.dp.toPx())
            }

            // Calcular puntos
            val points = asistenciasPorDia.mapIndexed { index, item ->
                val x = index * stepX
                val y = topPad + chartHeight * (1f - item.total.toFloat() / maxValue)
                Offset(x, y)
            }

            // Área bajo la curva
            if (points.size > 1) {
                val fillPath = Path().apply {
                    moveTo(points.first().x, size.height - bottomPad)
                    lineTo(points.first().x, points.first().y)
                    points.drop(1).forEach { lineTo(it.x, it.y) }
                    lineTo(points.last().x, size.height - bottomPad)
                    close()
                }
                drawPath(fillPath, color = BrightBlue.copy(alpha = 0.12f))
            }

            // Línea
            if (points.size > 1) {
                val linePath = Path().apply {
                    moveTo(points.first().x, points.first().y)
                    points.drop(1).forEach { lineTo(it.x, it.y) }
                }
                drawPath(
                    linePath,
                    color = BrightBlue,
                    style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
                )
            }

            // Puntos y etiquetas (solo mostrar cada N para no saturar)
            val labelStep = (count / 6).coerceAtLeast(1)
            points.forEachIndexed { index, point ->
                drawCircle(color = BrightBlue, radius = 3.dp.toPx(), center = point)
                drawCircle(color = SurfaceDark, radius = 1.5.dp.toPx(), center = point)

                if (index % labelStep == 0) {
                    val label = formatAxisDate(asistenciasPorDia[index].fecha)
                    val measured = textMeasurer.measure(label, labelStyle)
                    val labelX = (point.x - measured.size.width / 2f)
                        .coerceIn(0f, size.width - measured.size.width)
                    drawText(
                        textMeasurer = textMeasurer,
                        text = label,
                        topLeft = Offset(labelX, size.height - measured.size.height),
                        style = labelStyle
                    )
                }
            }
        }
    }
}

// ── Card contenedor gráficas ───────────────────────────────────────────────────
@Composable
private fun ChartCard(title: String, content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
            content()
        }
    }
}

@Composable
private fun EmptyChartMessage() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Sin datos aún",
            style = MaterialTheme.typography.bodySmall,
            color = TextMuted
        )
    }
}

// ── Fila de asistencia reciente ───────────────────────────────────────────────
@Composable
private fun AttendanceItemRow(attendance: AttendanceEntity) {
    val timeFormatter = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    val hora = remember(attendance.timestamp) {
        timeFormatter.format(Date(attendance.timestamp))
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(ElectricBlue.copy(alpha = 0.15f), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = ElectricBlue,
                    modifier = Modifier.size(22.dp)
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = attendance.nombreCliente,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
                Text(
                    text = attendance.fecha,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted
                )
            }
            Text(
                text = hora,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = BrightBlue
            )
        }
    }
}

// ── Helpers ───────────────────────────────────────────────────────────────────
private fun getLast7DaysData(data: List<AttendanceByDate>): List<AttendanceByDate> {
    val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val dataMap = data.associateBy { it.fecha }
    return (6 downTo 0).map { offset ->
        val cal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -offset) }
        val fecha = formatter.format(cal.time)
        dataMap[fecha] ?: AttendanceByDate(fecha, 0)
    }
}

private fun formatAxisDate(fecha: String): String {
    return try {
        val parts = fecha.split("-")
        "${parts[2]}/${parts[1]}"
    } catch (e: Exception) {
        fecha
    }
}
