package ui.wizard.robot_setup

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.a3d_agrobot_app.R


@Composable
fun CameraSetupScreen(
    header: String,
    viewModel: CameraSetupViewModel,
    onContinue: () -> Unit,
    modifier: Modifier = Modifier,
    navController: NavController,
    onBack: () -> Unit
){
    LaunchedEffect(Unit) {
        viewModel.testCamera()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF4FAE8))
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    )
    {


        StepProgressBar(
            currentStep = 3,
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
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                //Status Circular Progress Indicator
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.3f),
                    contentAlignment = Alignment.Center
                ) {
                    if (viewModel.cameraStatus == CameraStatus.TESTING) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(140.dp),
                            color = Color(0xFF436B1F),
                            strokeWidth = ProgressIndicatorDefaults.CircularStrokeWidth,
                            trackColor = Color(0xFFD6E8C0),
                            strokeCap = ProgressIndicatorDefaults.CircularIndeterminateStrokeCap,
                        )
                    }
                    Image(
                        painter = painterResource(id = R.drawable.camera),
                        contentDescription = "Camera icon",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(80.dp),
                        colorFilter = ColorFilter.tint(Color(0xFF436B1F))
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = when (viewModel.cameraStatus) {
                        CameraStatus.TESTING -> "Тестване на връзката с камерата..."
                        CameraStatus.READY -> "Камерата е готова!"
                        CameraStatus.FAILED -> "Връзката с камерата беше неуспешна"
                    },
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = when (viewModel.cameraStatus) {
                        CameraStatus.READY -> Color(0xFF4CAF50)
                        CameraStatus.FAILED -> Color(0xFFE53935)
                        else -> Color(0xFF436B1F)
                    },
                    textAlign = TextAlign.Center
                )
            }
        }
        //Buttons
        Spacer(modifier = modifier.height(24.dp))
        Row(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            //Test Again button
            Button(
                onClick = {
                    viewModel.testCamera()
                },
                modifier = Modifier
                    .height(63.dp)
                    .weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF1F5E9)),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 8.dp,
                    pressedElevation = 12.dp
                ),
                shape = RoundedCornerShape(12.dp),
                enabled = viewModel.cameraStatus != CameraStatus.TESTING
            ) {
                Text(
                    text = "Тествайте отново",
                    color = Color(0xFF436B1F),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    softWrap = true,
                )
            }
            Spacer(modifier = Modifier.width(12.dp))



                //Continue button
                Button(
                    onClick = onContinue,
                    modifier = Modifier
                        .height(63.dp)
                        .weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF436B1F)),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 8.dp,
                        pressedElevation = 12.dp
                    ),
                    shape = RoundedCornerShape(12.dp),
                    enabled = viewModel.cameraStatus == CameraStatus.READY

                ) {
                    Text(
                        text = "Продължете",
                        color = Color(0xFFEAF3DE),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        softWrap = true,
                        maxLines = 2,
                    )

            }
        }
    }
}
