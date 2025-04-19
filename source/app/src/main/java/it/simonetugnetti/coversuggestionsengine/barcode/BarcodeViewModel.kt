package it.simonetugnetti.coversuggestionsengine.barcode

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

/**
 * Barcode View Model
 * Classe View Model usata per contenere il barcode scansionato
 * @author Simone Tugnetti
 */
class BarcodeViewModel : ViewModel() {

    // Variabile utilizzata per salvare l'ISBN identificato tramite barcode
    private val _isbnBarcode = MutableLiveData<String>()
    val isbnBarcode: LiveData<String>
    get() = _isbnBarcode

    init {
        _isbnBarcode.value = null
    }

    fun setISBNBarcode(isbn: String) {
        _isbnBarcode.value = isbn
    }

    fun doneNavigation() {
        _isbnBarcode.value = null
    }

}
