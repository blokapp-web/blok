# ── BLOK ProGuard Rules ──

# Room
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-keep @androidx.room.Dao class *
-dontwarn androidx.room.**

# Kotlin Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembers class kotlinx.coroutines.** { volatile <fields>; }

# Compose — ships its own consumer R8 rules; no keep-all needed
-dontwarn androidx.compose.**

# Keep data classes used by Room
-keep class com.appblocker.data.** { *; }
-keep class com.appblocker.model.** { *; }

# Keep services
-keep class com.appblocker.service.AppBlockerAccessibilityService { *; }
-keep class com.appblocker.service.BlokNotificationListenerService { *; }

# Keep NFC activities
-keep class com.appblocker.NfcToggleActivity { *; }
-keep class com.appblocker.ui.screens.BlockOverlayActivity { *; }

# Keep Space entities
-keep class com.appblocker.data.Space { *; }
-keep class com.appblocker.data.SpaceApp { *; }
-keep class com.appblocker.data.SpaceWithApps { *; }
