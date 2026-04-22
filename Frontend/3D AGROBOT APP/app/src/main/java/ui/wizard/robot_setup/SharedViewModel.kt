package ui.wizard.robot_setup

import android.app.Application
import androidx.lifecycle.AndroidViewModel

class SharedViewModel(application: Application) : AndroidViewModel(application) {
    var gardenId: Int = -1
    var gardenRequestId: Int = -1

    var gardenPlant: String = ""

    var streamUrl: String = ""
    val bluetoothViewModel = BluetoothViewModel()
    val bluetoothManager = BluetoothManager(getApplication())
    val wifiViewModel = WiFiConfigurationViewModel(bluetoothManager)

    val cameraViewModel by lazy {
        CameraSetupViewModel(bluetoothManager, wifiViewModel)
    }
}
