package com.example.juego_movil.data

// Define los posibles estados de la sesión del usuario.
sealed class AuthState {
    object Loading : AuthState()
    object SignedOut : AuthState()
    data class SignedIn(val userId: String) : AuthState()
}