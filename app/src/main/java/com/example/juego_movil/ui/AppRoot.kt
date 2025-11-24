package com.example.juego_movil.ui

import android.app.Application
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.collectAsState
import com.example.juego_movil.data.AuthRequest
import com.example.juego_movil.data.leaderboard.LeaderboardApi
import com.example.juego_movil.repository.LocalProgressRepository
import com.example.juego_movil.ui.leaderboard.LeaderboardScreen
import com.example.juego_movil.repository.LeaderboardRepository
import com.example.juego_movil.repository.RealLeaderboardRepository
import com.example.juego_movil.MyApplication
import com.example.juego_movil.repository.AuthRepository
import com.example.juego_movil.data.AuthState
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

import androidx.compose.material3.ExperimentalMaterial3Api

// =======================================================
// CONFIGURACIÓN GLOBAL DE RED
// =======================================================

private const val BASE_DOMAIN_URL = "https://x8ki-letl-twmt.n7.xano.io/"
private const val SCORES_GROUP_PATH = "api:momBPpCL/"


// =======================================================
// INTERCEPTOR PARA EL TOKEN DE AUTORIZACIÓN (HEADERS)
// =======================================================

class AuthInterceptor(private val getToken: suspend () -> String?) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {

        val token = runBlocking { getToken() }

        var request = chain.request()

        if (token != null) {
            request = request.newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
        }
        return chain.proceed(request)
    }
}

// =======================================================
// INICIALIZACIÓN DE REPOSITORIOS Y NAVEGACIÓN
// =======================================================

enum class Screen { LOGIN, MENU, LEVEL_SELECT, GAME, LEADERBOARD }

@Composable
fun AppRoot() {
    // ----------------------------------------------------
    // ESTADO Y DEPENDENCIAS LOCALES
    // ----------------------------------------------------

    val application = LocalContext.current.applicationContext as Application
    val progressRepo = remember { LocalProgressRepository(application) }
    val scope = rememberCoroutineScope()

    // ----------------------------------------------------
    // 1. INICIALIZACIÓN DE AUTH REPOSITORY
    // ----------------------------------------------------

    val authRepo: AuthRepository = remember {
        MyApplication.getAuthRepository(application)
    }

    // Observamos el estado de autenticación desde el repositorio
    val authState by authRepo.authStateFlow.collectAsState(initial = AuthState.Loading)

    // ----------------------------------------------------
    // 2. NAVEGACIÓN INTERNA
    // ----------------------------------------------------

    var currentAuthenticatedScreen by remember { mutableStateOf(Screen.MENU) }
    var selectedLevel by remember { mutableStateOf(1) }

    // ----------------------------------------------------
    // 3. INICIALIZACIÓN DE SCORES API Y REPOSITORIO
    // ----------------------------------------------------

    val okHttpClient = remember {
        OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(getToken = {
                // Obtenemos el token real desde el repositorio (puede ser null)
                authRepo.getCurrentAuthToken()
            }))
            .build()
    }

    val scoresRetrofit: Retrofit = remember {
        Retrofit.Builder()
            .baseUrl(BASE_DOMAIN_URL + SCORES_GROUP_PATH)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val leaderboardApi: LeaderboardApi = remember { scoresRetrofit.create(LeaderboardApi::class.java) }
    val leaderboardRepo: LeaderboardRepository = remember { RealLeaderboardRepository(api = leaderboardApi) }

    // Activar modo inmersivo
    ImmersiveMode(enabled = true)

    // ----------------------------------------------------
    // 4. NAVEGACIÓN PRINCIPAL
    // ----------------------------------------------------

    when (authState) {
        is AuthState.Loading -> LoadingScreen()

        is AuthState.SignedOut -> {
            // Si se cierra sesión, reseteamos la pantalla interna
            currentAuthenticatedScreen = Screen.MENU
            LoginScreen(authRepository = authRepo)
        }

        is AuthState.SignedIn -> {
            when (currentAuthenticatedScreen) {
                Screen.MENU -> MainMenuScreen(
                    onStart = { currentAuthenticatedScreen = Screen.LEVEL_SELECT },
                    onResetProgress = {
                        scope.launch { progressRepo.setHighestUnlockedLevel(1) }
                    },
                    onLeaderboard = { currentAuthenticatedScreen = Screen.LEADERBOARD },
                    onLogout = {
                        // Logout debe llamarse dentro de una corrutina
                        scope.launch { authRepo.logout() }
                    }
                )

                Screen.LEVEL_SELECT -> LevelSelectScreen(
                    onLevelChosen = { level ->
                        selectedLevel = level
                        currentAuthenticatedScreen = Screen.GAME
                    },
                    onBackPressed = { currentAuthenticatedScreen = Screen.MENU },
                    progressRepo = progressRepo
                )

                Screen.GAME -> GameScreen(
                    initialLevel = selectedLevel,
                    onExitToMenu = { currentAuthenticatedScreen = Screen.MENU },
                    progressRepo = progressRepo
                )

                Screen.LEADERBOARD -> LeaderboardScreen(
                    repository = leaderboardRepo,
                    onBack = { currentAuthenticatedScreen = Screen.MENU }
                )

                Screen.LOGIN -> currentAuthenticatedScreen = Screen.MENU
            }
        }
    }
}

@Composable
fun LoadingScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0D0D1A)),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Completos Journey", fontSize = 32.sp, color = Color.White)
            Spacer(modifier = Modifier.height(24.dp))
            CircularProgressIndicator(color = Color(0xFFFFEB3B))
            Spacer(modifier = Modifier.height(16.dp))
            Text("Cargando...", color = Color.White.copy(alpha = 0.7f))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    authRepository: AuthRepository
) {
    var isLoginMode by remember { mutableStateOf(true) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorText by remember { mutableStateOf<String?>(null) }

    val scope = rememberCoroutineScope()

    val onAuthAction: () -> Unit = {
        errorText = null
        isLoading = true

        scope.launch {
            val request = if (isLoginMode) {
                AuthRequest(email, password)
            } else {
                AuthRequest(email, password, name)
            }

            val result = if (isLoginMode) {
                authRepository.login(request)
            } else {
                authRepository.signup(request)
            }

            isLoading = false

            if (!result.isSuccess) {
                errorText = result.exceptionOrNull()?.message ?: "Error desconocido."
            }
        }
    }

    val textFieldColors = OutlinedTextFieldDefaults.colors(
        unfocusedTextColor = Color.White,
        focusedTextColor = Color.White,
        unfocusedLabelColor = Color(0xFFBBDEFB),
        focusedLabelColor = Color(0xFFFFEB3B),
        unfocusedLeadingIconColor = Color(0xFFBBDEFB),
        focusedLeadingIconColor = Color(0xFFFFEB3B),
        focusedBorderColor = Color(0xFFFFEB3B),
        unfocusedBorderColor = Color(0xFFBBDEFB),
        cursorColor = Color(0xFFFFEB3B),
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color(0xFF2C3E50)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            Text(
                text = if (isLoginMode) "INICIAR SESIÓN" else "REGISTRO",
                fontSize = 32.sp,
                color = Color.White,
                style = MaterialTheme.typography.headlineLarge
            )
            Spacer(modifier = Modifier.height(20.dp))

            AnimatedVisibility(visible = !isLoginMode) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Usuario") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = "Nombre") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = textFieldColors
                )
            }
            if (!isLoginMode) Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                leadingIcon = { Icon(Icons.Default.MailOutline, contentDescription = "Email") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth(),
                colors = textFieldColors
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Contraseña") },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = "Contraseña") },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth(),
                colors = textFieldColors
            )
            Spacer(modifier = Modifier.height(20.dp))

            errorText?.let {
                Text(text = it, color = Color.Red, fontSize = 14.sp, modifier = Modifier.padding(bottom = 8.dp))
            }

            Button(
                onClick = onAuthAction,
                enabled = !isLoading && email.isNotBlank() && password.isNotBlank(),
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFEB3B))
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(24.dp))
                } else {
                    Text(text = if (isLoginMode) "INGRESAR" else "CREAR CUENTA", color = Color.Black, fontSize = 16.sp)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            TextButton(onClick = {
                isLoginMode = !isLoginMode
                errorText = null
            }) {
                Text(
                    text = if (isLoginMode) "¿No tienes cuenta? Regístrate" else "¿Ya tienes cuenta? Ingresa",
                    color = Color.White.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@Composable
fun MainMenuScreen(
    onStart: () -> Unit,
    onLeaderboard: () -> Unit,
    onResetProgress: () -> Unit,
    onLogout: () -> Unit
) {
    val infinite = rememberInfiniteTransition(label = "Blink")
    val blinkAlpha by infinite.animateFloat(
        initialValue = 0.1f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(800), RepeatMode.Reverse),
        label = "Alpha"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0D0D1A))
    ) {
        // CONTENIDO CENTRAL
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            Text(
                text = "Completo's Journey",
                fontSize = 48.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(32.dp))

            // BOTONES PRINCIPALES
            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "START",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFFEB3B),
                    modifier = Modifier
                        .clickable { onStart() }
                        .graphicsLayer { alpha = blinkAlpha }
                        .padding(16.dp)
                )

                Text(
                    text = "LEADERBOARD",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White,
                    modifier = Modifier
                        .clickable { onLeaderboard() }
                        .padding(16.dp)
                )
            }
        }

        // FOOTER (Logout y Reset)
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(bottom = 24.dp, start = 32.dp, end = 32.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "CERRAR SESIÓN",
                color = Color(0xFF90CAF9),
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier
                    .clickable { onLogout() }
                    .padding(8.dp)
            )

            Text(
                text = "RESETEAR PROGRESO",
                color = Color(0xFFEF5350),
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier
                    .clickable { onResetProgress() }
                    .padding(8.dp)
            )
        }
    }
}