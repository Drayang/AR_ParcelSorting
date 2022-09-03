package com.example.ar_parcelsorting

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.ar_parcelsorting.db.ParcelDao
import java.lang.IllegalArgumentException

/* This is like ViewModel Provider similar to the ViewModelDemo project that we
* call something like this
* "viewModel = ViewModelProvider(this).get(MainActivityViewModel::class.java)"
* in the "mainactivity.kt"
* THis is a can copy paste class
* */

class ParcelViewModelFactory(
    private val dao: ParcelDao
):ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if(modelClass.isAssignableFrom(ParcelViewModel::class.java)){
            return ParcelViewModel(dao) as T
        }
        throw IllegalArgumentException("Unknown View Model Class")
    }
}