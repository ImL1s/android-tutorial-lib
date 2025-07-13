package com.tutorial.lib.utils

import android.content.res.Resources
import android.graphics.*
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.view.View
import androidx.core.graphics.withSave
import com.tutorial.lib.model.HighlightShape
import com.tutorial.lib.model.LineStyle
import com.tutorial.lib.model.TargetInfo
import com.tutorial.lib.model.TutorialStyle
import timber.log.Timber
import kotlin.math.max
import kotlin.math.min

/**
 * Utility class for bitmap operations
 */
object BitmapProcessor {
    
    /**
     * Create a bitmap from a view
     */
    fun createViewBitmap(view: View): Bitmap? {
        return try {
            val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            view.draw(canvas)
            bitmap
        } catch (e: Exception) {
            Timber.e(e, "Failed to create bitmap from view")
            null
        }
    }
    
    /**
     * Create styled tutorial overlay bitmap
     */
    fun createStyledTutorialBitmap(
        sourceBitmap: Bitmap,
        targets: List<TargetInfo>,
        overlayColor: Int,
        style: TutorialStyle,
        resources: Resources
    ): Bitmap {
        val resultBitmap = sourceBitmap.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(resultBitmap)
        
        // Create dim overlay
        val dimBitmap = Bitmap.createBitmap(sourceBitmap.width, sourceBitmap.height, Bitmap.Config.ARGB_8888)
        val dimCanvas = Canvas(dimBitmap)
        dimCanvas.drawColor(overlayColor)
        
        // Paint for clearing highlights
        val clearPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
        }
        
        // Paint for borders
        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = style.highlightBorderColor
            this.style = Paint.Style.STROKE
            strokeWidth = dpToPx(style.highlightBorderWidthDp, resources)
        }
        
        // Process each target
        val highlightRects = mutableListOf<RectF>()
        targets.forEach { target ->
            val paddingPx = dpToPx(style.highlightPaddingDp, resources)
            val expandedRect = RectF(
                target.rect.left - paddingPx,
                target.rect.top - paddingPx,
                target.rect.right + paddingPx,
                target.rect.bottom + paddingPx
            )
            
            // Clear the highlight area
            when (target.shape) {
                HighlightShape.CIRCLE -> {
                    val centerX = expandedRect.centerX()
                    val centerY = expandedRect.centerY()
                    val radius = max(expandedRect.width(), expandedRect.height()) / 2
                    dimCanvas.drawCircle(centerX, centerY, radius, clearPaint)
                }
                HighlightShape.RECT -> {
                    dimCanvas.drawRect(expandedRect, clearPaint)
                }
                HighlightShape.ROUNDED_RECT -> {
                    val cornerRadius = dpToPx(style.highlightCornerRadiusDp, resources)
                    dimCanvas.drawRoundRect(expandedRect, cornerRadius, cornerRadius, clearPaint)
                }
            }
            
            highlightRects.add(expandedRect)
        }
        
        // Draw dim overlay
        canvas.drawBitmap(dimBitmap, 0f, 0f, null)
        
        // Draw borders and tooltips
        targets.forEachIndexed { index, target ->
            val rect = highlightRects[index]
            
            // Draw border
            when (target.shape) {
                HighlightShape.CIRCLE -> {
                    val centerX = rect.centerX()
                    val centerY = rect.centerY()
                    val radius = max(rect.width(), rect.height()) / 2
                    canvas.drawCircle(centerX, centerY, radius, borderPaint)
                }
                HighlightShape.RECT -> {
                    canvas.drawRect(rect, borderPaint)
                }
                HighlightShape.ROUNDED_RECT -> {
                    val cornerRadius = dpToPx(style.highlightCornerRadiusDp, resources)
                    canvas.drawRoundRect(rect, cornerRadius, cornerRadius, borderPaint)
                }
            }
        }
        
        // Draw tooltips
        drawTooltips(canvas, targets, highlightRects, style, resources)
        
        // Clean up
        dimBitmap.recycle()
        
        return resultBitmap
    }
    
    private fun drawTooltips(
        canvas: Canvas,
        targets: List<TargetInfo>,
        highlightRects: List<RectF>,
        style: TutorialStyle,
        resources: Resources
    ) {
        val tooltipPaint = createTooltipPaint(style)
        val textPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = style.tooltipTextColor
            textSize = spToPx(style.tooltipTextSizeSp, resources)
        }
        
        val tooltipSpacingPx = dpToPx(16f, resources)
        var currentY = dpToPx(100f, resources) // Start position for tooltips
        
        targets.forEachIndexed { index, target ->
            val rect = highlightRects[index]
            val text = target.guideText
            
            // Calculate tooltip dimensions
            val maxWidth = target.maxTooltipWidthDp?.let { dpToPx(it, resources) } 
                ?: dpToPx(250f, resources)
            val paddingH = dpToPx(style.tooltipPaddingHorizontalDp, resources)
            val paddingV = dpToPx(style.tooltipPaddingVerticalDp, resources)
            
            // Create text layout
            val textWidth = min(textPaint.measureText(text), maxWidth - 2 * paddingH)
            val staticLayout = createStaticLayout(text, textPaint, textWidth.toInt())
            
            // Calculate tooltip position
            val tooltipWidth = staticLayout.width + 2 * paddingH
            val tooltipHeight = staticLayout.height + 2 * paddingV
            val tooltipX = (canvas.width - tooltipWidth) / 2
            val tooltipY = currentY
            
            // Draw tooltip background
            val tooltipRect = RectF(
                tooltipX,
                tooltipY,
                tooltipX + tooltipWidth,
                tooltipY + tooltipHeight
            )
            
            if (style.tooltipStyle == com.tutorial.lib.model.TooltipStyle.GRADIENT) {
                val gradient = LinearGradient(
                    tooltipRect.right,
                    tooltipRect.bottom,
                    tooltipRect.left,
                    tooltipRect.top,
                    style.tooltipColors[0],
                    style.tooltipColors.getOrElse(1) { style.tooltipColors[0] },
                    Shader.TileMode.CLAMP
                )
                tooltipPaint.shader = gradient
            } else {
                tooltipPaint.color = style.tooltipColors[0]
            }
            
            val cornerRadius = dpToPx(style.tooltipCornerRadiusDp, resources)
            canvas.drawRoundRect(tooltipRect, cornerRadius, cornerRadius, tooltipPaint)
            
            // Draw text
            canvas.withSave {
                translate(tooltipX + paddingH, tooltipY + paddingV)
                staticLayout.draw(this)
            }
            
            // Draw connector line
            drawConnectorLine(canvas, tooltipRect, rect, style, resources)
            
            // Update Y position for next tooltip
            currentY = tooltipY + tooltipHeight + tooltipSpacingPx
        }
    }
    
    private fun drawConnectorLine(
        canvas: Canvas,
        tooltipRect: RectF,
        targetRect: RectF,
        style: TutorialStyle,
        resources: Resources
    ) {
        val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = style.connectorLineColor
            strokeWidth = dpToPx(style.connectorLineWidthDp, resources)
            this.style = Paint.Style.STROKE
            if (style.connectorLineStyle == LineStyle.DASHED) {
                pathEffect = DashPathEffect(floatArrayOf(10f, 5f), 0f)
            }
        }
        
        val ballPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = style.connectorLineColor
            this.style = Paint.Style.FILL
        }
        
        // Calculate connection points
        val startX = tooltipRect.centerX()
        val startY = tooltipRect.bottom
        val endX = targetRect.centerX()
        val endY = targetRect.top
        
        // Draw line
        val path = Path().apply {
            moveTo(startX, startY)
            lineTo(endX, endY)
        }
        canvas.drawPath(path, linePaint)
        
        // Draw balls at ends
        val ballRadius = dpToPx(style.connectorBallRadiusDp, resources)
        canvas.drawCircle(startX, startY, ballRadius, ballPaint)
        canvas.drawCircle(endX, endY, ballRadius, ballPaint)
    }
    
    private fun createTooltipPaint(tutorialStyle: TutorialStyle): Paint {
        return Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.style = Paint.Style.FILL
        }
    }
    
    private fun createStaticLayout(text: String, paint: TextPaint, width: Int): StaticLayout {
        return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            StaticLayout.Builder.obtain(text, 0, text.length, paint, width).build()
        } else {
            @Suppress("DEPRECATION")
            StaticLayout(text, paint, width, Layout.Alignment.ALIGN_NORMAL, 1f, 0f, false)
        }
    }
    
    private fun dpToPx(dp: Float, resources: Resources): Float {
        return dp * resources.displayMetrics.density
    }
    
    private fun spToPx(sp: Float, resources: Resources): Float {
        return sp * resources.displayMetrics.scaledDensity
    }
}