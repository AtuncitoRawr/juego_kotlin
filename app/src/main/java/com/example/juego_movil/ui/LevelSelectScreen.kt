package com.example.juego_movil.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.platform.LocalContext
import com.example.juego_movil.repository.LocalProgressRepository

@Composable
fun LevelSelectScreen(
    onLevelChosen: (Int) -> Unit,
    onBackPressed: () -> Unit,
    progressRepo: LocalProgressRepository
) {
    // El contexto local ya no se usa para el conteo de niveles,
    // pero se mantiene por si es necesario en otras partes.
    val ctx = LocalContext.current
    var unlocked by remember { mutableStateOf(1) }
    var maxLevel by remember { mutableStateOf(1) }

    LaunchedEffect(Unit) {
        // Obtener nivel desbloqueado
        unlocked = progressRepo.getHighestUnlockedLevel()

        // Contar niveles en assets
        maxLevel = progressRepo.countLevels()
    }

    var selected by remember { mutableStateOf(1) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF101020)),
        contentAlignment = Alignment.Center
    ) {

        Column(horizontalAlignment = Alignment.CenterHorizontally) {

            Text(
                "Select Level",
                color = Color.White,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold
            )

            Row(
                modifier = Modifier.padding(top = 24.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                // <
                Text(
                    "<",
                    fontSize = 48.sp,
                    color = if (selected > 1) Color.White else Color.Gray,
                    modifier = Modifier
                        .padding(24.dp)
                        .clickable(enabled = selected > 1) { selected-- }
                )

                // Nivel central
                Text(
                    "LEVEL $selected",
                    fontSize = 40.sp,
                    color = if (selected <= unlocked) Color(0xFFFFEB3B) else Color.Gray
                )

                // >
                Text(
                    ">",
                    fontSize = 48.sp,
                    color = if (selected < maxLevel) Color.White else Color.Gray,
                    modifier = Modifier
                        .padding(24.dp)
                        .clickable(enabled = selected < maxLevel) { selected++ }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = if (selected <= unlocked) "PLAY" else "LOCKED",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = if (selected <= unlocked) Color(0xFF4CAF50) else Color.Gray,
                modifier = Modifier
                    .clickable(enabled = selected <= unlocked) { onLevelChosen(selected) }
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "BACK",
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White,
                modifier = Modifier.clickable { onBackPressed() }
            )
        }
    }
}