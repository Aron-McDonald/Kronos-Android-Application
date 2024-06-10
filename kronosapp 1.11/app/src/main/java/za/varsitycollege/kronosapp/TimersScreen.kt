package za.varsitycollege.kronosapp

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Timer
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class TimersScreenActivity : ComponentActivity() {
    private lateinit var categoryManager: CategoryManager
    private lateinit var projectManager: ProjectManager
    private lateinit var timerManager: TimerManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        categoryManager = CategoryManager()
        projectManager = ProjectManager()
        timerManager = TimerManager()

        setContent {
            BlackScreen(
                categoryManager = categoryManager,
                projectManager = projectManager,
                timerManager = timerManager,
                navigateToHome = { finish() },
                navigateToCreateTimer = {
                    startActivity(Intent(this, NewScreenActivity::class.java))
                },
                navigateToProjects = {
                    startActivity(Intent(this, ProjectsScreenActivity::class.java))
                },
                navigateToCategories = {
                    startActivity(Intent(this, BlackBackgroundScreenActivity::class.java))
                }
            )
        }
    }
}

@Composable
fun BlackScreen(
    categoryManager: CategoryManager,
    projectManager: ProjectManager,
    timerManager: TimerManager,
    navigateToHome: () -> Unit,
    navigateToCreateTimer: () -> Unit,
    navigateToProjects: () -> Unit,
    navigateToCategories: () -> Unit
) {
    val context = LocalContext.current
    val calendar = Calendar.getInstance()
    val dateFormat = SimpleDateFormat("d/M/yyyy", Locale.getDefault())

    var startDate: String by remember { mutableStateOf(dateFormat.format(Date())) }
    var endDate: String by remember { mutableStateOf(dateFormat.format(Date())) }

    val startDatePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            calendar.set(year, month, dayOfMonth)
            startDate = dateFormat.format(calendar.time)
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    val endDatePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            calendar.set(year, month, dayOfMonth)
            endDate = dateFormat.format(calendar.time)
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    var loadingCategories by remember { mutableStateOf(true) }
    var loadingProjects by remember { mutableStateOf(true) }
    var errorCategories by remember { mutableStateOf<String?>(null) }
    var errorProjects by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        timerManager.fetchTimers { success, exception ->
            loading = false
            if (!success) {
                error = exception?.message
            }
        }
        categoryManager.fetchCategories { success, exception ->
            loadingCategories = false
            if (!success) {
                errorCategories = exception?.message
            }
        }
        projectManager.fetchProjects { success, exception ->
            loadingProjects = false
            if (!success) {
                errorProjects = exception?.message
            }
        }
    }

    Scaffold(
        bottomBar = { BottomAppBarModified(navigateToHome, navigateToProjects, navigateToCategories) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .padding(innerPadding)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                GradientText("Timers:", 24.sp)
                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth()) {
                    Button(
                        onClick = { startDatePickerDialog.show() },
                        colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFFFFA500)),
                        modifier = Modifier.weight(1f).padding(end = 8.dp)
                    ) {
                        Text("Start Date: $startDate", color = Color.Black)
                    }
                    Button(
                        onClick = { endDatePickerDialog.show() },
                        colors = ButtonDefaults.buttonColors(backgroundColor = Color.Cyan),
                        modifier = Modifier.weight(1f).padding(start = 8.dp)
                    ) {
                        Text("End Date: $endDate", color = Color.Black)
                    }
                }

                if (loading) {
                    Text("Loading timers...", color = Color.White)
                } else if (error != null) {
                    Text("Error: $error", color = Color.Red)
                } else {
                    val filteredTimers = timerManager.getTimers().filter { timer ->
                        val timerDate = dateFormat.parse(timer.date)
                        val start = dateFormat.parse(startDate)
                        val end = dateFormat.parse(endDate)
                        timerDate in start..end
                    }

                    LazyColumn {
                        items(filteredTimers) { timer ->
                            TimerItem(timer)
                        }
                    }
                }
            }
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
                    .size(56.dp)
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(Color(0xFFFF9800), Color(0xFF2196F3))
                        ),
                        shape = MaterialTheme.shapes.small
                    )
                    .clickable {
                        if (loadingCategories || loadingProjects) {
                            Toast.makeText(context, "Loading data, please wait...", Toast.LENGTH_SHORT).show()
                        } else if (categoryManager.getCategories().isEmpty()) {
                            Toast.makeText(context, "A category must be created first", Toast.LENGTH_SHORT).show()
                        } else if (projectManager.getProjects().isEmpty()) {
                            Toast.makeText(context, "A project must be created first", Toast.LENGTH_SHORT).show()
                        } else {
                            navigateToCreateTimer()
                        }
                    }
            ) {
                Text(
                    "+",
                    color = Color.Black,
                    fontSize = 24.sp,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    }
}

@Composable
fun TimerItem(timer: Timer) {
    Column(
        modifier = Modifier
            .padding(vertical = 8.dp)
            .background(Color(0xFF333333))
            .padding(16.dp)
    ) {
        Text(
            text = "Project: " + timer.project,
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Client: " + timer.client,
            color = Color.LightGray,
            fontSize = 16.sp
        )
        Text(
            text = "Category: " + timer.category,
            color = Color.LightGray,
            fontSize = 16.sp
        )
        Text(
            text = "Time: ${timer.date} | ${timer.startTime} - ${timer.endTime}",
            color = Color.LightGray,
            fontSize = 14.sp
        )
        Text(
            text = "Description: " + timer.description,
            color = Color.White,
            fontSize = 14.sp,
            modifier = Modifier.padding(top = 8.dp)
        )
        timer.image?.let {
            Image(
                bitmap = it.asImageBitmap(),
                contentDescription = "Attached Image",
                modifier = Modifier
                    .padding(top = 8.dp)
                    .size(200.dp)
            )
        }
    }
}

@Composable
fun BottomAppBarModified(
    navigateToHome: () -> Unit,
    navigateToProjects: () -> Unit,
    navigateToCategories: () -> Unit
) {
    BottomAppBar(
        backgroundColor = Color.Transparent,
        modifier = Modifier.background(
            brush = Brush.horizontalGradient(
                colors = listOf(Color(0xFFFF9800), Color(0xFF2196F3))
            )
        )
    ) {
        BottomNavigationItem(
            icon = { Icon(Icons.Filled.Home, contentDescription = "Home") },
            selected = false,
            onClick = navigateToHome
        )
        BottomNavigationItem(
            icon = { Icon(Icons.Filled.Timer, contentDescription = "Timer") },
            selected = true,
            onClick = { /* Do nothing */ }
        )
        BottomNavigationItem(
            icon = { Icon(Icons.Filled.List, contentDescription = "Projects") },
            selected = false,
            onClick = navigateToProjects
        )
        BottomNavigationItem(
            icon = { Icon(Icons.Filled.Category, contentDescription = "Categories") },
            selected = false,
            onClick = navigateToCategories
        )
    }
}

@Composable
fun GradientText(text: String, fontSize: TextUnit) {
    Text(
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

@Preview(showBackground = true)
@Composable
fun PreviewBlackScreen() {
    val categoryManager = CategoryManager()
    val projectManager = ProjectManager()
    val timerManager = TimerManager()
    BlackScreen(
        categoryManager = categoryManager,
        projectManager = projectManager,
        timerManager = timerManager,
        navigateToHome = { /* Do nothing */ },
        navigateToCreateTimer = { /* Do nothing */ },
        navigateToProjects = { /* Do nothing */ },
        navigateToCategories = { /* Do nothing */ }
    )
}
