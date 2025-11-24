package com.example.juego_movil.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.juego_movil.repository.LevelRepository
import com.example.juego_movil.repository.LocalProgressRepository

class GameVmFactory(
    private val levelRepo: LevelRepository,
    private val progressRepo: LocalProgressRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return GameViewModel(levelRepo, progressRepo) as T
    }
}
