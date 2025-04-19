package it.simonetugnetti.coversuggestionsengine.history

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import it.simonetugnetti.coversuggestionsengine.R
import it.simonetugnetti.coversuggestionsengine.util.InfoBooksTable
import it.simonetugnetti.coversuggestionsengine.util.InitFirebase
import it.simonetugnetti.coversuggestionsengine.util.loadImage
import kotlinx.android.synthetic.main.item_info_cover.view.*

/**
 * History Adapter
 * Classe adapter per la gestione dei singoli elementi all'interno della RecyclerView in
 * HistoryFragment
 * @author Simone Tugnetti
 */
class HistoryAdapter(private val detailClickListener: DetailClickListener,
                     private val deleteClickListener: DeleteClickListener):
    RecyclerView.Adapter<HistoryAdapter.ViewHolder>() {

    // Dati da visualizzare
    var data = arrayListOf<InfoBooksTable>()
    set(value) {
        field = value
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = data[position]

        holder.bind(item, detailClickListener, deleteClickListener)
    }

    override fun getItemCount() = data.size

    /**
     * Update List
     * Aggiorna gli elementi della lista in base a cosa è stato ricercato per filtrarli
     * @param newList nuova lista da visualizzare
     */
    fun updateList(newList: List<InfoBooksTable>) {
        data = arrayListOf()
        data.addAll(newList)
        notifyDataSetChanged()
    }

    /**
     * View Holder
     * Rappresenta il singolo item da visualizzare, collegandone il layout e popolandolo con i dati
     * corretti
     * @constructor Item da visualizzare
     */
    class ViewHolder private constructor(itemView: View): RecyclerView.ViewHolder(itemView) {
        private val image = itemView.coverImageHistory
        private val request = itemView.idRichiesta
        private val isbn = itemView.specificISBN
        private val title = itemView.specificTitleBook
        private val btnDetails = itemView.bookDetails
        private val btnDelete = itemView.deleteBook

        /**
         * Bind
         * Gestisce la visualizzazione dei contenuti all'interno dell'item e della loro
         * interattività
         */
        @SuppressLint("SetTextI18n")
        fun bind(item: InfoBooksTable, detailClickListener: DetailClickListener,
                 deleteClickListener: DeleteClickListener) {
            image.loadImage("connection_error")
            InitFirebase.mFirebaseStorage.reference.child(item.image).downloadUrl
                .addOnSuccessListener {
                image.loadImage(it.toString())
            }
            request.text = "Richiesta: ${item.idRequest}"
            isbn.text = item.isbn
            title.text = item.title
            btnDetails.setOnClickListener {
                detailClickListener.onClick(item.idRequest)
            }
            btnDelete.setOnClickListener {
                deleteClickListener.onClick(item.idRequest)
            }
        }

        /**
         * From
         * Funzione singleton che gestisce il layout da visualizzare per il singolo item
         */
        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val view = layoutInflater
                    .inflate(R.layout.item_info_cover, parent, false)
                return ViewHolder(view)
            }
        }

    }

}

/**
 * Detail Click Listener
 * Classe usata per prelevare le informazioni del singolo item quando viene selezionato
 * @param detailClickListener click sull'item desiderato
 */
class DetailClickListener(val detailClickListener: (request: Int) -> Unit) {
    fun onClick(requestSelected: Int) = detailClickListener(requestSelected)
}

/**
 * Delete Click Listener
 * Classe usata per eliminare il singolo item quando viene selezionato
 * @param deleteClickListener click sull'item desiderato
 */
class DeleteClickListener(val deleteClickListener: (request: Int) -> Unit) {
    fun onClick(requestSelected: Int) = deleteClickListener(requestSelected)
}