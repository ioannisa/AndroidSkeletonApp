plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp")
    id("com.google.dagger.hilt.android")
    id("de.mannodermaus.android-junit5") version "1.10.0.0"
}

android {
    namespace = "com.example.democompose"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.democompose"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
        multiDexEnabled = true

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }

        signingConfig = signingConfigs.getByName("debug")
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            isMinifyEnabled = false
            isShrinkResources = false
        }
    }

    fun stringField(value: String) = "\"$value\""

    flavorDimensions += "version"
    productFlavors {
        create("development") {
            applicationId = "com.example.democompose.test"
            versionName = defaultConfig.versionName
            buildConfigField("Boolean", "DEV", "true")
            buildConfigField("String", "BASE_URL", stringField("https://newsapi.org/"))
            buildConfigField("String", "API_KEY", stringField("b9b2b87647ec4773b5ec23d49f731811"))
        }
        create("production") {
            applicationId = "com.example.democompose"
            versionName = defaultConfig.versionName
            buildConfigField("Boolean", "DEV", "false")
            buildConfigField("String", "BASE_URL", stringField("https://newsapi.org/"))
            buildConfigField("String", "API_KEY", stringField("b9b2b87647ec4773b5ec23d49f731811"))
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.4"
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

val lifecycleVer = "2.7.0"

dependencies {

    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:$lifecycleVer")
    implementation("androidx.activity:activity-compose:1.8.2")
    implementation(platform("androidx.compose:compose-bom:2023.03.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.benchmark:benchmark-common:1.2.3")
    implementation("com.google.android.material:material:1.11.0")
    implementation(project(":coredata"))

    // CUSTOM IMPLEMENTATIONS START ----->

    hilt()
    retrofit()
    room()
    flipper()

    // https://coil-kt.github.io/coil/compose/
    implementation("io.coil-kt:coil-compose:2.5.0")

    // Timber
    implementation("com.jakewharton.timber:timber:5.0.1")

    // ViewModel Compose - (we can avoid this one as we handle viewModel using hiltViewModel instead)
    //implementation("androidx.lifecycle:lifecycle-viewmodel-compose:$lifecycleVer")

    // required for pull to refresh
    implementation("androidx.compose.material3:material3-adaptive-android:1.0.0-alpha06")

    implementation ("androidx.navigation:navigation-compose:2.7.7")

    // shared element transitions library
    // https://github.com/skydoves/Orbital
    implementation("com.github.skydoves:orbital:0.3.4")
    // <----- CUSTOM IMPLEMENTATIONS END

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2023.03.00"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}

// https://dagger.dev/hilt/gradle-setup.html
fun DependencyHandler.hilt() {
    val hiltVersion = "2.50"

    implementation("com.google.dagger:hilt-android:$hiltVersion")
    ksp("com.google.dagger:hilt-android-compiler:$hiltVersion")
    implementation("androidx.hilt:hilt-navigation-compose:1.1.0")
    implementation("androidx.hilt:hilt-navigation-fragment:1.1.0")


    // For instrumentation tests
    androidTestImplementation("com.google.dagger:hilt-android-testing:$hiltVersion")
    kspAndroidTest("com.google.dagger:hilt-compiler:$hiltVersion")

    // For local unit tests
    testImplementation("com.google.dagger:hilt-android-testing:$hiltVersion")
    kspTest("com.google.dagger:hilt-compiler:$hiltVersion")

    // HILT WORK MANAGER
    //ksp("androidx.hilt:hilt-compiler:1.1.0")
    //implementation("androidx.hilt:hilt-work:1.1.0")
    //implementation("androidx.work:work-runtime-ktx:2.9.0")
}

fun DependencyHandler.retrofit() {
    val retrofitVersion = "2.9.0"
    val okHttpVersion = "4.12.0"
    val moshiVersion = "1.15.1"

    implementation("com.squareup.retrofit2:retrofit:$retrofitVersion")

    implementation("com.squareup.moshi:moshi:$moshiVersion") // https://github.com/square/moshi
    implementation("com.squareup.moshi:moshi-kotlin:$moshiVersion")
    implementation("com.squareup.retrofit2:converter-moshi:$retrofitVersion")
    ksp("com.squareup.moshi:moshi-kotlin-codegen:$moshiVersion")

    implementation("com.squareup.okhttp3:okhttp:$okHttpVersion")
    implementation("com.squareup.okhttp3:logging-interceptor:$okHttpVersion")

    // mock (offline) web server for retrofit tests
    testImplementation("com.squareup.okhttp3:mockwebserver:$okHttpVersion")
}

// https://developer.android.com/jetpack/androidx/releases/room
fun DependencyHandler.room() {
        val roomVersion = "2.6.1"

        implementation("androidx.room:room-runtime:$roomVersion")
        annotationProcessor("androidx.room:room-compiler:$roomVersion")

        // To use Kotlin Symbol Processing (KSP)
        ksp("androidx.room:room-compiler:$roomVersion")

        // optional - Kotlin Extensions and Coroutines support for Room
        implementation("androidx.room:room-ktx:$roomVersion")

        // optional - RxJava2 support for Room
        //implementation("androidx.room:room-rxjava2:$roomVersion")

        // optional - RxJava3 support for Room
        //implementation("androidx.room:room-rxjava3:$roomVersion")

        // optional - Guava support for Room, including Optional and ListenableFuture
        //implementation("androidx.room:room-guava:$roomVersion")

        // optional - Test helpers
        //testImplementation("androidx.room:room-testing:$roomVersion")

        // optional - Paging 3 Integration
        //implementation("androidx.room:room-paging:$roomVersion")
}

fun DependencyHandler.flipper() {
    val flipperVersion = "0.247.0"

    debugImplementation("com.facebook.flipper:flipper:$flipperVersion")
    debugImplementation("com.facebook.flipper:flipper-network-plugin:$flipperVersion")
    debugImplementation("com.facebook.soloader:soloader:0.10.5")
}