package com.ghostwan.scoreskeeper

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat

/**
 * BroadcastReceiver for changing app language via ADB (debug/screenshots only).
 *
 * Usage:
 *   adb shell am broadcast -a com.ghostwan.scoreskeeper.SET_LANGUAGE --es lang fr
 *   adb shell am broadcast -a com.ghostwan.scoreskeeper.SET_LANGUAGE --es lang system
 */
class LanguageBroadcastReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "LanguageBroadcast"
        private const val ACTION_SET_LANGUAGE = "com.ghostwan.scoreskeeper.SET_LANGUAGE"
        private const val EXTRA_LANG = "lang"
        private val SUPPORTED_LANGUAGES = setOf("en", "fr", "es", "de")
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (!BuildConfig.DEBUG) {
            Log.w(TAG, "Language broadcast ignored in release build")
            return
        }

        if (intent.action != ACTION_SET_LANGUAGE) return

        val lang = intent.getStringExtra(EXTRA_LANG)
        if (lang == null) {
            Log.e(TAG, "Missing 'lang' extra. Usage: --es lang fr|en|es|de|system")
            return
        }

        val localeList = if (lang == "system") {
            Log.i(TAG, "Resetting to system language")
            LocaleListCompat.getEmptyLocaleList()
        } else if (lang in SUPPORTED_LANGUAGES) {
            Log.i(TAG, "Setting app language to: $lang")
            LocaleListCompat.forLanguageTags(lang)
        } else {
            Log.e(TAG, "Unsupported language: $lang. Supported: $SUPPORTED_LANGUAGES")
            return
        }

        AppCompatDelegate.setApplicationLocales(localeList)
    }
}
