# LiquidGlass

Android library that brings the **Liquid Glass** refraction effect to any view.  
Requires API 31+. Full shader effect on API 33+.

---

## Setup

### 1. Add JitPack to your root `settings.gradle.kts`

```kotlin
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}
```

### 2. Add dependency

```kotlin
dependencies {
    implementation("com.github.YOUR_GITHUB_USERNAME:LiquidGlass:1.0.0")
}
```

---

## Usage

### XML

Wrap your content in `GlassHostLayout` and add `GlassPanel` on top:

```xml
<com.liquidglass.ui.GlassHostLayout
    android:layout_width="match_parent"
    android:layout_height="match_parent">

    <!-- Your content (RecyclerView, images, etc.) -->
    <androidx.recyclerview.widget.RecyclerView
        android:id="@+id/recycler"
        android:layout_width="match_parent"
        android:layout_height="match_parent" />

    <!-- Glass panel floating on top -->
    <com.liquidglass.ui.GlassPanel
        android:layout_width="match_parent"
        android:layout_height="80dp"
        android:layout_gravity="bottom"
        android:layout_margin="16dp"
        app:glass_radius="24dp"
        app:glass_thickness="20dp"
        app:glass_intensity="0.75"
        app:glass_blur="8dp"
        app:glass_foreground_color="#18FFFFFF" />

</com.liquidglass.ui.GlassHostLayout>
```

### Invalidate on scroll

```kotlin
recycler.addOnScrollListener(object : RecyclerView.OnScrollListener() {
    override fun onScrolled(rv: RecyclerView, dx: Int, dy: Int) {
        host.invalidate()
    }
})
```

### Programmatic control

```kotlin
val glass = findViewById<GlassPanel>(R.id.glass_panel)
glass.setGlassRadius(24 * dp)
glass.setThickness(20 * dp)
glass.setIntensity(0.75f)
glass.setBlurRadius(8 * dp)
glass.setForegroundColor(0x18FFFFFF)
```

---

## XML Attributes

| Attribute                  | Type      | Default     | Description              |
|---------------------------|-----------|-------------|--------------------------|
| `app:glass_radius`        | dimension | `24dp`      | Corner radius            |
| `app:glass_blur`          | dimension | `8dp`       | Blur strength            |
| `app:glass_thickness`     | dimension | `20dp`      | Lens thickness           |
| `app:glass_intensity`     | float     | `0.75`      | Refraction intensity     |
| `app:glass_foreground_color` | color  | `#18FFFFFF` | Foreground tint          |

---

## Requirements

- minSdk 29
- Full effect (lens distortion) requires API 33+
- API 31-32: blur only, no lens shader
- Hardware acceleration must be enabled (default)
