package com.tutorial.sample

import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import android.util.Log
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.tutorial.lib.core.TutorialManager
import com.tutorial.lib.model.ButtonStyle
import com.tutorial.lib.model.HighlightShape
import com.tutorial.lib.model.LineStyle
import com.tutorial.lib.model.TooltipStyle

class MainActivity : AppCompatActivity() {
    
    private lateinit var tutorialManager: TutorialManager
    private val TAG = "MainActivity"
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate")
        
        // Create UI programmatically
        val layout = ConstraintLayout(this).apply {
            layoutParams = ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.MATCH_PARENT,
                ConstraintLayout.LayoutParams.MATCH_PARENT
            )
            setBackgroundColor(Color.parseColor("#F5F5F5"))
        }
        
        // Toolbar
        val toolbar = Toolbar(this).apply {
            id = android.R.id.text1
            title = "Tutorial Demo App"
            setBackgroundColor(Color.parseColor("#2196F3"))
            setTitleTextColor(Color.WHITE)
            elevation = 8f
        }
        layout.addView(toolbar, ConstraintLayout.LayoutParams(
            ConstraintLayout.LayoutParams.MATCH_PARENT,
            ConstraintLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            topToTop = ConstraintLayout.LayoutParams.PARENT_ID
        })
        
        // Search button
        val searchButton = Button(this).apply {
            id = android.R.id.button1
            text = "🔍 Search"
            setBackgroundColor(Color.WHITE)
            setTextColor(Color.parseColor("#2196F3"))
            setPadding(32, 16, 32, 16)
        }
        layout.addView(searchButton, ConstraintLayout.LayoutParams(
            ConstraintLayout.LayoutParams.WRAP_CONTENT,
            ConstraintLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            topToBottom = toolbar.id
            startToStart = ConstraintLayout.LayoutParams.PARENT_ID
            marginStart = 16
            topMargin = 16
        })
        
        // Profile button
        val profileButton = Button(this).apply {
            id = android.R.id.button2
            text = "👤 Profile"
            setBackgroundColor(Color.WHITE)
            setTextColor(Color.parseColor("#2196F3"))
            setPadding(32, 16, 32, 16)
        }
        layout.addView(profileButton, ConstraintLayout.LayoutParams(
            ConstraintLayout.LayoutParams.WRAP_CONTENT,
            ConstraintLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            topToBottom = toolbar.id
            endToEnd = ConstraintLayout.LayoutParams.PARENT_ID
            marginEnd = 16
            topMargin = 16
        })
        
        // Content card
        val contentCard = androidx.cardview.widget.CardView(this).apply {
            id = android.R.id.widget_frame
            radius = 12f
            cardElevation = 4f
            setCardBackgroundColor(Color.WHITE)
        }
        
        val contentText = TextView(this).apply {
            text = "Welcome to Tutorial Demo!\n\nThis app demonstrates different tutorial styles:\n\n" +
                   "• Blue Gradient Style - Professional look\n" +
                   "• Yellow Solid Style - Playful design\n\n" +
                   "Click the buttons below to see different tutorial styles in action."
            textSize = 16f
            setPadding(24, 24, 24, 24)
            setTextColor(Color.parseColor("#333333"))
        }
        contentCard.addView(contentText)
        layout.addView(contentCard, ConstraintLayout.LayoutParams(
            ConstraintLayout.LayoutParams.MATCH_PARENT,
            ConstraintLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            topToBottom = searchButton.id
            startToStart = ConstraintLayout.LayoutParams.PARENT_ID
            endToEnd = ConstraintLayout.LayoutParams.PARENT_ID
            topMargin = 32
            marginStart = 16
            marginEnd = 16
        })
        
        // FAB
        val fab = FloatingActionButton(this).apply {
            id = android.R.id.button3
            tag = "fab_button"
            setImageResource(android.R.drawable.ic_input_add)
            backgroundTintList = android.content.res.ColorStateList.valueOf(Color.parseColor("#FF5722"))
        }
        layout.addView(fab, ConstraintLayout.LayoutParams(
            ConstraintLayout.LayoutParams.WRAP_CONTENT,
            ConstraintLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID
            endToEnd = ConstraintLayout.LayoutParams.PARENT_ID
            bottomMargin = 16
            marginEnd = 16
        })
        
        // Blue style tutorial button
        val blueStyleButton = Button(this).apply {
            id = android.R.id.custom
            text = "Show Blue Style Tutorial"
            setBackgroundColor(Color.parseColor("#2196F3"))
            setTextColor(Color.WHITE)
            setPadding(32, 24, 32, 24)
        }
        layout.addView(blueStyleButton, ConstraintLayout.LayoutParams(
            ConstraintLayout.LayoutParams.MATCH_PARENT,
            ConstraintLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID
            startToStart = ConstraintLayout.LayoutParams.PARENT_ID
            endToEnd = ConstraintLayout.LayoutParams.PARENT_ID
            bottomMargin = 100
            marginStart = 24
            marginEnd = 24
        })
        
        // Yellow style tutorial button
        val yellowStyleButton = Button(this).apply {
            text = "Show Yellow Style Tutorial"
            setBackgroundColor(Color.parseColor("#FFC107"))
            setTextColor(Color.BLACK)
            setPadding(32, 24, 32, 24)
        }
        layout.addView(yellowStyleButton, ConstraintLayout.LayoutParams(
            ConstraintLayout.LayoutParams.MATCH_PARENT,
            ConstraintLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            bottomToTop = blueStyleButton.id
            startToStart = ConstraintLayout.LayoutParams.PARENT_ID
            endToEnd = ConstraintLayout.LayoutParams.PARENT_ID
            bottomMargin = 16
            marginStart = 24
            marginEnd = 24
        })
        
        setContentView(layout)
        setSupportActionBar(toolbar)
        
        // Set tags after layout is added
        searchButton.tag = "search_button"
        
        // Don't initialize tutorial here - wait for user action
        
        // Blue style button click
        blueStyleButton.setOnClickListener {
            // Destroy previous instance if exists
            if (::tutorialManager.isInitialized) {
                tutorialManager.destroy()
            }
            // Wait for window focus before showing tutorial
            window.decorView.postDelayed({
                if (hasWindowFocus()) {
                    setupBlueGradientTutorial()
                    tutorialManager.forceShow("demo_user")
                } else {
                    // Try again if window doesn't have focus
                    blueStyleButton.performClick()
                }
            }, 100)
        }
        
        // Yellow style button click
        yellowStyleButton.setOnClickListener {
            // Destroy previous instance if exists
            if (::tutorialManager.isInitialized) {
                tutorialManager.destroy()
            }
            // Wait for window focus before showing tutorial
            window.decorView.postDelayed({
                if (hasWindowFocus()) {
                    setupYellowSolidTutorial()
                    tutorialManager.forceShow("demo_user")
                } else {
                    // Try again if window doesn't have focus
                    yellowStyleButton.performClick()
                }
            }, 100)
        }
    }
    
    private fun setupBlueGradientTutorial() {
        tutorialManager = TutorialManager.builder(this)
            .config {
                tutorialId("blue_style_tutorial")
                showOnlyOnce(false) // For demo purposes
                overlayColor(0xD0000000.toInt())
                style(com.tutorial.lib.model.TutorialStyle(
                    highlightBorderColor = Color.parseColor("#2B44B1"),
                    highlightBorderWidthDp = 2f,
                    highlightCornerRadiusDp = 4f,
                    tooltipStyle = TooltipStyle.GRADIENT,
                    tooltipColors = listOf(
                        Color.parseColor("#2B44B1"),
                        Color.parseColor("#015BE2")
                    ),
                    tooltipTextColor = Color.WHITE,
                    connectorLineColor = Color.parseColor("#2B44B1"),
                    connectorLineStyle = LineStyle.DASHED,
                    buttonText = "Got it",
                    buttonStyle = ButtonStyle.GRADIENT_BLUE
                ))
            }
            .steps {
                step {
                    targetTag("search_button")
                    text("Click here to search")
                    maxWidth(120f)
                }
                step {
                    target(android.R.id.button2)
                    text("View your profile")
                    maxWidth(100f)
                }
                step {
                    targetTag("fab_button")
                    text("Add new items")
                    shape(HighlightShape.CIRCLE)
                }
            }
            .build()
    }
    
    private fun setupYellowSolidTutorial() {
        tutorialManager = TutorialManager.builder(this)
            .config {
                tutorialId("yellow_style_tutorial")
                showOnlyOnce(false) // For demo purposes
                overlayColor(0xD0000000.toInt())
                style(com.tutorial.lib.model.TutorialStyle(
                    highlightBorderColor = Color.parseColor("#F7B500"),
                    highlightBorderWidthDp = 2f,
                    highlightCornerRadiusDp = 4f,
                    tooltipStyle = TooltipStyle.SOLID,
                    tooltipColors = listOf(Color.parseColor("#F7B500")),
                    tooltipTextColor = Color.BLACK,
                    connectorLineColor = Color.parseColor("#F7B500"),
                    connectorLineStyle = LineStyle.SOLID,
                    buttonText = "OK",
                    buttonStyle = ButtonStyle.SOLID_YELLOW
                ))
            }
            .steps {
                step {
                    targetTag("search_button")
                    text("Search for content")
                    maxWidth(120f)
                }
                step {
                    target(android.R.id.button2)
                    text("Access your profile")
                    maxWidth(120f)
                }
                step {
                    target(android.R.id.text1)
                    text("App navigation")
                    maxWidth(100f)
                    shape(HighlightShape.RECT)
                }
                step {
                    targetTag("fab_button")
                    text("Quick actions")
                    maxWidth(100f)
                    shape(HighlightShape.CIRCLE)
                }
            }
            .build()
    }
    
    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        Log.d(TAG, "onWindowFocusChanged: $hasFocus")
    }
    
    override fun onDestroy() {
        super.onDestroy()
        if (::tutorialManager.isInitialized) {
            tutorialManager.destroy()
        }
    }
}