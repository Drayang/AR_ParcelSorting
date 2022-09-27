package com.example.ar_parcelsorting

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.findNavController
import com.example.ar_parcelsorting.LoginFragment.Companion.employeename
import com.example.ar_parcelsorting.databinding.FragmentMenuBinding


class MenuFragment : Fragment() {

    private lateinit var binding : FragmentMenuBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMenuBinding.inflate(inflater,container,false)

        // Show status bar
        requireActivity().window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE

        // Update the employee name on the top right corner
        binding.tvUsername.text = employeename

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


}