package com.example.ar_parcelsorting

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.ViewModelProvider
import com.example.ar_parcelsorting.databinding.ActivityMainBinding
import com.example.ar_parcelsorting.db.ParcelDB
import com.example.ar_parcelsorting.db.ParcelDao
import com.example.ar_parcelsorting.db.ParcelDatabase

class MainActivity : AppCompatActivity() {

    //ViewBinding
    private lateinit var binding: ActivityMainBinding
    private lateinit var dao: ParcelDao
    private lateinit var factory: ParcelViewModelFactory

    // Set up view model
    private lateinit var viewModel: ParcelViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initROOM()
//        deleteParcelsList()
        // TODO: to delete the data from database
//        dao.deleteAllData() // Not working

    }

    override fun onStop() {
        super.onStop()
        Log.i("My","Stopping app")
        dao.deleteAllData()
    }

    private fun initROOM(){
        dao = ParcelDatabase.getInstance(application).parcelDao()
        factory = ParcelViewModelFactory(dao)
        // initialise viewModel instance
        viewModel = ViewModelProvider(this, factory)[ParcelViewModel::class.java]
    }

    private fun deleteParcelsList() {
        viewModel.parcels.observe(this) {
            it?.forEach {
                Log.i("My", "${it}")
//                viewModel.deleteParcel(
//                    ParcelDB(
//                        it.id,
//                        it.parcelCode,
//                        it.parcelX,
//                        it.parcelY,
//                        it.parcelZ,
//                        it.parcelLength,
//                        it.parcelHeight,
//                        it.parcelWidth
//                    )
//                )
            }
        }
    }

}



