package it.simonetugnetti.coversuggestionsengine.manageImage

import android.Manifest
import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.provider.Settings
import android.view.*
import androidx.core.graphics.drawable.toBitmap
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import it.simonetugnetti.coversuggestionsengine.R
import it.simonetugnetti.coversuggestionsengine.databinding.ManageImageFragmentBinding
import it.simonetugnetti.coversuggestionsengine.sharedViewModels.ManageImageAndHistoryViewModel
import it.simonetugnetti.coversuggestionsengine.util.InfoBooks
import it.simonetugnetti.coversuggestionsengine.util.InitFirebase
import it.simonetugnetti.coversuggestionsengine.util.checkConnection
import it.simonetugnetti.coversuggestionsengine.util.createToast

/**
 * Manage Image Fragment
 * Fragment utilizzato per eseguire varie operazioni quali:
 * il salvataggio dell'immagine scelta in Cover Fragment all'interno della galleria del dispositivo,
 * scelta di un'immagine dal dispositivo,
 * suggerimento della copertina nel sistema Anobii con successivo salvataggio nella History sella
 * stessa.
 * @author Simone Tugnetti
 */
class ManageImageFragment : Fragment() {

    // Il ViewModel utilizzato in questo fragment è lo stesso utilizzato anche in HistoryFragment,
    // activityViewModels() consente la genstione del suddetto viewModel all'activity stessa, così
    // da renderla condivisibile
    private val viewModel: ManageImageAndHistoryViewModel by activityViewModels()
    private lateinit var binding: ManageImageFragmentBinding
    private lateinit var infoBook: InfoBooks
    private var sameImage: Boolean = false

    // Codice per il riconoscimento dell'operazione di scelta immagine dal dispositivo
    companion object {
        private const val IMAGE_PICK_CODE = 1000
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        setHasOptionsMenu(true)
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        // Data Binding -> Utilizzato per poter collegare direttamente il codice viewModel con il
        // layout del suddetto fragment
        binding = DataBindingUtil
            .inflate(inflater, R.layout.manage_image_fragment, container, false)

        // Informazioni della singola cover selezionata e passata da CoverFragment
        infoBook = ManageImageFragmentArgs.fromBundle(requireArguments()).infoBooks

        // Viene salvata l'immagine attuale
        viewModel.setImageUrl(infoBook.image)

        // Immagine Bitmap prelevata da CloudImageFragment, nel caso in cui non sia null, verrà
        // visualizzata all'interno di selectedImage
        ManageImageFragmentArgs.fromBundle(requireArguments())
            .imageCloudBitmap?.let { bitmap ->
                viewModel.setImageUrl("results_ok")
                binding.selectedImage.setImageBitmap(bitmap)
            }

        binding.viewModel = viewModel

        binding.lifecycleOwner = this

        // Prima di leggere o salvare in memoria, devono essere dati i permessi
        binding.saveDevice.setOnClickListener {
            checkPermission(true)
        }

        binding.readImage.setOnClickListener {
            checkPermission(false)
        }

        // Nel caso in cui sia presente una connesione ad internet, verrà verificato se la cover
        // attualmente visibile non sia già stata suggerita, altrimenti verrà inviata tra i
        // suggerimenti
        binding.suggestAnobii.setOnClickListener {
            if (checkConnection(requireContext())) {
                if (sameImage) {
                    createToast(requireContext(), "L'immagine è già stata suggerita")
                } else {
                    addImageToSuggest()
                    createToast(requireContext(), "L'immagine è stata suggerita")
                    sameImage = true
                }
            } else {
                createToast(requireContext(), "Connection Error")
            }
        }

        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        menu.findItem(R.id.cloudImageItem).isVisible = true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.getCloudImageSubItem -> {
                this.findNavController().navigate(
                    ManageImageFragmentDirections
                        .actionManageImageFragmentToCloudImageFragment(infoBook)
                )
            }
            R.id.uploadCloudImageSubItem -> {
                if (checkConnection(requireContext())) {
                    viewModel
                        .addImageCloud(binding.selectedImage.drawable.toBitmap(), requireContext())
                } else {
                    createToast(requireContext(), "Connection Error")
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    /**
     * Check Permission
     * Funzione utilizzata per gestire i permessi inerenti alla fotocamera con l'ausilio della
     * libreria Dexter.
     * Nel caso di permessi concessi, l'utente verrà indirizzato verso la prosecuzione del programma.
     * Nel caso di negati permanentemente, l'utente verrà indirizzato nelle impostazioni
     * dell'applicazione per la concessione manuale dei permessi
     * @param action Se eseguire la scrittura o la lettura di un'immagine sul dispositivo
     */
    private fun checkPermission(action: Boolean) {
        Dexter.withActivity(activity)
            .withPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE)
            .withListener(object: MultiplePermissionsListener{
                override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                    if (report!!.areAllPermissionsGranted()) {
                        if (action) {
                            saveImageToInternal()
                        } else {
                            pickImage()
                        }
                    }

                    for(i in 0 until report.deniedPermissionResponses.size) {
                        createToast(requireContext(), "Permission Denied " +
                                report.deniedPermissionResponses[i].permissionName)
                    }

                    if (report.isAnyPermissionPermanentlyDenied) {
                        val intent = Intent(
                            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                            Uri.fromParts("package", activity?.packageName, null))
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        startActivity(intent)
                    }

                }

                override fun onPermissionRationaleShouldBeShown(
                    permissions: MutableList<PermissionRequest>?, token: PermissionToken?) {
                    token?.continuePermissionRequest()
                }

            })
            .check()
    }

    /**
     * Add Image To Suggest
     * Funzione che verifica quale sia l'ultimo elemento inserito, aggiungendone
     * uno alla posizione successiva
     */
    private fun addImageToSuggest() {
        InitFirebase.mFirestore.collection("Books").get()
            .addOnSuccessListener { listDoc ->
                var i = 1

                if (!listDoc.isEmpty) {
                    if (listDoc.documents.isNotEmpty()) {
                        i = listDoc.documents.last().id.toInt() + 1
                    }
                }

                viewModel.addInfoImage(infoBook,
                    requireActivity().application,
                    binding.selectedImage.drawable.toBitmap(), i)

            }
    }

    /**
     * Save Image To Internal
     * Salva l'immagine selezionata in CoverFragment all'interno della galleria del dispositivo
     */
    private fun saveImageToInternal() {
        val externalStorageState = Environment.getExternalStorageState()

        if (externalStorageState == Environment.MEDIA_MOUNTED) {

            val resolver = requireContext().contentResolver
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, infoBook.title)
                put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    put(MediaStore.MediaColumns.RELATIVE_PATH, "DCIM/Books")
                }
            }

            val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

            resolver.openOutputStream(uri!!).use {
                val drawable = binding.selectedImage.drawable.toBitmap()
                drawable.compress(Bitmap.CompressFormat.JPEG, 100, it)
                it!!.flush()
                it.close()
                createToast(requireContext(), "Image save success")
            }

        } else {
            createToast(requireContext(), "Unable to access to storage")
        }

    }

    /**
     * Pick Image
     * Funzione che apre una finestra di scelta inerente a quale immagine si desidera prelevare
     * dalla galleria per essere poi visualizzata nella sezione apposita
     */
    private fun pickImage() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, IMAGE_PICK_CODE)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        // Nel caso in cui venga selezionata un'immagine, l'ImageView avrà la stessa come contenuto
        if (resultCode == Activity.RESULT_OK && requestCode == IMAGE_PICK_CODE) {
            binding.selectedImage.setImageURI(data?.data)
        }
    }

    override fun onPause() {
        super.onPause()

        // Per evitare di inviare più e più volte la stessa immagine attualmente visibile
        sameImage = false
    }

}
