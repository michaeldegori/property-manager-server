package com.app.propertymanager.service

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody

@Service
class AirbnbAuthService(
    private val client: WebClient,
) {
    suspend fun performLogin(email: String, password: String): String {
        val loginPageHtml = client.get()
            .uri("/login")
            .retrieve()
            .awaitBody<String>()

        val bootstrapData = parseDataBootstrapJson(loginPageHtml)
        val apiKey = bootstrapData?.layoutInit?.apiConfig?.key
            ?: error("Could not find 'api_config.key' in data-bootstrap script!")

        val loginRequestBody = mapOf(
            "metadata" to mapOf("sxsMode" to "OFF"),
            "fromWeb" to true,
            "queryParams" to "{\"has_logged_out\":\"1\",\"redirect_params\":\"{}\"}",
            "authenticationParams" to mapOf(
                "email" to mapOf(
                    "email" to email,
                    "password" to password
                )
            )
        )

        val response = client.post()
            .uri { uriBuilder ->
                uriBuilder
                    .path("/api/v2/login_for_web")
                    .queryParam("key", apiKey)
                    .build()
            }
            .contentType(MediaType.APPLICATION_JSON)
            .header("x-csrf-without-token", "1")
            .header("x-requested-with", "XMLHttpRequest")
            .bodyValue(loginRequestBody)
            .retrieve()
            .awaitBody<String>()

        println("Login response:\n$response")
        return response
    }

    private fun parseDataBootstrapJson(html: String): BootstrapData? {
        val scriptRegex = Regex("""<script id="data-bootstrap"[^>]*>(.*?)</script>""", RegexOption.DOT_MATCHES_ALL)
        val scriptMatch = scriptRegex.find(html) ?: return null

        val jsonContent = scriptMatch.groups[1]?.value ?: return null

        return try {
            val mapper = jacksonObjectMapper()
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            mapper.readValue<BootstrapData>(jsonContent)
        } catch (e: Exception) {
            println("Error parsing data-bootstrap JSON: ${e.message}")
            null
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    data class BootstrapData(
        @JsonProperty("layout-init")
        val layoutInit: LayoutInit? = null
    )

    @JsonIgnoreProperties(ignoreUnknown = true)
    data class LayoutInit(
        @JsonProperty("api_config")
        val apiConfig: ApiConfig? = null
    )

    @JsonIgnoreProperties(ignoreUnknown = true)
    data class ApiConfig(
        val key: String? = null,
        val baseUrl: String? = null
    )
}
