package com.AppexSolutions.gymsync.features.notifications.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.AppexSolutions.gymsync.core.datastore.AnnouncementType
import com.AppexSolutions.gymsync.features.notifications.domain.model.Announcement
import com.AppexSolutions.gymsync.features.users.presentation.components.UserBottomNavBar
import com.AppexSolutions.gymsync.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    onTabSelected: (Int) -> Unit,
    viewModel: NotificationsViewModel = hiltViewModel()
) {
    val items by viewModel.announcements.collectAsStateWithLifecycle()
    val unread by viewModel.unreadCount.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = NavyBlue,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Avisos del gimnasio",
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    if (unread > 0) {
                        IconButton(onClick = viewModel::markAll) {
                            Icon(Icons.Default.DoneAll, "Marcar todos leídos", tint = TextPrimary)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkNavy)
            )
        },
        bottomBar = {
            UserBottomNavBar(
                selectedTab = 5,
                onTabSelected = onTabSelected,
                unreadAnnouncements = unread
            )
        }
    ) { pad ->
        if (items.isEmpty()) {
            EmptyAnnouncements(Modifier.padding(pad).fillMaxSize())
        } else {
            LazyColumn(
                modifier = Modifier.padding(pad).fillMaxSize().background(NavyBlue),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(items, key = { it.id }) { a ->
                    AnnouncementRow(
                        item = a,
                        onClick = { if (!a.isRead) viewModel.onOpen(a.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun AnnouncementRow(item: Announcement, onClick: () -> Unit) {
    val accent = item.type.accent()
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (item.isRead) CardBackground else MidnightBlue
        ),
        shape = RoundedCornerShape(14.dp)
    ) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .size(40.dp)
                    .background(accent.copy(alpha = 0.15f), shape = RoundedCornerShape(20.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Campaign, null, tint = accent)
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        item.type.name,
                        color = accent,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.weight(1f))
                    Text(
                        formatDate(item.sentAt),
                        color = TextMuted,
                        fontSize = 11.sp
                    )
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    item.title,
                    color = TextPrimary,
                    fontWeight = if (item.isRead) FontWeight.Normal else FontWeight.Bold
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    item.message,
                    color = TextSecondary,
                    fontSize = 13.sp,
                    maxLines = 3
                )
            }
            if (!item.isRead) {
                Spacer(Modifier.width(8.dp))
                Box(
                    Modifier
                        .size(8.dp)
                        .background(ElectricBlue, shape = RoundedCornerShape(4.dp))
                )
            }
        }
    }
}

@Composable
private fun EmptyAnnouncements(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.background(NavyBlue),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            Icons.Default.NotificationsNone,
            null,
            tint = TextMuted,
            modifier = Modifier.size(64.dp)
        )
        Spacer(Modifier.height(12.dp))
        Text("No tienes avisos aún", color = TextSecondary)
    }
}

private fun AnnouncementType.accent(): Color = when (this) {
    AnnouncementType.GENERAL -> ElectricBlue
    AnnouncementType.URGENTE -> ErrorRed
    AnnouncementType.PROMOCION -> SuccessGreen
}

private fun formatDate(millis: Long): String {
    val fmt = SimpleDateFormat("dd MMM · HH:mm", Locale("es"))
    return fmt.format(Date(millis))
}
