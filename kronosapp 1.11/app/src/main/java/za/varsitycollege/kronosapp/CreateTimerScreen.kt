package za.varsitycollege.kronosapp

import android.Manifest
import android.app.Activity
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.text.ParseException
import java.util.Calendar

class NewScreenActivity : ComponentActivity() {
    companion object {
        const val REQUEST_CAMERA_PERMISSION = 101
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val categoryManager = CategoryManager()
        val projectManager = ProjectManager()
        val timerManager = TimerManager()
        setContent {
            NewBlackScreen(
                categoryManager = categoryManager,
                projectManager = projectManager,
                timerManager = timerManager,
                navigateToTimers = { finish() },
                requestCodeCameraPermission = REQUEST_CAMERA_PERMISSION
            )
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), REQUEST_CAMERA_PERMISSION)
        }
    }
}

@Composable
fun NewBlackScreen(
    categoryManager: CategoryManager,
    projectManager: ProjectManager,
    timerManager: TimerManager,
    navigateToTimers: () -> Unit,
    requestCodeCameraPermission: Int
) {
    val context = LocalContext.current
    var imageBitmap by remember { mutableStateOf<android.graphics.Bitmap?>(null) }
    var date by remember { mutableStateOf("") }
    var startTime by remember { mutableStateOf("") }
    var endTime by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedProjectIndex by remember { mutableStateOf(0) }
    var selectedClientIndex by remember { mutableStateOf(0) }
    var selectedCategoryIndex by remember { mutableStateOf(0) }
    val calendar = Calendar.getInstance()
    val year = calendar.get(Calendar.YEAR)
    val month = calendar.get(Calendar.MONTH)
    val day = calendar.get(Calendar.DAY_OF_MONTH)
    val hour = calendar.get(Calendar.HOUR_OF_DAY)
    val minute = calendar.get(Calendar.MINUTE)

    val timeFormat = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault())

    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data
            imageBitmap = data?.extras?.get("data") as? android.graphics.Bitmap
        } else {
            Toast.makeText(context, "Failed to capture image.", Toast.LENGTH_LONG).show()
        }
    }

    fun takePicture() {
        val permissionCheck = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
            val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            try {
                takePictureLauncher.launch(takePictureIntent)
            } catch (e: ActivityNotFoundException) {
                Toast.makeText(context, "Error: Camera not available.", Toast.LENGTH_LONG).show()
            } catch (e: Exception) {
                Toast.makeText(context, "An unexpected error occurred while opening the camera.", Toast.LENGTH_LONG).show()
            }
        } else {
            ActivityCompat.requestPermissions(
                context as Activity,
                arrayOf(Manifest.permission.CAMERA),
                requestCodeCameraPermission
            )
        }
    }

    val datePickerDialog = DatePickerDialog(
        context,
        { _, selectedYear, selectedMonth, selectedDayOfMonth ->
            date = "$selectedDayOfMonth/${selectedMonth + 1}/$selectedYear"
        }, year, month, day
    )

    val startTimePickerDialog = TimePickerDialog(
        context,
        { _, selectedHour, selectedMinute ->
            startTime = String.format("%02d:%02d", selectedHour, selectedMinute)
        }, hour, minute, true
    )

    val endTimePickerDialog = TimePickerDialog(
        context,
        { _, selectedHour, selectedMinute ->
            endTime = String.format("%02d:%02d", selectedHour, selectedMinute)
        }, hour, minute, true
    )

    Scaffold {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .padding(it)
        ) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                CreateTimerGradientText("Create New Timer:", 24.sp)
                Spacer(modifier = Modifier.height(16.dp))

                CreateTimerGradientText("Select a Project:", 20.sp)
                ProjectNameDropdown(
                    projects = projectManager.getProjects(),
                    selectedIndex = selectedProjectIndex,
                    onIndexChanged = { selectedProjectIndex = it }
                )

                CreateTimerGradientText("Select a Client:", 20.sp)
                ClientNameDropdown(
                    projects = projectManager.getProjects(),
                    selectedIndex = selectedClientIndex,
                    onIndexChanged = { selectedClientIndex = it }
                )

                CreateTimerGradientText("Select a Category:", 20.sp)
                CategoryDropdown(
                    categories = categoryManager.getCategories(),
                    selectedIndex = selectedCategoryIndex,
                    onIndexChanged = { selectedCategoryIndex = it }
                )

                CreateTimerGradientText("Date:", 20.sp)
                Button(
                    onClick = { datePickerDialog.show() },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFFFFA500))
                ) {
                    Text(if (date.isNotEmpty()) date else "Select Date", color = Color.Black)
                }

                Row(horizontalArrangement = Arrangement.SpaceBetween) {
                    Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                        CreateTimerGradientText("Start Time:", 20.sp)
                        Button(
                            onClick = { startTimePickerDialog.show() },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF00FFFF))
                        ) {
                            Text(if (startTime.isNotEmpty()) startTime else "Select Start Time", color = Color.Black)
                        }
                    }
                    Column(modifier = Modifier.weight(1f).padding(start = 8.dp)) {
                        CreateTimerGradientText("End Time:", 20.sp)
                        Button(
                            onClick = { endTimePickerDialog.show() },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF00FFFF)) // Cyan color
                        ) {
                            Text(if (endTime.isNotEmpty()) endTime else "Select End Time", color = Color.Black)
                        }
                    }
                }

                CreateTimerGradientText("Attach an Image:", 20.sp)
                Button(
                    onClick = { takePicture() },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFFFFA500))
                ) {
                    Text("Add Image", color = Color.Black)
                }

                imageBitmap?.let {
                    Text(
                        text = "Image Attached",
                        color = Color.Blue,
                        modifier = Modifier.padding(20.dp)
                    )
                }

                VerticalScroller {
                    CreateTimerGradientText("Description:", 20.sp)
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Enter description", color = Color.Gray) },
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            textColor = Color.White,
                            backgroundColor = Color.Transparent,
                            cursorColor = Color.White,
                            focusedBorderColor = Color(0xFF2196F3),
                            unfocusedBorderColor = Color(0xFFFF9800)
                        ),
                        modifier = Modifier.padding(8.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    GradientButton(text = "Add", onClick = {
                        if (description.isNotBlank()) {
                            val startCalendar = java.util.Calendar.getInstance()
                            val endCalendar = java.util.Calendar.getInstance()
                            try {
                                startCalendar.time = timeFormat.parse(startTime)
                                endCalendar.time = timeFormat.parse(endTime)
                                val durationMillis = endCalendar.timeInMillis - startCalendar.timeInMillis
                                val hours = durationMillis / (1000 * 60 * 60).toFloat()
                                if (hours < 0) {
                                    Toast.makeText(context, "End time must be after start time", Toast.LENGTH_LONG).show()
                                    return@GradientButton
                                }

                                val timer = Timer(
                                    project = projectManager.getProjects()[selectedProjectIndex].projectName,
                                    client = projectManager.getProjects()[selectedClientIndex].client,
                                    category = categoryManager.getCategories()[selectedCategoryIndex].name,
                                    date = date,
                                    startTime = startTime,
                                    endTime = endTime,
                                    description = description,
                                    image = imageBitmap,
                                    hours = hours
                                )
                                timerManager.addTimer(timer, categoryManager)
                                navigateToTimers()
                            } catch (e: ParseException) {
                                Toast.makeText(context, "Invalid time format", Toast.LENGTH_LONG).show()
                            }
                        } else {
                            Toast.makeText(context, "Please enter a description.", Toast.LENGTH_LONG).show()
                        }
                    })
                }
            }
        }
    }
}

@Composable
fun ProjectNameDropdown(
    projects: List<Project>,
    selectedIndex: Int,
    onIndexChanged: (Int) -> Unit
) {
    val projectNames = projects.map { it.projectName }
    DropdownMenuLogic(
        items = projectNames,
        label = "Select Project",
        selectedIndex = selectedIndex,
        onIndexChanged = onIndexChanged
    )
}

@Composable
fun ClientNameDropdown(
    projects: List<Project>,
    selectedIndex: Int,
    onIndexChanged: (Int) -> Unit
) {
    val clientNames = projects.map { it.client }.distinct()
    DropdownMenuLogic(
        items = clientNames,
        label = "Select Client",
        selectedIndex = selectedIndex,
        onIndexChanged = onIndexChanged
    )
}

@Composable
fun CategoryDropdown(
    categories: List<Category>,
    selectedIndex: Int,
    onIndexChanged: (Int) -> Unit
) {
    val categoryNames = categories.map { it.name }
    DropdownMenuLogic(
        items = categoryNames,
        label = "Select Category",
        selectedIndex = selectedIndex,
        onIndexChanged = onIndexChanged
    )
}

@Composable
fun DropdownMenuLogic(
    items: List<String>,
    label: String,
    selectedIndex: Int,
    onIndexChanged: (Int) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Column {
        Button(
            onClick = { expanded = true },
            colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF333333)),
            modifier = Modifier.padding(8.dp)
        ) {
            Text(if (items.isNotEmpty()) items[selectedIndex] else label, color = Color.White, fontSize = 18.sp)
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(Color.Gray).padding(4.dp)
        ) {
            items.forEachIndexed { index, item ->
                DropdownMenuItem(onClick = {
                    onIndexChanged(index)
                    expanded = false
                }) {
                    Text(item, color = Color.Black, fontSize = 16.sp)
                }
            }
        }
    }
}

@Composable
fun GradientButton(text: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(backgroundColor = Color.Transparent),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(Color(0xFFFF9800), Color(0xFF2196F3))
                )
            )
    ) {
        Text(
            text,
            color = Color.White,
            modifier = Modifier.padding(all = 0.dp),
            fontSize = 16.sp
        )
    }
}

@Composable
fun CreateTimerGradientText(text: String, fontSize: TextUnit) {
    androidx.compose.foundation.text.BasicText(
        text = buildAnnotatedString {
            withStyle(style = SpanStyle(
                fontSize = fontSize,
                brush = Brush.horizontalGradient(
                    colors = listOf(Color(0xFFFF9800), Color(0xFF2196F3))
                )
            )) {
                append(text)
            }
        },
        modifier = Modifier.padding(16.dp, 8.dp, 16.dp, 0.dp)
    )
}

@Composable
fun VerticalScroller(content: @Composable () -> Unit) {
    val scrollState = rememberScrollState()
    Column(
        modifier = Modifier
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        content()
    }
}
