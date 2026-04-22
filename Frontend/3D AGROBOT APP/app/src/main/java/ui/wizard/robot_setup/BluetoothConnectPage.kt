package ui.wizard.robot_setup

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.a3d_agrobot_app.R
import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.platform.LocalConfiguration
import androidx.navigation.NavController

@Composable
fun BluetoothConnectionApp(
    header: String,
    viewModel: BluetoothViewModel,
    bluetoothManager: BluetoothManager,
    onContinue: () -> Unit,
    modifier: Modifier = Modifier,
    navController: NavController,
    onBack: () -> Unit
) {
    val context = LocalContext.current

    // Launcher to enable Bluetooth
    val enableBtLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        viewModel.startScan(bluetoothManager)
    }

    // Launcher to request Bluetooth permissions (Android 12+)
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.values.all { it }
        if (allGranted) {
            // Permissions granted
            if (!bluetoothManager.isBluetoothEnabled()) {
                enableBtLauncher.launch(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE))
            } else {
                viewModel.startScan(bluetoothManager)
            }
        } else {
            // Permissions denied
            viewModel.setStatus(BluetoothStatus.Failed)
        }
    }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // Android 12+ — request permissions
            val btScan = context.checkSelfPermission(Manifest.permission.BLUETOOTH_SCAN)
            val btConnect = context.checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT)

            if (btScan != PackageManager.PERMISSION_GRANTED ||
                btConnect != PackageManager.PERMISSION_GRANTED
            ) {
                permissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.BLUETOOTH_SCAN,
                        Manifest.permission.BLUETOOTH_CONNECT
                    )
                )
            } else {
                // Already granted
                if (!bluetoothManager.isBluetoothEnabled()) {
                    enableBtLauncher.launch(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE))
                } else {
                    viewModel.startScan(bluetoothManager)
                }
            }
        } else {
            // Android 11 and below
            if (!bluetoothManager.isBluetoothEnabled()) {
                enableBtLauncher.launch(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE))
            } else {
                viewModel.startScan(bluetoothManager)
            }
        }
    }

    // Stop scanning when screen closes
    DisposableEffect(Unit) {
        onDispose { bluetoothManager.stopScan() }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF4FAE8))
            .verticalScroll(rememberScrollState())
        ,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        StepProgressBar(
            currentStep = 1,
            totalSteps = 4
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { onBack() },
                modifier = Modifier
                    .background(
                        Color.White, CircleShape
                    )
                    .size(40.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.back_icon),
                    contentDescription = "Back",
                    tint = Color(0xFF436B1F),
                    modifier = Modifier.size(20.dp)
                )
            }
            Text(
                text = header,
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF1A3207)
            )

        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .defaultMinSize(minHeight = LocalConfiguration.current.screenHeightDp.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            StatusZone(status = viewModel.status.value)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 16.dp)
            ) {
                DeviceListZone(
                    devices = viewModel.visibleDevices,
                    bluetoothManager = bluetoothManager,
                    viewModel = viewModel
                )
            }

        }
        ActionZone(
            modifier = modifier,
            viewModel = viewModel,
            bluetoothManager = bluetoothManager,
            onContinue = onContinue,
            navController = navController
        )
    }
}

@Composable
fun StatusZone(
    status: BluetoothStatus,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 24.dp, bottom = 24.dp)

    ) {
        //Status Circular Progress Indicator
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.3f),
            contentAlignment = Alignment.Center
        ) {
            if (status == BluetoothStatus.Scanning || status == BluetoothStatus.Connecting) {
                CircularProgressIndicator(
                    modifier = Modifier.size(140.dp),
                    color = Color(0xFF436B1F),
                    strokeWidth = ProgressIndicatorDefaults.CircularStrokeWidth,
                    trackColor = Color(0xFFD6E8C0),
                    strokeCap = ProgressIndicatorDefaults.CircularIndeterminateStrokeCap,
                )
            }
            Image(
                painter = painterResource(
                    when (status) {
                        BluetoothStatus.Connected -> R.drawable.bluetooth_connected
                        BluetoothStatus.Failed -> R.drawable.error_bluetooth
                        else -> R.drawable.bluetooth
                    }
                ),
                contentDescription = "Status icon",
                modifier = Modifier.size(80.dp)
            )

        }

        //Status text
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            contentAlignment = Alignment.Center
        ) {

            val statusText = when (status) {
                BluetoothStatus.Scanning -> "Търсят се устройсва..."
                BluetoothStatus.Connecting -> "Свързване..."
                BluetoothStatus.Connected -> "Успешно свързване"
                BluetoothStatus.Failed -> "Неуспешна връзка"
            }
            Text(
                text = statusText,
                style =
                    TextStyle(
                        color = when (status) {
                            BluetoothStatus.Connected -> Color(0xFF4CAF50)
                            BluetoothStatus.Failed -> Color(0xFFE53935)
                            BluetoothStatus.Connecting -> Color(0xFF436B1F)
                            BluetoothStatus.Scanning ->  Color(0xFF1A3207)
                        },
                        fontSize = 16.sp,
                        fontFamily = FontFamily.SansSerif,
                        fontWeight = FontWeight.Normal,
                        lineHeight = 22.sp,
                        textAlign = TextAlign.Center
                    ),
                modifier = modifier
                    .padding(top = 16.dp, bottom = 16.dp)

            )
        }
    }
}


@Composable
fun DeviceListZone(
    devices: List<BluetoothDevice>,
    bluetoothManager: BluetoothManager,
    viewModel: BluetoothViewModel,
    modifier: Modifier = Modifier
) {
    if (devices.isEmpty()) {

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Няма налични устройства",
                    fontSize = 16.sp,
                    color = Color(0xFF757575)
                )
                Text(
                    text = "Проверете дали роботът е включен",
                    fontSize = 14.sp,
                    color = Color(0xFF9E9E9E),
                    textAlign = TextAlign.Center
                )
            }
        }
    } else {
        LazyColumn(
            modifier = modifier.fillMaxWidth()
        ) {
            items(devices) { device ->
                val isConnected = viewModel.connectedDevice.value == device
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        @SuppressLint("MissingPermission")
                        Text(
                            text = device.name ?: "Unknown device",
                            fontSize = 16.sp,
                            modifier = Modifier.weight(1f)
                        )

                        Button(
                            onClick = {
                                viewModel.connect(device, bluetoothManager)
                            },
                            modifier = Modifier
                                .height(40.dp)
                                .widthIn(min = 110.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isConnected) Color(0xFFF1F5E9) else Color(0xFF436B1F),
                                contentColor   = if (isConnected) Color(0xFF436B1F) else Color(0xFFEAF3DE),
                                disabledContainerColor = Color(0xFFE0E0E0),
                                disabledContentColor   = Color(0xFF9E9E9E)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            enabled = viewModel.status.value != BluetoothStatus.Connecting
                        ) {
                            Text(
                                text = if (isConnected) "Свързано" else "Свържете",
                                color = if (!isConnected) Color(0xFFEAF3DE) else Color(0xFF436B1F),
                                fontSize = 13.sp,
                                maxLines = 2,
                                overflow = TextOverflow.Visible
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ActionZone(
    modifier: Modifier,
    viewModel: BluetoothViewModel,
    bluetoothManager: BluetoothManager,
    onContinue: () -> Unit,
    navController: NavController
) {
    Row(
        modifier = modifier
            .fillMaxWidth(0.95f)
            .padding(vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {


//Try Again button
        Button(
            onClick = {
                viewModel.onTryAgain(bluetoothManager)
            },
            modifier = Modifier
                .height(63.dp)
                .weight(1f),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF5F5F5)),
            elevation = ButtonDefaults.buttonElevation(
                defaultElevation = 8.dp,
                pressedElevation = 12.dp
            ),
            shape = RoundedCornerShape(12.dp),
            enabled = true
        ) {
            Text(
                text = "Опитайте отново",
                color = Color(0xFF436B1F),
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                softWrap = true,
                maxLines = 2,
            )
        }
        Spacer(modifier = Modifier.width(12.dp))

        //Continue button
        Button(
            onClick = onContinue,
            modifier = Modifier
                .height(63.dp)
                .weight(1f),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF436B1F)
            ),
            elevation = ButtonDefaults.buttonElevation(
                defaultElevation = 8.dp,
                pressedElevation = 12.dp
            ),
            shape = RoundedCornerShape(12.dp),
            enabled = viewModel.status.value == BluetoothStatus.Connected

        ) {
            Text(
                text = "Продължете напред",
                color = Color(0xFFEAF3DE),
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                softWrap = true,
                maxLines = 2,
            )
        }
    }
}
