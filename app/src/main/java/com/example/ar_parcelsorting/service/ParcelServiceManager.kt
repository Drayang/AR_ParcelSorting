package com.example.ar_parcelsorting.service

import android.util.Log
import com.example.ar_parcelsorting.data.Parcel
import com.example.ar_parcelsorting.data.ParcelTesting
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

//Implement the actual Service which we will directly invoke
// Do all service function here
class ParcelServiceManager {
    // TIPS: Match the function name same as the service (i.e ParcelService Interface) function name
    var retrofitService: ParcelService = ParcelServiceBuilder
        .getRetrofitInstance()
        .create(ParcelService::class.java)

    // The function name we should use in Fragment - e.g ParcelServiceManager.postBarcode
    fun postBarcode(parcelData: Parcel, onResult: (Parcel?) -> Unit){
        //The method is called from APIService interface
        retrofitService.postBarcode(parcelData).enqueue(
            object : Callback<Parcel> {
                override fun onFailure(call: Call<Parcel>, t: Throwable) {
                    Log.i("My", t.toString())
                    onResult(null)
                }
                //Response back Barcode
                override fun onResponse(call: Call<Parcel>, response: Response<Parcel>) {
                    val parcelDataSent = response.body()
                    onResult(parcelDataSent)
                }

            }
        )
    }

    /** FOR TESTING ONLY*/
    suspend fun getAlbum(id: Int): Response<ParcelTesting> {
        return retrofitService.getAlbum(3)
    }
}