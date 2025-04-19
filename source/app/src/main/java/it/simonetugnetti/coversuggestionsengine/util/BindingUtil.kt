package it.simonetugnetti.coversuggestionsengine.util

import android.widget.ImageView
import androidx.databinding.BindingAdapter
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.bumptech.glide.Glide
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.module.AppGlideModule
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import it.simonetugnetti.coversuggestionsengine.R

/**
 * @author Simone Tugnetti
 */

/**
 * My Glide Module
 * Classe utilizzata per creare un istanza di AppGlideModule() allo scopo di utilizzare le sue
 * funzioni di conversione
 */
@GlideModule
class MyGlideModule: AppGlideModule()

/**
 * Load Image
 * Funzione utilizzata esclusivamente dalle ImageView, permette di inserire un'immagine specifica
 * all'interno delle stesse quando si esegue una ricerca.
 * Nel caso in cui non ci siano risultati, cioè null, viene applicato un placeholder.
 * Nel caso in cui non ci sia connessione, cioè connection_error, viene applicata una notifica di
 * connessione inesistente.
 * Nel caso in cui vengano trovati dei risultati, la librearia Glide eseguirà una ricerca tramite
 * l'url per ricavarne un drawable da inserire nell'ImageView
 * L'annotazione @BindingAdapter permette a questa funzione di essere chiamata all'interno di un
 * layout che utilizzi il Binding
 * @param url url all'immagine da visualizzare
 */
@BindingAdapter("loadImage")
fun ImageView.loadImage(url: String?) {
    apply {
        val circularProgressDrawable = CircularProgressDrawable(context)
        circularProgressDrawable.strokeWidth = 5f
        circularProgressDrawable.centerRadius = 30f
        circularProgressDrawable.start()
        when (url) {
            null -> setImageResource(R.drawable.book_placeholder)
            "connection_error" -> setImageResource(R.drawable.ic_connection_error)
            "results_ok" -> {}
            else -> {
                Glide.with(context).asDrawable().load(url)
                    .apply(
                        RequestOptions()
                            .fitCenter()
                            .format(DecodeFormat.PREFER_ARGB_8888)
                            .override(Target.SIZE_ORIGINAL)
                            .placeholder(circularProgressDrawable)
                            .error(R.drawable.ic_broken_image)
                    )
                    .into(this)
            }
        }
    }
}