package com.medtrackpro.demo.data.remote

data class GeminiGenerateContentRequest(
    val contents: List<GeminiContent>
)

data class GeminiContent(
    val parts: List<GeminiPart>? = null
)

data class GeminiPart(
    val text: String? = null
)

data class GeminiGenerateContentResponse(
    val candidates: List<GeminiCandidate>? = null
)

data class GeminiCandidate(
    val content: GeminiContent? = null
)
