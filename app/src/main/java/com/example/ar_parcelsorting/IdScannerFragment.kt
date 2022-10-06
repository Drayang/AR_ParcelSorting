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
import androidx.core.view.isInvisible
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.budiyev.android.codescanner.*
import com.example.ar_parcelsorting.databinding.FragmentIdScannerBinding

class IdScannerFragment : Fragment() {

    private lateinit var binding : FragmentIdScannerBinding
    private lateinit var  codeScanner: CodeScanner
    private lateinit var scannerView: CodeScannerView



    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {

        binding = FragmentIdScannerBinding.inflate(inflater,container,false)

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
        }

        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        setupPermission() //Setup permission
        initCodeScanner(view) // Initialise the code scanner
    }


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

            // What barcode detect
            decodeCallback = DecodeCallback {
                activity.runOnUiThread {

                    LoginFragment.employeename = it.text //update the employee name with scanned data
                    Toast.makeText(activity, "Welcome ${it.text}.", Toast.LENGTH_SHORT).show() //show the text scanned

                    Handler(Looper.getMainLooper()).postDelayed({
                        // Navigate to menu page
                        view.findNavController().navigate(R.id.action_idScannerFragment_to_menuFragment)
                    }, 500)
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
            CAMERA_REQUEST_CODE)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode){
            CAMERA_REQUEST_CODE -> {
                if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED){
                    Toast.makeText(activity,"You need the camera permission to be able to use this app!", Toast.LENGTH_SHORT).show()
                }
                else{
                    //successful
                }
            }

        }
    }

    companion object{
        const val CAMERA_REQUEST_CODE = 101

    }

}