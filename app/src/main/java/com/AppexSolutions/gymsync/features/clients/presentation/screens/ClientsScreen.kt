package com.AppexSolutions.gymsync.features.clients.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.AppexSolutions.gymsync.features.clients.presentation.components.ClientListItem
import com.AppexSolutions.gymsync.features.clients.presentation.components.ClientSearchBar
import com.AppexSolutions.gymsync.features.clients.presentation.components.GymBottomNavigationBar
import com.AppexSolutions.gymsync.features.clients.presentation.viewmodels.ClientsViewModel
import com.AppexSolutions.gymsync.features.clients.presentation.viewmodels.ClientsViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientsScreen(
    factory: ClientsViewModelFactory,
    shouldRefresh: Boolean = false,  // <-- agregar
    onAddClient: () -> Unit = {},
    onClientClick: (Int) -> Unit = {},
    onTabSelected: (Int) -> Unit = {}
) {
    val viewModel: ClientsViewModel = viewModel(factory = factory)
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Cuando shouldRefresh cambia a true (al regresar de Editar), recarga la lista
    LaunchedEffect(shouldRefresh) {
        if (shouldRefresh) viewModel.refresh()
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Clientes", fontWeight = FontWeight.Bold, fontSize = 24.sp) },
                actions = {
                    IconButton(onClick = {}) {
                        Icon(Icons.Default.FilterList, "Filtrar", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF0A1628), titleContentColor = Color.White
                )
            )
        },
        bottomBar = { GymBottomNavigationBar(selectedTab = 1, onTabSelected = onTabSelected) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddClient,
                containerColor = Color(0xFF3B82F6),
                contentColor = Color.White,
                shape = CircleShape
            ) { Icon(Icons.Default.Add, "Agregar usuario", Modifier.size(28.dp)) }
        },
        containerColor = Color(0xFF0A1628)
    ) { paddingValues ->
        Column(Modifier.fillMaxSize().background(Color(0xFF0A1628)).padding(paddingValues)) {
            Spacer(Modifier.height(8.dp))
            ClientSearchBar(query = uiState.searchQuery, onQueryChange = viewModel::onSearchQueryChange)
            Spacer(Modifier.height(16.dp))

            when {
                uiState.isLoading -> LoadingState()
                uiState.error != null -> ErrorState(uiState.error!!, viewModel::refresh)
                uiState.filteredClients.isEmpty() && uiState.searchQuery.isNotBlank() -> EmptySearchState()
                uiState.filteredClients.isEmpty() -> EmptyClientsState()
                else -> {
                    LazyColumn(
                        Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 16.dp)
                    ) {
                        items(items = uiState.filteredClients, key = { it.id }) { client ->
                            ClientListItem(client = client, onClick = { onClientClick(client.id) })
                        }
                    }
                }
            }
        }
    }
}

@Composable fun LoadingState() {
    Box(Modifier.fillMaxSize(), Alignment.Center) { CircularProgressIndicator(color = Color(0xFF60A5FA)) }
}

@Composable fun ErrorState(message: String, onRetry: () -> Unit) {
    Box(Modifier.fillMaxSize(), Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(32.dp)) {
            Text("⚠️", fontSize = 64.sp)
            Spacer(Modifier.height(16.dp))
            Text(message, color = Color(0xFF9CA3AF), fontSize = 16.sp)
            Spacer(Modifier.height(24.dp))
            Button(onClick = onRetry, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6))) {
                Text("Reintentar")
            }
        }
    }
}

@Composable fun EmptyClientsState() {
    Box(Modifier.fillMaxSize(), Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("👥", fontSize = 64.sp)
            Spacer(Modifier.height(16.dp))
            Text("No hay clientes registrados", color = Color(0xFF9CA3AF), fontSize = 16.sp)
            Spacer(Modifier.height(8.dp))
            Text("Presiona + para agregar uno", color = Color(0xFF6B7280), fontSize = 14.sp)
        }
    }
}

@Composable fun EmptySearchState() {
    Box(Modifier.fillMaxSize(), Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("🔍", fontSize = 64.sp)
            Spacer(Modifier.height(16.dp))
            Text("No se encontraron clientes", color = Color(0xFF9CA3AF), fontSize = 16.sp)
        }
    }
}
