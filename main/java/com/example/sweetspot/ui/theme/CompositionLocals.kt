package com.example.sweetspot.ui.theme

import ThemeViewModel
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.lifecycle.viewmodel.compose.viewModel

import androidx.compose.runtime.staticCompositionLocalOf
import com.example.sweetspot.AuthViewModel

val LocalThemeViewModel = staticCompositionLocalOf<ThemeViewModel> {
    error("No ThemeViewModel provided")
}

val LocalAuthViewModel = staticCompositionLocalOf<AuthViewModel> {
    error("No AuthViewModel provided")
}

class CompositionLocals {
}