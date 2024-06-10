package za.varsitycollege.kronosapp

import HomePageScreen
import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.ktx.Firebase
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.initialize


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Firebase.initialize(this)
        val categoryManager = CategoryManager()
        val projectManager = ProjectManager()
        val timerManager = TimerManager()
        setContent {
            val navController = rememberNavController()
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    AppContent(navController, categoryManager, projectManager, timerManager)
                }
            }
        }
    }
}

@Composable
fun AppContent(
    navController: NavHostController,
    categoryManager: CategoryManager,
    projectManager: ProjectManager,
    timerManager: TimerManager
) {
    NavHost(navController = navController, startDestination = "login") {
        composable("login") {
            val context = LocalContext.current
            LoginScreen(
                onBackToSignUp = {
                    navController.navigate("signup")
                },
                context = context,
                onLoginSuccess = {
                    navController.navigate("home")
                }
            )
        }
        composable("signup") {
            val context = LocalContext.current
            SignUpScreen(
                onLogInClicked = {
                    navController.navigate("login")
                },
                context = context
            )
        }
        composable("home") {
            HomePageScreen(
                navigateToTimers = {
                    navController.navigate("timers")
                },
                navigateToProjects = {
                    navController.navigate("projects")
                },
                navigateToCategories = {
                    navController.navigate("categories")
                },
                navigateToReports = {
                    navController.navigate("reports")
                },
                navController = navController // Pass the navController to HomePageScreen
            )
        }
        composable("projects") {
            ProjectsScreen(
                navController = navController,
                projectManager = projectManager,
                navigateToReports = { navController.navigate("reports") }
            )
        }
        composable("timers") {
            BlackScreen(
                categoryManager = categoryManager,
                projectManager = projectManager,
                timerManager = timerManager,
                navigateToHome = {
                    navController.navigate("home")
                },
                navigateToCreateTimer = {
                    navController.navigate("createtimer")
                },
                navigateToProjects = {
                    navController.navigate("projects")
                },
                navigateToCategories = {
                    navController.navigate("categories")
                }
            )
        }
        composable("createtimer") {
            NewBlackScreen(
                categoryManager = categoryManager,
                projectManager = projectManager,
                timerManager = timerManager,
                navigateToTimers = {
                    navController.popBackStack()
                },
                requestCodeCameraPermission = NewScreenActivity.REQUEST_CAMERA_PERMISSION
            )
        }
        composable("categories") {
            BlackBackgroundScreen(
                categoryManager = categoryManager,
                timerManager = timerManager,
                navigateToHome = {
                    navController.navigate("home")
                },
                navigateToTimers = {
                    navController.navigate("timers")
                },
                navigateToProjects = {
                    navController.navigate("projects")
                },
                navigateToCategories = {
                    navController.navigate("categories")
                },
                navigateToReports = {
                    navController.navigate("reports")
                })
        }
        composable("reports") {
            ReportPage(timerManager = timerManager, navController = navController)
        }
    }
}

@OptIn(ExperimentalTextApi::class)
@Composable
fun LoginScreen(
    onBackToSignUp: () -> Unit,
    context: Context,
    onLoginSuccess: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var emailError by remember { mutableStateOf(false) }
    val auth: FirebaseAuth = FirebaseAuth.getInstance()

    fun isValidEmail(input: String): Boolean {
        return input.all { it.isLetterOrDigit() || it == '@' || it == '.' }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(48.dp))
            Image(
                painter = painterResource(id = R.drawable.kronos_banner), // Ensure correct drawable resource
                contentDescription = "Logo",
                modifier = Modifier
                    .size(300.dp)
                    .clip(RoundedCornerShape(60.dp))
            )
            Spacer(modifier = Modifier.height(32.dp))
            Text(
                text = buildAnnotatedString {
                    withStyle(
                        style = SpanStyle(
                            brush = Brush.horizontalGradient(
                                colors = listOf(Color(0xFFFF9800), Color(0xFF2196F3)),
                            ),
                            fontWeight = FontWeight.Bold
                        )
                    ) {
                        append("Log In")
                    }
                },
                textAlign = TextAlign.Center,
                fontSize = 32.sp,
                style = MaterialTheme.typography.h4,
                textDecoration = TextDecoration.Underline,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(32.dp))
            OutlinedTextField(
                value = email,
                onValueChange = {
                    if (isValidEmail(it)) {
                        email = it
                        emailError = false
                    } else {
                        emailError = true
                    }
                },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    textColor = Color.Black,
                    backgroundColor = Color.White
                ),
                isError = emailError
            )
            if (emailError) {
                Text("Email can only contain letters, numbers, '@', and '.'",
                    color = Color.Red,
                    style = MaterialTheme.typography.caption,
                    modifier = Modifier.padding(start = 16.dp))
            }
            Spacer(modifier = Modifier.height(24.dp))
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    textColor = Color.Black,
                    backgroundColor = Color.White
                )
            )
            Spacer(modifier = Modifier.height(32.dp))
            Button(
                onClick = {
                    if (email.isBlank() || password.isBlank()) {
                        Toast.makeText(context, "Fields cannot be empty", Toast.LENGTH_LONG).show()
                    } else {
                        auth.signInWithEmailAndPassword(email, password)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    Toast.makeText(context, "Successfully logged in", Toast.LENGTH_LONG).show()
                                    onLoginSuccess()
                                } else {
                                    Toast.makeText(context, "Invalid credentials: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                                }
                            }
                    }
                },
                modifier = Modifier
                    .width(200.dp)
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
                        text = "Log In",
                        color = Color.Black,
                        style = MaterialTheme.typography.button,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Don't have an account?", color = Color.White)
                TextButton(onClick = onBackToSignUp) {
                    Text("Sign Up", color = Color.Yellow)
                }
            }
        }
    }
}

@OptIn(ExperimentalTextApi::class)
@Composable
fun SignUpScreen(
    onLogInClicked: () -> Unit,
    context: Context
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var emailError by remember { mutableStateOf(false) }
    val auth: FirebaseAuth = FirebaseAuth.getInstance()

    fun isValidEmail(input: String): Boolean {
        return input.all { it.isLetterOrDigit() || it == '@' || it == '.' }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(48.dp))
            Image(
                painter = painterResource(id = R.drawable.kronos_banner),
                contentDescription = "Logo",
                modifier = Modifier
                    .size(300.dp)
                    .clip(RoundedCornerShape(60.dp))
            )
            Spacer(modifier = Modifier.height(32.dp))
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
                        append("Sign Up")
                    }
                },
                textAlign = TextAlign.Center,
                fontSize = 32.sp,
                style = MaterialTheme.typography.h4,
                textDecoration = TextDecoration.Underline,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(32.dp))
            OutlinedTextField(
                value = email,
                onValueChange = {
                    if (isValidEmail(it)) {
                        email = it
                        emailError = false
                    } else {
                        emailError = true
                    }
                },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    textColor = Color.Black,
                    backgroundColor = Color.White
                ),
                isError = emailError
            )
            if (emailError) {
                Text("Email can only contain letters, numbers, '@', and '.'",
                    color = Color.Red,
                    style = MaterialTheme.typography.caption,
                    modifier = Modifier.padding(start = 16.dp))
            }
            Spacer(modifier = Modifier.height(24.dp))
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    textColor = Color.Black,
                    backgroundColor = Color.White
                )
            )
            Spacer(modifier = Modifier.height(24.dp))
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = { Text("Re-enter Password") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    textColor = Color.Black,
                    backgroundColor = Color.White
                )
            )
            Spacer(modifier = Modifier.height(32.dp))
            Button(
                onClick = {
                    if (email.isBlank() || password.isBlank() || confirmPassword.isBlank()) {
                        Toast.makeText(context, "Fields cannot be empty", Toast.LENGTH_LONG).show()
                    } else if (password != confirmPassword) {
                        Toast.makeText(context, "Passwords do not match", Toast.LENGTH_LONG).show()
                    } else {
                        auth.createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    Toast.makeText(context, "Account created successfully", Toast.LENGTH_LONG).show()
                                    onLogInClicked()
                                } else {
                                    Toast.makeText(context, "Error: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                                }
                            }
                    }
                },
                modifier = Modifier
                    .width(200.dp)
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
                        text = "Sign Up",
                        color = Color.Black,
                        style = MaterialTheme.typography.button,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
            ClickableText(
                text = buildAnnotatedString {
                    withStyle(style = SpanStyle(color = Color.White)) {
                        append("Already have an account? ")
                    }
                    withStyle(style = SpanStyle(color = Color.Blue)) {
                        append("Log in")
                    }
                },
                onClick = { onLogInClicked() },
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}
