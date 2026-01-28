package com.drivehub.browser.web

import android.webkit.WebResourceResponse
import java.io.ByteArrayInputStream

object AdBlocker {
    
    private val youtubeAdPatterns = setOf(
        // YouTube Ad URLs
        "youtube.com/api/stats/ads",
        "youtube.com/api/stats/watchtime",
        "youtube.com/pagead",
        "youtube.com/ptracking",
        "youtube.com/get_midroll_info",
        "youtube.com/annotations_invideo",
        "youtube.com/eurl",
        "youtube.com/youtubei/v1/player/ad_break",
        "youtube.com/youtubei/v1/next",
        "youtube.com/iframe_api",
        "youtube.com/player_ias",
        "ytimg.com/yts/img/pixel",
        "ytimg.com/yts/jsbin/player",
        "googlevideo.com/videoplayback?*ad_break",
        "googlevideo.com/videoplayback?*adformat",
        "googlevideo.com/videoplayback?*adsystem",
        
        // YouTube Ad Parameters
        "ad_format=",
        "ad_tag=",
        "ad_slots=",
        "midroll",
        "preroll",
        "postroll",
        "overlay",
        "ad_break",
        "ad_pod",
        "ad_sequence",
        "adunit",
        "adsystem",
        "adformat",
        "ad_type",
        "ad_flags",
        "ad_tag",
        "_ads",
        "_ad=",
        "-ads",
        "-ad=",
        ".ads",
        ".ad=",
        "ads.",
        "ad."
    )
    
    private val adHosts = setOf(
        // Google Ads
        "googleads.g.doubleclick.net",
        "googlesyndication.com",
        "googleadservices.com",
        "google-analytics.com",
        "googletagmanager.com",
        "googletagservices.com",
        "doubleclick.net",
        "googleanalytics.com",
        
        // YouTube Ad Domains
        "r1---sn-vgqs7n7d.googlevideo.com",
        "r2---sn-vgqs7n7d.googlevideo.com",
        "r3---sn-vgqs7n7d.googlevideo.com",
        "r4---sn-vgqs7n7d.googlevideo.com",
        "r5---sn-vgqs7n7d.googlevideo.com",
        
        // Social Media Ads
        "facebook.com/tr",
        "connect.facebook.net",
        "ads.twitter.com",
        "analytics.twitter.com",
        
        // Other Ad Networks
        "ads.yahoo.com",
        "ads.linkedin.com",
        "ads.pinterest.com",
        "ads.reddit.com",
        "outbrain.com",
        "taboola.com",
        "scorecardresearch.com",
        "quantserve.com",
        "2mdn.net",
        "adnxs.com",
        "criteo.com",
        "adsafeprotected.com",
        "doubleverify.com",
        "moatads.com",
        "serving-sys.com",
        "turn.com",
        "rubiconproject.com",
        "indexww.com"
    )
    
    private val adUrlPatterns = listOf(
        "/ads/",
        "/ad/",
        "/advertisement/",
        "/advertising/",
        "/adsystem/",
        "/adservice/",
        "/adserver/",
        "/adnxs/",
        "/doubleclick/",
        "/googleads/",
        "/googlesyndication/",
        "/googleadservices/",
        "/googletagmanager/",
        "/google-analytics/",
        "/facebook.com/tr",
        "/analytics/",
        "/tracking/",
        "/metrics/",
        "/beacon/",
        "/pixel/",
        "/impression/",
        "/click/",
        "/view/",
        "/conversion/"
    )
    
    // Essential services that should never be blocked
    private val essentialServices = setOf(
        "google.com/recaptcha",
        "recaptcha.net",
        "www.google.com/recaptcha",
        "www.recaptcha.net",
        "youtube.com/watch",
        "youtube.com/embed",
        "youtube.com/player",
        "youtube.com/iframe",
        "googlevideo.com/videoplayback",
        "youtube.com/get_video",
        "youtube.com/youtubei/v1/playback",
        "google.com/video",
        "fonts.googleapis.com",
        "fonts.gstatic.com",
        "ajax.googleapis.com",
        "cdnjs.cloudflare.com",
        "unpkg.com",
        "jsdelivr.net"
    )
    
    fun shouldBlockUrl(url: String): Boolean {
        val lowerUrl = url.lowercase()
        
        // Never block essential services
        essentialServices.forEach { service ->
            if (lowerUrl.contains(service)) return false
        }
        
        // Allow common CDN and utility services
        val allowedServices = listOf(
            "cloudflare.com",
            "cloudinary.com",
            "amazonaws.com",
            "github.com",
            "gitlab.com",
            "bitbucket.org",
            "stackoverflow.com",
            "w3.org",
            "jquery.com",
            "bootstrapcdn.com"
        )
        
        allowedServices.forEach { service ->
            if (lowerUrl.contains(service)) return false
        }
        
        // Enhanced YouTube ad blocking
        if (lowerUrl.contains("youtube.com") || lowerUrl.contains("googlevideo.com") || lowerUrl.contains("ytimg.com")) {
            // Allow essential YouTube functionality
            if (lowerUrl.contains("watch?v=") ||
                lowerUrl.contains("embed/") ||
                lowerUrl.contains("www.youtube.com") ||
                lowerUrl.contains("m.youtube.com") ||
                lowerUrl.contains("/iframe") ||
                lowerUrl.contains("youtube.com/youtubei/v1/playback") ||
                lowerUrl.contains("youtube.com/get_video") ||
                lowerUrl.contains("youtube.com/api/manifest/") ||
                lowerUrl.contains("youtube.com/videoplayback") ||
                lowerUrl.contains("ytimg.com/vi/") ||
                lowerUrl.contains("youtube.com/s/player/") ||
                lowerUrl.contains("youtube.com/player_")) {
                
                // But still block if it contains ad patterns
                youtubeAdPatterns.forEach { pattern ->
                    if (lowerUrl.contains(pattern)) return true
                }
                return false
            }
            
            // Block YouTube ad-related requests
            youtubeAdPatterns.forEach { pattern ->
                if (lowerUrl.contains(pattern)) return true
            }
            
            // Block suspicious YouTube URLs
            if (lowerUrl.contains("/ad") ||
                lowerUrl.contains("ad_") ||
                lowerUrl.contains("_ad") ||
                lowerUrl.contains("-ad") ||
                lowerUrl.contains(".ad") ||
                lowerUrl.contains("doubleclick") ||
                lowerUrl.contains("pagead") ||
                lowerUrl.contains("syndication")) {
                return true
            }
        }
        
        // Block known ad hosts
        adHosts.forEach { host ->
            if (lowerUrl.contains(host)) {
                if (!lowerUrl.contains("videoplayback") && 
                    !lowerUrl.contains("recaptcha")) {
                    return true
                }
            }
        }
        
        // Block URLs with ad patterns
        adUrlPatterns.forEach { pattern ->
            if (lowerUrl.contains(pattern)) {
                if (!lowerUrl.contains("videoplayback") && 
                    !lowerUrl.contains("recaptcha")) {
                    return true
                }
            }
        }
        
        // Block tracking domains
        val trackingDomains = listOf(
            "doubleclick.net",
            "google-analytics.com",
            "googletagmanager.com",
            "googlesyndication.com",
            "facebook.com/tr",
            "connect.facebook.net",
            "analytics.twitter.com",
            "scorecardresearch.com",
            "quantserve.com"
        )
        
        trackingDomains.forEach { domain ->
            if (lowerUrl.contains(domain)) {
                if (!lowerUrl.contains("recaptcha") && 
                    !lowerUrl.contains("videoplayback")) {
                    return true
                }
            }
        }
        
        return false
    }
    
    fun createEmptyResponse(): WebResourceResponse {
        return WebResourceResponse(
            "text/plain",
            "utf-8",
            ByteArrayInputStream("".toByteArray())
        )
    }
}
