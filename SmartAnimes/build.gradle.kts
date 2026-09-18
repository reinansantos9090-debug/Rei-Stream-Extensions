dependencies {
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.recyclerview:recyclerview:1.3.2")
}

version = 4

cloudstream {
    description = "Catálogo e player local para a biblioteca ReiFlix"
    authors = listOf("Rei-Stream")
    status = 1
    tvTypes = listOf("Anime")
    requiresResources = false
    language = "pt-br"
}

android {
    buildFeatures {
        buildConfig = true
        viewBinding = true
    }
}
