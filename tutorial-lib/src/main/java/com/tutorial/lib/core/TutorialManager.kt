package com.tutorial.lib.core

import android.app.Activity
import android.graphics.Rect
import android.view.View
import androidx.core.view.doOnLayout
import androidx.core.view.doOnPreDraw
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentActivity
import com.tutorial.lib.model.*
import com.tutorial.lib.storage.SharedPreferencesStorage
import com.tutorial.lib.storage.TutorialStorage
import com.tutorial.lib.ui.TutorialDialogFragment
import com.tutorial.lib.utils.BitmapProcessor
import android.os.Looper
import com.tutorial.lib.utils.ViewFinder
import kotlinx.coroutines.*
import timber.log.Timber
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

/**
 * Main entry point for the tutorial library
 */
class TutorialManager private constructor(
    private val activity: FragmentActivity,
    private val config: TutorialConfig,
    private val steps: List<TutorialStep>,
    private val storage: TutorialStorage,
    private val triggers: List<TutorialTrigger>
) {
    
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private val isProcessing = AtomicBoolean(false)
    
    init {
        setupTriggers()
    }
    
    /**
     * Show the tutorial
     */
    fun show(userId: String? = null) {
        if (!isProcessing.compareAndSet(false, true)) {
            Timber.w("Tutorial is already being processed")
            return
        }
        
        if (config.showOnlyOnce && storage.isTutorialShown(config.tutorialId, userId)) {
            Timber.d("Tutorial already shown for user: $userId")
            isProcessing.set(false)
            return
        }
        
        scope.launch {
            try {
                delay(300) // Small delay to ensure UI is ready
                showTutorialInternal(userId)
            } catch (e: Exception) {
                Timber.e(e, "Error showing tutorial")
                isProcessing.set(false)
            }
        }
    }
    
    /**
     * Force show the tutorial (ignores showOnlyOnce setting)
     */
    fun forceShow(userId: String? = null) {
        Timber.d("forceShow called for userId: $userId")
        
        if (!isProcessing.compareAndSet(false, true)) {
            Timber.w("Tutorial is already being processed")
            return
        }
        
        scope.launch {
            try {
                // Wait for activity to be fully ready
                var attempts = 0
                while (!isActivityReady() && attempts < 10) {
                    delay(100)
                    attempts++
                }
                
                if (!isActivityReady()) {
                    Timber.e("Activity not ready after ${attempts * 100}ms")
                    isProcessing.set(false)
                    return@launch
                }
                
                Timber.d("Activity ready, showing tutorial")
                showTutorialInternal(userId)
            } catch (e: Exception) {
                Timber.e(e, "Error showing tutorial")
                isProcessing.set(false)
            }
        }
    }
    
    private suspend fun showTutorialInternal(userId: String?) = withContext(Dispatchers.Main) {
        val rootView = activity.window.decorView.rootView
        Timber.d("showTutorialInternal - rootView dimensions: ${rootView.width}x${rootView.height}")
        
        // Ensure root view is ready
        if (rootView.width <= 0 || rootView.height <= 0) {
            Timber.d("Root view not ready, waiting for layout")
            rootView.doOnLayout {
                Timber.d("Root view layout complete, dimensions: ${it.width}x${it.height}")
                scope.launch {
                    processAndShowTutorial(it, userId)
                }
            }
        } else {
            processAndShowTutorial(rootView, userId)
        }
    }
    
    private suspend fun processAndShowTutorial(rootView: View, userId: String?) {
        try {
            Timber.d("processAndShowTutorial started")
            
            // Ensure we're on the main thread
            ensureMainThread()
            
            // Wait a frame to ensure all views are properly laid out
            delay(16)
            
            // Collect all targets synchronously
            val targets = collectTargetsSync(rootView)
            
            if (targets.isEmpty()) {
                Timber.e("No valid targets found out of ${steps.size} steps")
                // Log details about each step for debugging
                steps.forEachIndexed { index, step ->
                    Timber.d("Step $index: tag=${step.targetTag}, id=${step.targetViewId}, text=${step.text}")
                }
                isProcessing.set(false)
                return
            }
            
            Timber.d("Found ${targets.size} of ${steps.size} targets")
            
            // Create screenshot
            val screenshotBitmap = withContext(Dispatchers.IO) {
                BitmapProcessor.createViewBitmap(rootView)
            }
            
            if (screenshotBitmap == null) {
                Timber.e("Failed to create screenshot")
                isProcessing.set(false)
                return
            }
            
            // Process bitmap
            val processedBitmap = withContext(Dispatchers.Default) {
                BitmapProcessor.createStyledTutorialBitmap(
                    sourceBitmap = screenshotBitmap,
                    targets = targets,
                    overlayColor = config.overlayColor,
                    style = config.style,
                    resources = activity.resources
                )
            }
            
            // Recycle original screenshot
            withContext(Dispatchers.IO) {
                screenshotBitmap.recycle()
            }
            
            // Show dialog on main thread
            withContext(Dispatchers.Main) {
                if (activity.isFinishing || activity.isDestroyed) {
                    processedBitmap.recycle()
                    isProcessing.set(false)
                    return@withContext
                }
                
                val fragmentManager = activity.supportFragmentManager
                if (!fragmentManager.isStateSaved) {
                    try {
                        // Dismiss any existing dialog
                        (fragmentManager.findFragmentByTag(TutorialDialogFragment.TAG) as? TutorialDialogFragment)?.dismiss()
                        
                        val dialog = TutorialDialogFragment.newInstance(
                            bitmap = processedBitmap,
                            config = config,
                            onDismiss = {
                                processedBitmap.recycle()
                                isProcessing.set(false)
                                if (config.showOnlyOnce) {
                                    storage.setTutorialShown(config.tutorialId, true, userId)
                                }
                            }
                        )
                        dialog.show(fragmentManager, TutorialDialogFragment.TAG)
                        Timber.d("Tutorial dialog shown successfully")
                    } catch (e: Exception) {
                        Timber.e(e, "Failed to show dialog")
                        processedBitmap.recycle()
                        isProcessing.set(false)
                    }
                } else {
                    Timber.w("FragmentManager state is saved, cannot show dialog")
                    processedBitmap.recycle()
                    isProcessing.set(false)
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Error in processAndShowTutorial")
            isProcessing.set(false)
        }
    }
    
    private fun collectTargetsSync(rootView: View): List<TargetInfo> {
        val targetInfos = mutableListOf<TargetInfo>()
        
        Timber.d("collectTargetsSync: Processing ${steps.size} steps")
        
        steps.forEachIndexed { index, step ->
            val targetView = findTargetView(rootView, step)
            
            if (targetView != null) {
                Timber.d("Step $index: Found view - isShown=${targetView.isShown}, visibility=${targetView.visibility}, " +
                        "width=${targetView.width}, height=${targetView.height}, " +
                        "tag=${targetView.tag}, id=${targetView.id}")
                
                if (targetView.isShown) {
                    val rect = Rect()
                    val hasRect = targetView.getGlobalVisibleRect(rect)
                    
                    Timber.d("Step $index: getGlobalVisibleRect=$hasRect, rect=$rect")
                    
                    if (hasRect && !rect.isEmpty) {
                        targetInfos.add(
                            TargetInfo(
                                rect = rect,
                                guideText = step.text,
                                maxTooltipWidthDp = step.maxTooltipWidthDp,
                                shape = step.shape
                            )
                        )
                        Timber.d("Step $index: Successfully added target: ${step.text}")
                    } else {
                        Timber.w("Step $index: Target view has empty rect: ${step.text}")
                    }
                } else {
                    Timber.w("Step $index: Target view not shown: ${step.text}")
                }
            } else {
                Timber.w("Step $index: Target view not found: tag=${step.targetTag}, id=${step.targetViewId}")
            }
        }
        
        Timber.d("collectTargetsSync: Found ${targetInfos.size} valid targets")
        return targetInfos
    }
    
    private fun findTargetView(rootView: View, step: TutorialStep): View? {
        var targetView: View? = null
        
        // Try finding by tag first
        if (step.targetTag != null) {
            val viewsWithTag = ViewFinder.findViewsWithTag(rootView, step.targetTag)
            Timber.d("Found ${viewsWithTag.size} views with tag '${step.targetTag}'")
            targetView = viewsWithTag.firstOrNull { it.isVisible }
        }
        
        // Try finding by ID if tag search failed
        if (targetView == null && step.targetViewId != null) {
            targetView = ViewFinder.findViewById(rootView, step.targetViewId)
            if (targetView != null) {
                Timber.d("Found view by ID ${step.targetViewId}, visible=${targetView.isVisible}")
            }
        }
        
        return targetView
    }
    
    private fun setupTriggers() {
        triggers.forEach { trigger ->
            when (trigger) {
                is TutorialTrigger.OnFirstLaunch -> {
                    // Automatically handled in show() method
                }
                is TutorialTrigger.OnEvent -> {
                    // User needs to manually call show() when event occurs
                }
                is TutorialTrigger.Manual -> {
                    // User manually calls show()
                }
            }
        }
    }
    
    /**
     * Check if activity is ready to show tutorial
     */
    private fun isActivityReady(): Boolean {
        return !activity.isFinishing && 
               !activity.isDestroyed && 
               activity.window != null &&
               activity.window.decorView.width > 0 &&
               activity.window.decorView.height > 0 &&
               activity.hasWindowFocus()
    }
    
    /**
     * Ensure we're on the main thread
     */
    private fun ensureMainThread() {
        check(Looper.myLooper() == Looper.getMainLooper()) {
            "Must be called on the main thread"
        }
    }
    
    /**
     * Clean up resources
     */
    fun destroy() {
        scope.cancel()
        isProcessing.set(false)
    }
    
    companion object {
        /**
         * Create a new tutorial builder
         */
        fun builder(activity: FragmentActivity): Builder {
            return Builder(activity)
        }
    }
    
    /**
     * Builder for creating TutorialManager instances
     */
    class Builder(private val activity: FragmentActivity) {
        private var config = TutorialConfig()
        private val steps = mutableListOf<TutorialStep>()
        private var storage: TutorialStorage? = null
        private val triggers = mutableListOf<TutorialTrigger>()
        
        fun config(block: TutorialConfig.Builder.() -> Unit) = apply {
            config = TutorialConfig.Builder().apply(block).build()
        }
        
        fun storage(storage: TutorialStorage) = apply {
            this.storage = storage
        }
        
        fun steps(block: StepsBuilder.() -> Unit) = apply {
            steps.addAll(StepsBuilder().apply(block).build())
        }
        
        fun addStep(step: TutorialStep) = apply {
            steps.add(step)
        }
        
        fun triggers(block: TriggersBuilder.() -> Unit) = apply {
            triggers.addAll(TriggersBuilder().apply(block).build())
        }
        
        fun build(): TutorialManager {
            val storageImpl = storage ?: SharedPreferencesStorage(activity)
            return TutorialManager(
                activity = activity,
                config = config,
                steps = steps,
                storage = storageImpl,
                triggers = triggers.ifEmpty { listOf(TutorialTrigger.Manual) }
            )
        }
    }
    
    /**
     * DSL builder for steps
     */
    class StepsBuilder {
        private val steps = mutableListOf<TutorialStep>()
        
        fun step(block: TutorialStep.Builder.() -> Unit) {
            steps.add(TutorialStep.Builder().apply(block).build())
        }
        
        fun build() = steps
    }
    
    /**
     * DSL builder for triggers
     */
    class TriggersBuilder {
        private val triggers = mutableListOf<TutorialTrigger>()
        
        fun onFirstLaunch() {
            triggers.add(TutorialTrigger.OnFirstLaunch)
        }
        
        fun <T> onEvent(eventClass: Class<T>) {
            triggers.add(TutorialTrigger.OnEvent(eventClass))
        }
        
        fun manual() {
            triggers.add(TutorialTrigger.Manual)
        }
        
        fun build() = triggers
    }
}

/**
 * Tutorial trigger types
 */
sealed class TutorialTrigger {
    object OnFirstLaunch : TutorialTrigger()
    data class OnEvent(val eventClass: Class<*>) : TutorialTrigger()
    object Manual : TutorialTrigger()
}