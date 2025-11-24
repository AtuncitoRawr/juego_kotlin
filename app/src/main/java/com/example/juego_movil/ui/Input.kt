package com.example.juego_movil.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import kotlin.math.hypot

@Composable
fun MovementControls(
    modifier: Modifier = Modifier,
    onMoveDir: (Float) -> Unit,
    onJumpPressed: () -> Unit
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomCenter
    ) {
        // Zona inferior con controles
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp, vertical = 32.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            // Joystick (izquierda)
            Joystick(
                sizeDp = 128,
                onMove = { xAxis -> onMoveDir(xAxis) }
            )

            // Botón de salto (derecha)
            JumpButton(onJumpPressed = onJumpPressed)
        }
    }
}

@Composable
private fun Joystick(
    sizeDp: Int,
    onMove: (Float) -> Unit
) {
    val sizePx = with(LocalDensity.current) { sizeDp.dp.toPx() }
    val radius = sizePx / 2f
    var knob by remember { mutableStateOf(Offset.Zero) }
    var isDragging by remember { mutableStateOf(false) }

    LaunchedEffect(isDragging, knob) {
        // Normalizar a rango [-1, 1] solo eje X
        val nx = if (radius > 0f) (knob.x / radius).coerceIn(-1f, 1f) else 0f
        onMove(nx)
    }

    Surface(
        modifier = Modifier.size(sizeDp.dp),
        shape = CircleShape,
        color = androidx.compose.ui.graphics.Color(0x22FFFFFF),
        tonalElevation = 4.dp
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            isDragging = true
                            val center = Offset(radius, radius)
                            knob = (offset - center).limit(radius)
                        },
                        onDrag = { change, _ ->
                            val center = Offset(radius, radius)
                            knob = (change.position - center).limit(radius)
                        },
                        onDragEnd = {
                            isDragging = false
                            knob = Offset.Zero
                        },
                        onDragCancel = {
                            isDragging = false
                            knob = Offset.Zero
                        }
                    )
                }
        ) {
            val center = Offset(radius, radius)
            // Base
            drawCircle(
                color = androidx.compose.ui.graphics.Color(0x3322FFFF),
                radius = radius * 0.95f,
                center = center
            )
            // Knob
            val knobPos = center + knob
            drawCircle(
                color = androidx.compose.ui.graphics.Color(0xFF22AAFF),
                radius = radius * 0.35f,
                center = knobPos
            )
        }
    }
}

private fun Offset.limit(maxLen: Float): Offset {
    val l = hypot(x.toDouble(), y.toDouble()).toFloat()
    if (l <= maxLen) return this
    val k = maxLen / (if (l == 0f) 1f else l)
    return Offset(x * k, y * k)
}

@Composable
private fun JumpButton(
    onJumpPressed: () -> Unit
) {
    // Botón táctil que reacciona al PRESIONAR (no al soltar)
    Surface(
        modifier = Modifier
            .size(96.dp)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        onJumpPressed()      // dispara salto inmediatamente
                        tryAwaitRelease()    // espera a que se suelte, sin bloquear
                    }
                )
            },
        shape = CircleShape,
        color = Color(0x43FFFFFF) // supertransparente
    ) {}
}