package com.AppexSolutions.gymsync.features.progress.presentation.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.AppexSolutions.gymsync.features.notifications.presentation.UnreadAnnouncementsBadgeViewModel
import com.AppexSolutions.gymsync.features.progress.domain.entities.ProgressEntry
import com.AppexSolutions.gymsync.features.progress.presentation.viewmodels.ProgressViewModel
import com.AppexSolutions.gymsync.features.users.presentation.components.UserBottomNavBar
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun ProgressDashboardScreen(
    userId: Int,
    onNavigateToAdd: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onTabSelected: (Int) -> Unit,
    viewModel: ProgressViewModel = hiltViewModel(),
    badgeViewModel: UnreadAnnouncementsBadgeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val unread by badgeViewModel.unreadCount.collectAsStateWithLifecycle()

    LaunchedEffect(userId) { viewModel.loadHistory(userId) }

    Scaffold(
        bottomBar = {
            UserBottomNavBar(
                selectedTab = 4,
                onTabSelected = onTabSelected,
                unreadAnnouncements = unread
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToAdd,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Agregar registro")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(Modifier.height(8.dp))

            Text(
                "Mi Progreso",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            if (uiState.isLoading) {
                Box(Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (uiState.entries.isEmpty()) {
                EmptyProgressCard(onNavigateToAdd)
            } else {
                val latest = uiState.entries.first()
                LatestMetricsCard(latest)

                val weightEntries = uiState.entries
                    .filter { it.weight != null }
                    .takeLast(8)
                    .reversed()

                if (weightEntries.size >= 2) {
                    WeightChartCard(weightEntries)
                }

                OutlinedButton(
                    onClick = onNavigateToHistory,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.History, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Ver historial completo")
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun LatestMetricsCard(entry: ProgressEntry) {
    val fmt = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                "Último registro — ${fmt.format(entry.date)}",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            // Foto de progreso si existe
            entry.photoUri?.let { uri ->
                AsyncImage(
                    model = uri,
                    contentDescription = "Foto de progreso",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )
            }
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                entry.weight?.let { MetricChip("Peso", "$it kg") }
                entry.waist?.let { MetricChip("Cintura", "$it cm") }
                entry.chest?.let { MetricChip("Pecho", "$it cm") }
                entry.arms?.let  { MetricChip("Brazos", "$it cm") }
            }
        }
    }
}

@Composable
private fun MetricChip(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontWeight = FontWeight.Bold, fontSize = 18.sp,
            color = MaterialTheme.colorScheme.onPrimaryContainer)
        Text(label, style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer)
    }
}

@Composable
private fun WeightChartCard(entries: List<ProgressEntry>) {
    val primary = MaterialTheme.colorScheme.primary
    val surface = MaterialTheme.colorScheme.surfaceVariant
    val fmt = SimpleDateFormat("dd/MM", Locale.getDefault())

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text("Evolución de peso", fontWeight = FontWeight.SemiBold,
                style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(12.dp))

            val weights = entries.mapNotNull { it.weight }
            val minW = weights.min()
            val maxW = weights.max()
            val range = (maxW - minW).coerceAtLeast(1f)

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
            ) {
                val w = size.width
                val h = size.height
                val step = w / (entries.size - 1).coerceAtLeast(1)

                // Grid line
                drawLine(surface, Offset(0f, h / 2), Offset(w, h / 2), strokeWidth = 1.dp.toPx())

                // Line path
                val path = Path()
                entries.forEachIndexed { i, entry ->
                    val x = i * step
                    val y = h - ((entry.weight!! - minW) / range) * h
                    if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                }
                drawPath(path, primary, style = Stroke(width = 2.dp.toPx()))

                // Dots
                entries.forEachIndexed { i, entry ->
                    val x = i * step
                    val y = h - ((entry.weight!! - minW) / range) * h
                    drawCircle(primary, radius = 4.dp.toPx(), center = Offset(x, y))
                    drawCircle(Color.White, radius = 2.dp.toPx(), center = Offset(x, y))
                }
            }

            // X-axis labels
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                entries.forEach { entry ->
                    Text(fmt.format(entry.date), style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
private fun EmptyProgressCard(onNavigateToAdd: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            Modifier.padding(32.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Sin registros aún", fontWeight = FontWeight.SemiBold,
                style = MaterialTheme.typography.titleMedium)
            Text("Agrega tu primer registro para ver tu evolución.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Button(onClick = onNavigateToAdd) { Text("Agregar registro") }
        }
    }
}
