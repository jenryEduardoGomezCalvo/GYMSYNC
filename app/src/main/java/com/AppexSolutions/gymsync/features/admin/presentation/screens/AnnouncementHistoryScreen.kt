package com.AppexSolutions.gymsync.features.admin.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.People
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.AppexSolutions.gymsync.features.admin.presentation.viewmodels.AnnouncementHistoryViewModel
import com.AppexSolutions.gymsync.features.notifications.domain.model.Announcement
import com.AppexSolutions.gymsync.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnnouncementHistoryScreen(
    onNavigateBack: () -> Unit,
    viewModel: AnnouncementHistoryViewModel = hiltViewModel()
) {
    val items by viewModel.announcements.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = NavyBlue,
        topBar = {
            TopAppBar(
                title = { Text("Historial de anuncios", color = TextPrimary, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, null, tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkNavy)
            )
        }
    ) { pad ->
        if (items.isEmpty()) {
            EmptyState(modifier = Modifier.padding(pad).fillMaxSize())
        } else {
            LazyColumn(
                modifier = Modifier.padding(pad).fillMaxSize().background(NavyBlue),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(items, key = { it.id }) { a -> HistoryItem(a) }
            }
        }
    }
}

@Composable
private fun HistoryItem(a: Announcement) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .size(10.dp)
                        .background(a.type.accentColor(), shape = RoundedCornerShape(5.dp))
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    a.type.name,
                    color = a.type.accentColor(),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.weight(1f))
                Text(
                    formatDate(a.sentAt),
                    color = TextMuted,
                    fontSize = 12.sp
                )
            }
            Spacer(Modifier.height(6.dp))
            Text(a.title, color = TextPrimary, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(4.dp))
            Text(
                a.message,
                color = TextSecondary,
                fontSize = 13.sp,
                maxLines = 3
            )
            Spacer(Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.People,
                    null,
                    tint = TextMuted,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    "${a.recipientCount} destinatario(s)",
                    color = TextMuted,
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
private fun EmptyState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.background(NavyBlue),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(Icons.Default.Inbox, null, tint = TextMuted, modifier = Modifier.size(64.dp))
        Spacer(Modifier.height(12.dp))
        Text("Aún no has enviado anuncios", color = TextSecondary)
    }
}

private fun formatDate(millis: Long): String {
    val fmt = SimpleDateFormat("dd MMM · HH:mm", Locale("es"))
    return fmt.format(Date(millis))
}
