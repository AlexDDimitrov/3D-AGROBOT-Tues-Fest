package ui.wizard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.example.a3d_agrobot_app.R
import ui.wizard.robot_setup.BluetoothConnectionApp
import ui.wizard.robot_setup.CameraSetupScreen
import ui.wizard.robot_setup.LiveCameraScreen
import ui.wizard.robot_setup.LiveCameraViewModel
import ui.wizard.robot_setup.RequirementsPageContent
import ui.wizard.robot_setup.SharedViewModel
import ui.wizard.robot_setup.WifiConfigurationApp

@Composable
fun HomeScreen(onLogout: () -> Unit = {}) {
    val context = LocalContext.current
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var gardenScreen by rememberSaveable  { mutableStateOf("list") }
    var selectedGarden by remember { mutableStateOf<GardenData?>(null) }
    var currentToken by remember { mutableStateOf("") }
    var healthRefreshKey by remember { mutableIntStateOf(0) }
    var robotScreen by rememberSaveable  { mutableStateOf("list") }
    var selectedGardenId by rememberSaveable  { mutableStateOf(-1) }
    val sharedViewModel: SharedViewModel = viewModel()
    val navController = rememberNavController()


    LaunchedEffect(Unit) {
        firstName = withContext(Dispatchers.IO) {
            TokenStore.getFirstName(context) ?: "" }
        lastName = withContext(Dispatchers.IO) {
            TokenStore.getLastName(context) ?: ""
        }
        currentToken = withContext(Dispatchers.IO) { TokenStore.getToken(context) ?: "" }
    }

    var selectedTab by rememberSaveable  { mutableIntStateOf(0) }

    Scaffold (
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF4FAE8)),
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xFF27500A),
                                Color(0xFF3B6D11),
                                Color(0xFF639922)
                            )
                        )
                    )
                    .statusBarsPadding()
                    .padding(top = 16.dp, bottom = 24.dp, start = 24.dp, end = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Добре дошли,",
                            fontSize = 13.sp,
                            color = Color(0xFFEAF3DE).copy(alpha = 0.75f)
                        )
                        Text(
                            text = "$firstName $lastName",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFFEAF3DE),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    IconButton(onClick = {
                        CoroutineScope(Dispatchers.IO).launch {
                            TokenStore.clearToken(context)
                            withContext(Dispatchers.Main) {
                                onLogout()
                            }
                        }
                    }) {
                        Icon(
                            painter = painterResource(id = R.drawable.logout),
                            contentDescription = "Logout",
                            tint = Color(0xFFEAF3DE)
                        )
                    }
                }
            }
        },
        bottomBar = {
            BottomNavigationBar(
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it }
            )
        }

    ) { values ->
        Box(modifier = Modifier.padding(values)) {
            when (selectedTab) {
                0 -> when (gardenScreen) {
                    "list" -> GardenScreen(
                        onAddClick = { gardenScreen = "create" },
                        onEditClick = { garden ->
                            selectedGarden = garden
                            gardenScreen = "edit"
                        }
                    )
                    "create" -> CreateGardenScreen(
                        onSuccess = { gardenScreen = "list" },
                        onBack = { gardenScreen = "list" }
                    )
                    "edit" -> selectedGarden?.let { garden ->
                        EditGardenScreen(
                            garden = garden,
                            onSuccess = { gardenScreen = "list" },
                            onBack = { gardenScreen = "list" }
                        )
                    }
                }
                1 -> when (robotScreen) {
                    "list" -> RobotScreen(
                        onStartClick = { gardenId, plant ->
                            selectedGardenId = gardenId
                            sharedViewModel.gardenId = gardenId
                            sharedViewModel.gardenPlant = plant

                            CoroutineScope(Dispatchers.IO).launch {
                                try {
                                    val startUrl = java.net.URL(
                                        "https://3d-agrobot-tues-fest-production.up.railway.app/garden-request/start"
                                    )
                                    val startConn = startUrl.openConnection()
                                            as java.net.HttpURLConnection
                                    startConn.requestMethod = "POST"
                                    startConn.setRequestProperty("Authorization", "Bearer $currentToken")
                                    startConn.setRequestProperty("Content-Type", "application/json")
                                    startConn.doOutput = true
                                    val startBody = org.json.JSONObject().apply {
                                        put("garden_id", gardenId)
                                    }
                                    startConn.outputStream.write(startBody.toString().toByteArray())
                                    startConn.inputStream.bufferedReader().readText()
                                    startConn.disconnect()
                                } catch (e: Exception) {
                                }

                                try {
                                    val statusUrl = java.net.URL(
                                        "https://3d-agrobot-tues-fest-production.up.railway.app/garden-request/status"
                                    )
                                    val statusConn = statusUrl.openConnection()
                                            as java.net.HttpURLConnection
                                    statusConn.requestMethod = "GET"
                                    statusConn.setRequestProperty("Authorization", "Bearer $currentToken")
                                    val statusResponse = statusConn.inputStream.bufferedReader().readText()
                                    statusConn.disconnect()

                                    val statusJson = org.json.JSONObject(statusResponse)
                                    val requestObj = statusJson.optJSONObject("request")
                                    val requestId = requestObj?.getInt("id") ?: -1

                                    withContext(Dispatchers.Main) {
                                        sharedViewModel.gardenRequestId = requestId
                                        robotScreen = "welcome"
                                    }
                                } catch (e: Exception) {
                                    withContext(Dispatchers.Main) {
                                        robotScreen = "welcome"
                                    }
                                }
                            }
                        }
                    )
                    "welcome" -> RequirementsPageContent(
                        onContinue = { robotScreen = "bluetooth" },
                        navController = navController,
                        onBack = { robotScreen = "list" },
                    )
                    "bluetooth" -> BluetoothConnectionApp(
                        header = "Свързване чрез Bluetooth",
                        viewModel = sharedViewModel.bluetoothViewModel,
                        bluetoothManager = sharedViewModel.bluetoothManager,
                        onContinue = { robotScreen = "wifi" },
                        navController = navController,
                        onBack = { robotScreen = "welcome" },
                    )
                    "wifi" -> WifiConfigurationApp(
                        header = "Свързване с Wi-Fi",
                        viewModel = sharedViewModel.wifiViewModel,
                        onContinue = { robotScreen = "camera" },
                        navController = navController,
                        onBack = { robotScreen = "bluetooth" },
                    )
                    "camera" -> CameraSetupScreen(
                        header = "Тестване на камерата",
                        viewModel = sharedViewModel.cameraViewModel,
                        onContinue = {
                            val ip = sharedViewModel.wifiViewModel.deviceIp ?: "192.168.1.100"
                            sharedViewModel.streamUrl = "http://$ip:81/stream"
                            robotScreen = "live"
                        },
                        navController = navController,
                        onBack = { robotScreen = "wifi" },
                        )
                    "live" -> {
                        val liveCameraViewModel = remember {
                            LiveCameraViewModel(streamUrl = sharedViewModel.streamUrl)
                        }
                        LiveCameraScreen(
                            header = "Наблюдение в реално време",
                            viewModel = liveCameraViewModel,
                            gardenId = sharedViewModel.gardenId,
                            gardenRequestId = sharedViewModel.gardenRequestId,
                            token = currentToken,
                            plant = sharedViewModel.gardenPlant,
                            onReportSaved = {
                                healthRefreshKey++
                            },
                            navController = navController,
                            onBack = { robotScreen = "camera" },
                        )
                    }
                }

                2 -> HealthCheckerScreen(refreshKey = healthRefreshKey)
            }
        }
    }
}
@Composable
fun BottomNavigationBar(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 10.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        val navItems = listOf(
            R.drawable.wheat_icon to "Градини",
            R.drawable.robot to "Робот",
            R.drawable.health_icon to "Здраве"
        )

        navItems.forEachIndexed { index, item ->
            val isSelected = selectedTab == index
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.clickable { onTabSelected(index) }
            ) {
                Box(
                    modifier = Modifier
                        .size(45.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isSelected) Color(0xFF436B1F) else Color.Transparent),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(item.first),
                        contentDescription = null,
                        modifier = Modifier.size(50.dp),
                        tint = if (isSelected) Color.White else Color(0xFF5E8A37)
                    )
                }
            }
        }
    }
}


