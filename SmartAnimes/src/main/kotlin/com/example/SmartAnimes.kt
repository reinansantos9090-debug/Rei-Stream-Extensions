package com.example

import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.utils.*
import org.jsoup.nodes.Document

class SmartAnimes : MainAPI() {
    override var mainUrl = "https://smartanimes.net"
    override var name = "SmartAnimes"
    override val hasMainPage = true
    override var lang = "pt-br"
    override val supportedTypes = setOf(TvType.Anime)

    // 1. Página Inicial / Catálogo Principal
    override suspend fun getMainPage(page: Int, request: MainPageRequest): HomePageResponse {
        val document = app.get(mainUrl).document
        val homeItems = ArrayList<HomePageList>()

        // Raspagem segura dos itens da Home
        val items = document.select("div.item, article.anime-item, div.poster").mapNotNull { element ->
            val title = element.selectFirst("a")?.attr("title") 
                ?: element.selectFirst("h3, h2, .title")?.text() 
                ?: return@mapNotNull null
            
            val href = element.selectFirst("a")?.attr("href") ?: return@mapNotNull null
            val poster = element.selectFirst("img")?.attr("data-src") 
                ?: element.selectFirst("img")?.attr("src")

            newAnimeSearchResponse(title, fixUrl(href), TvType.Anime) {
                this.posterUrl = poster?.let { fixUrl(it) }
            }
        }

        if (items.isNotEmpty()) {
            homeItems.add(HomePageList("Adicionados Recentemente", items))
        }

        return newHomePageResponse(homeItems)
    }

    // 2. Sistema de Busca
    override suspend fun search(query: String): List<SearchResponse> {
        val url = "$mainUrl/?s=$query"
        val document = app.get(url).document

        return document.select("div.item, article.anime-item, div.result-item").mapNotNull { element ->
            val title = element.selectFirst("a")?.attr("title") 
                ?: element.selectFirst("h3, h2, .title")?.text() 
                ?: return@mapNotNull null
            
            val href = element.selectFirst("a")?.attr("href") ?: return@mapNotNull null
            val poster = element.selectFirst("img")?.attr("data-src") 
                ?: element.selectFirst("img")?.attr("src")

            newAnimeSearchResponse(title, fixUrl(href), TvType.Anime) {
                this.posterUrl = poster?.let { fixUrl(it) }
            }
        }
    }

        // 3. Detalhes do Anime e Lista de Episódios
    override suspend fun load(url: String): LoadResponse {
        val document = app.get(url).document

        val title = document.selectFirst("h1.entry-title, h1.title, div.anime-title")?.text()
            ?: "Sem título"

        val poster = document.selectFirst("div.poster img, div.anime-thumbnail img")?.attr("src")
        val description = document.selectFirst("div.description, div.sinopse, p.story")?.text()

        val episodes = document.select("ul.episodes-list li, div.episodes a, ul.list-episodes li").mapNotNull { element ->
            val epHref = element.selectFirst("a")?.attr("href") ?: return@mapNotNull null
            val epTitle = element.text().trim()

            newEpisode(fixUrl(epHref)) {
                this.name = epTitle
            }
        }

        return newAnimeLoadResponse(title, url, TvType.Anime) {
            this.posterUrl = poster?.let { fixUrl(it) }
            this.plot = description
            this.addEpisodes(DubStatus.Subbed, episodes)
        }
    }

    // 4. Extrator de Videos / Players
    override suspend fun loadLinks(
        data: String,
        isCasting: Boolean,
        subtitleCallback: (SubtitleFile) -> Unit,
        callback: (ExtractorLink) -> Unit
    ): Boolean {
        val document = app.get(data).document

        // Procura por iframes e tags de vídeo na página do episódio
        document.select("iframe, video source").forEach { element ->
            val src = element.attr("src").ifEmpty { element.attr("data-src") }
            if (src.isNotEmpty()) {
                val fullUrl = fixUrl(src)

                // Tenta resolver automaticamente via extratores nativos do CloudStream
                loadExtractor(fullUrl, subtitleCallback, callback)
            }
        }

        return true
    }
}
