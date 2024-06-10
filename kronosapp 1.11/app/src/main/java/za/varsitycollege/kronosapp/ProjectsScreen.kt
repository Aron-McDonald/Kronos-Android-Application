package za.varsitycollege.kronosapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.AlertDialog
import androidx.compose.material.BottomAppBar
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Icon
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Timer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController



data class Project(
    val projectName: String = "",
    val client: String = "",
    val description: String = ""
) {
    constructor() : this("", "", "")
}


class ProjectsScreenActivity : ComponentActivity() {
    private lateinit var projectManager: ProjectManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        projectManager = ProjectManager()
        setContent {
            val navController = rememberNavController()
            ProjectsScreen(
                navController = navController,
                projectManager = projectManager,
                navigateToReports = { navController.navigate("reports") }
            )
        }
    }
}

@Composable
fun ProjectsScreen(
    navController: NavController,
    projectManager: ProjectManager,
    navigateToReports: () -> Unit
) {
    var showProjectDialog by remember { mutableStateOf(false) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        projectManager.fetchProjects { success, exception ->
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
                    selected = true,
                    onClick = { /* Do nothing */ }
                )
                BottomNavigationItem(
                    icon = { Icon(Icons.Filled.Category, contentDescription = "Categories") },
                    selected = false,
                    onClick = { navController.navigate("categories") }
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
                        )))
                        {
                            append("Projects:")
                        }
                    },
                    fontSize = 24.sp,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(16.dp))

                if (loading) {
                    Text("Loading...", color = Color.White)
                } else if (error != null) {
                    Text("Error: $error", color = Color.Red)
                } else {
                    ProjectList(projectManager.getProjects())
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
                    .clickable { showProjectDialog = true }
            ) {
                Text("+", color = Color.Black, fontSize = 24.sp, modifier = Modifier.align(Alignment.Center))
            }
        }

        if (showProjectDialog) {
            AddProjectDialog(projectManager = projectManager, onDismiss = { showProjectDialog = false })
        }
    }
}

@Composable
fun AddProjectDialog(projectManager: ProjectManager, onDismiss: () -> Unit) {
    var projectName by remember { mutableStateOf("") }
    var client by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }

    fun isValidName(input: String): Boolean = input.all { it.isLetter() }

    fun isValidInput(input: String): Boolean = input.all { it.isLetterOrDigit() || it.isWhitespace() }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Project", color = Color.White) },
        text = {
            Surface(modifier = Modifier.fillMaxWidth(), color = Color.DarkGray) {
                Column {
                    TextField(
                        value = projectName,
                        onValueChange = {
                            if (isValidInput(it)) {
                                projectName = it
                                errorMessage = ""
                            }
                        },
                        label = { Text("Project Name", color = Color.Gray) },
                        textStyle = LocalTextStyle.current.copy(color = Color.White),
                        isError = errorMessage.isNotEmpty()
                    )
                    TextField(
                        value = client,
                        onValueChange = {
                            if (isValidName(it)) {
                                client = it
                                errorMessage = ""
                            }
                        },
                        label = { Text("Client", color = Color.Gray) },
                        textStyle = LocalTextStyle.current.copy(color = Color.White),
                        isError = errorMessage.isNotEmpty()
                    )
                    TextField(
                        value = description,
                        onValueChange = {
                            if (isValidInput(it)) {
                                description = it
                                errorMessage = ""
                            }
                        },
                        label = { Text("Description", color = Color.Gray) },
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
                    if (projectName.isBlank() || !isValidInput(projectName) ||
                        client.isBlank() || !isValidName(client) ||
                        description.isBlank() || !isValidInput(description)) {
                        errorMessage = "Invalid input. Please use only letters for the client, and letters, digits, or spaces for other fields."
                    } else {
                        projectManager.addProject(Project(projectName, client, description))
                        projectName = ""
                        client = ""
                        description = ""
                        onDismiss()
                    }
                },
                colors = ButtonDefaults.buttonColors(backgroundColor = Color.Cyan, contentColor = Color.Black)
            ) { Text("Save") }
        },
        dismissButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFFFF9800), contentColor = Color.Black)
            ) { Text("Cancel") }
        },
        backgroundColor = Color.DarkGray,
        contentColor = Color.White
    )
}

@Composable
fun ProjectList(projects: List<Project>) {
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Black)
    ) {
        items(projects) { project ->
            ProjectItem(project)
        }
    }
}

@Composable
fun ProjectItem(project: Project) {
    Column(modifier = Modifier
        .fillMaxWidth()
        .padding(8.dp)
        .background(Color(0xFF333333))
        .padding(8.dp)) {
        Text("Project Name: ${project.projectName}", color = Color.White, fontSize = 18.sp)
        Text("Client: ${project.client}", color = Color.White, fontSize = 16.sp)
        Text("Description: ${project.description}", color = Color.White, fontSize = 14.sp)
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewProjectsScreen() {
    val projectManager = ProjectManager()
    val navController = rememberNavController()
    ProjectsScreen(
        navController = navController,
        projectManager = projectManager,
        navigateToReports = { /* Do nothing */ }
    )
}