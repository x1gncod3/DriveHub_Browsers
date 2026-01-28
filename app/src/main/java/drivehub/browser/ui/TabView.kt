package com.drivehub.browser.ui

import android.content.Context
import android.util.AttributeSet
import android.util.TypedValue
import android.widget.LinearLayout
import android.widget.TextView
import com.drivehub.browser.data.Tab
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView

class TabView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : MaterialCardView(context, attrs, defStyleAttr) {

    private var tabTitle: TextView
    private var closeButton: MaterialButton
    private var tab: Tab? = null
    private var onCloseClick: ((String) -> Unit)? = null
    private var onClick: ((String) -> Unit)? = null

    init {
        val density = context.resources.displayMetrics.density
        
        // Configure card appearance
        radius = 12 * density
        cardElevation = 2 * density
        strokeWidth = (1 * density).toInt()
        
        // Create container layout
        val container = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding((12 * density).toInt(), (8 * density).toInt(), (4 * density).toInt(), (8 * density).toInt())
        }
        
        tabTitle = TextView(context).apply {
            setTextAppearance(com.google.android.material.R.style.TextAppearance_Material3_BodyMedium)
            maxLines = 1
            ellipsize = android.text.TextUtils.TruncateAt.END
            gravity = android.view.Gravity.CENTER_VERTICAL
            minWidth = (80 * density).toInt()
            maxWidth = (120 * density).toInt()
        }
        
        closeButton = MaterialButton(context, null, com.google.android.material.R.attr.materialIconButtonStyle).apply {
            text = "×"
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
            layoutParams = LinearLayout.LayoutParams((28 * density).toInt(), (28 * density).toInt()).apply {
                marginStart = (8 * density).toInt()
            }
            insetTop = 0
            insetBottom = 0
            // insetLeft and insetRight are not available in MaterialButton
            // minimumWidth = 0
            // minimumHeight = 0
        }
        
        container.addView(tabTitle, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
        container.addView(closeButton)
        
        addView(container)
        setupClickListeners()
    }

    private fun setupClickListeners() {
        setOnClickListener {
            tab?.let { onClick?.invoke(it.id) }
        }
        
        closeButton.setOnClickListener { event ->
            // stopPropagation() is not available in click listeners
            tab?.let { onCloseClick?.invoke(it.id) }
        }
    }

    fun bind(tab: Tab, isActive: Boolean, onCloseClick: ((String) -> Unit)? = null, onClick: ((String) -> Unit)? = null) {
        this.tab = tab
        this.onCloseClick = onCloseClick
        this.onClick = onClick
        
        tabTitle.text = tab.title.ifEmpty { "New Tab" }
        isSelected = isActive
        
        // Update appearance based on active state
        if (isActive) {
            setCardBackgroundColor(resolveThemeColor(com.google.android.material.R.attr.colorPrimaryContainer))
            strokeColor = resolveThemeColor(com.google.android.material.R.attr.colorSecondary)
            tabTitle.setTextColor(resolveThemeColor(com.google.android.material.R.attr.colorOnPrimaryContainer))
            closeButton.setBackgroundColor(resolveThemeColor(com.google.android.material.R.attr.colorSecondary))
            closeButton.setTextColor(resolveThemeColor(com.google.android.material.R.attr.colorOnSecondary))
        } else {
            setCardBackgroundColor(resolveThemeColor(com.google.android.material.R.attr.colorSurfaceContainer))
            strokeColor = resolveThemeColor(com.google.android.material.R.attr.colorOutlineVariant)
            tabTitle.setTextColor(resolveThemeColor(com.google.android.material.R.attr.colorOnSurface))
            closeButton.setBackgroundColor(resolveThemeColor(com.google.android.material.R.attr.colorSurfaceVariant))
            closeButton.setTextColor(resolveThemeColor(com.google.android.material.R.attr.colorOnSurfaceVariant))
        }
    }
    
    private fun resolveThemeColor(attrRes: Int): Int {
        val typedValue = TypedValue()
        context.theme.resolveAttribute(attrRes, typedValue, true)
        return typedValue.data
    }
}
