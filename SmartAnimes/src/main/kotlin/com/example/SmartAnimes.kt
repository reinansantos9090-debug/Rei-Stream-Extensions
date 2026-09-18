package com.example

import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.utils.*
import java.io.File

/**
 * Local-first catalogue provider for a ReiFlix media library.
 *
 * Expected layout:
 * /storage/emulated/0/ReiFlix/<genre>/<anime>/<episode>.mp4
 *
 * A cover named poster.jpg/png/webp and a description.txt can be placed inside
 * an anime directory. They are optional, so indexing never needs the network.
 */
class SmartAnimes : MainAPI() {
    override var mainUrl = "file:///storage/emulated/0/ReiFlix"
    override var name = "ReiFlix Local"
    override val hasMainPage = true
    override var lang = "pt-br"
    override val supportedTypes = setOf(TvType.Anime)

    private val videoExtensions = setOf("mp4", "mkv", "webm", "avi", "mov", "m4v")
    private val imageNames = listOf("poster.jpg", "poster.jpeg", "poster.png", "poster.webp", "cover.jpg")

    private fun libraryRoot(): File = listOf(
        File("/storage/emulated/0/ReiFlix"),
        File("/sdcard/ReiFlix"),
        File("/storage/emulated/0/Download/ReiFlix")
    ).firstOrNull { it.isDirectory } ?: File("/storage/emulated/0/ReiFlix")

    private fun File.isVideo() = isFile && extension.lowercase() in videoExtensions

    private fun File.posterUrl(): String? = imageNames
        .asSequence()
        .map(::File)
        .firstOrNull(File::isFile)
        ?.toURI()
        ?.toString()

    private fun File.description(): String? = File(this, "description.txt")
        .takeIf(File::isFile)
        ?.readText()
        ?.trim()
        ?.takeIf(String::isNotBlank)

    private fun animeResponse(directory: File): AnimeSearchResponse =
        newAnimeSearchResponse(directory.name, directory.toURI().toString(), TvType.Anime) {
            posterUrl = directory.posterUrl()
        }

    // Genres are the first-level folders. This makes the catalogue navigable
    // without a remote provider or a plugin-specific search endpoint.
    override suspend fun getMainPage(page: Int, request: MainPageRequest): HomePageResponse {
        if (page > 1) return newHomePageResponse(emptyList())

        val root = libraryRoot()
        val sections = root.listFiles()
            ?.filter(File::isDirectory)
            ?.sortedBy { it.name.lowercase() }
            ?.mapNotNull { genre ->
                val anime = genre.listFiles()
                    ?.filter(File::isDirectory)
                    ?.sortedBy { it.name.lowercase() }
                    ?.map(::animeResponse)
                    .orEmpty()

                if (anime.isNotEmpty()) HomePageList(genre.name, anime) else null
            }
            .orEmpty()

        return newHomePageResponse(sections)
    }

    override suspend fun search(query: String): List<SearchResponse> {
        val normalizedQuery = query.trim()
        if (normalizedQuery.isEmpty()) return emptyList()

        return libraryRoot().walkTopDown()
            .maxDepth(2)
            .filter(File::isDirectory)
            .filter { it.parentFile != libraryRoot() }
            .filter { it.name.contains(normalizedQuery, ignoreCase = true) }
            .sortedBy { it.name.lowercase() }
            .map(::animeResponse)
            .toList()
    }

    override suspend fun load(url: String): LoadResponse {
        val animeDirectory = File(java.net.URI(url))
        val episodes = animeDirectory.listFiles()
            ?.filter(File::isVideo)
            ?.sortedBy { it.nameWithoutExtension.lowercase() }
            ?.map { episode ->
                newEpisode(episode.toURI().toString()) {
                    name = episode.nameWithoutExtension
                }
            }
            .orEmpty()

        return newAnimeLoadResponse(animeDirectory.name, url, TvType.Anime) {
            posterUrl = animeDirectory.posterUrl()
            plot = animeDirectory.description()
            tags = animeDirectory.parentFile?.name
                ?.split(',', '/', '|')
                ?.map(String::trim)
                ?.filter(String::isNotBlank)
                .orEmpty()
            addEpisodes(DubStatus.Subbed, episodes)
        }
    }

    override suspend fun loadLinks(
        data: String,
        isCasting: Boolean,
        subtitleCallback: (SubtitleFile) -> Unit,
        callback: (ExtractorLink) -> Unit
    ): Boolean {
        val video = File(java.net.URI(data))
        if (!video.isVideo()) return false

        callback(
            ExtractorLink(
                source = name,
                name = "Arquivo local",
                url = data,
                referer = "",
                quality = Qualities.Unknown.value,
                isM3u8 = false
            )
        )
        return true
    }
}
