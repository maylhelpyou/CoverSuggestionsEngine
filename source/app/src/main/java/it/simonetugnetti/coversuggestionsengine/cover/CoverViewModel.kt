package it.simonetugnetti.coversuggestionsengine.cover

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import it.simonetugnetti.coversuggestionsengine.modelGoodReads.GoodreadsResponse
import it.simonetugnetti.coversuggestionsengine.modelGoogleBooks.Books
import it.simonetugnetti.coversuggestionsengine.retrofit.GoodReadsApi
import it.simonetugnetti.coversuggestionsengine.retrofit.GoogleBooksApi
import it.simonetugnetti.coversuggestionsengine.util.InfoBooks
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * Cover ViewModel
 * Classe ViewModel per la gestione dei dati di CoverFragment.
 * Le variabili sono di tipo LiveData per contenere i risultati in contemporanea con il ciclo di
 * vita del fragment
 * @author Simone Tugnetti
 */
class CoverViewModel : ViewModel() {

    // Lista di immagini da visualizzare
    private val _images = MutableLiveData<List<InfoBooks>>()
    val images: LiveData<List<InfoBooks>>
        get() = _images

    // Utilizzata per eseguire la transizione verso il ManageFragment
    private val _infoImage = MutableLiveData<InfoBooks>()
    val infoImage: LiveData<InfoBooks>
        get() = _infoImage

    // Utilizzata per gestire il pulsante di ricerca
    private val _enableGetImage = MutableLiveData<Boolean>()
    val enableGetImage: LiveData<Boolean>
        get() = _enableGetImage

    // Utilizzata per non permettere la ricerca del valore precedentemente ricercato
    private val _actualSearchValue = MutableLiveData<String>()
    val actualSearchValue: LiveData<String>
        get() = _actualSearchValue

    // Utilizzata per gestire gli errori di connessione o assenza di risultati
    private val _imageUtil = MutableLiveData<String>()
    val imageUtil: LiveData<String>
        get() = _imageUtil

    // Utilizzata per rendere visibile il placeholder inerente agli errori
    private val _visibleImageUtil = MutableLiveData<Boolean>()
    val visibleImageUtil: LiveData<Boolean>
        get() = _visibleImageUtil

    // Utlizzata per visualizzare il messaaggio di assenza di risultati
    private val _alertZeroResult = MutableLiveData<Boolean>()
    val alertZeroResult: LiveData<Boolean>
        get() = _alertZeroResult

    // Utilizzata per rendere visibile il placeholder di attesa risposta dalla chiamata Retrofit
    private val _progressVisible = MutableLiveData<Boolean>()
    val progressVisible: LiveData<Boolean>
        get() = _progressVisible

    // Utilizzata per gestire il tipo di ricerca da eseguire, se ISBN, autore o titolo
    private val _hintSearchBook = MutableLiveData<String>()
    val hintSearchBook: LiveData<String>
        get() = _hintSearchBook

    init {
        _images.value = null
        _enableGetImage.value = true
        _actualSearchValue.value = ""
        _imageUtil.value = null
        _visibleImageUtil.value = true
        _infoImage.value = null
        _alertZeroResult.value = false
        _progressVisible.value = false
        _hintSearchBook.value = "Inserisci ISBN"
    }

    fun changeHintSearchBook(text: String) {
        _hintSearchBook.value = text
    }

    fun notVisibleAlertZeroResult() {
        _alertZeroResult.value = false
    }

    fun setInfoImage(info: InfoBooks) {
        _infoImage.value = info
    }

    fun navigatedToImage() {
        _infoImage.value = null
    }

    fun setActualSearchValue(text: String) {
        _actualSearchValue.value = text
    }

    fun enableGetImageTrue() {
        _enableGetImage.value = true
    }

    fun enableGetImageFalse() {
        _enableGetImage.value = false
    }

    /**
     * Get Image Goodreads
     * Funzione che esegue una chiamata Retrofit al servizio Goodreads per ricevere le immagini
     * correlate all'ISBN passato per parametro.
     * Nel caso non vi sono immagini pervenute, verrà eseguito un fallback alla chiamata successiva
     * @param isbn ISBN utilizzato per eseguirne la ricerca
     * @param key API key per utilizzare il servizio
     */
    fun getImageGoodreads(isbn: String, key: String) {
        val getBooks = GoodReadsApi.retrofitService.getBooks(isbn, key)

        // Placeholder carica avviato
        _progressVisible.value = true
        _images.value = null

        getBooks.enqueue(object : Callback<GoodreadsResponse> {

            override fun onFailure(call: Call<GoodreadsResponse>, t: Throwable) {
                _progressVisible.value = false
                _imageUtil.value = "connection_error"
                _visibleImageUtil.value = true
            }

            override fun onResponse(call: Call<GoodreadsResponse>, response: Response<GoodreadsResponse>) {
                val listBooks = response.body()

                listBooks?.let {

                    // Nel caso non ci siano risultati, si eseguirà la chiamata successiva
                    if (it.search?.totalResults!! > 0) {
                        val data = arrayListOf<InfoBooks>()

                        // La risposta deve contenere informazioni riguardanti i libri
                        it.search?.results?.work?.let { listWork ->
                            for (book in listWork) {

                                // La risposta deve contenere un'immagine
                                book.bestBook?.imageUrl?.let { image ->
                                    if (!image.contains("nophoto", ignoreCase = true)) {
                                        data.add(
                                            InfoBooks("Goodreads", isbn,
                                                book.bestBook?.title ?: "no_title",
                                                book.bestBook?.author?.name ?: "no_author",
                                                book.originalPublicationYear?.toString()
                                                    ?: "no_year",
                                                image.replace("SX98", "SX350"))
                                        )
                                    }
                                }
                            }
                        }

                        // Se sono stati trovati dei risultati, questi verranno salvati e
                        // visualizzati, altrimenti si eseguirà la chiamata successiva
                        if (data.isNotEmpty()) {
                            _images.value = data
                            _progressVisible.value = false
                            _visibleImageUtil.value = false
                            _imageUtil.value = "results_ok"
                        } else {
                            getImageGoogle(isbn)
                        }
                    } else {
                        getImageGoogle(isbn)
                    }
                }

                if (listBooks == null) {
                    getImageGoogle(isbn)
                }

            }

        })

    }

    /**
     * Get Image Google
     * Funzione che esegue una chiamata Retrofit al servizio Google Books per ricevere le immagini
     * correlate al testo passato per parametro.
     * @param text testo utilizzato per eseguirne la ricerca
     */
    fun getImageGoogle(text: String) {

        // In base al tipo di ricerca, verrà creata un'istanza specifica per la chiamata
        val getBooks = when (_hintSearchBook.value) {
            "Inserisci Nome Libro" -> GoogleBooksApi.retrofitService.getBooks("intitle:$text")
            "Inserisci Autore" -> GoogleBooksApi.retrofitService.getBooks("inauthor:$text")
            else -> GoogleBooksApi.retrofitService.getBooks("isbn:$text")
        }

        _progressVisible.value = true
        _images.value = null

        getBooks.enqueue(object : Callback<Books> {

            override fun onFailure(call: Call<Books>, t: Throwable) {
                _progressVisible.value = false
                _imageUtil.value = "connection_error"
                _visibleImageUtil.value = true
            }

            override fun onResponse(call: Call<Books>, response: Response<Books>) {
                val listBooks = response.body()

                listBooks?.let {
                    val data = arrayListOf<InfoBooks>()

                    if (it.totalItems > 0) {
                        for (books in it.items) {
                            books.volumeInfo.industryIdentifiers?.let { isbnInfo ->
                                books.volumeInfo.imageLinks?.thumbnail?.let { image ->

                                    var isbn = isbnInfo[0].identifier

                                    for (elem in isbnInfo) {
                                        if (elem.type == "ISBN_13") {
                                            isbn = elem.identifier
                                        }
                                    }

                                    data.add(
                                        InfoBooks(
                                            "Google Books", isbn, books.volumeInfo.title,
                                            books.volumeInfo
                                                .authors?.joinToString(", ") ?: "no_author",
                                            books.volumeInfo.publishedDate ?: "no_year",
                                            image.replace("http", "https")
                                        )
                                    )
                                }
                            }
                        }
                    }

                    if (data.isNotEmpty()) {
                        _images.value = data
                        _visibleImageUtil.value = false
                        _imageUtil.value = "results_ok"
                    } else {
                        _imageUtil.value = null
                        _visibleImageUtil.value = true
                        _alertZeroResult.value = true
                    }

                    _progressVisible.value = false

                }

            }

        })

    }

}
