package com.app.propertymanager.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.ExchangeFilterFunction
import org.springframework.web.reactive.function.client.WebClient
import reactor.netty.http.client.HttpClient
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.web.reactive.function.client.ClientRequest

@Configuration
class AirbnbClientConfig {
    private val cookieStore = mutableMapOf<String, String>()

    @Bean
    fun airbnbClient(): WebClient {
        val httpClient = HttpClient.create()
            .doOnResponse { response, _ ->
                val setCookieValues = response.responseHeaders().getAll("Set-Cookie")
                setCookieValues.forEach { rawCookie ->
                    parseCookie(rawCookie)?.let { (name, value) ->
                        cookieStore[name] = value
                    }
                }
            }

        return WebClient.builder()
            .baseUrl("https://www.airbnb.com")
            .clientConnector(ReactorClientHttpConnector(httpClient))
            .codecs { configurer ->
                configurer.defaultCodecs().maxInMemorySize(5 * 1024 * 1024)
            }
            .filter(addCookiesFilter(cookieStore))
            .build()
    }

    fun addCookiesFilter(cookieStore: Map<String, String>): ExchangeFilterFunction {
        return ExchangeFilterFunction { request: ClientRequest, next ->
            val combinedCookieHeader = cookieStore.entries
                .joinToString("; ") { (k, v) -> "$k=$v" }

            val newRequest = ClientRequest.from(request)
                .header("Cookie", combinedCookieHeader)
                .build()

            next.exchange(newRequest)
        }
    }

    private fun parseCookie(rawCookie: String): Pair<String, String>? {
        val parts = rawCookie.split(';').firstOrNull()?.trim() ?: return null
        val idx = parts.indexOf('=')
        if (idx <= 0) return null
        val name = parts.substring(0, idx).trim()
        val value = parts.substring(idx + 1).trim()
        return name to value
    }
}
