package com.example.ar_parcelsorting

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ar_parcelsorting.db.ParcelDB
import com.example.ar_parcelsorting.db.ParcelDao
import kotlinx.coroutines.launch

class ParcelViewModel(private val dao: ParcelDao): ViewModel() {

    val parcels = dao.getAllParcels()

    //Recap in viewModel we use viewModelScope instead of CoroutineScope
    fun insertParcel(parcelDB: ParcelDB)=viewModelScope.launch {
        dao.insertParcel(parcelDB)
    }

    fun updateParcel(parcelDB: ParcelDB)=viewModelScope.launch {
        dao.updateParcel(parcelDB)
    }

    fun deleteParcel(parcelDB: ParcelDB)=viewModelScope.launch {
        dao.deleteParcel(parcelDB)
    }

}