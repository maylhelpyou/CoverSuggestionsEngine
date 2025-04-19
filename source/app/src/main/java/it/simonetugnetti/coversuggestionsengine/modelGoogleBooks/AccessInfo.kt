package it.simonetugnetti.coversuggestionsengine.modelGoogleBooks

import com.google.gson.annotations.SerializedName

/**
 * @author Simone Tugnetti
 */
data class AccessInfo(
    @SerializedName("accessViewStatus")
    val accessViewStatus: String,
    @SerializedName("country")
    val country: String,
    @SerializedName("embeddable")
    val embeddable: Boolean,
    @SerializedName("epub")
    val epub: Epub,
    @SerializedName("pdf")
    val pdf: Pdf,
    @SerializedName("publicDomain")
    val publicDomain: Boolean,
    @SerializedName("quoteSharingAllowed")
    val quoteSharingAllowed: Boolean,
    @SerializedName("textToSpeechPermission")
    val textToSpeechPermission: String,
    @SerializedName("viewability")
    val viewability: String,
    @SerializedName("webReaderLink")
    val webReaderLink: String
)