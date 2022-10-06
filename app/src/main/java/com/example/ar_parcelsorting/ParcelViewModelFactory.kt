package com.example.ar_parcelsorting

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.ar_parcelsorting.db.ParcelDao
import java.lang.IllegalArgumentException

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