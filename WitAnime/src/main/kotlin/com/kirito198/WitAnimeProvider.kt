package com.kirito198

import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.utils.ExtractorLink

class WitAnimeProvider : MainAPI() {
    override var mainUrl = "https://witaanime.com" 
    override var name = "WitAnime"
    override val hasMainPage = true
    override var lang = "ar" 
    override val supportedTypes = setOf(TvType.Anime)

    override suspend fun getMainPage(page: Int, request: MainActivityLoadRequest): HomePageResponse? {
        val document = app.get(mainUrl).document
        val homeItems = ArrayList<SearchResponse>()
        
        document.select("div.anime-card-container, div.episodes-card-container, div.post-item").forEach { element ->
            val title = element.select("h3 a, .anime-title, .title").text().trim()
            val url = element.select("a").attr("href")
            val poster = element.select("img").attr("src")
            
            if (title.isNotEmpty() && url.isNotEmpty()) {
                homeItems.add(newAnimeSearchResponse(title, url, TvType.Anime) {
                    this.posterUrl = poster
                })
            }
        }
        return newHomePageResponse("آخر الحلقات المضافة", homeItems)
    }

    override suspend fun search(query: String): List<SearchResponse> {
        val searchUrl = "$mainUrl/?s=$query"
        val document = app.get(searchUrl).document
        
        return document.select("div.anime-card-container, div.post-item").mapNotNull { element ->
            val title = element.select("h3 a, .title").text().trim()
            val url = element.select("a").attr("href")
            val poster = element.select("img").attr("src")
            
            newAnimeSearchResponse(title, url, TvType.Anime) {
                this.posterUrl = poster
            }
        }
    }

    override suspend fun loadLinks(
        data: String,
        isCasting: Boolean,
        subtitleCallback: (SubtitleFile) -> Unit,
        callback: (ExtractorLink) -> Unit
    ): Boolean {
        val document = app.get(data).document
        
        document.select("ul.blurr-player-servers li a, iframe, video source").forEach { element ->
            val videoUrl = element.attr("data-url").ifEmpty { element.attr("src") }.ifEmpty { element.attr("value") }
            if (videoUrl.isNotEmpty()) {
                callback.invoke(
                    ExtractorLink(
                        name,
                        "WitAnime Server",
                        videoUrl,
                        referer = mainUrl,
                        quality = Qualities.Unknown.value
                    )
                )
            }
        }
        return true
    }
}
