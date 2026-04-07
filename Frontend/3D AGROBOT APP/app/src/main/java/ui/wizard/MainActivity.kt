package ui.wizard

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.a3d_agrobot_app.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                AppNavigation(modifier = Modifier)
            }
        }
    }
}

fun showNotification(context: Context, message: String) {
    val channelId = "plant_health_alerts"
    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val channel = NotificationChannel(
            channelId,
            "Здраве на растенията",
            NotificationManager.IMPORTANCE_HIGH
        )
        notificationManager.createNotificationChannel(channel)
    }

    val notification = NotificationCompat.Builder(context, channelId)
        .setSmallIcon(R.mipmap.ic_launcher_round)
        .setContentTitle("Проблем с растение!")
        .setContentText(message)
        .setStyle(NotificationCompat.BigTextStyle().bigText(message))
        .setColor(android.graphics.Color.parseColor("#FFB74D"))
        .setAutoCancel(true)
        .build()

    notificationManager.notify(message.hashCode(), notification)
}

@Composable
fun AppNavigation(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    val context = LocalContext.current

    var startDestination by rememberSaveable { mutableStateOf<String>("Welcome") }

    LaunchedEffect(Unit) {
        val token = withContext(Dispatchers.IO) {
            TokenStore.getToken(context)
        }
        if (token != null) {
            startDestination = "Home"
        } else startDestination = "Welcome"
    }

    LaunchedEffect(Unit) {
        while (true) {
            val token = withContext(Dispatchers.IO) {
                TokenStore.getToken(context)
            }
            if (token != null) {
                try {
                    val reports = withContext(Dispatchers.IO) {
                        ReportRepository().getReports(token)
                    }
                    val sickPlants = reports.filter {
                        it.health == "sick" || it.health == "ill"
                    }
                    if (sickPlants.isNotEmpty()) {
                        val names = sickPlants.mapNotNull {
                            it.plantType }.joinToString(", "
                            )
                        showNotification(
                            context,
                            "Болни растения: $names. Моля, обърнете внимание!"
                        )
                    }
                } catch (e: Exception) {
                }
            }
            kotlinx.coroutines.delay(5 * 60 * 1000L)
        }
    }


    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable("Welcome") {
            WelcomeScreen(
                onLoginClick = { navController.navigate("Login") },
                onSignupClick = { navController.navigate("Signup")}
            )
        }
        composable("Login") {
            LoginScreen (
                onSuccess = {
                    navController.navigate("Home") {
                        popUpTo("Welcome") { inclusive = true }
                    }
                }
            )

        }
        composable("Signup") {
            SignupScreen(
                onSuccess = {
                navController.navigate("Home") {
                    popUpTo("Welcome") { inclusive = true }
                }
            }
            )
        }
        composable("Home") {
            HomeScreen(
                onLogout = {
                navController.navigate("Welcome") {
                    popUpTo("Welcome") { inclusive = true }
                }
            }
            )
        }
    }
}
