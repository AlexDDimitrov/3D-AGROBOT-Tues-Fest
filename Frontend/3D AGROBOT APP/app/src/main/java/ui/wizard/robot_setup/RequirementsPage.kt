package ui.wizard.robot_setup

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.a3d_agrobot_app.R


@Composable
fun RequirementsPage(
    checklistHeader: String,
    condition1: String,
    condition2: String,
    condition3: String,
    condition4: String,
    buttonLabel: String,
    imagePainter: Painter,
    onContinue: () -> Unit,
    modifier: Modifier = Modifier,
    navController: NavController,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF4FAE8))
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        // Progress Bar
        StepProgressBar(
            currentStep = 0,
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
                text = "Свързване с робота",
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF1A3207)
            )

        }
        // Content
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Image
            Image(
                painter = imagePainter,
                contentDescription = "Photograph of the robot",
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .aspectRatio(1f)
                    .padding(top = 16.dp, bottom = 16.dp)
                    .clip(RoundedCornerShape(40.dp)),
                contentScale = ContentScale.Fit

            )

            // Ensure conditions
            Column(
                modifier = Modifier
                    .padding(
                        start = 15.dp,
                        top = 15.dp,
                        end = 15.dp,
                        bottom = 0.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = checklistHeader,
                    style = TextStyle(
                        color = Color(0xFF212121),
                        fontSize = 14.sp,
                        fontFamily = FontFamily.SansSerif,
                        fontWeight = FontWeight.Normal,
                        lineHeight = 20.sp,
                        textAlign = TextAlign.Center
                    ),
                    modifier = modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp, bottom = 16.dp)
                )

                val bulletPoints = listOf(condition1, condition2, condition3, condition4)
                Column(
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .padding(top = 8.dp)
                ) {
                    bulletPoints.forEach { condition ->
                        Row(
                            verticalAlignment = Alignment.Top,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp)
                        ) {
                            Text(
                                text = "•",
                                color = Color(0xFF5E8A37),
                                fontSize = 20.sp,
                                modifier = Modifier.padding(end = 8.dp)
                            )
                            Text(
                                text = condition,
                                style = TextStyle(
                                    color = Color(0xFF212121),
                                    fontSize = 14.sp,
                                    fontFamily = FontFamily.SansSerif,
                                    fontWeight = FontWeight.Normal,
                                    lineHeight = 22.sp
                                )
                            )
                        }
                    }
                }
            }
        }

            Button(
                onClick = onContinue,
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(63.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF436B1F)),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 8.dp,
                    pressedElevation = 12.dp
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = buttonLabel,
                    color = Color(0xFFEAF3DE),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    softWrap = false,
                    maxLines = 2,
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

}

@Composable
fun StepProgressBar(
    currentStep: Int,
    totalSteps: Int,
    modifier: Modifier = Modifier
) {
    val progress = (currentStep.toFloat() / totalSteps.toFloat())
    val percentage = (progress * 100).toInt()

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(17.dp)
            .background(Color(0xFFD6E8C0))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(progress)
                .fillMaxHeight()
                .background(
                    Color(0xFF436B1F
                    )
                )
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.CenterEnd
        ) {
            Text(
                text = "$percentage%",
                style = TextStyle(
                    color = if (progress > 0.99f) Color(0xFFEAF3DE) else Color(0xFF3B6D11),
                    fontSize = 12.sp,
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.Bold
                )
            )
        }
    }
}

@Composable
fun RequirementsPageContent(
    onContinue: () -> Unit, navController: NavController, onBack: () -> Unit
) {
    RequirementsPage(
        checklistHeader = "За да се свържете с робота, трябва следните условия да са изпълнени:",
        condition1 = "Роботът е включен",
        condition2 = "Bluetooth-ът на устройството е включен",
        condition3 = "Наясно сте с Wi-Fi данните си",
        condition4 = "ESP32-CAM е в режим на свързване",
        buttonLabel = "Изграждане на връзката",
        imagePainter = painterResource(R.drawable.robot_pic),
        onContinue = onContinue,
        navController = navController,
        onBack = onBack,
    )
}