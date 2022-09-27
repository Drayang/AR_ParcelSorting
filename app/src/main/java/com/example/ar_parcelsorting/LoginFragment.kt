package com.example.ar_parcelsorting

import android.os.Bundle
import android.text.TextUtils
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.findNavController
import com.example.ar_parcelsorting.databinding.FragmentLoginBinding


class LoginFragment : Fragment() {

    private lateinit var binding : FragmentLoginBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        // Show status bar
        requireActivity().window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE

        binding = FragmentLoginBinding.inflate(inflater,container,false)

        binding.apply {
            btnLogin.setOnClickListener {
                if (TextUtils.isEmpty(etUserId.text.toString())) {
                    etUserId.error = "This field cannot be empty!"
                }
                else if (TextUtils.isEmpty(etPassword.text.toString())) {
                    etPassword.error = "This field cannot be empty!"
                }
                else{
                    employeename = etUserId.text.toString()
                    Toast.makeText(activity, "Welcome $employeename.", Toast.LENGTH_SHORT).show() //show the text scanned
                    it.findNavController().navigate(R.id.action_loginFragment2_to_menuFragment)
                }
            }


            // Navigate to scanner
            btnScan.setOnClickListener {
                it.findNavController().navigate(R.id.action_loginFragment_to_idScannerFragment)
            }

        }

        return binding.root
    }

    companion object{
        lateinit var employeename: String //To store the employee name
        var markerScannedTracking = false  // To tell whether the marker have vbeen scanned before
        const val TAG = "My"  // For Log.i tag

    }


}