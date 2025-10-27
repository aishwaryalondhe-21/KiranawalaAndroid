plugins {
    id("com.android.application")
    id("kotlin-android")
    id("kotlin-kapt")
    id("com.google.devtools.ksp")
    id("com.google.dagger.hilt.android")
    id("kotlinx-serialization")
}

android {
    namespace = "com.kiranawala"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.kiranawalaandroid"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        
        // Supabase Configuration
        buildConfigField("String", "SUPABASE_URL", "\"https://fnblhmddgregqfafqkeh.supabase.co\"")
        buildConfigField("String", "SUPABASE_ANON_KEY", "\"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImZuYmxobWRkZ3JlZ3FmYWZxa2VoIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NjA3MjI5OTQsImV4cCI6MjA3NjI5ODk5NH0.CPmWxu5-VYKDhVlQGC5C8btnKpW_SeWPfp3vT19EbEc\"")
        buildConfigField("String", "MAPS_API_KEY", "\"AIzaSyAU8kwc-Ih9VEOJB3QnEll1YC-I97W3yQw\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.4" // Kotlin 1.9.20
    }
}

kapt {
    correctErrorTypes = true
    useBuildCache = true
    arguments {
        arg("room.schemaLocation", "$projectDir/schemas")
    }
    javacOptions {
        option("--add-exports", "jdk.compiler/com.sun.tools.javac.api=ALL-UNNAMED")
        option("--add-exports", "jdk.compiler/com.sun.tools.javac.code=ALL-UNNAMED")
        option("--add-exports", "jdk.compiler/com.sun.tools.javac.comp=ALL-UNNAMED")
        option("--add-exports", "jdk.compiler/com.sun.tools.javac.file=ALL-UNNAMED")
        option("--add-exports", "jdk.compiler/com.sun.tools.javac.main=ALL-UNNAMED")
        option("--add-exports", "jdk.compiler/com.sun.tools.javac.parser=ALL-UNNAMED")
        option("--add-exports", "jdk.compiler/com.sun.tools.javac.processing=ALL-UNNAMED")
        option("--add-exports", "jdk.compiler/com.sun.tools.javac.tree=ALL-UNNAMED")
        option("--add-exports", "jdk.compiler/com.sun.tools.javac.util=ALL-UNNAMED")
    }
}

dependencies {
    // Core Android
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    
    // Jetpack Compose - Use BOM for version management
    implementation(platform("androidx.compose:compose-bom:2024.02.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material") // For pull-refresh
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.compose.runtime:runtime")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.ui:ui-text-google-fonts:1.6.1") // Google Fonts
    debugImplementation("androidx.compose.ui:ui-tooling")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.6.2")
    
    // Navigation
    implementation("androidx.navigation:navigation-compose:2.7.5")
    
    // ViewModel & Lifecycle
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.2")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")
    
    // Supabase
    implementation("io.github.jan-tennert.supabase:postgrest-kt:2.1.3")
    implementation("io.github.jan-tennert.supabase:gotrue-kt:2.1.3")
    implementation("io.github.jan-tennert.supabase:storage-kt:2.1.3")
    implementation("io.github.jan-tennert.supabase:realtime-kt:2.1.3")
    
    // HTTP Client
    implementation("io.ktor:ktor-client-android:2.3.6")
    implementation("io.ktor:ktor-client-logging:2.3.6")
    
    // Serialization
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.1")
    
    // Dependency Injection - Hilt
    implementation("com.google.dagger:hilt-android:2.50")
    kapt("com.google.dagger:hilt-compiler:2.50")
    implementation("androidx.hilt:hilt-navigation-compose:1.1.0")
    
    // Room Database (Local Storage)
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")
    
    // Encryption
    implementation("androidx.security:security-crypto:1.1.0-alpha06")
    
    // Image Loading
    implementation("io.coil-kt:coil-compose:2.5.0")

    // Lottie Animations
    implementation("com.airbnb.android:lottie-compose:6.3.0")
    
    // Google Places SDK
    implementation("com.google.android.libraries.places:places:3.5.0")
    implementation("com.google.android.gms:play-services-maps:18.2.0")
    implementation("com.google.maps.android:maps-compose:4.3.0")
    
    // Local Notifications (Firebase can be added later)
    // implementation("com.google.firebase:firebase-messaging:23.4.1")
    // implementation("com.google.firebase:firebase-common-ktx:20.4.3")
    
    // Logging
    implementation("com.jakewharton.timber:timber:5.0.1")
    
    // Date/Time
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.5.0")
    
    // DataStore (for PreferencesManager)
    implementation("androidx.datastore:datastore-preferences:1.0.0")
    
    // Testing
    testImplementation("junit:junit:4.13.2")
    testImplementation("io.mockk:mockk:1.13.8")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
    testImplementation("androidx.arch.core:core-testing:2.2.0")
    
    // UI Testing
    androidTestImplementation("androidx.compose.ui:ui-test-junit4:1.6.0")
    androidTestImplementation("androidx.compose.ui:ui-test-manifest:1.6.0")
}