// ReportPage.kt
package za.varsitycollege.kronosapp

import android.app.DatePickerDialog
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
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
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

@OptIn(ExperimentalTextApi::class)
@Composable
fun ReportPage(timerManager: TimerManager, navController: NavHostController) {
    val context = LocalContext.current
    var selectedRange by remember { mutableStateOf<Pair<String, String>?>(null) }
    var timerData by remember { mutableStateOf<Map<String, Float>>(emptyMap()) }
    var minGoal by remember { mutableStateOf("") }
    var maxGoal by remember { mutableStateOf("") }

    // Load goals from Firestore when the screen loads
    LaunchedEffect(Unit) {
        loadGoals(context) { loadedMinGoal, loadedMaxGoal ->
            minGoal = loadedMinGoal
            maxGoal = loadedMaxGoal
        }
    }

    // Function to show the DatePickerDialog
    fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val datePickerDialog = DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                calendar.set(year, month, dayOfMonth)
                val startDate = calendar.time
                calendar.add(Calendar.DAY_OF_MONTH, 6)
                val endDate = calendar.time
                val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                selectedRange = dateFormat.format(startDate) to dateFormat.format(endDate)
                Log.d("ReportPage", "Selected date range: $startDate to $endDate")
                fetchTimersForWeek(timerManager, startDate, endDate) { data ->
                    timerData = data.toSortedMap(compareBy {
                        SimpleDateFormat("EEEE", Locale.getDefault()).parse(it)
                    })
                }
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.show()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = buildAnnotatedString {
                            withStyle(
                                style = SpanStyle(
                                    brush = Brush.horizontalGradient(
                                        colors = listOf(Color(0xFFFF9800), Color(0xFF2196F3))
                                    )
                                )
                            ) {
                                append("Report Page")
                            }
                        },
                        fontSize = 24.sp
                    )
                },
                backgroundColor = Color.Black,
                contentColor = Color.White,
                elevation = 0.dp
            )
        },
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
                    onClick = { navController.navigate("home") }
                )
                BottomNavigationItem(
                    icon = { Icon(Icons.Filled.Timer, contentDescription = "Timer") },
                    selected = false,
                    onClick = { navController.navigate("timers") }
                )
                BottomNavigationItem(
                    icon = { Icon(Icons.Filled.List, contentDescription = "Projects") },
                    selected = false,
                    onClick = { navController.navigate("projects") }
                )
                BottomNavigationItem(
                    icon = { Icon(Icons.Filled.Category, contentDescription = "Categories") },
                    selected = false,
                    onClick = { navController.navigate("categories") }
                )
                BottomNavigationItem(
                    icon = { Icon(Icons.Filled.Assessment, contentDescription = "Reports") },
                    selected = true,
                    onClick = { /* Already on reports screen */ }
                )
            }
        },
        content = { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
                    .padding(innerPadding)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {
                Spacer(modifier = Modifier.height(32.dp))

                Text(
                    text = "Filter by Start Date:",
                    color = Color.White,
                    fontSize = 18.sp,
                    modifier = Modifier.align(Alignment.Start)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { showDatePicker() },
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = Color.Transparent
                    ),
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(Color(0xFFFF9800), Color(0xFF2196F3))
                            )
                        )
                ) {
                    Text(text = "Select Date", color = Color.White)
                }

                if (selectedRange != null) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Selected Range: ${selectedRange!!.first} - ${selectedRange!!.second}",
                        color = Color.White,
                        fontSize = 16.sp
                    )
                }

                if (timerData.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(32.dp))
                    CustomBarChart(timerData, minGoal.toFloatOrNull(), maxGoal.toFloatOrNull())
                } else {
                    Text(
                        text = "No data available for the selected week.",
                        color = Color.White,
                        fontSize = 16.sp
                    )
                }
            }
        }
    )
}

// ... Rest of the code remains the same ...

@Composable
fun CustomBarChart(data: Map<String, Float>, minGoal: Float?, maxGoal: Float?) {
    val maxHours = 10f // Set the maximum hours to 10 for the Y-axis
    val barWidth = 40.dp
    val barSpacing = 16.dp
    val canvasHeight = 350.dp // Reduced canvas height to fit the title
    val xAxisOffset = 80f // Offset for X-axis labels

    Column(modifier = Modifier.fillMaxWidth().background(Color.Black)) {
        Canvas(modifier = Modifier
            .fillMaxWidth()
            .height(canvasHeight)
            .background(Color.Black)
        ) {
            val canvasHeightPx = size.height
            val canvasWidthPx = size.width

            // Draw Y-axis
            drawIntoCanvas { canvas ->
                val paint = androidx.compose.ui.graphics.Paint().asFrameworkPaint().apply {
                    color = android.graphics.Color.WHITE
                    textSize = 30f // Increased text size
                    isAntiAlias = true
                }
                for (i in 0..20) {
                    val y = canvasHeightPx - (i * (canvasHeightPx / 20))
                    canvas.nativeCanvas.drawText(
                        "${0.5f * i}",
                        10f,
                        y,
                        paint
                    )
                    drawLine(
                        Color.Gray.copy(alpha = 0.5f),
                        start = androidx.compose.ui.geometry.Offset(xAxisOffset, y),
                        end = androidx.compose.ui.geometry.Offset(canvasWidthPx, y)
                    )
                }

                // Draw Y-axis title
                canvas.nativeCanvas.save()
                canvas.nativeCanvas.rotate(-90f, 30f, canvasHeightPx / 2)
                canvas.nativeCanvas.drawText(
                    "Total Hours (HR)",
                    -canvasHeightPx / 2,
                    30f,
                    paint
                )
                canvas.nativeCanvas.restore()
            }

            // Draw min and max goal lines
            val minGoalPx = minGoal?.let { canvasHeightPx - (it / maxHours * canvasHeightPx) }
            val maxGoalPx = maxGoal?.let { canvasHeightPx - (it / maxHours * canvasHeightPx) }

            if (minGoalPx != null) {
                drawLine(
                    Color.Red,
                    start = androidx.compose.ui.geometry.Offset(0f, minGoalPx),
                    end = androidx.compose.ui.geometry.Offset(canvasWidthPx, minGoalPx),
                    strokeWidth = 5f
                )
            }

            if (maxGoalPx != null) {
                drawLine(
                    Color.Green,
                    start = androidx.compose.ui.geometry.Offset(0f, maxGoalPx),
                    end = androidx.compose.ui.geometry.Offset(canvasWidthPx, maxGoalPx),
                    strokeWidth = 5f
                )
            }

            // Draw X-axis and bars
            data.entries.forEachIndexed { index, entry ->
                val day = entry.key
                val hours = (entry.value * 2).roundToInt() / 2f // Round to the nearest 0.5
                val barHeight = (canvasHeightPx * (hours / maxHours))

                // Draw the bar
                drawRect(
                    color = Color.Blue,
                    topLeft = androidx.compose.ui.geometry.Offset(
                        x = xAxisOffset + index * (barWidth.toPx() + barSpacing.toPx()),
                        y = canvasHeightPx - barHeight
                    ),
                    size = androidx.compose.ui.geometry.Size(
                        width = barWidth.toPx(),
                        height = barHeight
                    )
                )

                // Draw the day label
                drawIntoCanvas { canvas ->
                    val paint = androidx.compose.ui.graphics.Paint().asFrameworkPaint().apply {
                        color = android.graphics.Color.WHITE
                        textSize = 30f // Increased text size
                        isAntiAlias = true
                    }
                    canvas.nativeCanvas.drawText(
                        day.take(3),
                        xAxisOffset + index * (barWidth.toPx() + barSpacing.toPx()),
                        canvasHeightPx - 5f,
                        paint
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Draw legend
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            LegendItem(color = Color.Red, label = "Min Goal")
            LegendItem(color = Color.Green, label = "Max Goal")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Draw X-axis title
        Text(
            text = "Days of the Week",
            color = Color.White,
            fontSize = 16.sp,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
    }
}

@Composable
fun LegendItem(color: Color, label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Canvas(modifier = Modifier.size(16.dp)) {
            drawRect(color = color)
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = label, color = Color.White)
    }
}

fun fetchTimersForWeek(timerManager: TimerManager, startDate: Date, endDate: Date, callback: (Map<String, Float>) -> Unit) {
    timerManager.fetchTimers { success, exception ->
        if (success) {
            val calendar = Calendar.getInstance()
            val timerData = mutableMapOf<String, Float>()

            val dateFormat = SimpleDateFormat("d/M/yyyy", Locale.getDefault())
            val start = dateFormat.parse(dateFormat.format(startDate))
            val end = dateFormat.parse(dateFormat.format(endDate))

            timerManager.getTimers().forEach { timer ->
                val timerDate = dateFormat.parse(timer.date)
                if (timerDate in start..end) {
                    calendar.time = timerDate
                    val dayOfWeek = SimpleDateFormat("EEEE", Locale.getDefault()).format(calendar.time)
                    timerData[dayOfWeek] = (timerData[dayOfWeek] ?: 0f) + timer.hours
                }
            }

            Log.d("ReportPage", "Fetched timer data: $timerData")
            callback(timerData)
        } else {
            Log.e("ReportPage", "Error fetching timers: ${exception?.message}")
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

@Preview(showBackground = true)
@Composable
fun ReportPagePreview() {
    val navController = rememberNavController() // Add this line
    ReportPage(TimerManager(), navController) // Update this line
}
