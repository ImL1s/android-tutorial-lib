package com.tutorial.lib.model

import android.graphics.Rect
import androidx.annotation.IdRes

/**
 * Represents a single step in the tutorial
 */
data class TutorialStep(
    val targetTag: String? = null,
    @IdRes val targetViewId: Int? = null,
    val text: String,
    val maxTooltipWidthDp: Float? = null,
    val shape: HighlightShape = HighlightShape.ROUNDED_RECT,
    val customAction: (() -> Unit)? = null
) {
    init {
        require(targetTag != null || targetViewId != null) {
            "TutorialStep must have at least a targetTag or a targetViewId"
        }
    }
    
    class Builder {
        private var targetTag: String? = null
        private var targetViewId: Int? = null
        private var text: String = ""
        private var maxTooltipWidthDp: Float? = null
        private var shape: HighlightShape = HighlightShape.ROUNDED_RECT
        private var customAction: (() -> Unit)? = null
        
        fun targetTag(tag: String) = apply { this.targetTag = tag }
        fun target(@IdRes viewId: Int) = apply { this.targetViewId = viewId }
        fun text(text: String) = apply { this.text = text }
        fun maxWidth(widthDp: Float) = apply { this.maxTooltipWidthDp = widthDp }
        fun shape(shape: HighlightShape) = apply { this.shape = shape }
        fun onHighlight(action: () -> Unit) = apply { this.customAction = action }
        
        fun build() = TutorialStep(
            targetTag = targetTag,
            targetViewId = targetViewId,
            text = text,
            maxTooltipWidthDp = maxTooltipWidthDp,
            shape = shape,
            customAction = customAction
        )
    }
}

/**
 * Information about a target view's position and associated guide text
 */
data class TargetInfo(
    val rect: Rect,
    val guideText: String,
    val maxTooltipWidthDp: Float? = null,
    val shape: HighlightShape = HighlightShape.ROUNDED_RECT
)

/**
 * Available highlight shapes
 */
enum class HighlightShape {
    CIRCLE,
    RECT,
    ROUNDED_RECT
}