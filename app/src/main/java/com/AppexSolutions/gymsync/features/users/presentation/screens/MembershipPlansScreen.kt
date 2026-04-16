package com.AppexSolutions.gymsync.features.users.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.AppexSolutions.gymsync.features.notifications.presentation.UnreadAnnouncementsBadgeViewModel
import com.AppexSolutions.gymsync.features.users.domain.entities.MembershipPlan
import com.AppexSolutions.gymsync.features.users.domain.entities.PlanIcon
import com.AppexSolutions.gymsync.features.users.presentation.components.UserBottomNavBar
import com.AppexSolutions.gymsync.features.users.presentation.viewmodels.UserViewModel
import com.AppexSolutions.gymsync.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MembershipPlansScreen(
    viewModel: UserViewModel = hiltViewModel(),
    badgeViewModel: UnreadAnnouncementsBadgeViewModel = hiltViewModel(),
    onTabSelected: (Int) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val unread by badgeViewModel.unreadCount.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Planes de Membresía",
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = NavyBlue,
                    titleContentColor = Color.White
                )
            )
        },
        bottomBar = {
            UserBottomNavBar(
                selectedTab = 1,
                onTabSelected = onTabSelected,
                unreadAnnouncements = unread
            )
        },
        containerColor = NavyBlue
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Spacer(Modifier.height(8.dp))

            // Subtitle
            Text(
                "Elige el plan perfecto para ti",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "Todos los planes incluyen acceso 24/7 y sin compromisos",
                style = MaterialTheme.typography.bodyMedium,
                color = TextMuted,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(24.dp))

            // Plan cards
            uiState.plans.forEach { plan ->
                PlanCard(plan = plan)
                Spacer(Modifier.height(16.dp))
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun PlanCard(plan: MembershipPlan) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Header: Icon + Name + Badge
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Plan icon
                val iconBg = when (plan.iconType) {
                    PlanIcon.BOLT -> Color(0xFFFBBF24)
                    PlanIcon.CROWN -> Color(0xFF8B5CF6)
                }
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(iconBg.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = when (plan.iconType) {
                            PlanIcon.BOLT -> Icons.Default.Bolt
                            PlanIcon.CROWN -> Icons.Default.Diamond
                        },
                        contentDescription = null,
                        tint = iconBg,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        plan.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        plan.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted
                    )
                }

                if (plan.isPopular) {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Color(0xFF8B5CF6).copy(alpha = 0.15f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = Color(0xFF8B5CF6),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                "Más Popular",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF8B5CF6),
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Price
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    "$${plan.pricePerMonth}",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    "/ mes",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextMuted,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }

            Spacer(Modifier.height(16.dp))

            // Features list
            plan.features.forEach { feature ->
                Row(
                    modifier = Modifier.padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = SuccessGreen,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(
                        feature,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }
            }
        }
    }
}
