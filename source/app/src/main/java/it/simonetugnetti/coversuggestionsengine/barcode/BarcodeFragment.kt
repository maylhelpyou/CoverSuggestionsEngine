package it.simonetugnetti.coversuggestionsengine.barcode

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.media.ToneGenerator
import android.net.Uri
import android.os.*
import android.provider.Settings
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.SurfaceHolder
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.android.gms.vision.CameraSource
import com.google.android.gms.vision.Detector
import com.google.android.gms.vision.barcode.Barcode
import com.google.android.gms.vision.barcode.BarcodeDetector
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener

import it.simonetugnetti.coversuggestionsengine.R
import it.simonetugnetti.coversuggestionsengine.util.createToast
import it.simonetugnetti.coversuggestionsengine.util.isValidISBN
import kotlinx.android.synthetic.main.barcode_fragment.*

/**
 * Barcode Fragment
 * Classe utilizzata per scannerizzare i barcode ricavandone il relativo ISBN
 * @author Simone Tugnetti
 */
class BarcodeFragment : Fragment() {

    private lateinit var viewModel: BarcodeViewModel
    private lateinit var cameraSource: CameraSource
    private lateinit var tone: ToneGenerator
    private lateinit var vibrate: Vibrator

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.barcode_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        // Per utilizzare un viewModel specifico
        viewModel = ViewModelProvider(this).get(BarcodeViewModel::class.java)

        // Per poter eseguire un Bip acustico
        tone = ToneGenerator(AudioManager.STREAM_MUSIC, 100)

        // Per poter eseguire la vibrazione
        vibrate = requireContext().getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

        cameraView.visibility = View.GONE

        checkPermission()

        /*
        * Nel caso in cui l'ISBN letto da barcode sia valido, verrà passato a CoverFragment tramite
        * navigation controller
        */
        viewModel.isbnBarcode.observe(viewLifecycleOwner, Observer {
            it?.let {
                if (isValidISBN(it)) {
                    val arg = Bundle()

                    arg.putString("isbnBarcode", it)

                    this.findNavController().navigate(
                        R.id.action_barcodeFragment_to_coverFragment, arg, null
                    )

                    viewModel.doneNavigation()
                } else {
                    createToast(requireContext(), "Invalid ISBN Code")
                }
            }
        })

    }

    /**
     * Check Permission
     * Funzione utilizzata per gestire i permessi inerenti alla fotocamera con l'ausilio della
     * libreria Dexter.
     * Nel caso di permessi concessi, l'utente verrà indirizzato verso la prosecuzione del programma.
     * Nel caso di negati permanentemente, l'utente verrà indirizzato nelle impostazioni
     * dell'applicazione per la concessione manuale dei permessi
     */
    private fun checkPermission() {
        Dexter.withActivity(activity).withPermission(Manifest.permission.CAMERA)
            .withListener(object: PermissionListener {
                override fun onPermissionGranted(response: PermissionGrantedResponse?) {
                    cameraView.visibility = View.VISIBLE
                    initialDetection()
                }

                override fun onPermissionRationaleShouldBeShown(permission: PermissionRequest?,
                                                                token: PermissionToken?) {
                    token?.continuePermissionRequest()
                }

                override fun onPermissionDenied(response: PermissionDeniedResponse?) {
                    response?.let {
                        if (it.isPermanentlyDenied) {
                            val intent = Intent(
                                Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                Uri.fromParts("package", activity?.packageName, null))
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            startActivity(intent)
                        }
                    }
                }
            })
            .check()
    }

    /**
     * Initial Detection
     * Funzione utilizzata per visualizzare l'immagine visibile in fotocamera tramite una
     * SurfaceView, cioè una View che ragiona attraverso un Thread differente rispetto alla GUI
     */
    private fun initialDetection() {

        // Istanza per identificare il barcode
        val barcodeDetector = BarcodeDetector.Builder(context)
            .setBarcodeFormats(Barcode.ALL_FORMATS).build()

        // Istanza per abilitare la fotocamera posteriore
        cameraSource = CameraSource.Builder(context, barcodeDetector)
            .setRequestedPreviewSize(1920, 1080).setAutoFocusEnabled(true)
            .setFacing(CameraSource.CAMERA_FACING_BACK)
            .setRequestedFps(15.0f)
            .build()

        // SurfaceView Holder, utilizzabile per determinare le operazioni da eseguire quando il
        // thread viene creato o distrutto
        cameraView.holder.addCallback(object: SurfaceHolder.Callback {
            override fun surfaceChanged(p0: SurfaceHolder, p1: Int, p2: Int, p3: Int) { }

            override fun surfaceDestroyed(p0: SurfaceHolder) {
                cameraSource.stop()
            }

            @SuppressLint("MissingPermission")
            override fun surfaceCreated(p0: SurfaceHolder) {
                // Viene avviata la visualizzazione della fotocamera all'interno della view
                cameraSource.start(cameraView.holder)
            }
        })

        var preValue = ""

        // Viene creato un processo per identificare il barcode visualizzato tramite fotocamera
        barcodeDetector.setProcessor(object: Detector.Processor<Barcode> {
            override fun release() { }

            override fun receiveDetections(detect: Detector.Detections<Barcode>?) {
                val barcodes = detect?.detectedItems

                /*
                 Nel caso in cui sia stato trovato un barcode e non sia già stato scannerizzato in
                 precedenza, quel valore viene salvato per poi essere controllato, facendo partire
                 un bip acustico ed una vibrazione
                 */
                if (barcodes!!.size() != 0 && preValue != barcodes.valueAt(0).displayValue) {
                    Handler(Looper.getMainLooper()).post {
                        preValue = barcodes.valueAt(0).displayValue
                        viewModel.setISBNBarcode(preValue)
                        tone.startTone(ToneGenerator.TONE_CDMA_PIP, 150)
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            vibrate.vibrate(VibrationEffect.createOneShot(150,
                                VibrationEffect.DEFAULT_AMPLITUDE))
                        } else {
                            @Suppress("DEPRECATION")
                            vibrate.vibrate(150)
                        }
                    }
                }
            }
        })

    }

    override fun onPause() {
        super.onPause()
        cameraSource.release()
    }

    override fun onResume() {
        super.onResume()
        initialDetection()
    }

}
