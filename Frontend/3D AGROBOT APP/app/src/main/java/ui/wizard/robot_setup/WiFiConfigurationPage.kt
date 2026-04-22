package ui.wizard.robot_setup

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.a3d_agrobot_app.R
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.navigation.NavController

@Composable
fun WifiConfigurationApp(
    header: String,
    viewModel: WiFiConfigurationViewModel,
    onContinue: (String) -> Unit,
    navController: NavController,
    onBack: () -> Unit
) {
    var ssid by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    // Request network scan when screen loads
    LaunchedEffect(Unit) {
        viewModel.requestNetworkScan()
    }
    val connectionState = viewModel.connectionState
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF4FAE8))
            .verticalScroll(rememberScrollState())
            .navigationBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally
    )
    {
        StepProgressBar(
            currentStep = 2,
            totalSteps = 4
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
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


        // Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF4FAE8)),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = R.drawable.wi_fi_signal),
                contentDescription = "Wi-Fi signal",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth(0.4f)
                    .aspectRatio(1f)
                    .padding(top = 16.dp),
                colorFilter = ColorFilter.tint(Color(0xFF436B1F))
            )
            Spacer(modifier = Modifier.height(16.dp))
            WiFiFormZone(
                networks = viewModel.networks,
                isScanning = viewModel.isScanning,
                onScanRequest = { viewModel.requestNetworkScan() },
                onCredentialsChanged = { newSsid, newPassword ->
                    ssid = newSsid
                    password = newPassword
                }
            )
            Spacer(modifier = Modifier.height(24.dp))

            WiFiStatusZone(
                connectionState = connectionState,
                deviceIp = viewModel.deviceIp
            )
        }
            Button(
                onClick = {
                    if (connectionState == WiFiConnectionState.CONNECTED) {
                        viewModel.deviceIp?.let { ip -> onContinue(ip) }
                    } else {
                        viewModel.sendCredentials(ssid, password)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp)
                    .height(52.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (connectionState !== WiFiConnectionState.CONNECTED)
                            Color.White else Color(0xFF436B1F),
                    disabledContainerColor = Color(0xFFE0E0E0)
                ),
                shape = RoundedCornerShape(12.dp),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 8.dp,
                    pressedElevation = 12.dp
                ),
                enabled = when (connectionState) {
                    WiFiConnectionState.CONNECTED -> true
                    WiFiConnectionState.SENDING,
                    WiFiConnectionState.CONNECTING -> false

                    else -> ssid.isNotBlank() && password.isNotBlank()
                }
            ) {
                Text(
                    text = when (connectionState) {
                        WiFiConnectionState.CONNECTED -> "Продължете"
                        else -> "Свържете"
                    },
                    color = if (connectionState !== WiFiConnectionState.CONNECTED) Color(0xFF436B1F) else Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    softWrap = true,
                    maxLines = 2,
                )
        }
    }
}

@Composable
fun WiFiStatusZone(
    connectionState: WiFiConnectionState,
    deviceIp: String?
) {
    val statusText = when (connectionState) {
        WiFiConnectionState.IDLE -> ""
        WiFiConnectionState.SENDING -> "Изпращане на данните до ESP32..."
        WiFiConnectionState.CONNECTING -> "ESP32 се свързва с WiFi..."
        WiFiConnectionState.CONNECTED -> "Успешно свързване!"
        WiFiConnectionState.FAILED -> "Неуспешно свързване"
    }

    if (statusText.isNotEmpty()) {
        Text(
            text = statusText,
            fontSize = 14.sp,
            color = when (connectionState) {
                WiFiConnectionState.CONNECTED -> Color(0xFF4CAF50)
                WiFiConnectionState.FAILED -> Color(0xFFE53935)
                else -> Color(0xFF436B1F)
            },
            fontWeight = if (connectionState == WiFiConnectionState.CONNECTED ||
                connectionState == WiFiConnectionState.FAILED
            )
                FontWeight.Bold else FontWeight.Normal,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun WiFiFormZone(
    networks: List<String>,
    isScanning: Boolean,
    onScanRequest: () -> Unit,
    onCredentialsChanged: (String, String) -> Unit

) {
    val itemPosition = remember { mutableStateOf(0) }

    var password by rememberSaveable { mutableStateOf("") }

    // Get selected SSID
    val selectedSsid = if (networks.isNotEmpty() && itemPosition.value < networks.size) {
        networks[itemPosition.value]
    } else {
        ""
    }
// Update parent whenever SSID or password changes
    LaunchedEffect(selectedSsid, password) {
        onCredentialsChanged(selectedSsid, password)
    }

    val isDropDownExpanded = remember { mutableStateOf(false) }
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxWidth(0.9f)
            .padding(horizontal = 16.dp)
    ) {
        // Scan Networks Button
        OutlinedButton(
            onClick = { onScanRequest() },
            modifier = Modifier
                .fillMaxWidth(),
            enabled = !isScanning,
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = Color(0xFF436B1F)
            )

        ) {
            if (isScanning) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                    color = Color(0xFF436B1F)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Сканиране...")
            } else {
                Text("Сканирайте мрежи")
            }
        }
        Spacer(modifier = Modifier.width(16.dp))
        // SSID dropdown
        if (networks.isEmpty()) {
            // Show placeholder when no networks available
            OutlinedTextField(
                value = "",
                onValueChange = { },
                label = { Text("WiFi мрежа" ) },
                placeholder = { Text("Няма налични мрежи") },
                readOnly = true,
                enabled = false,
                modifier = Modifier.fillMaxWidth()
            )
        } else {
            Box {
                OutlinedTextField(
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF436B1F),
                        unfocusedBorderColor = Color(0xFF9E9E9E),
                        focusedLabelColor = Color(0xFF436B1F),
                        unfocusedLabelColor = Color(0xFF9E9E9E),
                        cursorColor = Color(0xFF436B1F)
                    ),
                    value = selectedSsid,
                    onValueChange = { },
                    label = { Text("WiFi мрежа") },
                    readOnly = true,
                    trailingIcon = {
                        IconButton(onClick = { isDropDownExpanded.value = true }) {
                            Image(
                                painter = painterResource(id = R.drawable.drop_down_ic),
                                contentDescription = "Expand dropdown"
                            )
                        }
                    }
                    ,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { isDropDownExpanded.value = true }
                )

                DropdownMenu(
                    expanded = isDropDownExpanded.value,
                    onDismissRequest = {
                        isDropDownExpanded.value = false
                    },
                    modifier =
                        Modifier.
                        heightIn(max = 200.dp).
                        background(Color(0xFFF1F5E9))
                ) {
                    networks.forEachIndexed { index, network ->
                        DropdownMenuItem(
                            text = {
                                Text(text = network)
                            },
                            onClick = {
                                isDropDownExpanded.value = false
                                itemPosition.value = index
                            })
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))


        //Password Field
        var passwordVisibility by remember { mutableStateOf(false) }

        val icon = if (passwordVisibility)
            painterResource(id = R.drawable.visibility_icon)
        else
            painterResource(id = R.drawable.visibility_icon_off)

        OutlinedTextField(
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF436B1F),
                unfocusedBorderColor = Color(0xFF9E9E9E),
                focusedLabelColor = Color(0xFF436B1F),
                unfocusedLabelColor = Color(0xFF9E9E9E),
                cursorColor = Color(0xFF436B1F)
            ),
            value = password,
            onValueChange = {
                password = it
            },
            placeholder = { Text(text = "Парола") },
            label = { Text(text = "Wi-Fi парола") },
            trailingIcon = {
                IconButton(onClick = {
                    passwordVisibility = !passwordVisibility
                }) {
                    Icon(
                        painter = icon,
                        contentDescription = "Visibility icon"
                    )
                }
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password
            ),
            visualTransformation = if (passwordVisibility)
                VisualTransformation.None
            else
                PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            enabled = !isScanning
        )
    }
}








