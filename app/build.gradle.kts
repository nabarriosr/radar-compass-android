import java.util.Properties

plugins { id("com.android.application") }

val admobAppId = (findProperty("ADMOB_APP_ID") as String?)
    ?: "ca-app-pub-3940256099942544~3347511713"
val admobBannerUnitId = (findProperty("ADMOB_BANNER_UNIT_ID") as String?)
    ?: "ca-app-pub-3940256099942544/9214589741"

android {
    namespace = "com.orbitalcompass.app"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.orbitalcompass.app"
        minSdk = 23
        targetSdk = 35
        versionCode = 3
        versionName = "2.1"
        buildConfigField("String", "ADMOB_BANNER_UNIT_ID", "\"$admobBannerUnitId\"")
        manifestPlaceholders["admobAppId"] = admobAppId
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        buildConfig = true
    }

    signingConfigs {
        val keystorePropertiesFile = rootProject.file("keystore.properties")
        if (keystorePropertiesFile.exists()) {
            val props = Properties()
            keystorePropertiesFile.inputStream().use { props.load(it) }
            create("release") {
                keyAlias = props.getProperty("keyAlias")
                keyPassword = props.getProperty("keyPassword")
                storeFile = rootProject.file(props.getProperty("storeFile"))
                storePassword = props.getProperty("storePassword")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            if (signingConfigs.findByName("release") != null) {
                signingConfig = signingConfigs.getByName("release")
            }
            if (admobAppId.contains("3940256099942544")) {
                logger.warn("Release está usando IDs de prueba de AdMob. Sustituye ADMOB_APP_ID y ADMOB_BANNER_UNIT_ID antes de subir a Play.")
            }
        }
    }
}

dependencies {
    implementation("com.google.android.gms:play-services-ads:25.4.0")
    implementation("com.google.android.ump:user-messaging-platform:4.0.0")
}
