package ui.wizard.robot_setup

import android.os.Build
import android.webkit.WebView
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.a3d_agrobot_app.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.navigation.NavController
import kotlinx.coroutines.withContext

@RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
@Composable
fun LiveCameraScreen(
    header: String,
    viewModel: LiveCameraViewModel,
    gardenId: Int,
    gardenRequestId: Int,
    token: String,
    plant: String,
    onReportSaved: () -> Unit,
    navController: NavController,
    onBack: () -> Unit
) {
    var webViewRef by remember { mutableStateOf<WebView?>(null) }
    var analysisResult by remember { mutableStateOf("") }
    var isAnalyzing by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(1500)
        viewModel.onStreamLoaded()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFF4FAE8)),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
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

                StepProgressBar(currentStep = 4, totalSteps = 4)


                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(16f / 9f)
                            .background(Color.Black),
                        contentAlignment = Alignment.Center
                    ) {
                        if (!viewModel.hasError) {
                            AndroidView(
                                factory = { ctx ->
                                    WebView(ctx).apply {
                                        settings.javaScriptEnabled = true
                                        settings.loadWithOverviewMode = true
                                        settings.useWideViewPort = true
                                        settings.mediaPlaybackRequiresUserGesture = false
                                        setBackgroundColor(android.graphics.Color.TRANSPARENT)
                                        loadUrl(viewModel.streamUrl)
                                        webViewRef = this
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxSize()
                                    .then(
                                        if (viewModel.isLoading) Modifier.size(0.dp)
                                        else Modifier.fillMaxSize()
                                    )
                            )


                        if (viewModel.isLoading) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color(0xFF1A3207))
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(60.dp),
                                    color = Color(0xFF639922),
                                    strokeWidth = 4.dp,
                                    trackColor = Color(0x33FFFFFF)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "Свързване към стрийма...",
                                    color = Color.White,
                                    fontSize = 14.sp
                                )
                            }
                        }

                        if (viewModel.hasError) {
                            Text(
                                text = viewModel.errorMessage ?: "Проблем със свързването",
                                color = Color.White,
                                textAlign = TextAlign.Center,
                                fontSize = 16.sp
                            )
                        }
                    }


                }
                    Spacer(modifier = Modifier.height(8.dp))

                        Button(
                            onClick = {
                                val webView = webViewRef ?: return@Button
                                isAnalyzing = true
                                analysisResult = ""

                                //take screenshot
                                val bitmap = android.graphics.Bitmap.createBitmap(
                                    webView.width, webView.height,
                                    android.graphics.Bitmap.Config.ARGB_8888
                                )
                                val canvas = android.graphics.Canvas(bitmap)
                                webView.draw(canvas)

                                // Compress to JPEG and convert to base64
                                val stream = java.io.ByteArrayOutputStream()
                                bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 85, stream)
                                val base64Image  = android.util.Base64.encodeToString(
                                    stream.toByteArray(), android.util.Base64.NO_WRAP
                                )

                                CoroutineScope(Dispatchers.IO).launch {
                                    try {

                                        val body = org.json.JSONObject().apply {
                                            put("image", base64Image)
                                            put("garden_id", gardenId)
                                            put("garden_request_id", gardenRequestId)
                                            put("plant", plant)
                                        }

                                        // open Connection
                                        val conn = java.net.URL(
                                            "https://3d-agrobot-tues-fest-production.up.railway.app/report/analyze"
                                        ).openConnection() as java.net.HttpURLConnection
                                        conn.requestMethod = "POST"
                                        conn.setRequestProperty("Content-Type", "application/json")
                                        conn.setRequestProperty("Authorization", "Bearer $token")
                                        conn.doOutput = true

                                        // send Data
                                        conn.outputStream.use { os ->
                                            os.write(body.toString().toByteArray())
                                        }

                                        // read Response
                                        val response = conn.inputStream.bufferedReader().use { it.readText() }
                                        conn.disconnect()

                                        // parse result
                                        val json = org.json.JSONObject(response)

                                        // Only proceed if backend says result is 0
                                        if (json.optInt("result", -1) == 0) {
                                            val analysis = json.getJSONObject("analysis")

                                            val health = analysis.optString("health", "unknown")
                                            val plantType = analysis.optString("plant_type", "Непознато")

                                            val issuesArr = analysis.optJSONArray("issues")
                                            val issues = if (issuesArr != null)
                                                (0 until issuesArr.length()).map { issuesArr.getString(it) }
                                            else emptyList()

                                            val recsArr = analysis.optJSONArray("recommendations")
                                            val recommendations = if (recsArr != null)
                                                (0 until recsArr.length()).map { recsArr.getString(it) }
                                            else emptyList()

                                            val displayText = buildString {
                                                append("Здраве: ${when(health) {
                                                    "healthy" -> "Здраво"
                                                    "sick" -> "Болно "
                                                    "dead" -> "Мъртво"
                                                    else -> "Непознато"
                                                }}\n")
                                                append("Растение: $plantType\n")
                                                if (issues.isNotEmpty()) {
                                                    append("\nПроблеми:\n")
                                                    issues.forEach { append("• $it\n") }
                                                }
                                                if (recommendations.isNotEmpty()) {
                                                    append("\nСъвети:\n")
                                                    recommendations.forEach { append("• $it\n") }
                                                }
                                                append("\nОтчетът е запазен ✓")
                                            }

                                            withContext(Dispatchers.Main) {
                                                analysisResult = displayText
                                                isAnalyzing = false
                                                onReportSaved()
                                            }
                                        } else {
                                            val errorCode = json.optInt("result", 500)
                                            throw Exception("Сървърна грешка код: $errorCode")
                                        }

                                    } catch (e: Exception) {
                                        withContext(Dispatchers.Main) {
                                            analysisResult = "Грешка при анализ: ${e.localizedMessage}"
                                            isAnalyzing = false
                                        }
                                    }
                                }},
                            modifier = Modifier
                                .fillMaxWidth(0.8f)
                                .height(63.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF436B1F),
                                disabledContainerColor = Color(0xFFB0BFA8)
                            ),
                            enabled = !viewModel.isLoading && !viewModel.hasError,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            if (isAnalyzing) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    color = Color(0xFFEAF3DE),
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                            Text(
                                text = if (isAnalyzing) "Анализиране..." else "Снимайте и анализирайте",
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
    }
}