package ui.wizard.robot_setup

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class LiveCameraViewModel(
    val streamUrl: String
) : ViewModel() {
    var isLoading by mutableStateOf(true)
        private set
    var hasError by mutableStateOf(false)
        private set
    var errorMessage by mutableStateOf<String?>(null)
        private set

    fun onStreamLoaded() {
        isLoading = false
        hasError = false
    }

    fun onStreamError(message: String) {
        isLoading = false
        hasError = true
        errorMessage = message
    }

    fun resetLoading() {
        if (hasError) {
            isLoading = true
            hasError = false
            errorMessage = null
        }
    }
}