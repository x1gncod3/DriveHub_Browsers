package com.drivehub.browser.data

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONArray
import org.json.JSONObject

object TabManager {
    private const val PREFS_NAME = "tabs_prefs"
    private const val KEY_TABS = "tabs"
    private const val KEY_CURRENT_TAB = "current_tab"
    
    fun getTabs(context: Context): List<Tab> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val tabsJson = prefs.getString(KEY_TABS, null) ?: return emptyList()
        
        return try {
            val jsonArray = JSONArray(tabsJson)
            val tabs = mutableListOf<Tab>()
            for (i in 0 until jsonArray.length()) {
                val tabJson = jsonArray.getJSONObject(i)
                tabs.add(
                    Tab(
                        id = tabJson.getString("id"),
                        url = tabJson.getString("url"),
                        title = tabJson.getString("title"),
                        canGoBack = tabJson.optBoolean("canGoBack", false),
                        canGoForward = tabJson.optBoolean("canGoForward", false),
                        createdAt = tabJson.optLong("createdAt", System.currentTimeMillis())
                    )
                )
            }
            tabs
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    fun saveTabs(context: Context, tabs: List<Tab>) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val editor = prefs.edit()
        
        try {
            val jsonArray = JSONArray()
            tabs.forEach { tab ->
                val tabJson = JSONObject().apply {
                    put("id", tab.id)
                    put("url", tab.url)
                    put("title", tab.title)
                    put("canGoBack", tab.canGoBack)
                    put("canGoForward", tab.canGoForward)
                    put("createdAt", tab.createdAt)
                }
                jsonArray.put(tabJson)
            }
            editor.putString(KEY_TABS, jsonArray.toString())
        } catch (e: Exception) {
            // Handle error
        }
        
        editor.apply()
    }
    
    fun addTab(context: Context, tab: Tab): List<Tab> {
        val tabs = getTabs(context).toMutableList()
        tabs.add(tab)
        saveTabs(context, tabs)
        return tabs
    }
    
    fun removeTab(context: Context, tabId: String): List<Tab> {
        val tabs = getTabs(context).toMutableList()
        tabs.removeAll { it.id == tabId }
        saveTabs(context, tabs)
        return tabs
    }
    
    fun updateTab(context: Context, tab: Tab) {
        val tabs = getTabs(context).toMutableList()
        val index = tabs.indexOfFirst { it.id == tab.id }
        if (index >= 0) {
            tabs[index] = tab
            saveTabs(context, tabs)
        }
    }
    
    fun getCurrentTabId(context: Context): String? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_CURRENT_TAB, null)
    }
    
    fun setCurrentTabId(context: Context, tabId: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_CURRENT_TAB, tabId).apply()
    }
}
