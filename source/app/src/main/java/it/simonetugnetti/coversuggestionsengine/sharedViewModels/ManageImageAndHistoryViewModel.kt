package it.simonetugnetti.coversuggestionsengine.sharedViewModels

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import it.simonetugnetti.coversuggestionsengine.room.InfoBooksDatabase
import it.simonetugnetti.coversuggestionsengine.util.InfoBooks
import it.simonetugnetti.coversuggestionsengine.util.InfoBooksTable
import it.simonetugnetti.coversuggestionsengine.util.InitFirebase
import it.simonetugnetti.coversuggestionsengine.util.createToast
import kotlinx.coroutines.*
import java.io.ByteArrayOutputStream
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

/**
 * Manage Image And History ViewModel
 * Classe ViewModel per la gestione dei dati di SaveImageFragment e HistoryFragment, essi infatti
 * condividono le stesse informazioni dato che la prima classe salva i dati che verranno poi
 * visualizzati all'interno della seconda.
 * Inoltre, vi è un salvataggio in cloud della cover visualizzata come "cover salvate"
 * Le variabili sono di tipo LiveData per contenere i risultati in contemporanea con il ciclo di
 * vita del fragment
 * @author Simone Tugnetti
 */
class ManageImageAndHistoryViewModel : ViewModel() {

    // Crea un Job, cioè crea un lavoro da eseguire all'interno di un blocco di coroutines
    private val viewModelJob = Job()

    // Viene creato uno Scope, cioè uno scopo al quale una suspend function si deve collegare per
    // essere utilizzata.
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    // Preleva l'istanza di Firestore
    private val mFirestore = InitFirebase.mFirestore

    // Preleva l'istanza di Firebase Storage
    private val mFirebaseStorage = InitFirebase.mFirebaseStorage

    // Lista delle cover suggerite
    private val _infoListImage = MutableLiveData<ArrayList<InfoBooksTable>>()
    val infoListImage: LiveData<ArrayList<InfoBooksTable>>
        get() = _infoListImage

    // Informazioni della singola Cover selezionata
    private val _infoImage = MutableLiveData<InfoBooksTable>()
    val infoImage: LiveData<InfoBooksTable>
    get() = _infoImage

    // Utilizzata per gestire la visualizzazione di un placeholder nel caso in cui non vi siano
    // cover suggerite
    private val _showPlaceholderNoImage = MutableLiveData<Boolean>()
    val showPlaceholderNoImage: LiveData<Boolean>
        get() = _showPlaceholderNoImage

    // utlizzata per gestire la visualizzazione della progressbar per il caricamento
    private val _showProgressBar = MutableLiveData<Boolean>()
    val showProgressBar: LiveData<Boolean>
        get() = _showProgressBar

    // Utilizzata per contenere la cover attualmente visualizzata in SaveImageFragment
    private val _imageUrl = MutableLiveData<String>()
    val imageUrl: LiveData<String>
    get() = _imageUrl

    init {
        _infoListImage.value = null
        _imageUrl.value = null
        _infoImage.value = null
        _showPlaceholderNoImage.value = true
        _showProgressBar.value = false
    }

    fun setImageUrl(image: String) {
        _imageUrl.value = image
    }

    fun doneShowDetailsBook() {
        _infoImage.value = null
    }

    /**
     * Add Image Cloud
     * Funzione che gestisce il salvataggio in Cloud delle "cover salvate" creandone un riferimento
     * in Firestore
     * @param image immagine da inviare in Cloud
     * @param context Context per creare un Toast di avvenuto salvataggio
     */
    fun addImageCloud(image: Bitmap, context: Context) {
        val name = UUID.randomUUID().toString()
        val baos = ByteArrayOutputStream()
        val data: HashMap<String, String> = HashMap()

        image.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val imageByte: ByteArray = baos.toByteArray()
        val imageCloudRef = mFirebaseStorage.reference.child("savedCover/$name.jpg")
        imageCloudRef.putBytes(imageByte)

        mFirestore.collection("savedCover").document(name).set(data)
            .addOnSuccessListener {
                createToast(context, "Immagine salvata in Cloud")
            }

    }

    /**
     * Add Info Image
     * Funzione che inserisce all'interno del database remoto e locale le informazioni della
     * cover suggerita, nonchè l'immagine in Storage online
     * @param info informazioni della cover
     * @param application utilizzata per il funzionamento della Room
     * @param image immagine bitmap da inviare in Firebase Storage
     * @param request numero richiesta attribuito all'elemento
     */
    fun addInfoImage(info: InfoBooks, application: Application, image: Bitmap, request: Int) {
        val name = "${UUID.randomUUID()}.jpg"
        val baos = ByteArrayOutputStream()
        val data: HashMap<String, String> = HashMap()

        image.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val imageByte: ByteArray = baos.toByteArray()
        val imageCloudRef = mFirebaseStorage.reference.child(name)
        imageCloudRef.putBytes(imageByte)

        data["isbn"] = info.isbn
        data["service"] = info.service
        data["title"] = info.title
        data["author"] = info.author
        data["publicationYear"] = info.publicationYear
        data["image"] = name

        mFirestore.collection("Books").document(request.toString()).set(data)
            .addOnFailureListener {
                uiScope.launch {
                    InfoBooksDatabase.get(application).getInfoBooksDao.insertBook(
                        InfoBooksTable(request, info.isbn, info.service, info.title, info.author,
                            info.publicationYear, name)
                    )
                }
            }

    }

    /**
     * Get List Info Books
     * Preleva una lista di informazioni delle cover nel database remoto oppure, nel caso non sia
     * presente una connessione internet, di una lista nel database locale
     * @param application utilizzata per il funzionamento della Room
     */
    fun getListInfoBooks(application: Application) {
        _showProgressBar.value = true

        mFirestore.collection("Books").get().addOnSuccessListener { listDoc ->
            val data = arrayListOf<InfoBooksTable>()
            if (!listDoc.isEmpty) {
                if (listDoc.documents.isNotEmpty()) {
                    for (elem in listDoc.documents) {
                        data.add(InfoBooksTable(elem.id.toInt(), elem["isbn"].toString(),
                            elem["service"].toString(), elem["title"].toString(),
                            elem["author"].toString(), elem["publicationYear"].toString(),
                            elem["image"].toString()))
                    }
                }
            }

            if (data.isEmpty()) _showPlaceholderNoImage.value = true else {
                _infoListImage.value = data
                _showPlaceholderNoImage.value = false
            }

            _showProgressBar.value = false

        }
            .addOnFailureListener {
                uiScope.launch {
                    val data = InfoBooksDatabase.get(application).getInfoBooksDao.getBooks()
                            as ArrayList<InfoBooksTable>

                    if (data.isEmpty()) _showPlaceholderNoImage.value = true else {
                        _infoListImage.value = data
                        _showPlaceholderNoImage.value = false
                    }

                    _showProgressBar.value = false

                }
            }
    }

    /**
     * Get Book
     * Funzione che preleva le informazioni di una singola cover all'interno del database locale,
     * aggiornato o meno da quello in remoto
     * @param application utilizzata per il funzionamento della Room
     * @param request ID al quale è associata la cover da ricercare
     */
    fun getBook(application: Application, request: Int) {
        mFirestore.collection("Books").document(request.toString()).get()
            .addOnSuccessListener {
                _infoImage.value = InfoBooksTable(it.id.toInt(), it["isbn"].toString(),
                    it["service"].toString(), it["title"].toString(), it["author"].toString(),
                    it["publicationYear"].toString(), it["image"].toString())

            }.addOnFailureListener {
                uiScope.launch {
                    _infoImage.value = InfoBooksDatabase.get(application).getInfoBooksDao
                        .getSpecificBook(request)
                }
            }
    }

    /**
     * Delete Book
     * Funzione che elimina la singola cover suggerita desiderata sia dal database remoto che locale
     * @param application utilizzata per il funzionamento della Room
     * @param request ID al quale è associata la cover da eliminare
     */
    fun deleteBook(application: Application, request: Int) {
        mFirestore.collection("Books").document(request.toString()).get()
            .addOnSuccessListener {
                mFirebaseStorage.reference.child(it["image"].toString()).delete()
                    .addOnSuccessListener {
                        mFirestore.collection("Books").document(request.toString())
                            .delete()
                        getListInfoBooks(application)
                    }
            }
            .addOnFailureListener {
                uiScope.launch {
                    InfoBooksDatabase.get(application).getInfoBooksDao.deleteBook(request)
                    getListInfoBooks(application)
                }
            }
    }

    /**
     * Delete All Books
     * Funzione che gestisce l'eliminazione dell'intera lista di cover suggerite sia dal database
     * locale che da remoto
     * @param application utilizzata per il funzionamento della Room
     */
    fun deleteAllBooks(application: Application) {
        val jobFirebase = mFirestore.collection("Books")
        val ref = mFirebaseStorage.reference

        jobFirebase.get().addOnSuccessListener { listDoc ->
            if (!listDoc.isEmpty) {
                if (listDoc.documents.isNotEmpty()) {
                    for (elem in listDoc.documents) {
                        jobFirebase.document(elem.id).get().addOnSuccessListener {
                            ref.child(it["image"].toString()).delete()
                            jobFirebase.document(elem.id).delete()
                            getListInfoBooks(application)
                        }
                    }
                }
            }
        }.addOnFailureListener {
            uiScope.launch {
                InfoBooksDatabase.get(application).getInfoBooksDao.deleteAllBooks()
                getListInfoBooks(application)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }

}
