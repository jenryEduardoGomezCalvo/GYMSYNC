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

/**
 * Pantalla principal de Clientes
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientsScreen(
    factory: ClientsViewModelFactory = ClientsViewModelFactory(),
    onAddClient: () -> Unit = {},
    onClientClick: (Int) -> Unit = {},
    onTabSelected: (Int) -> Unit = {}
) {
    val viewModel: ClientsViewModel = viewModel(factory = factory)
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            // Top Bar
            TopAppBar(
                title = {
                    Text(
                        "Clientes",
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp
                    )
                },
                actions = {
                    IconButton(onClick = { /* TODO: Filtros */ }) {
                        Icon(
                            Icons.Default.FilterList,
                            contentDescription = "Filtrar",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF0A1628),
                    titleContentColor = Color.White
                )
            )
        },
        bottomBar = {
            GymBottomNavigationBar(
                selectedTab = 1,  // Clientes seleccionado
                onTabSelected = onTabSelected
            )
        },
        floatingActionButton = {
            // Botón flotante de agregar
            FloatingActionButton(
                onClick = onAddClient,
                containerColor = Color(0xFF3B82F6),
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Agregar cliente",
                    modifier = Modifier.size(28.dp)
                )
            }
        },
        containerColor = Color(0xFF0A1628)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Barra de búsqueda
            ClientSearchBar(
                query = uiState.searchQuery,
                onQueryChange = viewModel::onSearchQueryChange
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Lista de clientes
            if (uiState.filteredClients.isEmpty() && uiState.searchQuery.isNotBlank()) {
                // Estado vacío de búsqueda
                EmptySearchState()
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(
                        items = uiState.filteredClients,
                        key = { it.id }
                    ) { client ->
                        ClientListItem(
                            client = client,
                            onClick = { onClientClick(client.id) }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Estado cuando no hay resultados de búsqueda
 */
@Composable
fun EmptySearchState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "🔍",
                fontSize = 64.sp
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "No se encontraron clientes",
                color = Color(0xFF9CA3AF),
                fontSize = 16.sp
            )
        }
    }
}