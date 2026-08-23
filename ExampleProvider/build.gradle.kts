dependencies {
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.recyclerview:recyclerview:1.3.2")
}

version = 2

cloudstream {
    description = "Provedor de animes do SmartAnimes"
    authors = listOf("Rei-Stream")
    status = 1
    tvTypes = listOf("Anime")
    requiresResources = false
    language = "pt-br"
    iconUrl = "https://www.google.com/s2/favicons?domain=smartanimes.net&sz=128"
}

android {
    buildFeatures {
        buildConfig = true
        viewBinding = true
    }
}
