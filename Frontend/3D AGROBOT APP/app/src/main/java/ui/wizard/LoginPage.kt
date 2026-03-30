package ui.wizard

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
fun LoginScreen(onSuccess: () -> Unit = {}) {
    var email by remember { mutableStateOf(String()) }
    var isValid by remember { mutableStateOf(true) }
    var password by remember { mutableStateOf(String()) }
    var passwordVisibility by remember { mutableStateOf(false) }
    var statusMessage by rememberSaveable { mutableStateOf("") }
    var isError by rememberSaveable { mutableStateOf(false) }

    val isFormFilled = email.isNotBlank() && password.isNotBlank() && password.length >= 6

    val icon = if (passwordVisibility)
        painterResource(id = R.drawable.visibility_icon)
    else
        painterResource(id = R.drawable.visibility_icon_off)

    val textFieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = Color(0xFF3B6D11),
        unfocusedBorderColor = Color(0xFF639922).copy(alpha = 0.5f),
        focusedLabelColor = Color(0xFF3B6D11),
        unfocusedLabelColor = Color(0xFF639922),
        cursorColor = Color(0xFF3B6D11),
        focusedTextColor = Color(0xFF27500A),
        unfocusedTextColor = Color(0xFF27500A),
        unfocusedContainerColor = Color(0x33FFFFFF),
        focusedContainerColor = Color(0x55FFFFFF),
    )

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.wheat),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xCC1A3A1A),
                            Color(0x9927500A),
                            Color(0xCC0D2D1A)
                        )
                    )
                )
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding()
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "3D AgroBot",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFEAF3DE)
            )
            Text(
                text = "Влезте в своя акаунт",
                fontSize = 14.sp,
                color = Color(0xFF639922),
                modifier = Modifier.padding(bottom = 32.dp)
            )
            val context = androidx.compose.ui.platform.LocalContext.current

            Box(
                modifier = Modifier
                    .widthIn(max = 480.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xCCF4FAE8))
                    .padding(24.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    OutlinedTextField(
                        value = email,
                        label = { Text("Имейл") },
                        placeholder = { Text("Имейл") },
                        onValueChange = { new_email ->
                            email = new_email
                            isValid =
                                android.util.Patterns.EMAIL_ADDRESS.matcher(new_email).matches()
                        },
                        isError = !isValid && email.isNotEmpty(),
                        supportingText = {
                            if (!isValid && email.isNotEmpty()) {
                                Text("Невалиден имейл", color = Color(0xFFE57373))
                            }
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        colors = textFieldColors,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()

                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = password,
                        onValueChange = { newPassword ->
                            password = newPassword
                        },
                        placeholder = { Text(text = "Парола(6 цифри)") },
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
                        label = { Text(text = "Парола") },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password
                        ),
                        visualTransformation = if (passwordVisibility)
                            VisualTransformation.None
                        else
                            PasswordVisualTransformation(),
                        singleLine = true,
                        colors = textFieldColors,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    if (statusMessage.isNotEmpty()) {
                        Text(
                            text = statusMessage,
                            color = if (isError) Color(0xFFE57373) else Color(0xFF3B6D11),
                            fontSize = 13.sp,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                    }
                    Button(
                        onClick = {
                            CoroutineScope(Dispatchers.IO).launch {

                                try {
                                    val response = LoginData().login(
                                        email, password
                                    )

                                    val result = JSONObject(response).getInt("result")
                                    withContext(Dispatchers.Main) {
                                        statusMessage = when (result) {
                                            0 -> "Успешен вход"
                                            105 -> "Грешна парола или имейл"
                                            else -> "Неизвестна грешка: $result"
                                        }
                                        isError = result != 0
                                    }

                                    if (result == 0) {
                                        val json = JSONObject(response)
                                        val token = json.getString("token")
                                        val user = json.getJSONObject("user")
                                        val firstName = user.getString("first_name")
                                        val lastName = user.getString("last_name")
                                        TokenStore.saveToken(context, token, firstName, lastName)
                                        withContext(Dispatchers.Main) {
                                            onSuccess()
                                        }
                                    }
                                } catch (e: Exception) {
                                    withContext(Dispatchers.Main) {
                                        statusMessage = "Грешка: Опитайте отноео"
                                        isError = true
                                    }
                                }

                            }
                        },
                        enabled = isFormFilled,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF3B6D11),
                            contentColor = Color(0xFFEAF3DE),
                            disabledContainerColor = Color(0xFF3B6D11).copy(alpha = 0.5f),
                            disabledContentColor = Color(0xFFEAF3DE).copy(alpha = 0.5f)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                    ) {
                        Text(
                            text = "Влезте в акаунта си",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                }
            }
        }
    }
}