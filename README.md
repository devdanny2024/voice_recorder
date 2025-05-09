# MyVoiceRecorder - Development Log and Project Adjustments

This README details the comprehensive process of resolving various build issues and project configuration adjustments for the MyVoiceRecorder application. This log is intended to provide a continuous record of actions taken to address key issues, update dependencies, and align the project with the latest Android SDK and Kotlin requirements.

## ✅ **1. Project Configuration and Initial Setup**

* Updated the `build.gradle` files for both the project and app module to align with the latest Android SDK and Kotlin versions.
* Added the Kotlin plugin with the appropriate version:

  * `org.jetbrains.kotlin.android` version `1.9.10`
  * `org.jetbrains.compose` version `1.5.3`

### **Project-Level build.gradle:**

```gradle
plugins {
    id 'com.android.application' version '8.1.0' apply false
    id 'com.android.library' version '8.1.0' apply false
    id 'org.jetbrains.kotlin.android' version '1.9.10' apply false
    id 'org.jetbrains.compose' version '1.5.3' apply false
}

allprojects {
    repositories {
        google()
        mavenCentral()
        maven { url 'https://maven.pkg.jetbrains.space/public/p/compose/dev' }
    }
}
```

### **App-Level build.gradle:**

```gradle
plugins {
    id 'com.android.application'
    id 'org.jetbrains.kotlin.android'
    id 'org.jetbrains.compose'
}

android {
    compileSdk 35
    defaultConfig {
        applicationId "com.example.noman.myvoicerecorder"
        minSdk 21
        targetSdk 35
        versionCode 1
        versionName "1.0"
    }
    buildFeatures {
        compose true
    }
    composeOptions {
        kotlinCompilerExtensionVersion "1.5.3"
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    compileOptions {
        sourceCompatibility JavaVersion.VERSION_17
        targetCompatibility JavaVersion.VERSION_17
    }
    packagingOptions {
        resources.excludes += "/META-INF/{AL2.0,LGPL2.1}"
    }
}
```

---

## ✅ **2. Resolving Dependency Conflicts**

* Added the Compose repository to resolve missing `compose` dependencies:

```gradle
maven { url 'https://maven.pkg.jetbrains.space/public/p/compose/dev' }
```

* Updated the Compose BOM to the latest version to handle compatibility with SDK 35:

```gradle
implementation platform('androidx.compose:compose-bom:2025.05.00')
implementation "androidx.compose.ui:ui"
implementation "androidx.compose.material3:material3"
implementation "androidx.activity:activity-compose:1.7.2"
```

---

## ✅ **3. Handling Style Conflicts in styles.xml and themes.xml**

* Identified and resolved duplicate style definitions between `styles.xml` and `themes.xml`.
* Consolidated `Theme.MyVoiceRecorder` under one file to prevent resource conflicts.
* Adjusted the `parent` attribute to align with the Material 3 theme structure:

```xml
<style name="Theme.MyVoiceRecorder" parent="Theme.Material3.DayNight">
    <item name="colorPrimary">@color/colorPrimary</item>
    <item name="colorPrimaryVariant">@color/colorPrimaryDark</item>
    <item name="colorAccent">@color/colorAccent</item>
</style>
```

---

## ✅ **4. Addressing Compile SDK and AGP Version Warnings**

* Updated `compileSdk` and `targetSdk` to 35 to resolve dependency warnings.
* Suppressed unnecessary warnings related to compile SDK in `gradle.properties`:

```properties
android.suppressUnsupportedCompileSdk=34,35
```

---

## ✅ **5. Java and Kotlin Compatibility Adjustments**

* Ensured JVM compatibility with Java 17 across all modules:

```gradle
kotlinOptions {
    jvmTarget = "17"
}

compileOptions {
    sourceCompatibility JavaVersion.VERSION_17
    targetCompatibility JavaVersion.VERSION_17
}
```

---

## ✅ **6. Resolved Issues:**

* Fixed the recurring crash caused by `java.lang.IllegalStateException: You need to use a Theme.AppCompat theme (or descendant) with this activity` by ensuring that the application theme `Theme.MyVoiceRecorder` extends from `Theme.AppCompat.Light.DarkActionBar`.
* Corrected duplicate style definitions in `styles.xml` that were causing resource conflicts.
* Updated the `styles.xml` to properly define the theme using the correct AppCompat parent theme.

---

## ✅ **7. Next Steps and Recommendations:**

* Verify all Kotlin files have been properly converted and structured to maintain consistent Compose integration.
* Conduct thorough testing to ensure the application operates as expected with the latest dependencies and SDK configurations.
* Update all documentation to reflect the above changes and maintain alignment with new Android standards.

End of development log.
