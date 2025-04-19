package it.simonetugnetti.coversuggestionsengine.modelGoogleBooks

import com.google.gson.annotations.SerializedName

/**
 * @author Simone Tugnetti
 */
data class SearchInfo(
    @SerializedName("textSnippet")
    val textSnippet: String
)