package it.simonetugnetti.coversuggestionsengine.cover

import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.*
import androidx.fragment.app.Fragment
import android.view.inputmethod.EditorInfo
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import it.simonetugnetti.coversuggestionsengine.R

import it.simonetugnetti.coversuggestionsengine.databinding.CoverFragmentBinding
import it.simonetugnetti.coversuggestionsengine.util.createInfoAlert
import it.simonetugnetti.coversuggestionsengine.util.createToast
import it.simonetugnetti.coversuggestionsengine.util.isValidISBN

/**
 * Cover Fragment
 * Fragment principale, vengono ricercate le cover desiderate tramite ISBN, titolo o autore
 * @author Simone Tugnetti
 */
class CoverFragment : Fragment() {

    private lateinit var viewModel: CoverViewModel
    private lateinit var binding: CoverFragmentBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        // Per poter gestire gli elementi dell'option menu da questo fragment
        setHasOptionsMenu(true)
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle? ): View? {

        /*
        Data Binding -> Utilizzato per poter collegare direttamente il codice viewModel con il
        layout del suddetto fragment
         */
        binding = DataBindingUtil
            .inflate(inflater, R.layout.cover_fragment, container, false)

        // Per utilizzare un viewModel specifico
        viewModel = ViewModelProvider(this).get(CoverViewModel::class.java)

        binding.viewModel = viewModel
        binding.lifecycleOwner = this

        // Nel caso venga rilevato un ISBN tramite scannerizzazione, verrà inserito nel testo di
        // ricerca
        arguments?.let {
            val argument = CoverFragmentArgs.fromBundle(it)

            argument.isbnBarcode?.let{ isbn ->
                binding.infoISBN.setText(isbn)
            }

        }

        binding.barcode.setOnClickListener {
            this.findNavController().navigate(
                CoverFragmentDirections.actionCoverFragmentToBarcodeFragment()
            )
        }

        manageFilterSearchText()

        onTextChange()

        manageRecyclerViewItems()

        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)

        // Viene reso visibile il filtro per la ricerca solo in questo Fragment
        menu.findItem(R.id.filterSearchItem).isVisible = true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // In base al tipo selezionato, verrà cambiato il filtro di ricerca
        when (item.itemId) {
            R.id.filterISBNSubItem -> viewModel.changeHintSearchBook("Inserisci ISBN")
            R.id.filterNameSubItem -> viewModel.changeHintSearchBook("Inserisci Nome Libro")
            R.id.filterAuthorSubItem -> viewModel.changeHintSearchBook("Inserisci Autore")
        }

        return super.onOptionsItemSelected(item)
    }

    /**
     * Menage Filter Search Text
     * Funzione che gestisce il tipo del dato da ricercare
     */
    private fun manageFilterSearchText() {
        viewModel.hintSearchBook.observe(viewLifecycleOwner, Observer { hint ->
            binding.sendRequest.setOnClickListener {

                // Chiude la tastiera digitale
                binding.infoISBN.onEditorAction(EditorInfo.IME_ACTION_DONE)
                val text = binding.infoISBN.text.toString()

                // Nel caso in cui venga inserito un ISBN, questo verrà prima verificato, altrimenti
                // sarà fatta direttamente la ricerca dell'immagine
                if (hint == "Inserisci ISBN") {
                    startCheckISBN(text)
                } else {
                    startGetImage(text, true)
                }
            }

            // Viene cambiato il tipo di inserimento
            binding.infoISBN.inputType = if (hint == "Inserisci ISBN") {
                InputType.TYPE_CLASS_NUMBER
            } else {
                InputType.TYPE_CLASS_TEXT
            }

            viewModel.setActualSearchValue("")
            viewModel.enableGetImageTrue()

        })

    }

    /**
     * Menage RecyclerView Items
     * Funzione che gestisce la RecyclerView per la visualizzazione delle Cover
     */
    private fun manageRecyclerViewItems() {

        // Adapter per la gestione degli Item nella View, nel caso in cui venga premuta un'immagine,
        // i dati inerenti a tale immagine verranno salvati nel viewModel
        val adapter = CoverAdapter(ImageClickListener {
            viewModel.setInfoImage(it)
        })

        // Salva la lista di immagini nell'adapter
        viewModel.images.observe(viewLifecycleOwner, Observer {
            it?.let {
                adapter.data = it
            }
        })

        // Quando viene premuta l'immagine, verrà avviato il fragment successivo passando
        // tale immagine
        viewModel.infoImage.observe(viewLifecycleOwner, Observer { sourceImage ->
            sourceImage?.let {
                this.findNavController().navigate(
                    CoverFragmentDirections.actionCoverFragmentToManageImageFragment(sourceImage)
                )
                viewModel.navigatedToImage()
            }
        })

        // Nel caso non ci siano risultati
        viewModel.alertZeroResult.observe(viewLifecycleOwner, Observer {
            if (it) {
                createInfoAlert(requireContext(), "Attenzione", R.drawable.ic_baseline_warning_24,
                    "Nessun risultato trovato!")
                viewModel.notVisibleAlertZeroResult()
            }
        })

        binding.listCover.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)

        binding.listCover.adapter = adapter

    }

    /**
     * on Text Change
     * Funzione che gestisce l'attivazione del pulsante di ricerca in base al contenuto del testo.
     * Se è un valore già ricercato in precedenza, non sarà possibile rieseguire la ricerca
     */
    private fun onTextChange() {
        viewModel.run {
            actualSearchValue.observe(viewLifecycleOwner, Observer {
                binding.infoISBN.addTextChangedListener(object : TextWatcher {
                    override fun afterTextChanged(s: Editable?) { }
                    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int,
                                                   after: Int) { }

                    override fun onTextChanged(text: CharSequence?, start: Int, before: Int,
                                               count: Int) {
                        if (text.toString() != it) {
                            enableGetImageTrue()
                        } else {
                            imageUtil.observe(viewLifecycleOwner, Observer { message ->
                                if (message != "connection_error" || message == "results_ok") {
                                    enableGetImageFalse()
                                }
                            })
                        }
                    }

                })
            })
        }
    }

    /**
     * Start Check ISBN
     * Funzione che controlla se l'ISBN passato risulta valido e corretto per eseguirne la ricerca
     * @param isbn ISBN da controllare e, se valido, da utilizzare per ricercare l'immagine
     */
    private fun startCheckISBN(isbn: String) {
        val codeISBN = isbn.replace("-","")
        val notLong = codeISBN.toLongOrNull() != null

        if (notLong && isValidISBN(codeISBN)) {
            startGetImage(codeISBN, false)
        } else {
            createToast(requireContext(), "Invalid ISBN Code")
        }
    }

    /**
     * Start Get Image
     * Funzione che inizializza la ricerca delle immagini tramite il viewModel.
     * Nel caso in cui la ricerca avviene tramite ISBN, verrà eseguita la ricerca con GoodReads con
     * eventualmente un fallback, altrimenti solo tramite Google, dato che GoodReads non restituisce
     * tutti i dati necessari usando tale ricerca
     * @param text valore utilizzato per la ricerca
     * @param whichService quale servizio di ricerca deve essere avviato
     */
    private fun startGetImage(text: String, whichService: Boolean) {
        viewModel.run {
            if (whichService) {
                getImageGoogle(text)
            } else {
                getImageGoodreads(text, getString(R.string.goodreads_api_key))
            }
            setActualSearchValue(text)
            imageUtil.observe(viewLifecycleOwner, Observer {
                if (it != "connection_error" || it == "results_ok") {
                    enableGetImageFalse()
                } else {
                    enableGetImageTrue()
                }
            })
        }
    }

}
