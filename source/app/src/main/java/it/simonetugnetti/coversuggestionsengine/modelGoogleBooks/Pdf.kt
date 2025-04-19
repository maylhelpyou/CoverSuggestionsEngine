package it.simonetugnetti.coversuggestionsengine.modelGoogleBooks

import com.google.gson.annotations.SerializedName

/**
 * @author Simone Tugnetti
 */
data class Pdf(
    @SerializedName("isAvailable")
    val isAvailable: Boolean
)