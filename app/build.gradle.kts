plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ksp)
    id("com.google.gms.google-services")
}
android {
    namespace = "com.midterm22nh12.appbangiayonline"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.midterm22nh12.appbangiayonline"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

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
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures{
        viewBinding=true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.play.services.analytics.impl)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    implementation(libs.blurview)
    implementation(libs.androidx.constraintlayout.v214)
    implementation(libs.androidx.constraintlayout.compose)
    //cài đặt layout load
    implementation(libs.androidx.swiperefreshlayout)

    // add realtime database
    implementation(libs.firebase.database)

    //add cloud firestore
    implementation(libs.firebase.firestore)

    // FirebaseUI for Firebase Realtime Database
    implementation(libs.firebase.ui.database)

    // FirebaseUI for Cloud Firestore
    implementation(libs.firebase.ui.firestore)

    // FirebaseUI for Firebase Auth
    implementation(libs.firebase.ui.auth)

    // FirebaseUI for Cloud Storage
    implementation(libs.firebase.ui.storage)

    implementation(libs.firebase.bom) // Cập nhật BOM để đồng bộ phiên bản
    implementation(libs.firebase.storage)
    implementation(libs.firebase.auth)
    implementation(libs.google.firebase.firestore)

    // Thêm Firebase App Check nếu thiếu
    implementation(libs.firebase.appcheck.playintegrity)

    //xây dựng giao diện theo phong cách Material Design
    implementation(libs.material)

    implementation(libs.androidx.viewpager2)

    implementation(libs.firebase.bom)

    implementation(libs.firebase.analytics)

    val room_version = "2.6.1"

    implementation(libs.androidx.room.runtime)

    // If this project uses any Kotlin source, use Kotlin Symbol Processing (KSP)
    // See Add the KSP plugin to your project
    ksp(libs.androidx.room.compiler)

    // If this project only uses Java source, use the Java annotationProcessor
    // No additional plugins are necessary
    annotationProcessor(libs.androidx.room.compiler)

    // optional - Kotlin Extensions and Coroutines support for Room
    implementation(libs.androidx.room.ktx)

    // optional - RxJava2 support for Room
    implementation(libs.androidx.room.rxjava2)

    // optional - RxJava3 support for Room
    implementation(libs.androidx.room.rxjava3)

    // optional - Guava support for Room, including Optional and ListenableFuture
    implementation(libs.androidx.room.guava)

    // optional - Test helpers
    testImplementation(libs.androidx.room.testing)

    // optional - Paging 3 Integration
    implementation(libs.androidx.room.paging)

    implementation(libs.androidx.work.runtime.ktx)

    implementation(libs.google.services)

    implementation(libs.google.firebase.messaging.ktx)

    implementation("com.google.android.gms:play-services-location:20.0.0")
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")
}