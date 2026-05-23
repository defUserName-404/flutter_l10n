package com.defusername.flutter_l10n.translation

import com.defusername.flutter_l10n.config.TranslationConfig
import org.json.JSONObject
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration

class TranslationService(
    private val config: TranslationConfig = TranslationConfig(),
) {
    private val client: HttpClient = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(10))
        .build()

    fun translateBatch(texts: List<String>, targetLocale: String): List<String?> {
        if (texts.isEmpty()) return emptyList()

        val targetName = java.util.Locale(targetLocale).displayLanguage
        val prompt = config.promptFor(targetName)
        val input = texts.joinToString("\n")

        val body = JSONObject().apply {
            put("model", config.model)
            put("system", prompt)
            put("prompt", input)
            put("stream", false)
            put("options", JSONObject().apply {
                put("num_predict", 2048)
                put("temperature", 0.1)
            })
        }

        return runCatching {
            val request = HttpRequest.newBuilder()
                .uri(URI.create("${config.ollamaHost}${config.generateEndpoint}"))
                .header("Content-Type", "application/json")
                .timeout(Duration.ofMillis(config.timeoutMillis.toLong()))
                .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
                .build()

            val response = client.send(request, HttpResponse.BodyHandlers.ofString())
            if (response.statusCode() != 200) {
                return texts.map { null }
            }

            val json = JSONObject(response.body())
            val raw = json.optString("response", "").trim()
            TranslationResponseParser.parse(raw, texts.size)
        }.getOrElse {
            texts.map { null }
        }
    }
}
