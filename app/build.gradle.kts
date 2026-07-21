plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "com.rituraj.sevamitra"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.rituraj.sevamitra"
        minSdk = 26
        targetSdk = 36
        versionCode = 2
        versionName = "2.1.1"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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

    packaging {
        resources {
            excludes += "META-INF/DEPENDENCIES"
            excludes += "META-INF/INDEX.LIST"
        }
    }
}

dependencies {
    implementation(libs.activity.ktx)
    implementation(libs.appcompat)
    implementation(libs.cardview)
    implementation(libs.constraintlayout)
    implementation(libs.material)
    implementation(libs.recyclerview)
    testImplementation(libs.junit)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(libs.ext.junit)

    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")

    // Firebase BOM (versions automatic handle)
    implementation(platform("com.google.firebase:firebase-bom:33.5.1"))
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-firestore")
    implementation("com.google.firebase:firebase-database")
    implementation("com.google.android.gms:play-services-auth:21.4.0")
    implementation("com.google.auth:google-auth-library-oauth2-http:1.23.0")

    // Translation
    implementation("com.google.mlkit:translate:17.0.3")

    // Loading animation
    implementation("com.github.d-max:spots-dialog:1.1@aar")

    // Image handling
    implementation("com.github.dhaval2404:imagepicker:2.1")
    implementation("de.hdodenhof:circleimageview:3.1.0")
    implementation("com.squareup.picasso:picasso:2.71828")
    implementation("com.github.bumptech.glide:glide:4.16.0")
    annotationProcessor("com.github.bumptech.glide:compiler:4.16.0")

    // MPAndroidChart for beautiful charts and Pie Chart
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")

    // Only Selecting Month and Year
    implementation("com.github.dewinjm:monthyear-picker:1.0.2")
}