# 🪟 LiquidGlass

A lightweight Android library that brings the **Liquid Glass** refraction effect to your UI.  
Inspired by the blur pipeline from Telegram — built as a standalone, zero-dependency library.

![API](https://img.shields.io/badge/minSdk-29-green)
![API](https://img.shields.io/badge/fullEffect-API%2033%2B-blue)
[![JitPack](https://jitpack.io/v/VladimirIAlexandrov/LiquidGlass.svg)](https://jitpack.io/#VladimirIAlexandrov/LiquidGlass)

---

## ✨ Preview

https://github.com/user-attachments/assets/6336c68c-6cea-496a-8e34-009f373be6e2

---

## 🚀 Setup

### 1. Add JitPack to `settings.gradle.kts`

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

**Kotlin DSL** (`build.gradle.kts`):
```kotlin
dependencies {
    implementation("com.github.VladimirIAlexandrov:LiquidGlass:1.0.0")
}
```

**Groovy** (`build.gradle`):
```groovy
dependencies {
    implementation 'com.github.VladimirIAlexandrov:LiquidGlass:1.0.0'
}
```

---

## 📦 Components

### `GlassHostLayout`
The container that powers the effect. Wrap your content inside it.

### `GlassPanel`
A panel with the liquid glass effect. Can contain any child views.

### `GlassButton`
A clickable button with the liquid glass effect and scale animation on press.

### `GlassNavigationBar`
A bottom navigation bar with animated tab selection and liquid glass background.

---

## 🧩 Usage

```xml
<com.liquidglass.ui.GlassHostLayout
    android:layout_width="match_parent"
    android:layout_height="match_parent">

    <!-- Your content -->
    <androidx.recyclerview.widget.RecyclerView
        android:layout_width="match_parent"
        android:layout_height="match_parent" />

    <!-- Glass panel -->
    <com.liquidglass.ui.GlassPanel
        android:layout_width="match_parent"
        android:layout_height="80dp"
        android:layout_gravity="bottom"
        android:layout_margin="16dp"
        app:glass_radius="24dp"
        app:glass_thickness="20dp"
        app:glass_blur="8dp"
        app:glass_intensity="0.75"
        app:glass_foreground_color="#18FFFFFF" />

    <!-- Glass button -->
    <com.liquidglass.ui.GlassButton
        android:layout_width="200dp"
        android:layout_height="56dp"
        android:layout_gravity="center"
        app:glass_radius="28dp"
        app:glass_thickness="20dp"
        app:glass_text="Click me"
        app:glass_text_color="#FFFFFF"
        app:glass_text_size="16sp" />

    <!-- Navigation bar -->
    <com.liquidglass.ui.GlassNavigationBar
        android:id="@+id/nav_bar"
        android:layout_width="match_parent"
        android:layout_height="64dp"
        android:layout_gravity="bottom"
        android:layout_margin="16dp"
        app:glass_radius="32dp"
        app:glass_active_color="#000000"
        app:glass_inactive_color="#78000000"
        app:glass_indicator_color="#50000000" />

</com.liquidglass.ui.GlassHostLayout>
```

### Add tabs to navigation bar in code

```kotlin
val nav = findViewById<GlassNavigationBar>(R.id.nav_bar)
nav.addTab(R.drawable.ic_home,    "Home")
nav.addTab(R.drawable.ic_search,  "Search")
nav.addTab(R.drawable.ic_profile, "Profile")

nav.onTabSelected = { index ->
    // handle tab switch
}
```

### Invalidate on scroll

```kotlin
recycler.addOnScrollListener(object : RecyclerView.OnScrollListener() {
    override fun onScrolled(rv: RecyclerView, dx: Int, dy: Int) {
        hostLayout.invalidate()
    }
})
```

---

## 🎨 XML Attributes

| Attribute | Type | Default | Description |
|---|---|---|---|
| `app:glass_radius` | dimension | `24dp` | Corner radius |
| `app:glass_blur` | dimension | `8dp` | Blur strength |
| `app:glass_thickness` | dimension | `20dp` | Lens thickness |
| `app:glass_intensity` | float | `0.75` | Refraction intensity |
| `app:glass_foreground_color` | color | `#18FFFFFF` | Foreground tint |
| `app:glass_text` | string | — | Button label *(GlassButton)* |
| `app:glass_text_color` | color | `#FFFFFF` | Button text color *(GlassButton)* |
| `app:glass_text_size` | dimension | `16sp` | Button text size *(GlassButton)* |
| `app:glass_scale_on_press` | float | `0.95` | Scale on press *(GlassButton)* |
| `app:glass_active_color` | color | `#000000` | Active tab color *(GlassNavigationBar)* |
| `app:glass_inactive_color` | color | `#78000000` | Inactive tab color *(GlassNavigationBar)* |
| `app:glass_indicator_color` | color | `#50000000` | Indicator background *(GlassNavigationBar)* |

---

## ⚙️ Requirements

- `minSdk` 29
- Full liquid glass effect (lens distortion) requires **API 33+**
- API 31–32: blur only, no lens shader
- Hardware acceleration must be enabled (default on all modern devices)

---

## 📄 License

```
MIT License

Copyright (c) 2025 Vladimir Alexandrov

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.
```
