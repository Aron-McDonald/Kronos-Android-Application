import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
import za.varsitycollege.kronosapp.ConfettiEffect
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.material.AlertDialog
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke

// Define a TaskManager class to handle task operations
data class Task(
    val title: String,
    val description: String,
    var isCompleted: Boolean = false
) {
    constructor() : this("", "")
}

// Define a TaskManager class to handle task operations
class TaskManager {
    private val tasks = mutableStateListOf<Task>()
    private val firestore = Firebase.firestore
    private val auth = Firebase.auth

    fun removeTask(task: Task) {
        tasks.remove(task)
        deleteTaskFromFirestore(task)
    }

    fun addTask(task: Task) {
        tasks.add(task)
        saveTaskToFirestore(task)
    }

    private fun saveTaskToFirestore(task: Task) {
        val userId = auth.currentUser?.uid ?: return
        firestore.collection("users").document(userId).collection("tasks")
            .add(task)
            .addOnSuccessListener {
                // Successfully saved task
            }
            .addOnFailureListener { e ->
                // Handle error
            }
    }

    fun getTasks(): List<Task> {
        return tasks
    }

    fun fetchTasks(onFetchComplete: (Boolean, Exception?) -> Unit) {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            onFetchComplete(false, Exception("User not logged in"))
            return
        }
        firestore.collection("users").document(userId).collection("tasks")
            .get()
            .addOnSuccessListener { documents ->
                tasks.clear()
                for (document in documents) {
                    val task = document.toObject(Task::class.java)
                    tasks.add(task)
                }
                onFetchComplete(true, null)
            }
            .addOnFailureListener { e ->
                onFetchComplete(false, e)
            }
    }
}

@Composable
fun HomePageScreen(
    navigateToTimers: () -> Unit = {},
    navigateToProjects: () -> Unit = {},
    navigateToCategories: () -> Unit = {},
    navigateToReports: () -> Unit = {},
    navController: NavHostController
) {
    val context = LocalContext.current
    val currentUserEmail = getCurrentUserEmail(context)
    val sharedPref = context.getSharedPreferences("AppPreferences_${currentUserEmail}", Context.MODE_PRIVATE)
    var minGoal by remember { mutableStateOf(sharedPref.getString("minGoal", "") ?: "") }
    var maxGoal by remember { mutableStateOf(sharedPref.getString("maxGoal", "") ?: "") }

    // Load goals from Firestore when the screen loads
    LaunchedEffect(Unit) {
        loadGoals(context) { loadedMinGoal, loadedMaxGoal ->
            minGoal = loadedMinGoal
            maxGoal = loadedMaxGoal
        }
    }

    var showRatingDialog by remember { mutableStateOf(false) }
    var ratingText by remember { mutableStateOf("") }

    val taskManager = remember { TaskManager() }
    var showAddTaskDialog by remember { mutableStateOf(false) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    if (showAddTaskDialog) {
        AddTaskDialog(
            onDismiss = { showAddTaskDialog = false },
            onSave = { task ->
                taskManager.addTask(task)
                showAddTaskDialog = false
            }
        )
    }

    LaunchedEffect(Unit) {
        taskManager.fetchTasks { success, exception ->
            loading = false
            if (!success) {
                error = exception?.message
            }
        }
    }

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
                    selected = true,
                    onClick = { /* TODO */ }
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
                    selected = false,
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Text(
                    text = buildAnnotatedString {
                        withStyle(
                            style = SpanStyle(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(Color(0xFFFF9800), Color(0xFF2196F3))
                                ),
                                fontWeight = FontWeight.Bold
                            )
                        ) {
                            append("Home:")
                        }
                    },
                    fontSize = 24.sp,
                    modifier = Modifier.padding(vertical = 16.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Button(
                        onClick = navigateToProjects,
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = Color.Transparent
                        ),
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 8.dp)
                            .height(72.dp)
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(Color(0xFFFF9800), Color(0xFF2196F3))
                                )
                            )
                    ) {
                        Text(
                            text = "View Projects",
                            color = Color.White,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }

                    Button(
                        onClick = navigateToCategories,
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = Color.Transparent
                        ),
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 8.dp)
                            .height(72.dp)
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(Color(0xFFFF9800), Color(0xFF2196F3))
                                )
                            )
                    ) {
                        Text(
                            text = "View Categories",
                            color = Color.White,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Button(
                        onClick = navigateToTimers,
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = Color.Transparent
                        ),
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 8.dp)
                            .height(72.dp)
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(Color(0xFFFF9800), Color(0xFF2196F3))
                                )
                            )
                    ) {
                        Text(
                            text = "View Timers",
                            color = Color.White,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }

                    Button(
                        onClick = navigateToReports,
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = Color.Transparent
                        ),
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 8.dp)
                            .height(72.dp)
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(Color(0xFFFF9800), Color(0xFF2196F3))
                                )
                            )
                    ) {
                        Text(
                            text = "View Reports",
                            color = Color.White,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                Text(
                    text = buildAnnotatedString {
                        withStyle(
                            style = SpanStyle(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(Color(0xFFFF9800), Color(0xFF2196F3))
                                )
                            )
                        ) {
                            append("Task List:")
                        }
                    },
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = { showAddTaskDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = Color.Transparent
                    ),
                    contentPadding = PaddingValues(0.dp),
                    elevation = ButtonDefaults.elevation(0.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(Color(0xFFFF9800), Color(0xFF2196F3))
                                )
                            )
                    ) {
                        Text(
                            text = "Add a task",
                            color = Color.White,
                            style = MaterialTheme.typography.button,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                        color = Color.White
                    )
                } else {
                    if (error != null) {
                        Text(
                            text = error ?: "Unknown error",
                            color = Color.Red,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                    } else {
                        val tasks = taskManager.getTasks()
                        if (tasks.isEmpty()) {
                            Text(
                                text = "No tasks available",
                                color = Color.White,
                                modifier = Modifier.align(Alignment.CenterHorizontally)
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 200.dp)
                                    .background(
                                        color = Color.White,
                                        shape = RoundedCornerShape(16.dp)
                                    )
                                    .padding(16.dp)
                            ) {

                                LazyColumn {
                                    items(tasks) { task ->
                                        TaskItem(
                                            task = task,
                                            onComplete = {
                                                taskManager.removeTask(task)
                                            },
                                            onTaskClick = { /* TODO */ }
                                        )
                                    }
                                }

                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Minimum daily goal
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = buildAnnotatedString {
                                withStyle(
                                    style = SpanStyle(
                                        brush = Brush.horizontalGradient(
                                            colors = listOf(Color(0xFFFF9800), Color(0xFF2196F3))
                                        )
                                    )
                                ) {
                                    append("Set Minimum Daily Goal (Hours):")
                                }
                            },
                            fontSize = 16.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = minGoal,
                            onValueChange = { minGoal = it },
                            textStyle = LocalTextStyle.current.copy(color = Color.Black),
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.White, RoundedCornerShape(8.dp)),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Done
                            )
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    // Maximum daily goal
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = buildAnnotatedString {
                                withStyle(
                                    style = SpanStyle(
                                        brush = Brush.horizontalGradient(
                                            colors = listOf(Color(0xFFFF9800), Color(0xFF2196F3))
                                        )
                                    )
                                ) {
                                    append("Set Maximum Daily Goal (Hours):")
                                }
                            },
                            fontSize = 16.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = maxGoal,
                            onValueChange = { maxGoal = it },
                            textStyle = LocalTextStyle.current.copy(color = Color.Black),
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.White, RoundedCornerShape(8.dp)),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Done
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        if (minGoal.isBlank() || maxGoal.isBlank()) {
                            Toast.makeText(context, "You cannot save nothing as hours", Toast.LENGTH_LONG).show()
                        } else {
                            saveGoals(context, minGoal, maxGoal)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = Color.Transparent
                    ),
                    contentPadding = PaddingValues(0.dp),
                    elevation = ButtonDefaults.elevation(0.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(Color(0xFFFF9800), Color(0xFF2196F3))
                                )
                            )
                    ) {
                        Text(
                            text = "Save",
                            color = Color.White,
                            style = MaterialTheme.typography.button,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                }
                Spacer(modifier = Modifier.weight(1f))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Button(
                        onClick = { showRatingDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = Color.Transparent
                        ),
                        contentPadding = PaddingValues(0.dp),
                        elevation = ButtonDefaults.elevation(0.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    brush = Brush.horizontalGradient(
                                        colors = listOf(Color(0xFFFF9800), Color(0xFF2196F3))
                                    )
                                )
                        ) {
                            Text(
                                text = "Rate Your Experience",
                                color = Color.White,
                                style = MaterialTheme.typography.button,
                                modifier = Modifier.align(Alignment.Center)
                            )
                        }
                    }
                }
            }
        }
    }

    if (showRatingDialog) {
        RatingDialog(
            onDismiss = { showRatingDialog = false },
            onSubmit = { rating, comment ->
                saveRatingToFirestore(context, rating, comment)
                ratingText = "Rating: $rating\nComment: $comment"
                showRatingDialog = false
                Toast.makeText(context, "Thank you for your feedback!", Toast.LENGTH_SHORT).show()
            }
        )
    }
}
@Composable
fun RatingDialog(
    onDismiss: () -> Unit,
    onSubmit: (Int, String) -> Unit
) {
    var rating by remember { mutableStateOf(0) }
    var comment by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        text = {
            Column(
                modifier = Modifier
                    .padding(horizontal = 24.dp, vertical = 8.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    text = "Rate Your Experience",
                    style = MaterialTheme.typography.h6,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    for (i in 1..5) {
                        Icon(
                            imageVector = if (i <= rating) Icons.Filled.Star else Icons.Default.StarBorder,
                            contentDescription = null,
                            modifier = Modifier
                                .size(40.dp)
                                .padding(4.dp)
                                .clickable { rating = i },
                            tint = if (i <= rating) Color.Yellow else Color.Gray
                        )
                    }
                }
                OutlinedTextField(
                    value = comment,
                    onValueChange = { comment = it },
                    label = { Text("Enter your comment") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onSubmit(rating, comment)
                }
            ) {
                Text("Submit")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text("Cancel")
            }
        }
    )
}
@Composable
fun TaskItem(task: Task, onComplete: () -> Unit, onTaskClick: () -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .background(if (task.isCompleted) Color.Gray else Color.White)
            .clickable { expanded = !expanded }
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = task.title,
                modifier = Modifier.weight(1f).padding(8.dp)
            )
            Checkbox(
                checked = task.isCompleted,
                onCheckedChange = {
                    onComplete()
                    deleteTaskFromFirestore(task)
                }
            )
        }

        if (expanded) {
            Text(
                text = task.description,
                modifier = Modifier.padding(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 16.dp)
            )
        }
    }
}
private fun deleteTaskFromFirestore(task: Task) {
    val userId = Firebase.auth.currentUser?.uid ?: return
    val firestore = Firebase.firestore
    firestore.collection("users").document(userId).collection("tasks")
        .whereEqualTo("title", task.title)
        .whereEqualTo("description", task.description)
        .get()
        .addOnSuccessListener { documents ->
            for (document in documents) {
                document.reference.delete()
            }
        }
        .addOnFailureListener { e ->
// Handle error
        }
}
@Composable
fun AddTaskDialog(
    onDismiss: () -> Unit,
    onSave: (Task) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    val context = LocalContext.current
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Add Task") },
        text = {
            Column {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (title.isNotBlank() && description.isNotBlank()) {
                    onSave(Task(title, description))
                } else {
                    Toast.makeText(context, "All fields are required", Toast.LENGTH_SHORT).show()
                }
            }) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
fun saveGoals(context: Context, minGoal: String, maxGoal: String) {
    val currentUserEmail = getCurrentUserEmail(context)
    val sharedPref = context.getSharedPreferences("AppPreferences_${currentUserEmail}", Context.MODE_PRIVATE)
    if (minGoal.isBlank() || maxGoal.isBlank()) {
        Toast.makeText(context, "You cannot save empty values as hours", Toast.LENGTH_LONG).show()
    } else {
        with(sharedPref.edit()) {
            putString("minGoal", minGoal)
            putString("maxGoal", maxGoal)
            commit()
        }

        // Save to Firestore
        val userId = Firebase.auth.currentUser?.uid ?: return
        val firestore = Firebase.firestore
        val goals = hashMapOf("minGoal" to minGoal, "maxGoal" to maxGoal)
        firestore.collection("users").document(userId).set(goals)
            .addOnSuccessListener {
                Toast.makeText(context, "Hours saved successfully", Toast.LENGTH_LONG).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Error saving hours: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }
}
fun loadGoals(context: Context, onLoad: (String, String) -> Unit) {
    val userId = Firebase.auth.currentUser?.uid ?: return
    val firestore = Firebase.firestore
    firestore.collection("users").document(userId).get()
        .addOnSuccessListener { document ->
            if (document != null) {
                val minGoal = document.getString("minGoal") ?: ""
                val maxGoal = document.getString("maxGoal") ?: ""
                onLoad(minGoal, maxGoal)
            }
        }
        .addOnFailureListener { e ->
            Toast.makeText(context, "Error loading goals: ${e.message}", Toast.LENGTH_LONG).show()
        }
}
fun saveRatingToFirestore(context: Context, rating: Int, comment: String) {
    val userId = Firebase.auth.currentUser?.uid ?: return
    val firestore = Firebase.firestore
    val ratingData = hashMapOf("rating" to rating, "comment" to comment)
    firestore.collection("users").document(userId).collection("ratings").add(ratingData)
        .addOnSuccessListener {
            Toast.makeText(context, "Rating saved successfully", Toast.LENGTH_LONG).show()
        }
        .addOnFailureListener { e ->
            Toast.makeText(context, "Error saving rating: ${e.message}", Toast.LENGTH_LONG).show()
        }
}
fun getCurrentUserEmail(context: Context): String {
    val sharedPreferences = context.getSharedPreferences("user_credentials", Context.MODE_PRIVATE)
    return sharedPreferences.getString("email", "") ?: ""
}
fun logout(context: Context) {
    val sharedPreferences = context.getSharedPreferences("user_credentials", Context.MODE_PRIVATE)
    sharedPreferences.edit().remove("email").apply()
}