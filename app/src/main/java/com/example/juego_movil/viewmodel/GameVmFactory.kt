package com.example.juego_movil.viewmodel


import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.juego_movil.repository.LevelRepository

class GameVMFactory(
    private val repo: LevelRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GameViewModel::class.java)) {
            return GameViewModel(repo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
