package it.simonetugnetti.coversuggestionsengine.cloudImage

import android.content.DialogInterface
import android.content.res.Configuration
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import it.simonetugnetti.coversuggestionsengine.R
import it.simonetugnetti.coversuggestionsengine.util.checkConnection
import it.simonetugnetti.coversuggestionsengine.util.createConfirmAlert
import it.simonetugnetti.coversuggestionsengine.util.createToast
import kotlinx.android.synthetic.main.fragment_history.*

/**
 * Cloud Image Fragment
 * Fragment utilizzato per la visualizzazione, il prelievo e l'eliminazione delle immagini salvate
 * all'interno di Firebase Storage
 * @author Simone Tugnetti
 */
class CloudImageFragment : Fragment() {

    private lateinit var viewModel: CloudImageViewModel
    private lateinit var argument: CloudImageFragmentArgs

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_history, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(CloudImageViewModel::class.java)

        historyTitle.text = getString(R.string.cover_salvate)
        deleteAll.text = getString(R.string.cancella_lista_cover)

        viewModel.getListImageCloud()

        argument = CloudImageFragmentArgs.fromBundle(requireArguments())

        // Verrà visualizzato un placeholder nel caso in cui non ci siano cover salvate
        viewModel.showPlaceholderNoImage.observe(viewLifecycleOwner, Observer {
            if (it) {
                placeholderNoRequest.visibility = View.VISIBLE
                listCoverSent.visibility = View.GONE
            } else {
                placeholderNoRequest.visibility = View.GONE
                listCoverSent.visibility = View.VISIBLE
            }
            deleteAll.isEnabled = !it
        })

        // Verrà visualizzata una ProgressBar quando si effettuerà una chiamata in Cloud
        viewModel.showProgressBar.observe(viewLifecycleOwner, Observer {
            if (it) {
                progressHistory.visibility = View.VISIBLE
            } else {
                progressHistory.visibility = View.GONE
            }
        })

        // Elimina tutte le cover, controllando prima che vi sia una connessione internet attiva
        deleteAll.setOnClickListener {
            if (checkConnection(requireContext())) {
                createConfirmAlert(requireContext(), "Attenzione",
                    R.drawable.ic_baseline_warning_24,
                    "Sei sicuro di voler eliminare tutte le cover salvate?",
                    "Elimina", DialogInterface.OnClickListener { _, _ ->
                        viewModel.deleteAllBooks()
                    })
            } else {
                createToast(requireContext(), "Connection Error")
            }
        }

        manageRecyclerCloud()

    }

    /**
     * Manage Recycler Cloud
     * Funzione che gestisce la RecyclerView contenuta nel Fragment
     */
    private fun manageRecyclerCloud() {
        // Adapter per la gestione degli Item nella View.
        // Nel caso in cui venga premuto il pulsante "Preleva", verrà prelevato il bitmap
        // dell'immagine da passare per la navigazione a ManageImageFragment.
        // Nel caso in cui venga premuto il pulsante di elminazione, verrà prima verificato il
        // collegamento internet, per poi eliminare la cover selezionata
        val adapter = CloudImageAdapter(GetCloudImageClickListener {
            val arg = Bundle()
            arg.putParcelable("infoBooks", argument.infoBooks)
            arg.putParcelable("imageCloudBitmap", it)

            this.findNavController().navigate(
                R.id.action_cloudImageFragment_to_manageImageFragment, arg, null
            )
        }, DeleteClickListener {
            if (checkConnection(requireContext())) {
                createConfirmAlert(requireContext(), "Attenzione",
                    R.drawable.ic_baseline_warning_24,
                    "Sei sicuro di voler eliminare questa cover?",
                    "Elimina", DialogInterface.OnClickListener { _, _ ->
                        viewModel.deleteImage(it)
                    })
            } else {
                createToast(requireContext(), "Connection Error")
            }
        })

        viewModel.infoListImage.observe(viewLifecycleOwner, Observer {
            it?.let {
                adapter.data = it
            }
        })

        // Nel caso in cui l'orientamento del dispositivo sia Landscape, verrà settato il
        // layout manager della RecyclerView in modalità LinearLayout, altrimenti in modalità
        // GridLayout
        if (requireActivity().resources.configuration.orientation == Configuration
                .ORIENTATION_LANDSCAPE) {
            listCoverSent.layoutManager = LinearLayoutManager(requireContext(),
                LinearLayoutManager.HORIZONTAL, false)
        } else {
            listCoverSent.layoutManager = GridLayoutManager(requireContext(), 2)
        }

        listCoverSent.adapter = adapter
    }

}