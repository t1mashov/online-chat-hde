import com.vanniktech.maven.publish.SonatypeHost

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("com.vanniktech.maven.publish") version "0.29.0"
}

android {
    namespace = "com.example.online_chat_hde"
    compileSdk = 35

    defaultConfig {
        minSdk = 26

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
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
    }
}

dependencies {
    implementation("io.socket:socket.io-client:2.0.1")
    implementation("androidx.constraintlayout:constraintlayout-compose:1.1.1")
    implementation("androidx.lifecycle:lifecycle-process:2.9.3")
    implementation("androidx.navigation:navigation-compose:2.9.3")
    implementation("io.coil-kt.coil3:coil-compose:3.0.0")
    implementation("io.coil-kt.coil3:coil-network-okhttp:3.0.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")

    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")
    implementation("androidx.activity:activity-compose:1.10.0")
    implementation(platform("androidx.compose:compose-bom:2024.04.01"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.10.0")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}


mavenPublishing {
    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL)
    signAllPublications()

    coordinates(
        groupId = "io.github.t1mashov",
        artifactId = "online-chat-hde",
        version = "1.0.1"
    )

    pom {
        name.set("Online chat SDK")
        description.set("Android online chat SDK for HDE systems")
        url.set("https://github.com/t1mashov/online-chat-hde")
        licenses {
            license {
                name.set("Apache-2.0")
                url.set("https://www.apache.org/licenses/LICENSE-2.0")
            }
        }
        developers {
            developer {
                id.set("t1mashov")
                name.set("Petr Timashov")
                email.set("edelweiss2229@gmail.com")
            }
        }
        scm {
            url.set("https://github.com/t1mashov/online-chat-hde")
            connection.set("scm:git:git://github.com/t1mashov/online-chat-hde.git")
            developerConnection.set("scm:git:ssh://git@github.com/t1mashov/online-chat-hde.git")
        }
    }

}