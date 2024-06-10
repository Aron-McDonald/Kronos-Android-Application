package za.varsitycollege.kronosapp

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import java.text.SimpleDateFormat
import java.util.*

class BlackBackgroundScreenActivity : ComponentActivity() {
    private lateinit var categoryManager: CategoryManager
    private lateinit var timerManager: TimerManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        categoryManager = CategoryManager()
        timerManager = TimerManager()
        setContent {
            val navController = rememberNavController()
            BlackBackgroundScreen(
                categoryManager = categoryManager,
                timerManager = timerManager,
                navigateToHome = { navController.navigate("home") },
                navigateToTimers = { navController.navigate("timers") },
                navigateToProjects = { navController.navigate("projects") },
                navigateToCategories = { navController.navigate("categories") },
                navigateToReports = { navController.navigate("reports") }
            )
        }
    }
}

@OptIn(ExperimentalTextApi::class)
@Composable
fun BlackBackgroundScreen(
    categoryManager: CategoryManager,
    timerManager: TimerManager,
    navigateToHome: () -> Unit,
    navigateToTimers: () -> Unit,
    navigateToProjects: () -> Unit,
    navigateToCategories: () -> Unit,
    navigateToReports: () -> Unit
) {
    var showCategoryDialog by remember { mutableStateOf(false) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    if (showCategoryDialog) {
        AddCategoryDialog(
            onDismiss = { showCategoryDialog = false },
            onSave = { category ->
                categoryManager.addCategory(category)
                showCategoryDialog = false
            }
        )
    }

    LaunchedEffect(Unit) {
        categoryManager.fetchCategories { success, exception ->
            loading = false
            if (!success) {
                error = exception?.message
            }
        }
    }

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

    Scaffold(
        bottomBar = {
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
                    selected = false,
                    onClick = navigateToTimers
                )
                BottomNavigationItem(
                    icon = { Icon(Icons.Filled.List, contentDescription = "Projects") },
                    selected = false,
                    onClick = navigateToProjects
                )
                BottomNavigationItem(
                    icon = { Icon(Icons.Filled.Category, contentDescription = "Categories") },
                    selected = true,
                    onClick = navigateToCategories
                )
                BottomNavigationItem(
                    icon = { Icon(Icons.Filled.Assessment, contentDescription = "Reports") },
                    selected = false,
                    onClick = navigateToReports
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .padding(innerPadding)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = buildAnnotatedString {
                        withStyle(style = SpanStyle(brush = Brush.horizontalGradient(
                            colors = listOf(Color(0xFFFF9800), Color(0xFF2196F3))
                        ))) {
                            append("Categories:")
                        }
                    },
                    fontSize = 24.sp,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Date range picker now placed in a row to make them appear side by side
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
                    Text("Loading...", color = Color.White)
                } else if (error != null) {
                    Text("Error: $error", color = Color.Red)
                } else {
                    // Calculate the filtered categories
                    val filteredCategories = categoryManager.getCategories().map { category ->
                        val filteredTimers = timerManager.getTimers().filter { timer ->
                            val timerDate = dateFormat.parse(timer.date)
                            val start = dateFormat.parse(startDate)
                            val end = dateFormat.parse(endDate)
                            timerDate in start..end && timer.category == category.name
                        }
                        category.copy(totalHours = filteredTimers.sumByDouble { it.hours.toDouble() }.toFloat())
                    }

                    CategoryList(filteredCategories)
                }
            }
            // Gradient button for adding a category
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
                    .size(48.dp)
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(Color(0xFFFF9800), Color(0xFF2196F3))
                        ),
                        shape = MaterialTheme.shapes.small
                    )
                    .clickable { showCategoryDialog = true }
            ) {
                Text("+", color = Color.Black, fontSize = 24.sp, modifier = Modifier.align(Alignment.Center))
            }
        }
    }
}

@Composable
fun CategoryList(categories: List<Category>) {
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Black)
    ) {
        items(categories) { category ->
            CategoryItem(category)
        }
    }
}

@Composable
fun CategoryItem(category: Category) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 16.dp)
            .background(color = Color(0xFF333333), shape = MaterialTheme.shapes.medium)
            .padding(16.dp)
    ) {
        Text(
            text = "Category: ${category.name}",
            color = Color.White,
            fontSize = 18.sp
        )
        Text(
            text = "Total Hours: ${category.totalHours}",
            color = Color.White,
            fontSize = 16.sp
        )
    }
}

@Composable
fun AddCategoryDialog(onDismiss: () -> Unit, onSave: (String) -> Unit) {
    var categoryName by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }

    fun isValidInput(input: String): Boolean = input.all { it.isLetterOrDigit() || it.isWhitespace() }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Category", color = Color.White) },
        text = {
            Surface(modifier = Modifier.fillMaxWidth(), color = Color.DarkGray) {
                Column {
                    TextField(
                        value = categoryName,
                        onValueChange = {
                            if (isValidInput(it)) {
                                categoryName = it
                                errorMessage = ""
                            }
                        },
                        label = { Text("Category Name", color = Color.Gray) },
                        textStyle = LocalTextStyle.current.copy(color = Color.White),
                        isError = errorMessage.isNotEmpty()
                    )
                    if (errorMessage.isNotEmpty()) {
                        Text(errorMessage, color = MaterialTheme.colors.error)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (categoryName.isBlank() || !isValidInput(categoryName)) {
                        errorMessage = "Invalid input. Please use only letters, digits, or spaces."
                    } else {
                        onSave(categoryName)
                        categoryName = ""
                    }
                }, colors = ButtonDefaults.buttonColors(backgroundColor = Color.Cyan, contentColor = Color.Black)
            ) { Text("Save") }
        },
        dismissButton = {
            Button(onClick = onDismiss, colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFFFF9800), contentColor = Color.Black)) { Text("Cancel") }
        }, backgroundColor = Color.DarkGray,
        contentColor = Color.White
    )
}
