package com.example.ar_parcelsorting.db

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface ParcelDao {

    @Insert
    suspend fun insertParcel(parcelDB: ParcelDB)

    @Update
    suspend fun updateParcel(parcelDB: ParcelDB)

    @Delete
    suspend fun deleteParcel(parcelDB: ParcelDB)

    // @Query -> need SQL command
    // "parcel_data_table" is the name we give for the "parcel" database data class
    @Query("SELECT * FROM parcel_data_table")
    fun getAllParcels(): LiveData<List<ParcelDB>>

    @Query("DELETE FROM parcel_data_table")
    fun deleteAllData(){
        Log.i("My","Executing deleteAllData() function")
    }
}