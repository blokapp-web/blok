package com.appblocker.ui.util

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.outlined.Chat
import androidx.compose.material.icons.automirrored.outlined.DirectionsRun
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Curated icon catalog for Spaces.
 * Each entry: key (stored in DB) → filled icon + outlined icon.
 */
data class SpaceIconEntry(
    val key: String,
    val label: String,
    val filled: ImageVector,
    val outlined: ImageVector
)

val spaceIconCatalog: List<SpaceIconEntry> = listOf(
    // ── Trabajo & Productividad ──
    SpaceIconEntry("Work", "Trabajo", Icons.Filled.Work, Icons.Outlined.Work),
    SpaceIconEntry("Business", "Negocio", Icons.Filled.BusinessCenter, Icons.Outlined.BusinessCenter),
    SpaceIconEntry("Computer", "Ordenador", Icons.Filled.Computer, Icons.Outlined.Computer),
    SpaceIconEntry("Laptop", "Portátil", Icons.Filled.LaptopMac, Icons.Outlined.LaptopMac),
    SpaceIconEntry("Email", "Email", Icons.Filled.Email, Icons.Outlined.Email),
    SpaceIconEntry("Edit", "Editar", Icons.Filled.Edit, Icons.Outlined.Edit),
    SpaceIconEntry("Description", "Documento", Icons.Filled.Description, Icons.Outlined.Description),
    SpaceIconEntry("Folder", "Carpeta", Icons.Filled.Folder, Icons.Outlined.Folder),
    SpaceIconEntry("Task", "Tarea", Icons.Filled.TaskAlt, Icons.Outlined.TaskAlt),
    SpaceIconEntry("Schedule", "Horario", Icons.Filled.Schedule, Icons.Outlined.Schedule),

    // ── Estudio & Educación ──
    SpaceIconEntry("School", "Estudio", Icons.Filled.School, Icons.Outlined.School),
    SpaceIconEntry("Book", "Libro", Icons.Filled.Book, Icons.Outlined.Book),
    SpaceIconEntry("MenuBook", "Lectura", Icons.AutoMirrored.Filled.MenuBook, Icons.AutoMirrored.Outlined.MenuBook),
    SpaceIconEntry("Science", "Ciencia", Icons.Filled.Science, Icons.Outlined.Science),
    SpaceIconEntry("Calculate", "Cálculo", Icons.Filled.Calculate, Icons.Outlined.Calculate),
    SpaceIconEntry("Lightbulb", "Idea", Icons.Filled.Lightbulb, Icons.Outlined.Lightbulb),
    SpaceIconEntry("Psychology", "Mente", Icons.Filled.Psychology, Icons.Outlined.Psychology),
    SpaceIconEntry("Translate", "Idiomas", Icons.Filled.Translate, Icons.Outlined.Translate),

    // ── Deporte & Salud ──
    SpaceIconEntry("FitnessCenter", "Gym", Icons.Filled.FitnessCenter, Icons.Outlined.FitnessCenter),
    SpaceIconEntry("SportsBasketball", "Deporte", Icons.Filled.SportsBasketball, Icons.Outlined.SportsBasketball),
    SpaceIconEntry("DirectionsRun", "Correr", Icons.AutoMirrored.Filled.DirectionsRun, Icons.AutoMirrored.Outlined.DirectionsRun),
    SpaceIconEntry("SelfImprovement", "Meditar", Icons.Filled.SelfImprovement, Icons.Outlined.SelfImprovement),
    SpaceIconEntry("Spa", "Bienestar", Icons.Filled.Spa, Icons.Outlined.Spa),
    SpaceIconEntry("Favorite", "Salud", Icons.Filled.Favorite, Icons.Outlined.Favorite),
    SpaceIconEntry("LocalHospital", "Médico", Icons.Filled.LocalHospital, Icons.Outlined.LocalHospital),

    // ── Creatividad & Arte ──
    SpaceIconEntry("Palette", "Arte", Icons.Filled.Palette, Icons.Outlined.Palette),
    SpaceIconEntry("Brush", "Pintar", Icons.Filled.Brush, Icons.Outlined.Brush),
    SpaceIconEntry("CameraAlt", "Foto", Icons.Filled.CameraAlt, Icons.Outlined.CameraAlt),
    SpaceIconEntry("MusicNote", "Música", Icons.Filled.MusicNote, Icons.Outlined.MusicNote),
    SpaceIconEntry("Headphones", "Audio", Icons.Filled.Headphones, Icons.Outlined.Headphones),
    SpaceIconEntry("Movie", "Película", Icons.Filled.Movie, Icons.Outlined.Movie),
    SpaceIconEntry("DesignServices", "Diseño", Icons.Filled.DesignServices, Icons.Outlined.DesignServices),

    // ── Social & Comunicación ──
    SpaceIconEntry("People", "Social", Icons.Filled.People, Icons.Outlined.People),
    SpaceIconEntry("Chat", "Chat", Icons.AutoMirrored.Filled.Chat, Icons.AutoMirrored.Outlined.Chat),
    SpaceIconEntry("Forum", "Foro", Icons.Filled.Forum, Icons.Outlined.Forum),
    SpaceIconEntry("Group", "Grupo", Icons.Filled.Group, Icons.Outlined.Group),
    SpaceIconEntry("PersonAdd", "Contacto", Icons.Filled.PersonAdd, Icons.Outlined.PersonAdd),

    // ── Hogar & Vida diaria ──
    SpaceIconEntry("Home", "Hogar", Icons.Filled.Home, Icons.Outlined.Home),
    SpaceIconEntry("Kitchen", "Cocina", Icons.Filled.Kitchen, Icons.Outlined.Kitchen),
    SpaceIconEntry("ShoppingCart", "Compras", Icons.Filled.ShoppingCart, Icons.Outlined.ShoppingCart),
    SpaceIconEntry("Restaurant", "Comida", Icons.Filled.Restaurant, Icons.Outlined.Restaurant),
    SpaceIconEntry("LocalCafe", "Café", Icons.Filled.LocalCafe, Icons.Outlined.LocalCafe),
    SpaceIconEntry("Bed", "Dormir", Icons.Filled.Bed, Icons.Outlined.Bed),
    SpaceIconEntry("Weekend", "Relax", Icons.Filled.Weekend, Icons.Outlined.Weekend),

    // ── Viaje & Transporte ──
    SpaceIconEntry("Flight", "Viaje", Icons.Filled.Flight, Icons.Outlined.Flight),
    SpaceIconEntry("DirectionsCar", "Coche", Icons.Filled.DirectionsCar, Icons.Outlined.DirectionsCar),
    SpaceIconEntry("Train", "Tren", Icons.Filled.Train, Icons.Outlined.Train),
    SpaceIconEntry("Map", "Mapa", Icons.Filled.Map, Icons.Outlined.Map),
    SpaceIconEntry("Explore", "Explorar", Icons.Filled.Explore, Icons.Outlined.Explore),
    SpaceIconEntry("Terrain", "Naturaleza", Icons.Filled.Terrain, Icons.Outlined.Terrain),

    // ── Gaming & Entretenimiento ──
    SpaceIconEntry("SportsEsports", "Gaming", Icons.Filled.SportsEsports, Icons.Outlined.SportsEsports),
    SpaceIconEntry("Gamepad", "Control", Icons.Filled.Gamepad, Icons.Outlined.Gamepad),
    SpaceIconEntry("Casino", "Juego", Icons.Filled.Casino, Icons.Outlined.Casino),
    SpaceIconEntry("Celebration", "Fiesta", Icons.Filled.Celebration, Icons.Outlined.Celebration),
    SpaceIconEntry("TheaterComedy", "Teatro", Icons.Filled.TheaterComedy, Icons.Outlined.TheaterComedy),

    // ── Tech & Código ──
    SpaceIconEntry("Code", "Código", Icons.Filled.Code, Icons.Outlined.Code),
    SpaceIconEntry("Terminal", "Terminal", Icons.Filled.Terminal, Icons.Outlined.Terminal),
    SpaceIconEntry("Storage", "Datos", Icons.Filled.Storage, Icons.Outlined.Storage),
    SpaceIconEntry("Cloud", "Nube", Icons.Filled.Cloud, Icons.Outlined.Cloud),
    SpaceIconEntry("Wifi", "Red", Icons.Filled.Wifi, Icons.Outlined.Wifi),
    SpaceIconEntry("Settings", "Config", Icons.Filled.Settings, Icons.Outlined.Settings),

    // ── Misceláneo ──
    SpaceIconEntry("Star", "Favorito", Icons.Filled.Star, Icons.Outlined.Star),
    SpaceIconEntry("Bolt", "Rayo", Icons.Filled.Bolt, Icons.Outlined.Bolt),
    SpaceIconEntry("Shield", "Escudo", Icons.Filled.Shield, Icons.Outlined.Shield),
    SpaceIconEntry("Rocket", "Cohete", Icons.Filled.RocketLaunch, Icons.Outlined.RocketLaunch),
    SpaceIconEntry("Flag", "Meta", Icons.Filled.Flag, Icons.Outlined.Flag),
    SpaceIconEntry("Eco", "Eco", Icons.Filled.Eco, Icons.Outlined.Eco),
    SpaceIconEntry("Diamond", "Premium", Icons.Filled.Diamond, Icons.Outlined.Diamond),
    SpaceIconEntry("Timer", "Timer", Icons.Filled.Timer, Icons.Outlined.Timer),
    SpaceIconEntry("Nightlight", "Noche", Icons.Filled.Nightlight, Icons.Outlined.Nightlight),
    SpaceIconEntry("WbSunny", "Día", Icons.Filled.WbSunny, Icons.Outlined.WbSunny),
)

/** Quick lookup by key */
fun getSpaceIcon(key: String): SpaceIconEntry? =
    spaceIconCatalog.firstOrNull { it.key == key }

/** Default icon when none selected */
val defaultSpaceIcon = spaceIconCatalog.first { it.key == "Work" }
