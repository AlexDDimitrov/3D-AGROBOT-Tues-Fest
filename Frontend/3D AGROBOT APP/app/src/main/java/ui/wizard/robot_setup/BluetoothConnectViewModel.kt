package ui.wizard.robot_setup

import android.annotation.SuppressLint
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.util.Log
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID


enum class BluetoothStatus { Scanning, Connecting, Connected, Failed }

class BluetoothViewModel : ViewModel() {

    val devices = mutableStateOf<List<BluetoothDevice>>(emptyList())
    val status = mutableStateOf(BluetoothStatus.Scanning)
    val connectedDevice = mutableStateOf<BluetoothDevice?>(null)

    val visibleDevices: List<BluetoothDevice>
        @SuppressLint("MissingPermission")
        get() = devices.value.filter { it.name?.isNotBlank() == true }

    //Add devices to the list
    fun updateDevices(newList: List<BluetoothDevice>) {
        devices.value = newList
    }
    // Update state when scanning for devices
    fun onScanStarted() {
        status.value = BluetoothStatus.Scanning
        devices.value = emptyList()
    }

    //Update state when attempting connection
    fun onConnecting(device: BluetoothDevice) {
        connectedDevice.value = device
        status.value = BluetoothStatus.Connecting
    }

    //Update state when connection is successful
    fun onConnected(device: BluetoothDevice) {
        connectedDevice.value = device
        status.value = BluetoothStatus.Connected
    }

    //Update state when connection fails
    fun onConnectionFailed() {
        connectedDevice.value = null
        status.value = BluetoothStatus.Failed
    }

    fun setStatus(newStatus: BluetoothStatus) {
        status.value = newStatus
    }

    //Begin scanning
    @SuppressLint("MissingPermission")
    fun startScan(bluetoothManager: BluetoothManager) {
        onScanStarted()

        bluetoothManager.startScan { device ->
            //Add device if not in list
            if (!devices.value.contains(device)) {
                updateDevices(devices.value + device)
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun stopScan(bluetoothManager: BluetoothManager) {
        bluetoothManager.stopScan()
    }

    fun onTryAgain(bluetoothManager: BluetoothManager) {
        connectedDevice.value = null
        bluetoothManager.disconnect()
        bluetoothManager.stopScan()

        viewModelScope.launch {
            onScanStarted()
            delay(1000)
            bluetoothManager.startScan { device ->
                if (!devices.value.contains(device)) {
                    updateDevices(devices.value + device)
                }
            }
        }
    }


    //Attempt to connect to a device
    @SuppressLint("MissingPermission")
    fun connect(device: BluetoothDevice, bluetoothManager: BluetoothManager) {
        onConnecting(device)

        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {   // runs on background thread
                    bluetoothManager.connect(device)
                }
                onConnected(device)

            } catch (e: Exception) {
                onConnectionFailed()
            }
        }
    }

    //Disconnect from current device
    fun disconnect(bluetoothManager: BluetoothManager) {
        bluetoothManager.disconnect()
        connectedDevice.value = null
        status.value = BluetoothStatus.Scanning
    }
}


class BluetoothManager(
    private val context: Context,
) {

    private val bluetoothAdapter: BluetoothAdapter? =
        (context.getSystemService(Context.BLUETOOTH_SERVICE) as android.bluetooth.BluetoothManager).adapter

    private var socket: BluetoothSocket? = null
    private var isReceiverRegistered = false
    private var activeReceiver: BroadcastReceiver? = null


    companion object {
        // Standard Serial Port Profile UUID - used by ESP32 BluetoothSerial
        private val SPP_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
    }


    // Check if Bluetooth is on
    fun isBluetoothEnabled(): Boolean {
        return bluetoothAdapter?.isEnabled == true
    }

    // Check for a scan permission
    fun hasScanPermission(): Boolean {
        return context.checkSelfPermission(android.Manifest.permission.BLUETOOTH_SCAN) ==
                PackageManager.PERMISSION_GRANTED
    }

    // Check for a connection permission
    fun hasConnectPermission(): Boolean {
        return context.checkSelfPermission(android.Manifest.permission.BLUETOOTH_CONNECT) ==
                PackageManager.PERMISSION_GRANTED
    }

    @SuppressLint("MissingPermission")
    // Start Bluetooth Scan
    fun startScan(onDeviceFound: (BluetoothDevice) -> Unit) {
        // Check Permissions
        if (!hasScanPermission()) {
            Log.w("BluetoothManager", "Missing BLUETOOTH_SCAN permission")
            return
        }
        if (!isBluetoothEnabled()) {
            Log.w("BluetoothManager", "Bluetooth is disabled")
            return
        }

        stopScan()

        // Set up receiver for device discovery
        val filter = IntentFilter().apply {
            addAction(BluetoothDevice.ACTION_FOUND)
            addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
        }

        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                when (intent?.action) {
                    BluetoothDevice.ACTION_FOUND -> {
                        val device = intent.getParcelableExtra<BluetoothDevice>(
                            BluetoothDevice.EXTRA_DEVICE
                        )
                        if (device != null) onDeviceFound(device)
                    }
                    BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                        bluetoothAdapter?.startDiscovery() }
                }
            }
        }

        context.registerReceiver(receiver, filter)
        activeReceiver = receiver
        isReceiverRegistered = true

        //Start discovery
        bluetoothAdapter?.startDiscovery()
    }

    //Stop scanning for devices
    @SuppressLint("MissingPermission")
    fun stopScan() {
        bluetoothAdapter?.cancelDiscovery()

        if (isReceiverRegistered && activeReceiver != null) {
            try {
                context.unregisterReceiver(activeReceiver)
            } catch (_: Exception) {}
            isReceiverRegistered = false
            activeReceiver = null
        }
    }

    //Connect to a bluetooth device
    @SuppressLint("MissingPermission")
    fun connect(device: BluetoothDevice): BluetoothSocket {
        // Create RFCOMM socket using SPP UUID
        val tmp = device.createRfcommSocketToServiceRecord(SPP_UUID)

        // Stop discovery to improve connection speed
        bluetoothAdapter?.cancelDiscovery()
        tmp.connect()

        socket = tmp
        return tmp
    }

    //Write a line of text to the connected device
    fun writeLine(text: String) {
        try {
            socket?.outputStream?.write((text + "\n").toByteArray())
            socket?.outputStream?.flush()
            Log.d("BluetoothManager", "Sent: $text")
        } catch (e: Exception) {
            Log.e("BluetoothManager", "Error writing: ${e.message}")
        }
    }

    //Read a line of text from the connected device
    fun readLine(): String? {
        return try {
            socket?.inputStream?.bufferedReader()?.readLine()
        } catch (e: Exception) {
            Log.e("BluetoothManager", "Error reading: ${e.message}")
            null
        }
    }

    //Close the Bluetooth connection
    fun disconnect() {
        try {
            socket?.close()
            Log.d("BluetoothManager", "Disconnected")
        } catch (e: Exception) {
            Log.w("BluetoothManager", "Error disconnecting: ${e.message}")
        }
        socket = null
    }


    //Send WiFi credentials to ESP32
    fun sendWifiCredentials(
        ssid: String,
        password: String,
        onResult: (WifiResult) -> Unit
    ) {
        onResult(WifiResult.Sending)

        val s = socket
        if (s == null || !s.isConnected) {
            onResult(WifiResult.Failed)
            return
        }

        try {
            // Send SSID
            s.outputStream.write("WIFI:$ssid\n".toByteArray())
            s.outputStream.flush()
            Log.d("BluetoothManager", "Sent: WIFI:$ssid")

            // Send Password
            s.outputStream.write("PASS:$password\n".toByteArray())
            s.outputStream.flush()
            Log.d("BluetoothManager", "Sent: PASS:$password")

            // Credentials sent successfully
            onResult(WifiResult.Sent)

        } catch (e: Exception) {
            Log.e("BluetoothManager", "Error sending credentials: ${e.message}")
            onResult(WifiResult.Failed)
        }
    }

    // Get the current Bluetooth socket
    fun getSocket(): BluetoothSocket? = socket
    //Request ESP32 to scan for WiFi networks
    fun requestWifiScan() {
        socket?.outputStream?.write("SCAN\n".toByteArray())
    }
}
