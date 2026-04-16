package com.AppexSolutions.gymsync.features.admin.presentation.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

sealed class AdminTab(val index: Int, val label: String) {
    object Dashboard    : AdminTab(0, "Dashboard")
    object Scanner      : AdminTab(1, "Escáner")
    object Clients      : AdminTab(2, "Clientes")
    object Announcements: AdminTab(3, "Anuncios")
}

@Composable
fun AdminBottomNavBar(
    selectedTab: Int = 0,
    onTabSelected: (Int) -> Unit = {},
    modifier: Modifier = Modifier
) {
    NavigationBar(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface
    ) {
        val itemColors = NavigationBarItemDefaults.colors(
            selectedIconColor = MaterialTheme.colorScheme.primary,
            selectedTextColor = MaterialTheme.colorScheme.primary,
            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
            indicatorColor = MaterialTheme.colorScheme.primaryContainer
        )

        NavigationBarItem(
            icon = { Icon(Icons.Default.BarChart, contentDescription = "Dashboard") },
            label = { Text("Dashboard") },
            selected = selectedTab == AdminTab.Dashboard.index,
            onClick = { onTabSelected(AdminTab.Dashboard.index) },
            colors = itemColors
        )

        NavigationBarItem(
            icon = { Icon(Icons.Default.QrCodeScanner, contentDescription = "Escáner") },
            label = { Text("Escáner") },
            selected = selectedTab == AdminTab.Scanner.index,
            onClick = { onTabSelected(AdminTab.Scanner.index) },
            colors = itemColors
        )

        NavigationBarItem(
            icon = { Icon(Icons.Default.Group, contentDescription = "Clientes") },
            label = { Text("Clientes") },
            selected = selectedTab == AdminTab.Clients.index,
            onClick = { onTabSelected(AdminTab.Clients.index) },
            colors = itemColors
        )

        NavigationBarItem(
            icon = { Icon(Icons.Default.Campaign, contentDescription = "Anuncios") },
            label = { Text("Anuncios") },
            selected = selectedTab == AdminTab.Announcements.index,
            onClick = { onTabSelected(AdminTab.Announcements.index) },
            colors = itemColors
        )
    }
}
