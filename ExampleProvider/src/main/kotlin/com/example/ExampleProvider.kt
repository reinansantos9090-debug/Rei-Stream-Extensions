package com.lagradost

import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.utils.*
import org.jsoup.nodes.Element

class SmartAnimes : MainAPI() {
    override var mainUrl = "https://smartanimes.net"
    override var name = "SmartAnimes"
    override var lang = "pt-br"
    override val supportedTypes = setOf(TvType.Anime)

    override val hasMainPage = true

    override async fun getMainPage(page: Int, request: MainPageRequest): HomePageResponse {
        val document = app.get(mainUrl).document
        val home = mutableListOf<HomePageList>()

        val items = document.select("article.item, div.anime-item").mapNotNull {
            it.toSearchResult()
        }

        if (items.isNotEmpty()) {
            home.add(HomePageList("Animes Recentes", items))
        }

        return newHomePageResponse(home, false)
    }

    private fun Element.toSearchResult(): SearchResponse? {
        val title = this.selectFirst("h3, .title, .entry-title")?.text() ?: return null
        val href = this.selectFirst("a")?.attr("href") ?: return null
        val posterUrl = this.selectFirst("img")?.attr("src") ?: this.selectFirst("img")?.attr("data-src")

        return newAnimeSearchResponse(title, href, TvType.Anime) {
            this.posterUrl = posterUrl
        }
    }

    override async fun search(query: String): List<SearchResponse> {
        val url = "$mainUrl/?s=$query"
        val document = app.get(url).document

        return document.select("article.item, div.anime-item").mapNotNull {
            it.toSearchResult()
        }
    }
}
