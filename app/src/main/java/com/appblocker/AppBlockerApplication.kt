package com.appblocker

import android.app.Application
import com.appblocker.data.AppDatabase
import com.appblocker.data.BlockStateManager
import com.appblocker.data.EmergencyUnlockManager
import com.appblocker.data.LanguageManager
import com.appblocker.data.NotificationBlockManager
import com.appblocker.data.SpaceManager
import com.appblocker.data.ThemeManager

class AppBlockerApplication : Application() {

    lateinit var database: AppDatabase
        private set

    lateinit var blockStateManager: BlockStateManager
        private set

    lateinit var emergencyUnlockManager: EmergencyUnlockManager
        private set

    lateinit var themeManager: ThemeManager
        private set

    lateinit var spaceManager: SpaceManager
        private set

    lateinit var notificationBlockManager: NotificationBlockManager
        private set

    lateinit var languageManager: LanguageManager
        private set

    override fun onCreate() {
        super.onCreate()
        database = AppDatabase.getInstance(this)
        blockStateManager = BlockStateManager(this, database.statsDao())
        emergencyUnlockManager = EmergencyUnlockManager(this)
        themeManager = ThemeManager(this)
        spaceManager = SpaceManager(this, database.spaceDao(), database.blockedAppDao())
        notificationBlockManager = NotificationBlockManager(this)
        languageManager = LanguageManager(this)
    }
}
