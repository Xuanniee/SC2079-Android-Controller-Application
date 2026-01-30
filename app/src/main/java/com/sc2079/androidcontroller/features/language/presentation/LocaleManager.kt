package com.sc2079.androidcontroller.features.language.presentation

import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.os.LocaleListCompat

/**
 * Supported languages in the app
 */
enum class AppLanguage(
    val code: String,
    val displayName: String,
    val nativeName: String
) {
    SYSTEM("", "System Default", "System"),
    ENGLISH("en", "English", "English"),
    MALAY("ms", "Malay", "Bahasa Melayu"),
    TAMIL("ta", "Tamil", "தமிழ்"),
    CHINESE_SIMPLIFIED("zh-CN", "Chinese (Simplified)", "简体中文")
}

/**
 * Global language state holder
 */
object LocaleState {
    var currentLanguage by mutableStateOf(AppLanguage.SYSTEM)
        private set
    
    val isChangingLanguage = mutableStateOf(false)
    
    /**
     * Change the app language
     */
    fun setLanguage(language: AppLanguage) {
        if (currentLanguage == language) return
        
        isChangingLanguage.value = true
        currentLanguage = language
        
        val localeList = if (language == AppLanguage.SYSTEM) {
            // Use empty locale list to follow system default
            LocaleListCompat.getEmptyLocaleList()
        } else {
            LocaleListCompat.forLanguageTags(language.code)
        }
        
        AppCompatDelegate.setApplicationLocales(localeList)
    }
    
    /**
     * Initialize from current app locale
     */
    fun initFromCurrentLocale() {
        val currentLocales = AppCompatDelegate.getApplicationLocales()
        currentLanguage = if (currentLocales.isEmpty) {
            AppLanguage.SYSTEM
        } else {
            val tag = currentLocales.toLanguageTags()
            AppLanguage.entries.find { it.code == tag } ?: AppLanguage.SYSTEM
        }
        // Reset loading state when activity is recreated
        isChangingLanguage.value = false
    }
}

