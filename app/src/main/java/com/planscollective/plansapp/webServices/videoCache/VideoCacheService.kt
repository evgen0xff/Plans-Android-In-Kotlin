package com.planscollective.plansapp.webServices.videoCache

import com.planscollective.plansapp.webServices.base.BaseWebService
import com.danikula.videocache.HttpProxyCacheServer
import com.planscollective.plansapp.PLANS_APP
import java.io.File


object VideoCacheService : BaseWebService() {

    private var proxy: HttpProxyCacheServer? = null

    private fun newProxy(): HttpProxyCacheServer? {
        return HttpProxyCacheServer.Builder(PLANS_APP)
            .maxCacheSize((1024 * 1024 * 1024).toLong()) // 1 Gb for cache
            .build()
    }

    fun getProxy(): HttpProxyCacheServer? {
        return if (proxy == null) newProxy().also { proxy = it } else proxy
    }

    fun getProxyUrl(strUrl: String?): String? {
        return if (strUrl.isNullOrEmpty() || File(strUrl).exists()) strUrl else getProxy()?.getProxyUrl(strUrl)
    }



}