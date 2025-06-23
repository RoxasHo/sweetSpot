package com.example.sweetspot

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class OrderHistoryViewModelFactory(private val filterByUser: Boolean) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return OrderHistoryViewModel(filterByUser) as T
    }
}

