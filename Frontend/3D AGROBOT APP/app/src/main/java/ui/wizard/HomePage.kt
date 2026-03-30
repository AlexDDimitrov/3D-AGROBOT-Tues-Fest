package ui.wizard

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import com.example.a3d_agrobot_app.R

@Composable
fun HomeScreen(onLogout: () -> Unit = {}) {
    val context = LocalContext.current
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var gardenScreen by remember { mutableStateOf("list") }
    var selectedGarden by remember { mutableStateOf<GardenData?>(null) }

    LaunchedEffect(Unit) {
        firstName = withContext(Dispatchers.IO) {
            TokenStore.getFirstName(context) ?: "" }
        lastName = withContext(Dispatchers.IO) {
            TokenStore.getLastName(context) ?: ""
        }
    }

    var selectedTab by remember { mutableIntStateOf(0) }

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
                1 -> RobotScreen()
                2 -> HealthCheckerScreen()
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


