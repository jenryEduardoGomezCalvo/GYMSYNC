package com.AppexSolutions.gymsync.features.users.presentation.components

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CardMembership
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun UserBottomNavBar(
    selectedTab: Int = 0,
    onTabSelected: (Int) -> Unit = {},
    unreadAnnouncements: Int = 0,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface
    ) {
        NavigationBarItem(
            icon = {
                Icon(
                    Icons.Default.Home,
                    contentDescription = "Inicio",
                    modifier = Modifier.size(26.dp)
                )
            },
            label = { Text("Inicio") },
            selected = selectedTab == 0,
            onClick = { onTabSelected(0) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.primary,
                selectedTextColor = MaterialTheme.colorScheme.primary,
                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                indicatorColor = MaterialTheme.colorScheme.primaryContainer
            )
        )

        NavigationBarItem(
            icon = {
                Icon(
                    Icons.Default.CardMembership,
                    contentDescription = "Planes",
                    modifier = Modifier.size(26.dp)
                )
            },
            label = { Text("Planes") },
            selected = selectedTab == 1,
            onClick = { onTabSelected(1) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.primary,
                selectedTextColor = MaterialTheme.colorScheme.primary,
                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                indicatorColor = MaterialTheme.colorScheme.primaryContainer
            )
        )

        NavigationBarItem(
            icon = {
                Icon(
                    Icons.Default.Person,
                    contentDescription = "Perfil",
                    modifier = Modifier.size(26.dp)
                )
            },
            label = { Text("Perfil") },
            selected = selectedTab == 2,
            onClick = { onTabSelected(2) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.primary,
                selectedTextColor = MaterialTheme.colorScheme.primary,
                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                indicatorColor = MaterialTheme.colorScheme.primaryContainer
            )
        )

        NavigationBarItem(
            icon = {
                Icon(
                    Icons.Default.FitnessCenter,
                    contentDescription = "Rutinas",
                    modifier = Modifier.size(26.dp)
                )
            },
            label = { Text("Rutinas") },
            selected = selectedTab == 3,
            onClick = { onTabSelected(3) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.primary,
                selectedTextColor = MaterialTheme.colorScheme.primary,
                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                indicatorColor = MaterialTheme.colorScheme.primaryContainer
            )
        )

        NavigationBarItem(
            icon = {
                Icon(
                    Icons.Default.ShowChart,
                    contentDescription = "Progreso",
                    modifier = Modifier.size(26.dp)
                )
            },
            label = { Text("Progreso") },
            selected = selectedTab == 4,
            onClick = { onTabSelected(4) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.primary,
                selectedTextColor = MaterialTheme.colorScheme.primary,
                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                indicatorColor = MaterialTheme.colorScheme.primaryContainer
            )
        )

        NavigationBarItem(
            icon = {
                BadgedBox(
                    badge = {
                        if (unreadAnnouncements > 0) {
                            Badge {
                                Text(
                                    if (unreadAnnouncements > 99) "99+"
                                    else unreadAnnouncements.toString()
                                )
                            }
                        }
                    }
                ) {
                    Icon(
                        Icons.Default.Notifications,
                        contentDescription = "Avisos",
                        modifier = Modifier.size(26.dp)
                    )
                }
            },
            label = { Text("Avisos") },
            selected = selectedTab == 5,
            onClick = { onTabSelected(5) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.primary,
                selectedTextColor = MaterialTheme.colorScheme.primary,
                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                indicatorColor = MaterialTheme.colorScheme.primaryContainer
            )
        )
    }
}
