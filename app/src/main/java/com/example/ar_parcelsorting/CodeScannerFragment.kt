package com.example.ar_parcelsorting

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.view.isInvisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import com.budiyev.android.codescanner.AutoFocusMode
import com.budiyev.android.codescanner.CodeScanner
import com.budiyev.android.codescanner.CodeScannerView
import com.budiyev.android.codescanner.DecodeCallback
import com.budiyev.android.codescanner.ErrorCallback
import com.budiyev.android.codescanner.ScanMode
import com.example.ar_parcelsorting.data.Parcel
import com.example.ar_parcelsorting.databinding.FragmentCodeScannerBinding
import com.example.ar_parcelsorting.db.ParcelDB
import com.example.ar_parcelsorting.db.ParcelDatabase
import com.example.ar_parcelsorting.service.ParcelService
import com.example.ar_parcelsorting.service.ParcelServiceBuilder
import com.example.ar_parcelsorting.service.ParcelServiceManager

/**
Similar to the idScannerFragment.kt
 */
class CodeScannerFragment : Fragment() {

    private lateinit var binding : FragmentCodeScannerBinding
    private lateinit var  codeScanner: CodeScanner
    private lateinit var scannerView: CodeScannerView

    private lateinit var parcel : Parcel
    private lateinit var parcelCode : String

    //Retrofit setup
    private lateinit var retrofitService: ParcelService // NOT NECESSARY ANYMORE but still in use
    private val parcelServiceManager = ParcelServiceManager()

    // Set up view model
    private lateinit var viewModel: ParcelViewModel

    private var parcelHasBeenRecorded : Boolean = false
    private var firstTimeScanParcel : Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentCodeScannerBinding.inflate(inflater,container,false)

        // Hide Status Bar
        requireActivity().window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN

        // Change the frame shape to barcode size
        binding.ibtnHorizontal.setOnClickListener {
            scannerView.setFrameAspectRatio(4F,1F)
            binding.ibtnHorizontal.isInvisible = true
            binding.ibtnSquare.isInvisible = false
        }
        // Change the frame shape to original qrcode shape
        binding.ibtnSquare.setOnClickListener {
            scannerView.setFrameAspectRatio(1F,1F)
            binding.ibtnHorizontal.isInvisible = false
            binding.ibtnSquare.isInvisible = true
            // TODO: remove this after finishing project
            // postBarcode("SPXMY0011", it)
        }

        initRetrofitInstance() // Create retrofit instance
        initROOM() // Create ROOM instance - dao, factory and viewModel

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setupPermission() //Setup permission
        initCodeScanner(view) // Initialise the code scanner
    }

    // Create retrofit instance
    private fun initRetrofitInstance(){
        retrofitService = ParcelServiceBuilder
            .getRetrofitInstance()
            .create(ParcelService::class.java)
    }

    // Initialise ROOM instance
    private fun initROOM(){
        val dao = ParcelDatabase.getInstance(requireActivity().applicationContext).parcelDao()
        val factory = ParcelViewModelFactory(dao)
        // initialise viewModel instance
        viewModel = ViewModelProvider(this, factory)[ParcelViewModel::class.java]
    }


    // Post request - send the scanned barcode to MongoDB in order to retrieve corresponding parcel information
    private fun postBarcode(parcel_code:String = "SPXMY0001" , view: View) {
        val parcelBarcode = Parcel(
            parcelCode = parcel_code
        )

        parcelServiceManager.postBarcode(parcelBarcode) { it ->
            if (it?.parcelCode != null) {
                parcel = it
                parcelCode = it.parcelCode


                // Data to transfer between fragment
                val bundle = bundleOf(
                    "parcelCode" to it.parcelCode,
                    "parcelX" to it.parcelX.toString(),
                    "parcelY" to it.parcelY.toString(),
                    "parcelZ" to it.parcelZ.toString(),
                    "parcelLength" to it.parcelLength.toString(),
                    "parcelWidth" to it.parcelWidth.toString(),
                    "parcelHeight" to it.parcelHeight.toString(),
                    "parcelOrientation" to it.parcelOrientation.toString(),
                )


                /** To check parcel has been recorded inside the database before or not */
                viewModel.parcels.observe(requireActivity()) { it ->
                    it?.forEach {
                        if (parcelCode == it.parcelCode){ //Check the scanned barcode map with any existing barcode in the database
                            parcelHasBeenRecorded = true
                            Log.i("My", "Enter parcelHasBeenRecorded loop")
                        }
                    }
                    if (parcelHasBeenRecorded){ // Parcel recorded before
                        Toast.makeText(activity,"Navigate to AR View.", Toast.LENGTH_SHORT).show()
                        Log.i("My", "Duplicate parcel")
                        parcelHasBeenRecorded = false
                        Handler(Looper.getMainLooper()).postDelayed({
                            view.findNavController().navigate(R.id.action_codeScannerFragment_to_ARFragment,bundle)
                        }, 2000)

                    }
                    else{ //New parcel that do not store in database yet
                        Log.i("My", "Enter else loop")
                        saveParcelData(parcel) //save parcel information to database using ROOM
                        Toast.makeText(activity,"Parcel Recorded Successfully", Toast.LENGTH_SHORT).show()

                        /** A bug occur when we record new parcel into database. IllegalStateExecption will be triggereD
                        However if we store it-> crash-> rescan and trigger duplicate parcel loop again, then everything work fine as usual*/
                        firstTimeScanParcel = true // Solution : To call the postBarcode() function again in order to navigate to Ar Fragment
                    }
                }

            } else {
                Log.e("My", it.toString())
                Toast.makeText(activity, "Unrecognized Parcel! ", Toast.LENGTH_SHORT).show()
            }
        }

    }

    /** Becareful with Parcel and ParcelDB data class
     * Parcel : data class for Retrofit API
     * ParcelDB : data class for ROOM to store parcel information
     *
     * parcelResponse - response receive from Node-RED, which is the output of parcelServiceManager.postBarcode()
     * */
    private fun saveParcelData( parcelResponse : Parcel) {
        Log.i("My","Enter saveParcelData loop")
        /* Save parcel data*/
        viewModel.insertParcel(
            ParcelDB(
                null,
                parcelResponse.parcelCode,
                parcelResponse.parcelX,
                parcelResponse.parcelY,
                parcelResponse.parcelZ,
                parcelResponse.parcelLength,
                parcelResponse.parcelHeight,
                parcelResponse.parcelWidth,
                parcelResponse.parcelOrientation,
            )
        )
    }

    // Initialise Code Scannner
    private fun initCodeScanner(view:View){
        scannerView = view.findViewById(R.id.scanner_view)
        val activity = requireActivity()

        codeScanner = CodeScanner(activity, scannerView)
        codeScanner.apply{
            camera = CodeScanner.CAMERA_BACK
            formats = CodeScanner.ALL_FORMATS

            autoFocusMode = AutoFocusMode.SAFE
            scanMode = ScanMode.SINGLE
            isAutoFocusEnabled = true
            isFlashEnabled =false

            // When barcode is detected
            decodeCallback = DecodeCallback {
                activity.runOnUiThread {
                    postBarcode(it.text,view) // Do post request to retrieve parcel information from MongoDB
                    Handler(Looper.getMainLooper()).postDelayed({
                        scannerView.performClick() // To refresh the fragment for next scanning
                    }, 2000)

                }
            }
            //if something go wrong
            errorCallback = ErrorCallback {
                activity.runOnUiThread {
                    Log.e("My", "Camera intialization error: ${it.message}")
                }
            }
        }
        // Show the icon such as flash light button
        scannerView.setOnClickListener {
            codeScanner.startPreview()
        }
    }


    override fun onResume() {
        super.onResume()
        codeScanner.startPreview()
    }

    override fun onPause() {
        codeScanner.releaseResources()
        super.onPause()
    }

    private fun setupPermission(){
        val activity = requireActivity()
        val permission = ContextCompat.checkSelfPermission(activity, Manifest.permission.CAMERA) //this refer to the manifest file de camera

        if (permission != PackageManager.PERMISSION_GRANTED){
            makeRequest()
        }
    }

    //Pop up message to ask for request
    private fun makeRequest(){
        val activity = requireActivity()
        ActivityCompat.requestPermissions(activity, arrayOf(Manifest.permission.CAMERA),
            IdScannerFragment.CAMERA_REQUEST_CODE
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode){
            IdScannerFragment.CAMERA_REQUEST_CODE -> {
                if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED){
                    Toast.makeText(activity,"You need the camera permission to be able to use this app!", Toast.LENGTH_SHORT).show()
                }
                else{
                    //successful
                }
            }

        }
    }

}