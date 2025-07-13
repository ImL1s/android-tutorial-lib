package com.tutorial.lib.storage

import android.content.Context
import android.content.SharedPreferences

/**
 * Interface for tutorial state storage
 */
interface TutorialStorage {
    fun isTutorialShown(tutorialId: String, userId: String? = null): Boolean
    fun setTutorialShown(tutorialId: String, shown: Boolean, userId: String? = null)
    fun clearTutorialState(tutorialId: String, userId: String? = null)
    fun clearAllTutorialStates()
}

/**
 * Default implementation using SharedPreferences
 */
class SharedPreferencesStorage(context: Context) : TutorialStorage {
    
    private val prefs: SharedPreferences = context.getSharedPreferences(
        "tutorial_prefs",
        Context.MODE_PRIVATE
    )
    
    override fun isTutorialShown(tutorialId: String, userId: String?): Boolean {
        val key = buildKey(tutorialId, userId)
        return prefs.getBoolean(key, false)
    }
    
    override fun setTutorialShown(tutorialId: String, shown: Boolean, userId: String?) {
        val key = buildKey(tutorialId, userId)
        prefs.edit().putBoolean(key, shown).apply()
    }
    
    override fun clearTutorialState(tutorialId: String, userId: String?) {
        val key = buildKey(tutorialId, userId)
        prefs.edit().remove(key).apply()
    }
    
    override fun clearAllTutorialStates() {
        prefs.edit().clear().apply()
    }
    
    private fun buildKey(tutorialId: String, userId: String?): String {
        return if (userId != null) {
            "tutorial_${tutorialId}_user_$userId"
        } else {
            "tutorial_$tutorialId"
        }
    }
}