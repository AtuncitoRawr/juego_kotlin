<h1 align="center">🎮 Completos Journey</h1>

Completos Journey es un videojuego de plataformas 2D desarrollado nativamente para Android usando **Kotlin** y **Jetpack Compose**.  
Implementa una arquitectura **MVVM** con persistencia local mediante **Room** y sincronización remota con una **API REST en Xano**, permitiendo autenticación, progreso del jugador y un sistema global de marcadores.

---

# 📋 Características Principales

## 🕹️ Jugabilidad
- **Mecánicas de Plataforma:** Movimiento fluido, saltos, colisiones precisas y enemigos con IA básica de patrulla.  
- **Sistema de Niveles:** Diseño con mapas ASCII para múltiples escenarios.  
- **Modo Inmersivo:** La app opera 100% fullscreen ocultando las barras del sistema.

---

# 🔐 Sistema de Usuarios
- **Autenticación REST:** Login y Registro.  
- **Sesión Persistente:** El JWT permanece activo incluso tras cerrar la app.  
- **Navegación Condicional:** Transición automática entre Login y Menú según el estado de autenticación.

---

# 💾 Persistencia de Datos

## 🗄️ Local (Room)
- Almacenamiento del **nivel máximo desbloqueado**.  
- Guardado seguro del **token JWT**.  
- Gestión de estado global mediante la entidad `AppSession`.

## ☁️ Remoto (Xano + Retrofit)
- **Auth API:** login/signup.  
- **Leaderboard API:** puntajes filtrados por nivel, tiempo o muertes.  
- **Score Submission:** envío de puntajes al finalizar niveles.

---

# 🛠️ Stack Tecnológico

## 🔤 Lenguaje
- Kotlin

## 🎨 UI
- Jetpack Compose (Material 3)

## 🧩 Arquitectura
- MVVM  
- DI Manual (Singleton via `MyApplication`)

## 🌐 Red / Networking
- Retrofit 2  
- OkHttp (interceptor para Bearer Token)  
- Gson

## 🗃️ Base de Datos
- Room (SQLite)  
- Coroutines & Flow

## 🖥️ Backend
- Xano (backend no-code)

---

# 🏗️ Arquitectura del Proyecto (MVVM)

## 1️⃣ Model / Data
Define estructuras y lógica pura:
- `AppSession` — estado local persistente.  
- `LeaderboardEntry` — modelo de datos de la API.  
- `Physics.kt` — manejo de colisiones, gravedad y movimiento.

## 2️⃣ Repositories
Única fuente de verdad:
- `LocalProgressRepository` — progreso y token mediante Room.  
- `AuthRepository` — login/signup + persistencia del token.  
- `LeaderboardRepository` — puntajes globales desde Xano.

## 3️⃣ ViewModels
- `GameViewModel` — física del juego, bucle y avance.

## 4️⃣ UI (Compose)
- `AppRoot` — router según AuthState.  
- `GameScreen`, `LoginScreen`, `LeaderboardScreen`, etc.

---

# 🚀 Instalación y Ejecución

## 1. Clonar el repositorio
```bash
git clone https://github.com/AtuncitoRawr/juego_kotlin.git
```
## 🚀 2. Abrir en Android Studio


1. Abre **Android Studio**.  
2. Ve a **File → Open** y selecciona la carpeta del proyecto.  
3. Espera a que **Gradle** sincronice automáticamente.

---

## 🔧 3. Configurar APIs (Opcional)

Las URLs base del backend se encuentran en:

- `AppRoot.kt`
- `MyApplication.kt`

---

## ▶️ 4. Ejecutar la App

1. Conecta un **dispositivo Android físico** o inicia un **emulador**.  
2. Haz clic en **Run 'app'** para compilar y ejecutar el proyecto.

---

## 📡 Endpoints de Xano

### 🔑 Auth Group (api: `IHYvoOXu`)
- **POST** `/auth/signup` — Crea un nuevo usuario.  
- **POST** `/auth/login` — Retorna un token JWT para autenticación.

---

### 🏆 Scores Group (api: `momBPpCL`)
- **GET** `/GET/scores` — Obtiene leaderboard filtrado por nivel, tiempo o muertes.  
- **POST** `/POST_/score` — Envía la puntuación del jugador *(requiere `Authorization: Bearer <token>`)*.

---

## 📝 Créditos
Proyecto desarrollado por Nicolas Bozzo como entrega de la asignatura **Aplicaciones Móviles**.





