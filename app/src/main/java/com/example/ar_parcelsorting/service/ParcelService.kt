package com.example.ar_parcelsorting.service

import com.example.ar_parcelsorting.data.Parcel
import com.example.ar_parcelsorting.data.ParcelTesting
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.*

interface ParcelService {
    @Headers("Content-Type: application/json")
    @POST("/barcode") //route we set in NodeRed
//    suspend fun sendBarcode(@Body parcel: Parcel): Response<Parcel>
    fun postBarcode(@Body parcel: Parcel): Call<Parcel>

    @GET("/albums/{id}")
    suspend fun getAlbum(@Path(value = "id")albumId:Int) : Response<ParcelTesting> //return a retrofit response object of type "AlbumsItems"


}