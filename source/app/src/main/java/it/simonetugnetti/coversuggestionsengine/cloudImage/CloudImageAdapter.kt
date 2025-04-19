package it.simonetugnetti.coversuggestionsengine.cloudImage

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.graphics.drawable.toBitmap
import androidx.recyclerview.widget.RecyclerView
import it.simonetugnetti.coversuggestionsengine.R
import it.simonetugnetti.coversuggestionsengine.util.InitFirebase
import it.simonetugnetti.coversuggestionsengine.util.loadImage
import kotlinx.android.synthetic.main.item_cloud_cover.view.*

/**
 * Cloud Image Adapter
 * Classe adapter per la gestione dei singoli elementi all'interno della RecyclerView in
 * CloudImageFragment
 * @author Simone Tugnetti
 */
class CloudImageAdapter(private val getCloudImageClickListener: GetCloudImageClickListener,
                        private val deleteClickListener: DeleteClickListener):
    RecyclerView.Adapter<CloudImageAdapter.ViewHolder>() {

    // Dati da visualizzare
    var data = arrayListOf<String>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = data[position]

        holder.bind(item, getCloudImageClickListener, deleteClickListener)
    }

    override fun getItemCount() = data.size

    /**
     * View Holder
     * Rappresenta il singolo item da visualizzare, collegandone il layout e popolandolo con i dati
     * corretti
     * @constructor Item da visualizzare
     */
    class ViewHolder private constructor(itemView: View): RecyclerView.ViewHolder(itemView) {
        private val image = itemView.imageCloud
        private val btnGetCloudImage = itemView.getImageCloud
        private val btnDelete = itemView.deleteImageCloud

        /**
         * Bind
         * Gestisce la visualizzazione dei contenuti all'interno dell'item e della loro
         * interattività
         */
        @SuppressLint("SetTextI18n")
        fun bind(item: String, getImageCloudClickListener: GetCloudImageClickListener,
                 deleteClickListener: DeleteClickListener) {
            image.loadImage("connection_error")
            InitFirebase.mFirebaseStorage.reference.child("savedCover/$item.jpg")
                .downloadUrl.addOnSuccessListener {
                    image.loadImage(it.toString())
                }
            btnGetCloudImage.setOnClickListener {
                getImageCloudClickListener.onClick(image.drawable.toBitmap())
            }
            btnDelete.setOnClickListener {
                deleteClickListener.onClick(item)
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
                    .inflate(R.layout.item_cloud_cover, parent, false)
                return ViewHolder(view)
            }
        }

    }

}

/**
 * Get Cloud Image Click Listener
 * Classe usata per prelevare il bitmap dell'immagine visualizzata
 * @param getCloudImageClickListener click sull'item desiderato
 */
class GetCloudImageClickListener(val getCloudImageClickListener: (imageBitmap: Bitmap) -> Unit) {
    fun onClick(imageBitmapSelected: Bitmap) = getCloudImageClickListener(imageBitmapSelected)
}

/**
 * Delete Click Listener
 * Classe usata per eliminare il singolo item quando viene selezionato
 * @param deleteClickListener click sull'item desiderato
 */
class DeleteClickListener(val deleteClickListener: (image: String) -> Unit) {
    fun onClick(imageSelected: String) = deleteClickListener(imageSelected)
}