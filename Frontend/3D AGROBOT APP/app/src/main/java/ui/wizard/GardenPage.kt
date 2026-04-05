package ui.wizard

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import  androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import com.example.a3d_agrobot_app.R
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
public fun GardenScreen(
    onAddClick: () -> Unit,
    onEditClick: (GardenData) -> Unit
) {
    val context = LocalContext.current

    var gardens by remember { mutableStateOf<List<GardenData>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }

    val totalBeds = gardens.sumOf { it.number_beds }

    LaunchedEffect(Unit) {
        val token = withContext(Dispatchers.IO) {
                TokenStore.getToken(context)
        } ?: return@LaunchedEffect
        gardens = withContext(Dispatchers.IO) {
            GardenRepository().getGardens(token)
        }
        loading = false
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

            Text("Моите градини", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A3207))

            Spacer(modifier = Modifier.height(20.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard("Градини", gardens.size.toString(), Modifier.weight(1f))
                StatCard("Активни лехи", totalBeds.toString(), Modifier.weight(1f))
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onAddClick,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE2EDD1)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(55.dp)
            ) {
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Създайте градина",
                    color = Color(0xFF436B1F),
                    fontWeight = FontWeight.SemiBold
                )
            }
            Spacer(modifier = Modifier.height(16.dp))

            if (loading) {
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFF436B1F))
                }
            } else if (gardens.isEmpty()) {
                Text("Няма добавени градини", modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center, color = Color.Gray)
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    itemsIndexed(gardens) { index, garden ->
                        GardenListItem(
                            displayIndex = index + 1,
                            garden = garden,
                            onEditClick = { onEditClick(garden) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
public fun GardenListItem(displayIndex: Int, garden: GardenData, onEditClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .clickable { onEditClick() }
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

        IconButton(onClick = onEditClick) {
            Icon(
                painter = painterResource(id = R.drawable.edit_icon),
                contentDescription = "Edit",
                tint = Color(0xFF436B1F)
            )
        }
    }
}

@Composable
public fun StatCard(label: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .background(Color.White, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Text(
            label,
            color = Color(0xFF5E8A37),
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium)
        Text(
            value,
            color = Color(0xFF1A3207),
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold)
    }
}

@Composable
public fun CreateGardenScreen(onSuccess: () -> Unit, onBack: () -> Unit ) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var gardenName by rememberSaveable { mutableStateOf("") }
    var width by rememberSaveable { mutableStateOf("") }
    var height by rememberSaveable { mutableStateOf("") }
    var pathWidth by rememberSaveable { mutableStateOf("") }
    var beds by rememberSaveable { mutableStateOf("") }
    var plant by rememberSaveable { mutableStateOf("") }
    var plantsNum by rememberSaveable { mutableStateOf("") }
    var statusMessage by rememberSaveable { mutableStateOf("") }
    var isError by rememberSaveable { mutableStateOf(false) }
    var isLoading by rememberSaveable { mutableStateOf(false) }

    val textFieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = Color(0xFF436B1F),
        unfocusedBorderColor = Color.Transparent,
        focusedContainerColor = Color.White,
        unfocusedContainerColor = Color.White,
        cursorColor = Color(0xFF436B1F),
        focusedLabelColor = Color(0xFF436B1F),
        unfocusedLabelColor = Color.Gray,
    )

    val isFormFilled = gardenName.isNotBlank() && width.isNotBlank() &&
            height.isNotBlank() && pathWidth.isNotBlank() &&
            beds.isNotBlank() && plant.isNotBlank() && plantsNum.isNotBlank()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF1F5E9))
            .statusBarsPadding()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.background(
                    Color.White, CircleShape
                ).size(40.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.back_icon),
                    contentDescription = "Back",
                    tint = Color(0xFF436B1F),
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                "Нова градина",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A3207)
            )
        }
        Spacer(modifier = Modifier.height(32.dp))
        OutlinedTextField(
            value = gardenName,
            onValueChange = { gardenName = it },
            label = { Text("Име на градината") },
            singleLine = true, colors = textFieldColors,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(12.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = width, onValueChange = { width = it },
                label = { Text("Ширина (см)") },
                singleLine = true, colors = textFieldColors,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Next
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = height, onValueChange = { height = it },
                label = { Text("Дължина (см)") },
                singleLine = true, colors = textFieldColors,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Next
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = pathWidth, onValueChange = { pathWidth = it },
                label = { Text("Ширина пътека (cм)") },
                singleLine = true, colors = textFieldColors,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Next
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1f)
            )
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = beds,
                onValueChange = { beds = it },
                label = { Text("Брой лехи") },
                singleLine = true,
                colors = textFieldColors,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Next
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = plantsNum,
                onValueChange = { plantsNum = it },
                label = { Text("Брой растения на леха") },
                singleLine = true,
                colors = textFieldColors,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Next
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
            value = plant,
            onValueChange = { plant = it },
            label = { Text("Вид растение") },
            singleLine = true,
            colors = textFieldColors,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(24.dp))

        if (statusMessage.isNotEmpty()) {
            Text(
                text = statusMessage,
                color = if (isError) Color(0xFFD32F2F) else Color(0xFF436B1F),
                fontSize = 14.sp,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }
        Button(
            onClick = {
                isLoading = true
                scope.launch(Dispatchers.IO) {
                    try {
                        val token = TokenStore.getToken(context) ?: throw Exception("Няма токен")
                        val code = GardenRepository().createGarden(
                            token,
                            GardenData(
                                garden_name = gardenName,
                                garden_width = width.toInt(),
                                garden_height = height.toInt(),
                                path_width = pathWidth.toInt(),
                                number_beds = beds.toInt(),
                                plant = plant,
                                plantsNum = plantsNum.toInt()
                            )
                        )
                        withContext(Dispatchers.Main) {
                            if (code in 200..299) {
                                onSuccess()
                            } else {
                                statusMessage = "Грешка от сървъра: $code"
                                isError = true
                                isLoading = false
                            }
                        }
                    } catch (e: Exception) {
                        withContext(Dispatchers.Main) {
                            statusMessage = "Грешка: ${e.message}"
                            isError = true
                            isLoading = false
                        }
                    }
                }
            },
            enabled = isFormFilled && !isLoading,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF436B1F),
                contentColor = Color.White,
                disabledContainerColor = Color(0xFF436B1F).copy(alpha = 0.5f),
                disabledContentColor = Color.White.copy(alpha = 0.5f)
            ),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            if (isLoading) CircularProgressIndicator(
                color = Color.White,
                strokeWidth = 2.dp,
                modifier = Modifier.size(20.dp)
            )
            else Text("Създайте градина", fontSize = 16.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
public fun EditGardenScreen(garden: GardenData, onSuccess: () -> Unit, onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var gardenName by rememberSaveable { mutableStateOf(garden.garden_name) }
    var width by rememberSaveable { mutableStateOf(garden.garden_width.toString()) }
    var height by rememberSaveable { mutableStateOf(garden.garden_height.toString()) }
    var pathWidth by rememberSaveable { mutableStateOf(garden.path_width.toString()) }
    var beds by rememberSaveable { mutableStateOf(garden.number_beds.toString()) }
    var plant by rememberSaveable { mutableStateOf(garden.plant) }
    var plantsNum by rememberSaveable { mutableStateOf(garden.plantsNum.toString()) }

    var statusMessage by rememberSaveable { mutableStateOf("") }
    var isLoading by rememberSaveable { mutableStateOf(false) }
    var isError by rememberSaveable { mutableStateOf(false) }

    val textFieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = Color(0xFF436B1F),
        unfocusedBorderColor = Color.Transparent,
        focusedContainerColor = Color.White,
        unfocusedContainerColor = Color.White,
        cursorColor = Color(0xFF436B1F),
        focusedLabelColor = Color(0xFF436B1F),
        unfocusedLabelColor = Color.Gray,
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF1F5E9))
            .statusBarsPadding()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .background(Color.White, CircleShape).
                        size(40.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.back_icon),
                        contentDescription = "Back",
                        tint = Color(0xFF436B1F),
                        modifier = Modifier.size(20.dp)
                    )
                }
            Spacer(modifier = Modifier.height(32.dp))
            Text(
                "Редактирайте градина",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A3207)
            )
            }
            OutlinedTextField(
                value = gardenName,
                onValueChange = { gardenName = it },
                label = { Text("Име на градината") },
                singleLine = true,
                colors = textFieldColors,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = plant,
                onValueChange = { plant = it },
                label = { Text("Вид растение") },
                singleLine = true,
                colors = textFieldColors,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = beds,
                onValueChange = { beds = it },
                label = { Text("Брой лехи") },
                singleLine = true, colors = textFieldColors,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Next
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = plantsNum,
                onValueChange = { plantsNum = it },
                label = { Text("Брой растения на леха") },
                singleLine = true, colors = textFieldColors,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Next
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            if (statusMessage.isNotEmpty()) {
                Text(
                    statusMessage,
                    color = if (isError) Color(0xFFE57373) else Color(0xFF3B6D11),
                    fontSize = 13.sp,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
            }

            Button(
                onClick = {
                    isLoading = true
                    scope.launch(Dispatchers.IO) {
                        try {
                            val token =
                                TokenStore.getToken(context) ?: throw Exception("Няма токен")
                            val updatedData = GardenData(
                                id = garden.id,
                                garden_name = gardenName,
                                garden_width = width.toInt(),
                                garden_height = height.toInt(),
                                path_width = pathWidth.toInt(),
                                number_beds = beds.toInt(),
                                plant = plant,
                                plantsNum = plantsNum.toInt()
                            )
                            val code = GardenRepository().editGarden(token, garden.id, updatedData)

                            withContext(Dispatchers.Main) {
                                if (code in 200..299) {
                                    onSuccess()
                                } else {
                                    statusMessage = "Грешка от сървъра: $code"
                                    isError = true
                                    isLoading = false
                                }
                            }
                        } catch (e: Exception) {
                            withContext(Dispatchers.Main) {
                                statusMessage = "Грешка: ${e.message}"
                                isError = true
                                isLoading = false
                            }
                        }
                    }
                },
                enabled = gardenName.isNotBlank() && plant.isNotBlank() && beds.isNotBlank() && !isLoading,
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
                if (isLoading) CircularProgressIndicator(
                    color = Color.White,
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(20.dp)
                )
                else Text("Запазете промените", fontSize = 16.sp, fontWeight = FontWeight.Medium)
            }
        }
    }



