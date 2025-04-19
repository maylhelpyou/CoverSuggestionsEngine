package it.simonetugnetti.coversuggestionsengine.modelGoogleBooks

import com.google.gson.annotations.SerializedName

/**
 * @author Simone Tugnetti
 */
data class SaleInfo(
    @SerializedName("country")
    val country: String,
    @SerializedName("isEbook")
    val isEbook: Boolean,
    @SerializedName("saleability")
    val saleability: String
)