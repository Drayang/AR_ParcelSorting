package com.example.ar_parcelsorting

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.ar_parcelsorting.databinding.FragmentMenuBinding
import com.example.ar_parcelsorting.databinding.FragmentTrackingBinding


class TrackingFragment : Fragment() {
    private lateinit var binding : FragmentTrackingBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentTrackingBinding.inflate(inflater,container,false)



        // Inflate the layout for this fragment
        return binding.root
    }

    companion object {

    }
}