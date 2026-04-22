package ui.wizard.robot_setup

import ui.wizard.robot_setup.WifiResult
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch



enum class WiFiConnectionState {
    IDLE,
    SENDING,
    CONNECTING,
    CONNECTED,
    FAILED
}

class WiFiConfigurationViewModel(
    private val bluetoothManager: BluetoothManager
) : ViewModel() {

    var connectionState by mutableStateOf(WiFiConnectionState.IDLE)
        private set

    var deviceIp by mutableStateOf<String?>(null)
        private set

    // List of WiFi networks scanned by ESP32
    val networks = mutableStateListOf<String>()

    var isScanning by mutableStateOf(false)
        private set

    init {
        networks.addAll(listOf("Network1", "Network2", "Network3"))
    }
    fun requestNetworkScan() {
        isScanning = true
        networks.clear()

        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Send SCAN command to ESP32
                bluetoothManager.writeLine("SCAN")

                // Read response
                val socket = bluetoothManager.getSocket()
                if (socket == null || !socket.isConnected) {
                    isScanning = false
                    return@launch
                }

                val reader = socket.inputStream.bufferedReader()
                val startTime = System.currentTimeMillis()
                val timeout = 5000L // 15 seconds for scan

                while (System.currentTimeMillis() - startTime < timeout) {
                    val line = reader.readLine()

                    if (line == null) {
                        break
                    }


                    when {
                        line == "SCAN_START" -> {
                            networks.clear()
                        }

                        line.startsWith("SSID:") -> {
                            val ssid = line.removePrefix("SSID:").trim()
                            if (ssid.isNotBlank() && !networks.contains(ssid)) {
                                networks.add(ssid)
                            }
                        }

                        line == "SCAN_END" -> {
                            isScanning = false
                            return@launch
                        }
                    }
                }

                // Timeout
                isScanning = false

            } catch (e: Exception) {
                isScanning = false
            }
        }
    }

    //Send WiFi credentials to ESP32
    fun sendCredentials(ssid: String, password: String) {
        connectionState = WiFiConnectionState.SENDING

        viewModelScope.launch(Dispatchers.IO) {

            val socket = bluetoothManager.getSocket()
            // Check if Bluetooth is still connected
            if (socket == null || !socket.isConnected) {
                connectionState = WiFiConnectionState.FAILED
                return@launch
            }
            try {
                //Send credentials
                var failed = false

                bluetoothManager.sendWifiCredentials(ssid, password) { result ->
                    if (result == WifiResult.Failed) {
                        failed = true
                    }
                }

                // Check if sending failed
                if (failed) {
                    connectionState = WiFiConnectionState.FAILED
                    return@launch
                }

                // Read ESP32's response
                val reader = socket.inputStream.bufferedReader()

                val startTime = System.currentTimeMillis()
                val timeout = 30000L // 30 seconds

                while (System.currentTimeMillis() - startTime < timeout) {
                    // Read line from ESP32
                    val line = reader.readLine()

                    if (line == null) {
                        connectionState = WiFiConnectionState.FAILED
                        return@launch
                    }

                    when {
                        line.contains("FAILED", ignoreCase = true) ||
                                line.contains("✗") ||
                                line.contains("failed", ignoreCase = true)
                            -> {
                            connectionState = WiFiConnectionState.FAILED
                            return@launch
                        }

                        line.contains("Connecting", ignoreCase = true) ||
                                line.contains("connecting", ignoreCase = true) ||
                                line.trim() == "."
                            -> {
                            connectionState = WiFiConnectionState.CONNECTING
                        }

                        // Extract IP address
                        line.contains("http://") -> {
                            val ip = line.substringAfter("http://").trim()
                            deviceIp = ip
                            connectionState = WiFiConnectionState.CONNECTED
                            return@launch
                        }

                        // Extract IP address
                        line.startsWith("IP:", ignoreCase = true) -> {
                            val ip = line.substringAfter("IP:").trim()
                            deviceIp = ip
                            connectionState = WiFiConnectionState.CONNECTED
                            return@launch
                        }

                        // Check if entire line is an IP address
                        line.trim().matches(Regex("\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}")) -> {
                            deviceIp = line.trim()
                            connectionState = WiFiConnectionState.CONNECTED
                            return@launch
                        }

                        // Check for success indicator
                        line.contains("Connected", ignoreCase = true) -> {
                            connectionState = WiFiConnectionState.CONNECTING
                        }
                    }
                }
                connectionState = WiFiConnectionState.FAILED

            } catch (e: Exception) {
                e.printStackTrace()
                connectionState = WiFiConnectionState.FAILED
            }
        }
    }

    fun reset() {
        connectionState = WiFiConnectionState.IDLE
        deviceIp = null
    }
}
