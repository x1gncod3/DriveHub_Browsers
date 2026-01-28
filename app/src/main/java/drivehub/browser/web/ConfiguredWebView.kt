package com.drivehub.browser.web

import android.content.ActivityNotFoundException
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Message
import android.os.Build
import android.view.View
import android.webkit.CookieManager
import android.webkit.DownloadListener
import android.webkit.PermissionRequest
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.net.http.SslError
import android.webkit.SslErrorHandler
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.core.net.toUri
import com.drivehub.browser.R
import com.drivehub.browser.model.UserAgentProfile

data class BrowserCallbacks(
    val onUrlChange: (String) -> Unit = {},
    val onTitleChange: (String?) -> Unit = {},
    val onProgressChange: (Int) -> Unit = {},
    val onShowDownloadPrompt: (Uri) -> Unit = {},
    val onError: (Int, String?) -> Unit = { _, _ -> },
    val onCleartextNavigationRequested: (
        Uri,
        allowOnce: () -> Unit,
        allowHostPermanently: () -> Unit,
        cancel: () -> Unit
    ) -> Unit = { _, _, _, cancel -> cancel() },
    val onEnterFullscreen: (View, WebChromeClient.CustomViewCallback) -> Unit = { _, _ -> },
    val onExitFullscreen: () -> Unit = {}
)

fun configureWebView(
    webView: WebView,
    callbacks: BrowserCallbacks = BrowserCallbacks(),
    useDesktopMode: Boolean = false,
    userAgentProfile: UserAgentProfile = UserAgentProfile.ANDROID_CHROME
) {
    with(webView) {
        setBackgroundColor(Color.TRANSPARENT)
        isHorizontalScrollBarEnabled = false
        isVerticalScrollBarEnabled = true

        WebView.setWebContentsDebuggingEnabled(false)

        val originalUserAgent = settings.userAgentString
        setTag(R.id.webview_original_user_agent_tag, originalUserAgent)
        settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            javaScriptCanOpenWindowsAutomatically = true
            setSupportMultipleWindows(true)
            setSupportZoom(true)
            builtInZoomControls = true
            displayZoomControls = false
            loadWithOverviewMode = useDesktopMode
            useWideViewPort = useDesktopMode
            cacheMode = WebSettings.LOAD_DEFAULT
            allowContentAccess = true
            allowFileAccess = false
            mediaPlaybackRequiresUserGesture = false
            mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
            
            // Enhanced media settings for video playback
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                mediaPlaybackRequiresUserGesture = false
                setAllowFileAccessFromFileURLs(false)
                setAllowUniversalAccessFromFileURLs(false)
            }
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                layoutAlgorithm = WebSettings.LayoutAlgorithm.TEXT_AUTOSIZING
            }
            
            // Hardware acceleration for video
            setLayerType(View.LAYER_TYPE_HARDWARE, null)
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
                offscreenPreRaster = true
                // Enable remote debugging for development
                if (context.applicationInfo.flags and android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE != 0) {
                    WebView.setWebContentsDebuggingEnabled(true)
                }
            }
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                safeBrowsingEnabled = false
            }
            
            // Database and storage settings
            databaseEnabled = true
            // Note: App cache is deprecated in newer Android versions
            // setAppCacheEnabled(true)
            // setAppCachePath(context.cacheDir.absolutePath)
            // setAppCacheMaxSize(1024 * 1024 * 8) // 8MB cache
            
            // Font and text settings
            defaultFontSize = 16
            defaultTextEncodingName = "utf-8"
            
            // Network and performance settings
            loadsImagesAutomatically = true
            blockNetworkImage = false
            blockNetworkLoads = false
            
            // Additional compatibility settings
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // Note: ForceDark is deprecated in newer versions
                // setForceDark(WebSettings.ForceDark.OFF)
            }
        }
        applyUserAgent(userAgentProfile, useDesktopMode)
        val scale = context.resources.displayMetrics.density * 100
        setInitialScale(scale.toInt())

        // Configure Cookie Manager for better compatibility
        CookieManager.getInstance().apply {
            setAcceptCookie(true)
            setAcceptThirdPartyCookies(webView, true)
            // Note: File scheme cookies are deprecated in newer versions
            // if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            //     setAcceptFileSchemeCookies(true)
            // }
        }

        setLayerType(View.LAYER_TYPE_HARDWARE, null)

        webViewClient = object : WebViewClient() {
            override fun shouldInterceptRequest(view: WebView?, request: WebResourceRequest?): WebResourceResponse? {
                request?.url?.toString()?.let { url ->
                    if (com.drivehub.browser.data.BrowserPreferences.isAdBlockEnabled(view?.context ?: return@let) && 
                        com.drivehub.browser.web.AdBlocker.shouldBlockUrl(url)) {
                        return AdBlocker.createEmptyResponse()
                    }
                }
                return super.shouldInterceptRequest(view, request)
            }

            override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                val uri = request.url
                val scheme = uri.scheme?.lowercase()
                if (scheme == "http") {
                    val host = uri.host?.lowercase()
                    if (!com.drivehub.browser.data.BrowserPreferences.isHostAllowedCleartext(view.context, host)) {
                        val allowOnce = {
                            view.post { view.loadUrl(uri.toString()) }
                            kotlin.Unit
                        }
                        val allowHost = {
                            view.context?.let { ctx ->
                                val hostToStore = uri.host?.lowercase()
                                if (hostToStore != null) com.drivehub.browser.data.BrowserPreferences.addAllowedCleartextHost(ctx, hostToStore)
                            }
                            view.post { view.loadUrl(uri.toString()) }
                            kotlin.Unit
                        }
                        val cancel = { kotlin.Unit }
                        callbacks.onCleartextNavigationRequested(uri, allowOnce, allowHost, cancel)
                        return true
                    }
                }
                return handleUri(view, uri)
            }

            private fun handleUri(view: WebView, uri: Uri?): Boolean {
                uri ?: return false
                val scheme = uri.scheme?.lowercase()
                if (scheme == null || scheme in setOf("http", "https", "about", "file", "data", "javascript")) {
                    return false
                }

                return true
            }

            override fun onPageStarted(view: WebView, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                android.util.Log.d("DriveHub", "Page started loading: $url")
                val stringUrl = url ?: return
                val uri = Uri.parse(stringUrl)
                val scheme = uri.scheme?.lowercase()
                if (scheme == "http") {
                    val host = uri.host?.lowercase()
                    val allowedOnce = getTag(R.id.webview_allow_once_uri_tag) as? String
                    if (allowedOnce == stringUrl) {
                        setTag(R.id.webview_allow_once_uri_tag, null)
                    } else if (!com.drivehub.browser.data.BrowserPreferences.isHostAllowedCleartext(view.context, host)) {
                        stopLoading()
                        val allowOnce = {
                            setTag(R.id.webview_allow_once_uri_tag, stringUrl)
                            view.post { view.loadUrl(stringUrl) }
                            kotlin.Unit
                        }
                        val allowHost = {
                            view.context?.let { ctx ->
                                val hostToStore = uri.host?.lowercase()
                                if (hostToStore != null) com.drivehub.browser.data.BrowserPreferences.addAllowedCleartextHost(ctx, hostToStore)
                            }
                            view.post { view.loadUrl(stringUrl) }
                            kotlin.Unit
                        }
                        val cancel = { kotlin.Unit }
                        callbacks.onCleartextNavigationRequested(uri, allowOnce, allowHost, cancel)
                        return
                    }
                }
                url.let(callbacks.onUrlChange)
            }

            override fun onPageFinished(view: WebView, url: String?) {
                super.onPageFinished(view, url)
                android.util.Log.d("DriveHub", "Page finished loading: $url")
                url?.let(callbacks.onUrlChange)
            }

            override fun onReceivedError(
                view: WebView,
                request: WebResourceRequest,
                error: WebResourceError
            ) {
                if (request.isForMainFrame) {
                    val code = error.errorCode
                    val shouldShowErrorPage = when (code) {
                        WebViewClient.ERROR_HOST_LOOKUP,
                        WebViewClient.ERROR_CONNECT,
                        WebViewClient.ERROR_TIMEOUT,
                        WebViewClient.ERROR_UNKNOWN,
                        WebViewClient.ERROR_PROXY_AUTHENTICATION -> true
                        else -> false
                    }

                    if (shouldShowErrorPage) {
                        val failed = request.url?.toString().orEmpty()
                        val message = error.description?.toString().orEmpty()
                        
                        // For DNS errors, try to load a local fallback page instead
                        if (code == WebViewClient.ERROR_HOST_LOOKUP) {
                            try {
                                val fallbackHtml = createNetworkErrorFallbackHtml(failed, message)
                                view.loadDataWithBaseURL(null, fallbackHtml, "text/html", "UTF-8", null)
                                return
                            } catch (_: Exception) {
                                // Fallback to asset error page if HTML creation fails
                            }
                        }
                        
                        val assetUrl = "file:///android_asset/error.html?failedUrl=${Uri.encode(failed)}&code=$code&message=${Uri.encode(message)}"
                        try {
                            view.loadUrl(assetUrl)
                        } catch (_: Exception) {
                            callbacks.onError(code, error.description?.toString())
                        }
                        return
                    }
                }

                callbacks.onError(error.errorCode, error.description?.toString())
            }

            override fun onReceivedHttpError(
                view: WebView,
                request: WebResourceRequest,
                errorResponse: WebResourceResponse
            ) {
                if (request.isForMainFrame) {
                    val status = try { errorResponse.statusCode } catch (_: Exception) { -1 }
                    val reason = errorResponse.reasonPhrase ?: ""

                    if (status == 429) {
                        return
                    }

                    val failed = request.url?.toString().orEmpty()
                    val assetUrl = "file:///android_asset/error.html?failedUrl=${Uri.encode(failed)}&httpStatus=$status&code=$status&message=${Uri.encode(reason)}"
                    try {
                        view.loadUrl(assetUrl)
                        return
                    } catch (_: Exception) {}
                    
                    callbacks.onError(status, reason)
                    return
                }
            }

            override fun onReceivedSslError(view: WebView, handler: SslErrorHandler, error: SslError) {
                val primary = try { error.primaryError } catch (_: Exception) { -1 }
                val url = error.url ?: ""
                val message = "SSL error: $primary"
                val assetUrl = "file:///android_asset/error.html?failedUrl=${Uri.encode(url)}&sslError=$primary&message=${Uri.encode(message)}"
                try {
                    view.loadUrl(assetUrl)
                    handler.cancel()
                    return
                } catch (_: Exception) {}
                
                handler.cancel()
                callbacks.onError(primary, message)
            }
        }

        webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                callbacks.onProgressChange(newProgress)
            }

            override fun onReceivedTitle(view: WebView?, title: String?) {
                callbacks.onTitleChange(title)
            }

            override fun onShowCustomView(view: View?, callback: CustomViewCallback?) {
                if (view != null && callback != null) {
                    callbacks.onEnterFullscreen(view, callback)
                } else {
                    super.onShowCustomView(view, callback)
                }
            }

            override fun onHideCustomView() {
                callbacks.onExitFullscreen()
                super.onHideCustomView()
            }

            override fun onPermissionRequest(request: PermissionRequest?) {
                if (request == null) {
                    super.onPermissionRequest(null)
                    return
                }

                // Grant all media-related permissions for video playback
                val grantable = request.resources.filter { resource ->
                    resource == PermissionRequest.RESOURCE_PROTECTED_MEDIA_ID ||
                    resource == PermissionRequest.RESOURCE_VIDEO_CAPTURE ||
                    resource == PermissionRequest.RESOURCE_AUDIO_CAPTURE ||
                    resource == PermissionRequest.RESOURCE_MIDI_SYSEX
                }.toTypedArray()

                if (grantable.isNotEmpty()) {
                    this@with.post { request.grant(grantable) }
                } else {
                    // For other permissions, deny them
                    request.deny()
                }
            }

            override fun onCreateWindow(
                view: WebView?,
                isDialog: Boolean,
                isUserGesture: Boolean,
                resultMsg: Message?
            ): Boolean {
                return false
            }
            
            override fun onShowCustomView(view: View?, requestedOrientation: Int, callback: CustomViewCallback?) {
                // Handle custom view with orientation
                if (view != null && callback != null) {
                    callbacks.onEnterFullscreen(view, callback)
                } else {
                    super.onShowCustomView(view, requestedOrientation, callback)
                }
            }
        }

        setDownloadListener(DownloadListener { url, _, _, _, _ ->
            val uri = url?.takeIf { it.isNotBlank() }?.toUri() ?: return@DownloadListener
            callbacks.onShowDownloadPrompt(uri)
        })
    }
}

fun WebView.updateDesktopMode(enable: Boolean, profile: UserAgentProfile) {
    applyUserAgent(profile, enable)
    reload()
}

fun WebView.updateUserAgentProfile(profile: UserAgentProfile, desktop: Boolean) {
    applyUserAgent(profile, desktop)
    reload()
}

fun WebView.releaseCompletely() {
    stopLoading()
    webChromeClient = WebChromeClient()
    webViewClient = WebViewClient()
    destroy()
}

private fun WebView.applyUserAgent(profile: UserAgentProfile, desktop: Boolean) {
    setTag(R.id.webview_user_agent_profile_tag, profile.storageKey)
    settings.userAgentString = buildUserAgent(profile, desktop)
    settings.useWideViewPort = desktop
    settings.loadWithOverviewMode = desktop
}

private fun buildUserAgent(profile: UserAgentProfile, desktop: Boolean): String {
    return when (profile) {
        UserAgentProfile.ANDROID_CHROME -> if (desktop) WINDOWS_CHROME_UA else MOBILE_CHROME_UA
        UserAgentProfile.SAFARI -> if (desktop) SAFARI_MAC_UA else SAFARI_IOS_UA
    }
}

private const val CHROME_VERSION = "143.0.0.0"
private const val MOBILE_CHROME_UA = "Mozilla/5.0 (Linux; Android 10; K) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/${CHROME_VERSION} Mobile Safari/537.36"
private const val WINDOWS_CHROME_UA = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/${CHROME_VERSION} Safari/537.36"
private const val SAFARI_MAC_UA = "Mozilla/5.0 (Macintosh; Intel Mac OS X 14_0_0) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/17.0 Safari/605.1.15"
private const val SAFARI_IOS_UA = "Mozilla/5.0 (iPhone; CPU iPhone OS 17_0 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/17.0 Mobile/15E148 Safari/604.1"

private fun createNetworkErrorFallbackHtml(failedUrl: String, errorMessage: String): String {
    return """
        <!DOCTYPE html>
        <html>
        <head>
            <title>Network Error - DriveHub Browser</title>
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <style>
                body { 
                    font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
                    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
                    margin: 0; padding: 20px; min-height: 100vh;
                    display: flex; align-items: center; justify-content: center;
                    color: white;
                }
                .container { 
                    max-width: 400px; text-align: center; 
                    background: rgba(255,255,255,0.1); padding: 30px; border-radius: 20px;
                    backdrop-filter: blur(10px);
                }
                h1 { font-size: 2.5em; margin-bottom: 20px; }
                .url { 
                    background: rgba(255,255,255,0.2); padding: 10px; border-radius: 10px; 
                    margin: 20px 0; word-break: break-all; font-family: monospace;
                }
                .message { background: rgba(255,255,255,0.1); padding: 15px; border-radius: 10px; margin: 20px 0; }
                .buttons { margin-top: 30px; }
                button { 
                    background: rgba(255,255,255,0.2); border: 1px solid rgba(255,255,255,0.3);
                    color: white; padding: 12px 20px; margin: 5px; border-radius: 25px;
                    cursor: pointer; font-size: 14px; transition: all 0.3s;
                }
                button:hover { background: rgba(255,255,255,0.3); transform: translateY(-2px); }
                .local-links { margin-top: 30px; font-size: 12px; opacity: 0.8; }
                a { color: #ffd700; text-decoration: none; }
                a:hover { text-decoration: underline; }
            </style>
        </head>
        <body>
            <div class="container">
                <h1>🌐 Network Error</h1>
                <p>Cannot reach this site due to network issues in the emulator.</p>
                
                <div class="url">
                    <strong>Attempted URL:</strong><br>
                    ${failedUrl.takeIf { it.isNotBlank() } ?: "Unknown"}
                </div>
                
                <div class="message">
                    <strong>Error:</strong> ${errorMessage.takeIf { it.isNotBlank() } ?: "DNS resolution failed"}
                </div>
                
                <div class="buttons">
                    <button onclick="window.history.back()">← Go Back</button>
                    <button onclick="window.location.reload()">↻ Retry</button>
                </div>
                
                <div class="local-links">
                    <p><strong>Try local content:</strong></p>
                    <a href="file:///sdcard/Download/test.html">Test Page</a> |
                    <a href="file:///android_asset/www/index.html">Local Docs</a>
                </div>
                
                <p style="margin-top: 30px; font-size: 12px; opacity: 0.7;">
                    This is a common issue with Android emulators.<br>
                    The browser works with local files and when deployed on real devices.
                </p>
            </div>
            
            <script>
                // Auto-retry after 10 seconds
                setTimeout(() => {
                    if (confirm('Auto-retry loading the page?')) {
                        window.location.reload();
                    }
                }, 10000);
            </script>
        </body>
        </html>
    """.trimIndent()
}
