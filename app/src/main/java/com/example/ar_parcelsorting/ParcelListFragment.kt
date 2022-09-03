package com.example.ar_parcelsorting

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.ar_parcelsorting.data.Parcel
import com.example.ar_parcelsorting.databinding.FragmentLoginBinding
import com.example.ar_parcelsorting.databinding.FragmentParcelListBinding
import com.example.ar_parcelsorting.db.ParcelDB
import com.example.ar_parcelsorting.db.ParcelDatabase


class ParcelListFragment : Fragment() {
    private lateinit var binding : FragmentParcelListBinding

//    private lateinit var parcelList : ArrayList<Parcel>

//    val parcelList = listOf<Parcel>(
//        Parcel("Mango",1,2,3,4,5,6),
//        Parcel("Durian",1,2,3,4,5,6),
//        Parcel("Apple", 1,2,3,4,5,6),
//        Parcel("Banana",1,2,3,4,5,6),
//    )

    //Recycle view [NOT NECESSARY ANYMORE]
    private lateinit var parcelRecyclerView: RecyclerView
    private lateinit var adapter:ParcelRecycleViewAdapter

    // Set up view model
    private lateinit var viewModel: ParcelViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {



        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentParcelListBinding.inflate(inflater,container,false)

        // Update the employee name on the top right corner
        binding.tvUsername.setText(LoginFragment.employeename)

        // Initialise recycler view
        binding.rvParcelList.layoutManager = LinearLayoutManager(this.activity)
        binding.rvParcelList.adapter = ParcelRecycleViewAdapter(){
                selectedItem: ParcelDB -> listItemClicked(selectedItem)
        }

        // Create ROOM instance - dao, factory and viewModel
        initROOM()

        // Display the stored parcel data
        displayParcelList(binding.rvParcelList.adapter as ParcelRecycleViewAdapter)

        // Navigate to Home page
        binding.btnHome.setOnClickListener {
            it.findNavController().navigate(R.id.action_parcelListFragment_to_loginFragment)
        }

        // Inflate the layout for this fragment
        return binding.root
    }

    // Initialise ROOM instance
    private fun initROOM(){
        val dao = ParcelDatabase.getInstance(requireActivity().applicationContext).parcelDao()
        val factory = ParcelViewModelFactory(dao)
        // initialise viewModel instance
        viewModel = ViewModelProvider(this, factory)[ParcelViewModel::class.java]
    }

    // To display the parcel list, pass in adapter as argument
    private fun displayParcelList(adapter: ParcelRecycleViewAdapter) {
        viewModel.parcels.observe(requireActivity()) {
            Log.i("My","$it")
            adapter.setList(it)
            adapter.notifyDataSetChanged()
        }
    }

    // TODO: click on the parcel will jump to trackingFragment to check the parcel location
    // Function to call when clicking the selected parcel
    private fun listItemClicked(selectedParcel : ParcelDB){
        Toast.makeText(
            this.activity,
            "Parcel name is ${selectedParcel.parcelCode}",
            Toast.LENGTH_SHORT
        ).show()
    }


}