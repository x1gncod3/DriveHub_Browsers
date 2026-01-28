package com.drivehub.browser.model

enum class UserAgentProfile(val storageKey: String, val displayName: String) {
    ANDROID_CHROME("android_chrome", "Android Chrome"),
    SAFARI("safari", "Safari (iOS/macOS)")
}
