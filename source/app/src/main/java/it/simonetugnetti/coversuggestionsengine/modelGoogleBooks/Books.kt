package it.simonetugnetti.coversuggestionsengine.modelGoogleBooks

import com.google.gson.annotations.SerializedName

/**
 * Google Books Response
 * Classe utilizzata per gestire la risposta JSON di Google Books al momento della richiesta
 * eseguita con retrofit
 * @author Simone Tugnetti
 */
data class Books(
    @SerializedName("items")  // Nome field all'interno del JSON
    val items: List<Item>,
    @SerializedName("kind")
    val kind: String,
    @SerializedName("totalItems")
    val totalItems: Int
)