@file:Suppress("DEPRECATION")

package it.simonetugnetti.coversuggestionsengine.retrofit

import it.simonetugnetti.coversuggestionsengine.modelGoodReads.GoodreadsResponse
import it.simonetugnetti.coversuggestionsengine.modelGoogleBooks.Books
import org.simpleframework.xml.convert.AnnotationStrategy
import org.simpleframework.xml.core.Persister
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.simplexml.SimpleXmlConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Retrofit Call
 * Collezione di operazioni per implementare le chiamate retrofit
 * @author Simone Tugnetti
 */


private const val BASEURLGOOGLE = "https://www.googleapis.com/books/v1/"

private const val BASEURLGOODREADS = "https://www.goodreads.com/search/"

// Build per la chiamata retrofit a Google Books con JSON Converter
private val retrofitGoogle = Retrofit.Builder().baseUrl(BASEURLGOOGLE)
    .addConverterFactory(GsonConverterFactory.create())
    .build()

// Build per la chiamata retrofit a Goodreads con XML Converter
private val retrofitGoodReads = Retrofit.Builder().baseUrl(BASEURLGOODREADS)
    .addConverterFactory(SimpleXmlConverterFactory.createNonStrict(
        Persister(AnnotationStrategy())
    ))
    .build()

// Interfaccia utilizzata per ricevere un risultato dalla chiamata
interface GoogleBooks {

    @GET("volumes")  // Preleva il risultato richiamando la funzione
    fun getBooks(
        @Query("q") isbn: String  // Ulteriore dato da inserire per ricevere il giusto risultato
    ): Call<Books>
    // Valore restituito tramite un oggetto Call dello stesso tipo della classe di decodifica

}

interface GoodReadsBooks {

    @GET("index.xml")
    fun getBooks(
        @Query("q") isbn: String,
        @Query("key") key: String
    ): Call<GoodreadsResponse>
}

// Oggetto usato per istanziare la chiamata retrofit utilizzando la rispettiva interfaccia
object GoogleBooksApi {
    val retrofitService: GoogleBooks = retrofitGoogle.create(GoogleBooks::class.java)
}

object GoodReadsApi {
    val retrofitService: GoodReadsBooks = retrofitGoodReads.create(GoodReadsBooks::class.java)
}