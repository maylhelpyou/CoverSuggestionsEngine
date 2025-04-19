package it.simonetugnetti.coversuggestionsengine.modelGoogleBooks

import com.google.gson.annotations.SerializedName

/**
 * @author Simone Tugnetti
 */
data class Epub(
    @SerializedName("isAvailable")
    val isAvailable: Boolean
)