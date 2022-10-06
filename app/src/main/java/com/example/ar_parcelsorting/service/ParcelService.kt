package com.example.ar_parcelsorting.service

import com.example.ar_parcelsorting.data.Parcel
import retrofit2.Call
import retrofit2.http.*

interface ParcelService {
    @Headers("Content-Type: application/json")
    @POST("/barcode") //route we set in NodeRed
    fun postBarcode(@Body parcel: Parcel): Call<Parcel>

}