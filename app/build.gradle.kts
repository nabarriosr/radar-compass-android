plugins { id("com.android.application") }

android {
    namespace = "com.agroaltrek.saiyancompass"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.agroaltrek.saiyancompass"
        minSdk = 23
        targetSdk = 35
        versionCode = 2
        versionName = "2.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
}
