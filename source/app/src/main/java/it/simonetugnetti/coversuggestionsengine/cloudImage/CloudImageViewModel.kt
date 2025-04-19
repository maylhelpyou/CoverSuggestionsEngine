package it.simonetugnetti.coversuggestionsengine.cloudImage

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import it.simonetugnetti.coversuggestionsengine.util.InitFirebase
import kotlin.collections.ArrayList

/**
 * Cloud Image ViewModel
 * Classe ViewModel per la gestione dei dati di CloudImageFragment.
 * Le variabili sono di tipo LiveData per contenere i risultati in contemporanea con il ciclo di
 * vita del fragment
 * @author Simone Tugnetti
 */
class CloudImageViewModel : ViewModel() {

    // Preleva l'istanza di Firestore
    private val mFirestore = InitFirebase.mFirestore

    // Preleva l'istanza di Firebase Storage
    private val mFirebaseStorage = InitFirebase.mFirebaseStorage

    // Lista delle cover salvate
    private val _infoListImage = MutableLiveData<ArrayList<String>>()
    val infoListImage: LiveData<ArrayList<String>>
        get() = _infoListImage

    // Utilizzata per gestire la visualizzazione di un placeholder nel caso in cui non vi siano
    // cover salvate
    private val _showPlaceholderNoImage = MutableLiveData<Boolean>()
    val showPlaceholderNoImage: LiveData<Boolean>
        get() = _showPlaceholderNoImage

    // utlizzata per gestire la visualizzazione della progressbar per il caricamento
    private val _showProgressBar = MutableLiveData<Boolean>()
    val showProgressBar: LiveData<Boolean>
        get() = _showProgressBar


    init {
        _infoListImage.value = null
        _showPlaceholderNoImage.value = true
        _showProgressBar.value = false
    }

    /**
     * Get List Image Cloud
     * Funzione che preleva l'ID di tutte le immagini salvate all'intero di coverSaved in Firestore,
     * i quali puntano ad una specifica immagine in Firebase Storage
     */
    fun getListImageCloud() {
        _showProgressBar.value = true

        mFirestore.collection("savedCover").get().addOnSuccessListener { listDoc ->
            val data = arrayListOf<String>()
            if (!listDoc.isEmpty) {
                if (listDoc.documents.isNotEmpty()) {
                    for (elem in listDoc.documents) {
                        data.add(elem.id)
                    }
                }
            }
            if (data.isEmpty()) _showPlaceholderNoImage.value = true else {
                _infoListImage.value = data
                _showPlaceholderNoImage.value = false
            }
            _showProgressBar.value = false
        }
    }

    /**
     * Delete Image
     * Funzione che elimina un'immagine specifica sia da Firebase Firestore e sia da Storage per poi
     * ricaricare la lista di immagini salvate
     * @param name nome dell'immagine da eliminare
     */
    fun deleteImage(name: String) {
        mFirestore.collection("savedCover").document(name).delete()
        mFirebaseStorage.reference.child("savedCover/$name.jpg").delete()
        getListImageCloud()
    }

    /**
     * Delete All Books
     * Funzione che elimina tutte le immagini salvate sia da Firebase Firestore e sia da Storage per
     * poi ricaricare la lista di immagini salvate
     */
    fun deleteAllBooks() {
        mFirestore.collection("savedCover").get().addOnSuccessListener { listDoc ->
            if (!listDoc.isEmpty) {
                if (listDoc.documents.isNotEmpty()) {
                    for (elem in listDoc.documents) {
                        mFirestore.collection("savedCover").document(elem.id).delete()
                            .addOnSuccessListener {
                                mFirebaseStorage.reference
                                    .child("savedCover/${elem.id}.jpg").delete()
                            }
                    }
                    getListImageCloud()
                }
            }
        }
    }

}