package com.tutorial.lib.utils

import android.view.View
import android.view.ViewGroup
import androidx.annotation.IdRes

/**
 * Utility class for finding views
 */
object ViewFinder {
    
    /**
     * Find all views with a specific tag in the view hierarchy
     */
    fun findViewsWithTag(root: View, tag: String): List<View> {
        val views = mutableListOf<View>()
        findViewsWithTagRecursive(root, tag, views)
        return views
    }
    
    private fun findViewsWithTagRecursive(view: View, tag: String, results: MutableList<View>) {
        if (view.tag == tag) {
            results.add(view)
        }
        
        if (view is ViewGroup) {
            for (i in 0 until view.childCount) {
                findViewsWithTagRecursive(view.getChildAt(i), tag, results)
            }
        }
    }
    
    /**
     * Find a view by ID in the view hierarchy
     */
    fun findViewById(root: View, @IdRes viewId: Int): View? {
        if (root.id == viewId) {
            return root
        }
        
        if (root is ViewGroup) {
            for (i in 0 until root.childCount) {
                val found = findViewById(root.getChildAt(i), viewId)
                if (found != null) {
                    return found
                }
            }
        }
        
        return null
    }
    
    /**
     * Find multiple views by their IDs
     */
    fun findViewsByIds(root: View, @IdRes vararg viewIds: Int): List<View> {
        val views = mutableListOf<View>()
        viewIds.forEach { id ->
            findViewById(root, id)?.let { views.add(it) }
        }
        return views
    }
}