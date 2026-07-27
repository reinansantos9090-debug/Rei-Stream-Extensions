package com.example

import com.lagradost.cloudstream3.MainAPI
import com.lagradost.cloudstream3.SearchResponse
import com.lagradost.cloudstream3.TvType

class ExampleProvider : MainAPI() {
    override var mainUrl = "https://smartanimes.net"
    override var name = "SmartAnimes"
    override val supportedTypes = setOf(TvType.Anime)

    override var lang = "pt"

    override val hasMainPage = true

    override suspend fun search(query: String): List<SearchResponse> {
        return listOf()
    }
} 
