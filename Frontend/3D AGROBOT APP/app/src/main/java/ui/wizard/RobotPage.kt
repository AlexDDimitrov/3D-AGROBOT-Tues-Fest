package ui.wizard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.a3d_agrobot_app.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun RobotScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var gardens by remember { mutableStateOf<List<GardenData>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var robotStatus by remember { mutableStateOf<Int?>(null) }
    var statusMessage by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var token by remember { mutableStateOf<String?>(null) }
    val totalBeds = gardens.sumOf { it.number_beds }


    fun connectRobot(gardenId: Int) {
        isLoading = true
        statusMessage = ""
        scope.launch(Dispatchers.IO) {
            try {
                val result = StartRobotRequest().startRobot(token!!, gardenId)
                withContext(Dispatchers.Main) {
                    statusMessage = when (result) {
                        0    -> "Заявката е изпратена!"
                        301  -> "Вече има активна заявка"
                        202  -> "Моля изберете градина"
                        else -> "Неизвестна грешка: $result"
                    }
                    isLoading = false
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    statusMessage = "Грешка: ${e.message}"
                    isLoading = false
                }
            }
        }
    }


    LaunchedEffect(Unit) {
        token = withContext(Dispatchers.IO) {
            TokenStore.getToken(context)
        } ?: return@LaunchedEffect
        gardens = withContext(Dispatchers.IO) {
            GardenRepository().getGardens(token!!)
        }
        loading = false
    }

    LaunchedEffect(Unit) {
        while (true) {
            val currentToken = TokenStore.getToken(context)
            if (currentToken != null) {
                val status = withContext(Dispatchers.IO) {
                    try {
                        StartRobotRequest().getStatus(currentToken)
                    } catch (e: Exception) {
                        null
                    }
                }
                robotStatus = status
            }
            kotlinx.coroutines.delay(5000)
        }
    }

    val (statusText, statusColor) = when {
        statusMessage.isNotEmpty() -> {
            val color = if (statusMessage.startsWith("Грешка") || statusMessage.startsWith("Вече"))
                Color(0xFFE57373) else Color(0xFF3B6D11)
            statusMessage to color
        }
        else -> when (robotStatus) {
            0 -> "Осъществяване на връзка..."  to Color(0xFF888800)
            1 -> "Роботът извършва работа" to Color(0xFF3B6D11)
            2 -> "Роботът приключи с работата си" to Color(0xFF1D6B2F)
            null -> "Няма активна заявка" to Color.Gray
            else -> "Статус: $robotStatus" to Color.Gray
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF1F5E9)),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            Text(
                "Изберете градина, за да стартирате робота",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A3207)
            )

            Spacer(modifier = Modifier.height(20.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard("Градини", gardens.size.toString(), Modifier.weight(1f))
                StatCard("Активни лехи", totalBeds.toString(), Modifier.weight(1f))
            }
            Spacer(modifier = Modifier.height(8.dp))


            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(statusColor.copy(alpha = 0.1f))
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                Text(
                    text = statusText,
                    color = statusColor,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            if (loading) {
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFF436B1F))
                }
            } else if (gardens.isEmpty()) {
                Text(
                    "Няма добавени градини",
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    color = Color.Gray
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    itemsIndexed(gardens) { index, garden ->
                        ListItem(
                            displayIndex = index + 1,
                            garden = garden,
                            onStartClick = { connectRobot(garden.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ListItem(displayIndex: Int, garden: GardenData, onStartClick: (Int) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .clickable { onStartClick(garden.id) }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(Color(0xFFF1F5E9), RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(R.drawable.wheat_icon),
                contentDescription = null,
                tint = Color(0xFF5E8A37),
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "$displayIndex. ${garden.garden_name}",
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A3207),
                fontSize = 16.sp
            )
            Text(
                text = "${garden.plant} | ${garden.number_beds} лехи",
                color = Color.Gray,
                fontSize = 13.sp
            )
        }

        IconButton(onClick = { onStartClick(garden.id) }) {
            Icon(
                painter = painterResource(id = R.drawable.start_icon),
                contentDescription = "Edit",
                tint = Color(0xFF3B6D11)
            )
        }
    }
}

