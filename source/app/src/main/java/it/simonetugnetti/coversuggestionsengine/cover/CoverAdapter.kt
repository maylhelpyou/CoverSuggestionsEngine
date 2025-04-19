package it.simonetugnetti.coversuggestionsengine.cover

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import it.simonetugnetti.coversuggestionsengine.R
import it.simonetugnetti.coversuggestionsengine.util.InfoBooks
import it.simonetugnetti.coversuggestionsengine.util.loadImage
import kotlinx.android.synthetic.main.item_image_cover.view.*

/**
 * Cover Adapter
 * Classe adapter per la gestione dei singoli elementi all'interno della RecyclerView in
 * CoverFragment
 * @author Simone Tugnetti
 */
class CoverAdapter(private val clickListener: ImageClickListener):
    RecyclerView.Adapter<CoverAdapter.ViewHolder>() {

    // Dati da visualizzare
    var data = listOf<InfoBooks>()
    set(value) {
        field = value
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }

    override fun getItemCount() = data.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = data[position]

        holder.bind(item, clickListener)
    }

    /**
     * View Holder
     * Rappresenta il singolo item da visualizzare, collegandone il layout e popolandolo con i dati
     * corretti
     * @constructor Item da visualizzare
     */
    class ViewHolder private constructor(itemView: View): RecyclerView.ViewHolder(itemView) {
        private val image = itemView.coverImage
        private val source = itemView.sourceImage

        /**
         * Bind
         * Gestisce la visualizzazione dei contenuti all'interno dell'item e della loro
         * interattività
         */
        fun bind(item: InfoBooks, clickListener: ImageClickListener) {
            image.loadImage(item.image)
            source.text = item.service
            image.setOnClickListener {
                clickListener.onClick(item)
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
                    .inflate(R.layout.item_image_cover, parent, false)
                return ViewHolder(view)
            }
        }

    }

}

/**
 * Image Click Listener
 * Classe usata per prelevare le informazioni del singolo item quando viene selezionato
 * @param clickListener click sull'item desiderato
 */
class ImageClickListener(val clickListener: (infoBooks: InfoBooks) -> Unit) {
    fun onClick(infoBooksSelected: InfoBooks) = clickListener(infoBooksSelected)
}