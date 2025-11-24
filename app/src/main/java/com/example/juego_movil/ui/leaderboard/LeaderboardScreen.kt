package com.example.juego_movil.ui.leaderboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
// 💡 FIX: Importaciones explícitas para resolver el conflicto de tipos
import com.example.juego_movil.data.LeaderboardEntry
import com.example.juego_movil.data.leaderboard.LeaderboardMode
import com.example.juego_movil.data.leaderboard.LeaderboardCategory
import com.example.juego_movil.repository.LeaderboardRepository
import kotlinx.coroutines.delay

// 💡 FIX: Importar las anotaciones experimentales
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarDefaults

// Máximo nivel soportado para la selección
private const val MAX_LEVEL = 4

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeaderboardScreen(
    repository: LeaderboardRepository,
    onBack: () -> Unit
) {
    // ----------------------------------------------------
    // ESTADO DE FILTRO
    // ----------------------------------------------------
    var mode by remember { mutableStateOf(LeaderboardMode.LEVEL) }
    var category by remember { mutableStateOf(LeaderboardCategory.TIME) }
    var selectedLevel by remember { mutableStateOf(1) }

    // ----------------------------------------------------
    // ESTADO DE DATOS Y CARGA
    // ----------------------------------------------------
    var entries by remember { mutableStateOf<List<LeaderboardEntry>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var fetchError by remember { mutableStateOf(false) }

    // Función para alternar el modo
    val toggleMode = {
        mode = if (mode == LeaderboardMode.LEVEL) LeaderboardMode.GLOBAL else LeaderboardMode.LEVEL
        selectedLevel = 1 // Resetear nivel al cambiar de modo
    }

    // Función para alternar la categoría
    val toggleCategory = {
        category =
            if (category == LeaderboardCategory.TIME) LeaderboardCategory.DEATHS else LeaderboardCategory.TIME
    }

    // Efecto lanzado cuando cambian los filtros
    LaunchedEffect(mode, category, selectedLevel) {
        isLoading = true
        fetchError = false // Resetear error
        // Simular un retraso mínimo para evitar parpadeos si la respuesta es demasiado rápida
        delay(300)

        val result = repository.loadLeaderboard(mode, category, selectedLevel)

        result.onSuccess { data ->
            entries = data
        }.onFailure {
            entries = emptyList()
            fetchError = true
            println("Error al cargar Leaderboard: $it")
        }

        isLoading = false
    }

    Scaffold(
        topBar = {
            // AppBar para el título y el botón de atrás
            TopAppBar(
                title = {
                    Text(
                        text = "MARCADORES GLOBALES",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Volver", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0D0D1A))
            )
        },
        containerColor = Color(0xFF0D0D1A) // Fondo oscuro
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ------------------
            // 1. FILTROS
            // ------------------
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF1F2833)),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Modo (GLOBAL vs LEVEL)
                FilterChip(
                    label = {
                        Text(
                            text = if (mode == LeaderboardMode.LEVEL) "Por Nivel" else "Global",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    },
                    selected = true,
                    onClick = toggleMode,
                    modifier = Modifier.weight(1f).padding(horizontal = 4.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFFFFEB3B),
                        selectedLabelColor = Color.Black
                    )
                )

                // Categoría (TIME vs DEATHS)
                FilterChip(
                    label = {
                        Text(
                            text = if (category == LeaderboardCategory.TIME) "Tiempo (s)" else "Muertes",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    },
                    selected = true,
                    onClick = toggleCategory,
                    modifier = Modifier.weight(1f).padding(horizontal = 4.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFF00BCD4),
                        selectedLabelColor = Color.Black
                    )
                )
            }

            // ------------------
            // 2. SELECTOR DE NIVEL (Solo en modo LEVEL)
            // ------------------
            if (mode == LeaderboardMode.LEVEL) {
                Spacer(Modifier.height(12.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Flecha Izquierda
                    IconButton(
                        onClick = { if (selectedLevel > 1) selectedLevel-- },
                        enabled = selectedLevel > 1 && !isLoading
                    ) {
                        Icon(Icons.Filled.KeyboardArrowLeft, contentDescription = "Nivel anterior", tint = Color.White)
                    }

                    Text(
                        text = "NIVEL $selectedLevel",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFFFFEB3B),
                        modifier = Modifier.width(120.dp),
                        textAlign = TextAlign.Center
                    )

                    // Flecha Derecha
                    IconButton(
                        onClick = { if (selectedLevel < MAX_LEVEL) selectedLevel++ },
                        enabled = selectedLevel < MAX_LEVEL && !isLoading
                    ) {
                        Icon(Icons.Filled.KeyboardArrowRight, contentDescription = "Nivel siguiente", tint = Color.White)
                    }
                }
                Spacer(Modifier.height(12.dp))
            } else {
                Spacer(Modifier.height(24.dp))
            }


            // ------------------
            // 3. ENCABEZADOS DE LA LISTA
            // ------------------
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .background(Color(0xFF323B44), RoundedCornerShape(4.dp))
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Rank", color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.weight(0.5f))
                Text("Usuario", color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.weight(2f))
                Text(
                    text = if (category == LeaderboardCategory.TIME) "Tiempo" else "Muertes",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.End,
                    modifier = Modifier.weight(1f)
                )
            }

            // ------------------
            // 4. CONTENIDO DE LA LISTA (O CARGA/ERROR)
            // ------------------

            if (isLoading) {
                // Estado de Carga
                Box(
                    modifier = Modifier.fillMaxSize().padding(top = 50.dp),
                    contentAlignment = Alignment.TopCenter
                ) {
                    CircularProgressIndicator(color = Color(0xFFFFEB3B))
                }
            } else if (fetchError) {
                // Estado de Error
                Text(
                    text = "No se pudieron cargar los marcadores. Revisa tu conexión de red o la configuración de la API.",
                    color = Color(0xFFFF4500), // Rojo anaranjado para errores
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 50.dp)
                )
            } else if (entries.isEmpty()) {
                // Estado vacío
                Text(
                    text = "No hay entradas para esta selección.",
                    color = Color.Gray,
                    fontSize = 18.sp,
                    modifier = Modifier.padding(top = 50.dp)
                )
            } else {
                // Lista de Marcadores
                LazyColumn(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    itemsIndexed(entries) { index, entry ->
                        LeaderboardItem(index + 1, entry, category)
                    }
                }
            }
        }
    }
}

@Composable
fun LeaderboardItem(rank: Int, entry: LeaderboardEntry, cat: LeaderboardCategory) {
    val isHighlighted = rank == 1 // Resaltar el primer puesto
    val bgColor = if (isHighlighted) Color(0xFF4CAF50).copy(alpha = 0.2f) else Color.Transparent
    val textColor = if (isHighlighted) Color(0xFFFFEB3B) else Color.White

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(4.dp))
            .background(bgColor)
            .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Rank
        Text(
            text = "$rank.",
            color = textColor,
            fontSize = 18.sp,
            fontWeight = if (isHighlighted) FontWeight.ExtraBold else FontWeight.Normal,
            modifier = Modifier.weight(0.5f)
        )
        // Username
        Text(
            text = entry.username,
            color = textColor,
            fontSize = 18.sp,
            modifier = Modifier.weight(2f)
        )
        // Value (Time/Deaths)
        Text(
            text = formatValue(entry, cat),
            color = Color(0xFF00BCD4), // Color de valor
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1f)
        )
    }
    // Separador ligero
    Divider(color = Color(0xFF323B44), thickness = 1.dp)
}

private fun formatValue(entry: LeaderboardEntry, cat: LeaderboardCategory): String =
    when (cat) {
        LeaderboardCategory.TIME -> "${entry.score}s"
        LeaderboardCategory.DEATHS -> "${entry.score}"
    }