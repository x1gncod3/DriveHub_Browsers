package com.drivehub.browser.data

import android.content.Context
import android.content.SharedPreferences
import com.drivehub.browser.model.UserAgentProfile

object BrowserPreferences {
    private const val PREFS_NAME = "browser_prefs"
    private const val KEY_DEFAULT_URL = "default_url"
    private const val KEY_DESKTOP_MODE = "desktop_mode"
    private const val KEY_USER_AGENT = "user_agent"
    private const val KEY_ADBLOCK_ENABLED = "adblock_enabled"
    private const val KEY_LAST_URL = "last_url"
    private const val KEY_BOOKMARKS = "bookmarks"
    private const val KEY_ALLOWED_HOSTS = "allowed_hosts"
    
    fun defaultUrl(): String {
        return "https://www.google.com"
    }
    
    fun resolveInitialUrl(context: Context): String {
        return getLastUrl(context) ?: defaultUrl()
    }
    
    fun formatNavigableUrl(input: String): String {
        val trimmed = input.trim()
        if (trimmed.isEmpty()) return ""
        
        return when {
            trimmed.startsWith("http://") || trimmed.startsWith("https://") -> trimmed
            trimmed.contains(".") && !trimmed.contains(" ") -> "https://$trimmed"
            else -> "https://www.google.com/search?q=${trimmed.replace(" ", "+")}"
        }
    }
    
    fun persistUrl(context: Context, url: String) {
        getPrefs(context).edit().putString(KEY_LAST_URL, url).apply()
    }
    
    fun getLastUrl(context: Context): String? {
        return getPrefs(context).getString(KEY_LAST_URL, null)
    }
    
    fun shouldUseDesktopMode(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_DESKTOP_MODE, false)
    }
    
    fun setDesktopMode(context: Context, enabled: Boolean) {
        getPrefs(context).edit().putBoolean(KEY_DESKTOP_MODE, enabled).apply()
    }
    
    fun getUserAgentProfile(context: Context): UserAgentProfile {
        val profileName = getPrefs(context).getString(KEY_USER_AGENT, null)
        return UserAgentProfile.values().find { it.storageKey == profileName } ?: UserAgentProfile.ANDROID_CHROME
    }
    
    fun setUserAgentProfile(context: Context, profile: UserAgentProfile) {
        getPrefs(context).edit().putString(KEY_USER_AGENT, profile.storageKey).apply()
    }
    
    fun isAdBlockEnabled(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_ADBLOCK_ENABLED, true)
    }
    
    fun setAdBlockEnabled(context: Context, enabled: Boolean) {
        getPrefs(context).edit().putBoolean(KEY_ADBLOCK_ENABLED, enabled).apply()
    }
    
    // Bookmarks
    fun getBookmarks(context: Context): List<String> {
        val bookmarksSet = getPrefs(context).getStringSet(KEY_BOOKMARKS, emptySet())
        return bookmarksSet?.toList() ?: emptyList()
    }
    
    fun addBookmark(context: Context, url: String): Boolean {
        val bookmarks = getBookmarks(context).toMutableSet()
        return if (bookmarks.add(url)) {
            getPrefs(context).edit().putStringSet(KEY_BOOKMARKS, bookmarks).apply()
            true
        } else {
            false
        }
    }
    
    fun removeBookmark(context: Context, url: String): Boolean {
        val bookmarks = getBookmarks(context).toMutableSet()
        return if (bookmarks.remove(url)) {
            getPrefs(context).edit().putStringSet(KEY_BOOKMARKS, bookmarks).apply()
            true
        } else {
            false
        }
    }
    
    // Cleartext HTTP handling
    fun isHostAllowedCleartext(context: Context, host: String?): Boolean {
        if (host == null) return false
        val allowedHosts = getPrefs(context).getStringSet(KEY_ALLOWED_HOSTS, emptySet())
        return allowedHosts?.contains(host.lowercase()) == true
    }
    
    fun addAllowedCleartextHost(context: Context, host: String) {
        val allowedHosts = getPrefs(context).getStringSet(KEY_ALLOWED_HOSTS, emptySet())?.toMutableSet() ?: mutableSetOf()
        allowedHosts.add(host.lowercase())
        getPrefs(context).edit().putStringSet(KEY_ALLOWED_HOSTS, allowedHosts).apply()
    }
    
    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }
}
