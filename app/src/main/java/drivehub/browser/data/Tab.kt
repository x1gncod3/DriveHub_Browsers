package com.drivehub.browser.data

data class Tab(
    val id: String,
    var url: String,
    var title: String,
    var canGoBack: Boolean = false,
    var canGoForward: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
) {
    companion object {
        fun createNew(): Tab {
            return Tab(
                id = "tab_${System.currentTimeMillis()}",
                url = "https://www.google.com",
                title = "New Tab"
            )
        }
    }
}
