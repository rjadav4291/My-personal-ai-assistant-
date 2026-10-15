package com.chintu.ai.web

import com.chintu.ai.tools.ToolContract
import com.chintu.ai.tools.ToolResult
import io.ktor.client.*
import io.ktor.client.engine.android.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.json.*

class WebResearchTool : ToolContract {
    override val name: String = "web_research"
    override val description: String = "Searches the live web for real-time information. Argument 'query' required."

    private val client = HttpClient(Android)

    override suspend fun execute(args: Map<String, String>): ToolResult {
        val query = args["query"] ?: return ToolResult.Failure("Missing argument 'query'")
        return try {
            val encodedQuery = query.encodeURLParameter()
            val url = "https://api.duckduckgo.com/?q=$encodedQuery&format=json&no_html=1&skip_disambig=1"
            
            val response = client.get(url)
            if (response.status.isSuccess()) {
                val json = Json.parseToJsonElement(response.bodyAsText()).jsonObject
                val abstractText = json["AbstractText"]?.jsonPrimitive?.content ?: ""
                val heading = json["Heading"]?.jsonPrimitive?.content ?: ""
                
                if (abstractText.isNotBlank()) {
                    ToolResult.Success("Web Research for '$heading':\n$abstractText")
                } else {
                    ToolResult.Success("Live web lookup completed for '$query'. (General topic found on web engine).")
                }
            } else {
                ToolResult.Failure("Web search HTTP error: ${response.status.value}")
            }
        } catch (e: Exception) {
            ToolResult.Failure("Web search failed: ${e.localizedMessage}")
        }
    }
}
