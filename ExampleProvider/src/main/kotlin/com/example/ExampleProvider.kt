package com.example

import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.utils.*
import org.jsoup.nodes.Element

class SmartAnimes : MainAPI() {
    override var mainUrl = "https://smartanimes.net"
    override var name = "SmartAnimes"
    override var lang = "pt-br"
    override val supportedTypes = setOf(TvType.Anime)

    override val hasMainPage = true

    override suspend fun getMainPage(page: Int, request: MainPageRequest): HomePageResponse {
        val document = app.get(mainUrl).document
        val home = mutableListOf<HomePageList>()

        val items = document.select("article.item, div.anime-item, div.poster").mapNotNull {
            it.toSearchResult()
        }

        if (items.isNotEmpty()) {
            home.add(HomePageList("Animes Recentes", items))
        }

        return newHomePageResponse(home, false)
    }

    private fun Element.toSearchResult(): SearchResponse? {
        val title = this.selectFirst("h3, .title, .entry-title, a")?.text() ?: return null
        val href = this.selectFirst("a")?.attr("href") ?: return null
        val posterUrl = this.selectFirst("img")?.attr("src") ?: this.selectFirst("img")?.attr("data-src")

        return newAnimeSearchResponse(title, href, TvType.Anime) {
            this.posterUrl = posterUrl
        }
    }

    override suspend fun search(query: String): List<SearchResponse> {
        val url = "$mainUrl/?s=$query"
        val document = app.get(url).document

        return document.select("article.item, div.anime-item, div.poster").mapNotNull {
            it.toSearchResult()
        }
    }

    override suspend fun load(url: String): LoadResponse? {
        val document = app.get(url).document
        val title = document.selectFirst("h1.entry-title, h1")?.text() ?: return null
        val poster = document.selectFirst("div.poster img, img.wp-post-image")?.attr("src")
        val description = document.selectFirst("div.entry-content, div.synopsis")?.text()

        val episodes = document.select("ul.episodios li, div.episodio, a.episode").mapNotNull { ep ->
            val epHref = ep.selectFirst("a")?.attr("href") ?: return@mapNotNull null
            val epName = ep.selectFirst(".title, .num, a")?.text() ?: "Episódio"
            newEpisode(epHref) {
                this.name = epName
            }
        }

        return newAnimeLoadResponse(title, url, TvType.Anime) {
            this.posterUrl = poster
            this.plot = description
            addEpisodes(DubStatus.Subbed, episodes)
        }
    }

    override suspend fun loadLinks(
        data: String,
        isCasting: Boolean,
        subtitleCallback: (SubtitleFile) -> Unit,
        callback: (ExtractorLink) -> Unit
    ): Boolean {
        val document = app.get(data).document
        val iframeUrl = document.selectFirst("iframe")?.attr("src")

        if (!iframeUrl.isNullOrEmpty()) {
            loadExtractor(iframeUrl, data, subtitleCallback, callback)
        }

        return true
    }
}

