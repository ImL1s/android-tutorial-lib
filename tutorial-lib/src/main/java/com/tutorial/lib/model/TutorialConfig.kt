package com.tutorial.lib.model

import android.graphics.Color
import androidx.annotation.ColorInt

/**
 * Configuration for the tutorial appearance and behavior
 */
data class TutorialConfig(
    val tutorialId: String = "default_tutorial",
    val showOnlyOnce: Boolean = true,
    val overlayColor: Int = 0xD0000000.toInt(),
    val style: TutorialStyle = TutorialStyle(),
    val animation: AnimationConfig = AnimationConfig()
) {
    class Builder {
        private var tutorialId: String = "default_tutorial"
        private var showOnlyOnce: Boolean = true
        private var overlayColor: Int = 0xD0000000.toInt()
        private var style: TutorialStyle = TutorialStyle()
        private var animation: AnimationConfig = AnimationConfig()
        
        fun tutorialId(id: String) = apply { this.tutorialId = id }
        fun showOnlyOnce(once: Boolean) = apply { this.showOnlyOnce = once }
        fun overlayColor(@ColorInt color: Int) = apply { this.overlayColor = color }
        fun style(style: TutorialStyle) = apply { this.style = style }
        fun animation(animation: AnimationConfig) = apply { this.animation = animation }
        
        fun build() = TutorialConfig(
            tutorialId = tutorialId,
            showOnlyOnce = showOnlyOnce,
            overlayColor = overlayColor,
            style = style,
            animation = animation
        )
    }
}

/**
 * Visual style configuration
 */
data class TutorialStyle(
    val highlightBorderColor: Int = Color.parseColor("#2B44B1"),
    val highlightBorderWidthDp: Float = 2f,
    val highlightCornerRadiusDp: Float = 4f,
    val highlightPaddingDp: Float = 8f,
    val tooltipStyle: TooltipStyle = TooltipStyle.GRADIENT,
    val tooltipColors: List<Int> = listOf(
        Color.parseColor("#2B44B1"),
        Color.parseColor("#015BE2")
    ),
    val tooltipTextColor: Int = Color.WHITE,
    val tooltipTextSizeSp: Float = 14f,
    val tooltipCornerRadiusDp: Float = 100f,
    val tooltipPaddingHorizontalDp: Float = 16f,
    val tooltipPaddingVerticalDp: Float = 8f,
    val connectorLineColor: Int = Color.parseColor("#2B44B1"),
    val connectorLineStyle: LineStyle = LineStyle.DASHED,
    val connectorLineWidthDp: Float = 2f,
    val connectorBallRadiusDp: Float = 4f,
    val buttonText: String = "知道了",
    val buttonStyle: ButtonStyle = ButtonStyle.GRADIENT_BLUE
)

/**
 * Animation configuration
 */
data class AnimationConfig(
    val fadeInDuration: Long = 300L,
    val fadeOutDuration: Long = 200L,
    val highlightPulseDuration: Long = 1000L,
    val enablePulseAnimation: Boolean = false
)

enum class TooltipStyle {
    SOLID,
    GRADIENT
}

enum class LineStyle {
    SOLID,
    DASHED
}

enum class ButtonStyle {
    SOLID_BLUE,
    GRADIENT_BLUE,
    SOLID_YELLOW,
    GRADIENT_YELLOW,
    CUSTOM
}