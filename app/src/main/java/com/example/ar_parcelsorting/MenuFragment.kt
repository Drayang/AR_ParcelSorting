package com.example.ar_parcelsorting

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.liveData
import androidx.navigation.findNavController
import com.example.ar_parcelsorting.LoginFragment.Companion.employeename
import com.example.ar_parcelsorting.data.Parcel
import com.example.ar_parcelsorting.data.ParcelTesting
import com.example.ar_parcelsorting.databinding.FragmentLoginBinding
import com.example.ar_parcelsorting.databinding.FragmentMenuBinding
import com.example.ar_parcelsorting.service.ParcelService
import com.example.ar_parcelsorting.service.ParcelServiceBuilder
import com.example.ar_parcelsorting.service.ParcelServiceManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class MenuFragment : Fragment() {

    private lateinit var binding : FragmentMenuBinding

    // TODO: all code with "Testing" should later all move to "CodeSCannerFragment"

    /** FOR TESTING ONLY*/
    private lateinit var retrofitService: ParcelService // Not necessary anymore
    private val apiService = ParcelServiceManager() // Not necessary anymore


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMenuBinding.inflate(inflater,container,false)

        // Show status bar
        requireActivity().window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE

        // Update the employee name on the top right corner
        binding.tvUsername.setText(employeename)

        // Navigate to Parcel Record
        binding.btnRecord.setOnClickListener {
            it.findNavController().navigate(R.id.action_menuFragment_to_codeScannerFragment)
        }

        // Navigate to Parcel List Fragment
        binding.btnTrack.setOnClickListener {
            it.findNavController().navigate(R.id.action_menuFragment_to_parcelListFragment)
        }

        // Navigate to Home page
        binding.btnHome.setOnClickListener {
            it.findNavController().navigate(R.id.action_menuFragment_to_loginFragment)
        }

        // Inflate the layout for this fragment
        return binding.root
    }

    /** FOR TESTING ONLY*/
    /*
    private fun postBarcode() {
        val parcelBarcode = Parcel(
            parcelCode = "SPXMY0001",
            )
        apiService.postBarcode(parcelBarcode) {
            if (it?.parcelCode != null) {
                Log.i("My", it.toString())
            } else {
                Log.e("My", it.toString())
            }
        }
        /** FOR TESTING ONLY */
//        val pathResponse: LiveData<Response<ParcelTesting>> = liveData {
//            val response = apiService.getAlbum(3)
//            emit(response)
//        }
//
//        pathResponse.observe(this, Observer {
//            val title = it.body()?.title
//            Toast.makeText(
//                activity,
//                title.toString(),
//                Toast.LENGTH_LONG).show()
//        })
    }
     */
}