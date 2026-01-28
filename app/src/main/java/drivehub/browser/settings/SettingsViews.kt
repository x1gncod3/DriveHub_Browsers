package com.drivehub.browser.settings

import android.content.Context
import android.view.View
import android.widget.LinearLayout
import com.google.android.material.card.MaterialCardView
import com.google.android.material.materialswitch.MaterialSwitch
import com.google.android.material.textview.MaterialTextView
import com.drivehub.browser.data.BrowserPreferences
import com.drivehub.browser.model.UserAgentProfile

object SettingsViews {
    
    fun createSettingsContent(context: Context, isCarMode: Boolean, onClose: () -> Unit): View {
        val container = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40, 40, 40, 40)
        }
        
        // Ad Block Setting
        container.addView(createSettingItem(
            context,
            "Ad Blocker",
            "Block ads and tracking scripts",
            BrowserPreferences.isAdBlockEnabled(context),
            { enabled -> BrowserPreferences.setAdBlockEnabled(context, enabled) }
        ))
        
        // Desktop Mode Setting
        container.addView(createSettingItem(
            context,
            "Desktop Mode",
            "Request desktop versions of websites",
            BrowserPreferences.shouldUseDesktopMode(context),
            { enabled -> BrowserPreferences.setDesktopMode(context, enabled) }
        ))
        
        return container
    }
    
    private fun createSettingItem(
        context: Context,
        title: String,
        description: String,
        defaultValue: Boolean,
        onToggle: (Boolean) -> Unit
    ): View {
        val card = MaterialCardView(context).apply {
            radius = 16f
            setCardBackgroundColor(context.getColor(com.google.android.material.R.attr.colorSurfaceContainer))
            strokeWidth = 1
            strokeColor = context.getColor(com.google.android.material.R.attr.colorOutlineVariant)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 0, 24)
            }
        }
        
        val content = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(24, 24, 24, 24)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }
        
        val textContainer = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            ).apply {
                marginEnd = 16
            }
        }
        
        val titleView = MaterialTextView(context).apply {
            text = title
            setTextAppearance(com.google.android.material.R.style.TextAppearance_Material3_BodyLarge)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = 4
            }
        }
        
        val descriptionView = MaterialTextView(context).apply {
            text = description
            setTextAppearance(com.google.android.material.R.style.TextAppearance_Material3_BodySmall)
            alpha = 0.7f
        }
        
        val switch = MaterialSwitch(context).apply {
            isChecked = defaultValue
            setOnCheckedChangeListener { _, isChecked ->
                onToggle(isChecked)
            }
        }
        
        textContainer.addView(titleView)
        textContainer.addView(descriptionView)
        content.addView(textContainer)
        content.addView(switch)
        card.addView(content)
        
        return card
    }
}
