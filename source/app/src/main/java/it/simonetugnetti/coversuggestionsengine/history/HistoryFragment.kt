package it.simonetugnetti.coversuggestionsengine.history

import android.app.Application
import android.content.DialogInterface
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager

import it.simonetugnetti.coversuggestionsengine.R
import it.simonetugnetti.coversuggestionsengine.sharedViewModels.ManageImageAndHistoryViewModel
import it.simonetugnetti.coversuggestionsengine.util.*
import kotlinx.android.synthetic.main.fragment_history.*
import java.util.*

/**
 * History Fragment
 * Fragment utilizzato per tenere una cronologia di tutte le cover suggerite.
 * Il salvataggio viene eseguito in un database sia locale che online
 * @author Simone Tugnetti
 */
class HistoryFragment : Fragment(), SearchView.OnQueryTextListener {

    // Il ViewModel utilizzato in questo fragment è lo stesso utilizzato anche in ManageImageFragment,
    // activityViewModels() consente la genstione del suddetto viewModel all'activity stessa, così
    // da renderla condivisibile
    private val viewModel: ManageImageAndHistoryViewModel by activityViewModels()
    private lateinit var adapter: HistoryAdapter
    private lateinit var application: Application

    override fun onCreate(savedInstanceState: Bundle?) {
        // Per poter gestire gli elementi dell'option menu da questo fragment
        setHasOptionsMenu(true)
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_history, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        // Per il funzionamento della Room
        application = requireActivity().application

        // All'avvio del fragment, verrà eseguita la ricerca delle cover salvate online o,
        // in alternativa, nel caso non ci sia un collegamento internet, delle cover salvate in
        // locale
        viewModel.getListInfoBooks(application)

        // Verrà visualizzato un placeholder nel caso in cui non ci siano cover suggerite
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

        // Elimina tutte le cover, controllando prima che vi sia una connessione internet
        deleteAll.setOnClickListener {
            if (checkConnection(requireContext())) {
                createConfirmAlert(requireContext(), "Attenzione",
                    R.drawable.ic_baseline_warning_24,
                    "Sei sicuro di voler eliminare tutti i suggerimenti?",
                    "Elimina", DialogInterface.OnClickListener { _, _ ->
                        viewModel.deleteAllBooks(application)
                    })
            } else {
                createToast(requireContext(), "Connection Error")
            }
        }

        // Esegue un'aggiornamento della lista cover
        refreshHistory.setOnClickListener {
            viewModel.getListInfoBooks(application)
        }

        manageRecyclerHistory()

    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)

        // Rende visibile il filtraggio della lista
        val searchItem = menu.findItem(R.id.searchBookItem)
        searchItem.isVisible = true
        menu.findItem(R.id.historyItem).isVisible = false
        val searchView = searchItem.actionView as SearchView
        searchView.maxWidth = Integer.MAX_VALUE

        // Viene utilizzato un Listener per filtrare il contenuto in base alla ricerca
        searchView.setOnQueryTextListener(this)
    }

    /**
     * Manage Recycler History
     * Funzione che gestisce la RecyclerView contenuta nel Fragment
     */
    private fun manageRecyclerHistory() {
        // Adapter per la gestione degli Item nella View.
        // Nel caso in cui venga premuto il pulsante di dettaglio, i dati inerenti a tale Cover
        // verranno visualizzati all'utente.
        // Nel caso in cui venga premuto il pulsante di elminazione, verrà prima verificato il
        // collegamento internet, per poi eliminare la cover selezionata
        adapter = HistoryAdapter(DetailClickListener {
            viewModel.getBook(application, it)
        }, DeleteClickListener {
            if (checkConnection(requireContext())) {
                createConfirmAlert(requireContext(), "Attenzione",
                    R.drawable.ic_baseline_warning_24,
                    "Sei sicuro di voler eliminare la richiesta n°$it?",
                    "Elimina", DialogInterface.OnClickListener { _, _ ->
                        viewModel.deleteBook(application, it)
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

        // Visualizza le info della singola cover
        viewModel.infoImage.observe(viewLifecycleOwner, Observer {
            it?.run {
                createInfoAlert(requireContext(), "Dettagli Libro",
                    R.drawable.ic_baseline_info_24,
                    "Richiesta: $idRequest" +
                            "\nISBN: $isbn" +
                            "\nService: $service" +
                            "\nTitolo: $title" +
                            "\nAutori: $author" +
                            "\nAnno di pubblicazione: $publicationYear")
                viewModel.doneShowDetailsBook()
            }
        })

        listCoverSent.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL,
            false)

        listCoverSent.adapter = adapter

    }

    override fun onQueryTextSubmit(query: String?): Boolean { return false }

    /**
     * In base alla ricerca effettuata, verranno identificati la lista di cover che combacia con
     * ciò che è stato richiesto, prima per id Richiesta e successivamente per tutti gli altri campi.
     * Tali risultati verranno poi inseriti in una lista che aggiornerà la RecyclerView
     */
    override fun onQueryTextChange(newText: String?): Boolean {
        viewModel.infoListImage.observe(viewLifecycleOwner, Observer {
            it?.let { listBook ->
                newText?.let { input ->
                    val inputSearch = input.toLowerCase(Locale.ROOT)

                    val newList = arrayListOf<InfoBooksTable>()

                    for(elem in listBook) {
                        if (elem.idRequest.toString().contains(inputSearch)) {
                            newList.add(elem)
                        } else if (elem.author.toLowerCase(Locale.ROOT).contains(inputSearch) ||
                            elem.title.toLowerCase(Locale.ROOT).contains(inputSearch) ||
                            elem.isbn.contains(inputSearch) ||
                            elem.service.toLowerCase(Locale.ROOT).contains(inputSearch) ||
                            elem.publicationYear.contains(inputSearch)) {

                            newList.add(elem)

                        }
                    }

                    adapter.updateList(newList)

                }
            }
        })

        return true
    }

    override fun onResume() {
        super.onResume()

        // Blocca l'orientamento dell'Activity in Portrait
        requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    }

    override fun onPause() {
        super.onPause()

        // Setta l'orientamento dell'Activity sia in Portrait che Landscape
        requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
    }

}
