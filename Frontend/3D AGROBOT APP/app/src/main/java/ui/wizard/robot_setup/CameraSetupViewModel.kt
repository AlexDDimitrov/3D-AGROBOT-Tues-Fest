package ui.wizard.robot_setup

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

enum class CameraStatus {
    TESTING,
    READY,
    FAILED
}

class CameraSetupViewModel(
    private val bluetoothManager: BluetoothManager,
    private val wifiViewModel: WiFiConfigurationViewModel
) : ViewModel() {

    var cameraStatus by mutableStateOf(CameraStatus.TESTING)
        private set

    fun testCamera() {
        cameraStatus = CameraStatus.TESTING

        viewModelScope.launch(Dispatchers.IO) {
            delay(4000)
            try {
                val ip = wifiViewModel.deviceIp
                android.util.Log.d("CameraTest", "IP is: '$ip'")

                if (ip.isNullOrBlank()) {
                    android.util.Log.d("CameraTest", "IP is null/blank — FAILED")
                    cameraStatus = CameraStatus.FAILED
                    return@launch
                }

                val url = java.net.URL("http://$ip/")
                android.util.Log.d("CameraTest", "Connecting to: $url")

                val connection = url.openConnection() as java.net.HttpURLConnection
                connection.connectTimeout = 5000
                connection.readTimeout = 5000
                connection.requestMethod = "GET"
                connection.connect()

                val code = connection.responseCode
                android.util.Log.d("CameraTest", "Response code: $code")
                connection.disconnect()

                cameraStatus = if (code in 200..299) CameraStatus.READY else CameraStatus.FAILED

            } catch (e: Exception) {
                android.util.Log.e("CameraTest", "Exception: ${e.javaClass.simpleName}: ${e.message}")
                cameraStatus = CameraStatus.FAILED
            }
        }
    }
}