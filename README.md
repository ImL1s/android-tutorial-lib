# Android Tutorial Library

English | [中文](README_CN.md)

A powerful and highly customizable Android tutorial library that supports multiple visual styles and flexible triggering methods.

## Features

- 🎨 **Highly Customizable** - Customize colors, styles, shapes, and animations
- 🎯 **Flexible Targeting** - Find target views by View ID or Tag
- 📐 **Multiple Highlight Shapes** - Circle, Rectangle, Rounded Rectangle
- 🌈 **Gradient Support** - Tooltips and buttons support gradient effects
- 💾 **State Management** - Automatically tracks tutorial display status per user
- 🎭 **Multiple Triggers** - Manual, first launch, event-based triggers
- 📱 **Compatibility** - Supports Android 5.0 (API 21) and above
- 🚀 **Lightweight** - Small core library size with no redundant dependencies

## How It Works

This library implements tutorial overlays through the following steps:

1. **View Targeting**: Finds target views in the view hierarchy by ID or Tag
2. **Coordinate Calculation**: Gets absolute screen coordinates of target views
3. **Screenshot Processing**: Captures current screen and adds semi-transparent overlay
4. **Highlight Drawing**: Clears target areas on overlay and draws highlight borders
5. **Tooltip Display**: Draws tooltip boxes at appropriate positions with connector lines
6. **Interaction Handling**: Shows processed image in full-screen DialogFragment with click-to-dismiss

## Installation

### Gradle

```gradle
dependencies {
    implementation 'com.github.yourusername:android-tutorial-lib:1.0.0'
}
```

### Maven

```xml
<dependency>
    <groupId>com.github.yourusername</groupId>
    <artifactId>android-tutorial-lib</artifactId>
    <version>1.0.0</version>
</dependency>
```

## Usage

### Basic Usage

```kotlin
// Create and show tutorial
val tutorialManager = TutorialManager.builder(this)
    .steps {
        step {
            target(R.id.search_button)      // Target by ID
            text("Click here to search")
            maxWidth(100f)                  // Max tooltip width in dp
        }
        step {
            targetTag("settings_tag")       // Target by Tag
            text("Settings options")
            shape(HighlightShape.CIRCLE)    // Circle highlight
        }
    }
    .build()

// Show tutorial
tutorialManager.show()
```

### Blue Gradient Style (Enterprise App Style)

```kotlin
TutorialManager.builder(this)
    .config {
        tutorialId("home_tutorial")
        showOnlyOnce(true)                  // Show only once
        overlayColor(0xD0000000.toInt())    // Overlay color with alpha
        style(TutorialStyle(
            highlightBorderColor = Color.parseColor("#2B44B1"),
            highlightBorderWidthDp = 2f,
            highlightCornerRadiusDp = 4f,
            tooltipStyle = TooltipStyle.GRADIENT,
            tooltipColors = listOf(
                Color.parseColor("#2B44B1"),
                Color.parseColor("#015BE2")
            ),
            tooltipTextColor = Color.WHITE,
            tooltipCornerRadiusDp = 100f,
            connectorLineStyle = LineStyle.DASHED,
            buttonText = "Got it",
            buttonStyle = ButtonStyle.GRADIENT_BLUE
        ))
    }
    .steps {
        step {
            target(R.id.search_button)
            text("Click here to search")
            maxWidth(100f)
        }
    }
    .build()
    .show()
```

### Yellow Solid Style (Playful App Style)

```kotlin
TutorialManager.builder(this)
    .config {
        style(TutorialStyle(
            highlightBorderColor = Color.parseColor("#F7B500"),
            tooltipStyle = TooltipStyle.SOLID,
            tooltipColors = listOf(Color.parseColor("#F7B500")),
            tooltipTextColor = Color.BLACK,
            connectorLineStyle = LineStyle.SOLID,
            buttonText = "OK",
            buttonStyle = ButtonStyle.SOLID_YELLOW
        ))
    }
    .steps {
        step {
            target(R.id.hot_button)
            text("View trending content")
            shape(HighlightShape.ROUNDED_RECT)
        }
    }
    .build()
    .show()
```

## Advanced Features

### Custom Storage Implementation

By default, SharedPreferences is used to store tutorial display status. You can implement custom storage:

```kotlin
class CustomStorage : TutorialStorage {
    override fun isTutorialShown(tutorialId: String, userId: String?): Boolean {
        // Implement your storage logic
        return database.isTutorialShown(tutorialId, userId)
    }
    
    override fun setTutorialShown(tutorialId: String, shown: Boolean, userId: String?) {
        // Implement your storage logic
        database.setTutorialShown(tutorialId, shown, userId)
    }
}

// Use custom storage
TutorialManager.builder(this)
    .storage(CustomStorage())
    .build()
```

### Per-User Tutorial Management

```kotlin
// Show tutorial for specific user
tutorialManager.show(userId = "user123")

// Force show (ignores if already shown)
tutorialManager.forceShow(userId = "user123")
```

### Multi-Step Tutorial

```kotlin
TutorialManager.builder(this)
    .steps {
        step {
            target(R.id.step1)
            text("Step 1: Select file")
        }
        step {
            target(R.id.step2)
            text("Step 2: Edit content")
        }
        step {
            target(R.id.step3)
            text("Step 3: Save file")
        }
    }
    .build()
    .show()
```

### Custom Animation Configuration

```kotlin
TutorialManager.builder(this)
    .config {
        animation(AnimationConfig(
            fadeInDuration = 500L,
            fadeOutDuration = 300L,
            highlightPulseDuration = 1000L,
            enablePulseAnimation = true
        ))
    }
    .build()
```

## API Reference

### TutorialConfig Options

| Property | Type | Default | Description |
|----------|------|---------|-------------|
| tutorialId | String | "default_tutorial" | Unique tutorial identifier |
| showOnlyOnce | Boolean | true | Whether to show only once |
| overlayColor | Int | 0xD0000000 | Overlay color with alpha |
| style | TutorialStyle | - | Visual style configuration |
| animation | AnimationConfig | - | Animation configuration |

### TutorialStyle Properties

| Property | Type | Description |
|----------|------|-------------|
| highlightBorderColor | Int | Highlight border color |
| highlightBorderWidthDp | Float | Highlight border width in dp |
| highlightCornerRadiusDp | Float | Highlight corner radius in dp |
| highlightPaddingDp | Float | Highlight area padding in dp |
| tooltipStyle | TooltipStyle | Tooltip style (SOLID/GRADIENT) |
| tooltipColors | List<Int> | Tooltip color list |
| tooltipTextColor | Int | Tooltip text color |
| tooltipTextSizeSp | Float | Tooltip text size in sp |
| buttonStyle | ButtonStyle | Button style |

### HighlightShape Options

- `CIRCLE` - Circle shape
- `RECT` - Rectangle shape  
- `ROUNDED_RECT` - Rounded rectangle shape

## Best Practices

1. **View Targeting**
   - Prefer View ID targeting (more reliable)
   - Use Tags for dynamically created views
   - Ensure target views are fully loaded

2. **Performance Optimization**
   - Tutorial images are automatically recycled
   - Call `tutorialManager.destroy()` in Activity's `onDestroy()`

3. **User Experience**
   - Don't interrupt user actions with tutorials
   - Provide skip option (click anywhere to dismiss)
   - Keep text descriptions concise and clear

## FAQ

**Q: Why do I sometimes need to click multiple times to show the tutorial?**
A: This usually happens when views aren't fully loaded or the activity doesn't have window focus. Solutions:

1. **Wait for Window Focus** (Recommended):
```kotlin
button.setOnClickListener {
    window.decorView.postDelayed({
        if (hasWindowFocus()) {
            tutorialManager.forceShow()
        }
    }, 100)
}
```

2. **Use onWindowFocusChanged**:
```kotlin
override fun onWindowFocusChanged(hasFocus: Boolean) {
    super.onWindowFocusChanged(hasFocus)
    if (hasFocus && shouldShowTutorial) {
        tutorialManager.show()
    }
}
```

3. **Enable Debug Logging** to diagnose issues:
```kotlin
// In your Application class
Timber.plant(Timber.DebugTree())
```

**Q: How to support dark mode?**
A: Dynamically adjust colors based on system theme:
```kotlin
val isDarkMode = resources.configuration.uiMode and 
    Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
    
val style = if (isDarkMode) darkStyle else lightStyle
```

## Sample Project

Check the `sample` module for complete examples including:
- Blue gradient style example
- Yellow solid style example
- Multi-step tutorial example
- Different highlight shapes example

## Contributing

Issues and Pull Requests are welcome!

## License

```
Copyright 2024 Android Tutorial Library Contributors

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```