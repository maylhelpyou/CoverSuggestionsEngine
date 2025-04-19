package it.simonetugnetti.coversuggestionsengine.util

import android.content.Context
import android.content.DialogInterface
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Parcelable
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import it.simonetugnetti.coversuggestionsengine.R
import kotlinx.android.parcel.Parcelize

/**
 * @author Simone Tugnetti
 */

/**
 * Check ISBN
 * In base alla lunghezza della riga, verrà eseguito un controllo per determinare se
 * l'ISBN inserito risulta valido
 * @param isbn ISBN da verificare
 * @return ISBN verificato
 */
fun isValidISBN(isbn: String): Boolean {

    when (isbn.length) {
        10 -> return checkISBN10(isbn)
        13 -> return checkISBN13(isbn)
    }

    return false
}

/**
 * Check ISBN 10
 * Verifica dell'ISBN nel caso la riga sia lunga 10 caratteri
 * @param isbn ISBN da verificare
 * @return ISBN verificato
 */
fun checkISBN10(isbn: String): Boolean {
    var sum = 0
    var subISBN: String
    for (i in 0 until 10) {
        subISBN = isbn.substring(i, i+1)
        sum += if (i < 9 || subISBN != "X") {
            subISBN.toInt() * (10 - i)
        } else {
            10
        }

    }

    return (sum % 11 == 0)
}

/**
 * Check ISBN 13
 * Verifica dell'ISBN nel caso la riga sia lunga 13 caratteri
 * @param isbn ISBN da verificare
 * @return ISBN verificato
 */
fun checkISBN13(isbn: String): Boolean {
    var sum = 0
    var subISBN: Int
    for (i in 0 until 13) {
        subISBN = isbn.substring(i, i + 1).toInt()
        sum += subISBN * if (i % 2 == 0) 1 else 3
    }

    return (sum % 10 == 0)
}

/**
 * Create Info Alert
 * Crea un alert informativo di default
 * @param context context per creare l'alert
 * @param title titolo visibile nell'alert
 * @param icon icona visibile nell'alert
 * @param message corpo visibile nell'alert
 */
fun createInfoAlert(context: Context, title: String, icon: Int, message: String) {
    val alert = AlertDialog.Builder(context).setTitle(title).setIcon(icon).setMessage(message)
        .setPositiveButton("OK", null)
        .create()

    changeColorButtonAlert(context, alert)

    alert.show()
}

/**
 * Create Confirm Alert
 * Crea un alert di conferma con un'azione specifica per il positive button
 * @param context context per creare l'alert
 * @param title titolo visibile nell'alert
 * @param icon icona visibile nell'alert
 * @param message corpo visibile nell'alert
 * @param posText testo del pulsante positivo
 * @param positive listener del pulsante positivo
 */
fun createConfirmAlert(context: Context, title: String, icon: Int, message: String, posText: String,
                       positive: DialogInterface.OnClickListener) {
    val alert = AlertDialog.Builder(context).setTitle(title).setIcon(icon).setMessage(message)
        .setPositiveButton(posText, positive).setNegativeButton("Chiudi", null)
        .create()

    changeColorButtonAlert(context, alert)

    alert.show()
}

/**
 * Change Color Button Alert
 * Funzione che modifica il colore di uno specifico AlertDialog
 * @param context context per ricevere il colore
 * @param alert alert specifico
 */
private fun changeColorButtonAlert(context: Context, alert: AlertDialog) {
    alert.setOnShowListener {
        alert.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(
            ContextCompat.getColor(context, R.color.colorPrimary))
        alert.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(
            ContextCompat.getColor(context, R.color.colorPrimary))
    }
}

/**
 * Create Toast
 * Funzione che crea un Toast di default
 * @param context context per creare il Toast
 * @param text testo visualizzato
 */
fun createToast(context: Context, text: String) {
    Toast.makeText(context, text, Toast.LENGTH_SHORT).show()
}

/**
 * Check Connection
 * Funzione che esegue un controllo per verificare se il dispositivo è collegato alla rete internet
 * @param context Context per ricevere il servizio di connettività
 */
@Suppress("DEPRECATION")
fun checkConnection(context: Context): Boolean {
    val connectivity = context.getSystemService(Context.CONNECTIVITY_SERVICE)
            as ConnectivityManager
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        val nw = connectivity.activeNetwork ?: return false
        val actNw = connectivity.getNetworkCapabilities(nw) ?: return false
        return when {
            actNw.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
            actNw.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
            else -> false
        }
    } else {
        val nwInfo = connectivity.activeNetworkInfo ?: return false
        return nwInfo.isConnected
    }
}

/**
 * Info Books
 * Data class, cioè una classe usata al solo scopo di contenere dati, utilizzata per salvare
 * le informazioni base inserenti al singolo libro.
 * Estende la libreria Parcelable per poter passare queste informazioni attraverso i fragment
 * usando Navigation Controller.
 */
@Parcelize
data class InfoBooks(
    val service: String,
    val isbn: String,
    val title: String,
    val author: String,
    val publicationYear: String,
    var image: String
): Parcelable

/**
 * Info Books Table
 * Data class gestita come Entità allo scopo di essere utilizzata attraverso la Room per il
 * salvataggio dei dati dei libri in un database locale.
 * Il salvataggio è gestito usando come chiave primaria il numero di richiesta
 */
@Entity
data class InfoBooksTable(
    @PrimaryKey
    val idRequest: Int,
    val isbn: String,
    val service: String,
    val title: String,
    val author: String,
    val publicationYear: String,
    val image: String
)

/**
 * Init Firebase
 * Istanza singleton per l'utilizzo del servizio Firebase Firestore e Storage utilizzato per il
 * salvataggio online dei dati
 */
object InitFirebase {
    val mFirestore = FirebaseFirestore.getInstance()
    val mFirebaseStorage = FirebaseStorage.getInstance()
}