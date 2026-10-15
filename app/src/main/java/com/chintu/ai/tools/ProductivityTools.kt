package com.chintu.ai.tools

import android.content.Context
import android.content.Intent
import android.net.Uri

class ProductivityTools(private val context: Context) {

    fun getMapsNavigationTool(): ToolContract = object : ToolContract {
        override val name: String = "open_maps_navigation"
        override val description: String = "Opens Google Maps navigation for a given destination. Argument 'destination' required."

        override suspend fun execute(args: Map<String, String>): ToolResult {
            val destination = args["destination"] ?: return ToolResult.Failure("Missing argument 'destination'")
            return try {
                val gmmIntentUri = Uri.parse("google.navigation:q=${Uri.encode(destination)}")
                val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri).apply {
                    setPackage("com.google.android.apps.maps")
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                if (mapIntent.resolveActivity(context.packageManager) != null) {
                    context.startActivity(mapIntent)
                    ToolResult.Success("Opening Google Maps for $destination")
                } else {
                    val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/maps/search/?api=1&query=${Uri.encode(destination)}")).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(browserIntent)
                    ToolResult.Success("Opening Maps in browser for $destination")
                }
            } catch (e: Exception) {
                ToolResult.Failure("Maps navigation error: ${e.localizedMessage}")
            }
        }
    }

    fun getYouTubeSearchTool(): ToolContract = object : ToolContract {
        override val name: String = "search_youtube"
        override val description: String = "Searches YouTube videos. Argument 'query' required."

        override suspend fun execute(args: Map<String, String>): ToolResult {
            val query = args["query"] ?: return ToolResult.Failure("Missing argument 'query'")
            return try {
                val intent = Intent(Intent.ACTION_SEARCH).apply {
                    setPackage("com.google.android.youtube")
                    putExtra("query", query)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                if (intent.resolveActivity(context.packageManager) != null) {
                    context.startActivity(intent)
                    ToolResult.Success("Searching YouTube for $query")
                } else {
                    val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/results?search_query=${Uri.encode(query)}")).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(webIntent)
                    ToolResult.Success("Opening YouTube in browser for $query")
                }
            } catch (e: Exception) {
                ToolResult.Failure("YouTube launch error: ${e.localizedMessage}")
            }
        }
    }
}
