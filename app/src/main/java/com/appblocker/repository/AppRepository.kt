package com.appblocker.repository

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import com.appblocker.data.AppDatabase
import com.appblocker.data.BlockedApp
import com.appblocker.model.InstalledApp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class AppRepository(context: Context) {

    private val pm = context.packageManager
    private val dao = AppDatabase.getInstance(context).blockedAppDao()
    private val ownPackage = context.packageName

    /** Devuelve todas las apps con lanzador (icono en el drawer), excepto AppBlocker */
    suspend fun getInstalledApps(): List<InstalledApp> = withContext(Dispatchers.IO) {
        val launcherIntent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        pm.queryIntentActivities(launcherIntent, PackageManager.MATCH_ALL)
            .asSequence()
            .map { it.activityInfo }
            .filter { it.packageName != ownPackage }
            .distinctBy { it.packageName }
            .map { info ->
                InstalledApp(
                    packageName = info.packageName,
                    appName = info.loadLabel(pm).toString()
                )
            }
            .sortedBy { it.appName.lowercase() }
            .toList()
    }

    fun getBlockedPackages(): Flow<List<String>> = dao.getAllBlockedPackages()

    suspend fun toggleBlock(app: InstalledApp) {
        if (dao.isBlocked(app.packageName)) {
            dao.deleteByPackage(app.packageName)
        } else {
            dao.insert(BlockedApp(packageName = app.packageName, appName = app.appName))
        }
    }
}
