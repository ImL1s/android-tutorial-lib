package com.tutorial.lib.ui

import android.content.DialogInterface
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.core.graphics.toColorInt
import androidx.fragment.app.DialogFragment
import com.tutorial.lib.model.ButtonStyle
import com.tutorial.lib.model.TutorialConfig
import timber.log.Timber

/**
 * Full-screen dialog fragment that displays the tutorial overlay
 */
class TutorialDialogFragment : DialogFragment() {
    
    private var bitmap: Bitmap? = null
    private var config: TutorialConfig? = null
    private var onDismissCallback: (() -> Unit)? = null
    
    companion object {
        const val TAG = "TutorialDialog"
        
        fun newInstance(
            bitmap: Bitmap,
            config: TutorialConfig,
            onDismiss: (() -> Unit)? = null
        ): TutorialDialogFragment {
            return TutorialDialogFragment().apply {
                this.bitmap = bitmap
                this.config = config
                this.onDismissCallback = onDismiss
            }
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_TITLE, android.R.style.Theme_Black_NoTitleBar_Fullscreen)
    }
    
    override fun onStart() {
        super.onStart()
        dialog?.window?.apply {
            setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN or
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN or
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
            )
            setBackgroundDrawable(ColorDrawable(Color.BLACK))
            attributes?.apply {
                width = WindowManager.LayoutParams.MATCH_PARENT
                height = WindowManager.LayoutParams.MATCH_PARENT
                flags = flags or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
            }
        }
    }
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val context = requireContext()
        val resources = context.resources
        val style = config?.style ?: return View(context)
        
        // Root layout
        val rootLayout = FrameLayout(context).apply {
            layoutParams = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            setBackgroundColor(Color.BLACK)
        }
        
        // ImageView for bitmap
        val imageView = ImageView(context).apply {
            layoutParams = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            scaleType = ImageView.ScaleType.FIT_XY
            bitmap?.let { setImageBitmap(it) }
        }
        rootLayout.addView(imageView)
        
        // Close button
        val closeButton = createCloseButton(context, style)
        rootLayout.addView(closeButton)
        
        // Click listeners
        closeButton.setOnClickListener { dismiss() }
        rootLayout.setOnClickListener { dismiss() }
        
        return rootLayout
    }
    
    private fun createCloseButton(context: android.content.Context, style: com.tutorial.lib.model.TutorialStyle): Button {
        return Button(context).apply {
            text = style.buttonText
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
            setTextColor(Color.WHITE)
            typeface = typeface?.let {
                android.graphics.Typeface.create(it, android.graphics.Typeface.BOLD)
            } ?: android.graphics.Typeface.DEFAULT_BOLD
            
            // Set button background based on style
            background = when (style.buttonStyle) {
                ButtonStyle.GRADIENT_BLUE -> createGradientDrawable(
                    listOf("#2B44B1".toColorInt(), "#015BE2".toColorInt())
                )
                ButtonStyle.GRADIENT_YELLOW -> createGradientDrawable(
                    listOf("#F7B500".toColorInt(), "#FFD700".toColorInt())
                )
                ButtonStyle.SOLID_BLUE -> createSolidDrawable("#2B44B1".toColorInt())
                ButtonStyle.SOLID_YELLOW -> createSolidDrawable("#F7B500".toColorInt())
                ButtonStyle.CUSTOM -> createSolidDrawable(style.tooltipColors.firstOrNull() ?: Color.BLUE)
            }
            
            layoutParams = FrameLayout.LayoutParams(
                dpToPx(120f),
                dpToPx(50f)
            ).apply {
                gravity = Gravity.CENTER_HORIZONTAL or Gravity.BOTTOM
                bottomMargin = dpToPx(80f)
            }
        }
    }
    
    private fun createGradientDrawable(colors: List<Int>): GradientDrawable {
        return GradientDrawable().apply {
            orientation = GradientDrawable.Orientation.BR_TL
            setColors(colors.toIntArray())
            cornerRadius = dpToPx(25f).toFloat()
        }
    }
    
    private fun createSolidDrawable(color: Int): GradientDrawable {
        return GradientDrawable().apply {
            setColor(color)
            cornerRadius = dpToPx(25f).toFloat()
        }
    }
    
    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        onDismissCallback?.invoke()
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        // Don't recycle bitmap here as it might be used elsewhere
        bitmap = null
    }
    
    private fun dpToPx(dp: Float): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dp,
            resources.displayMetrics
        ).toInt()
    }
}