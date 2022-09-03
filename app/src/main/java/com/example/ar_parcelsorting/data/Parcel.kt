package com.example.ar_parcelsorting.data

import com.google.gson.annotations.SerializedName

/***
 * Add new property to data class need change the following file
 * 1. Parcel.kt
 * 2. ParcelDB.kt
 * 4. CodeScannerFragment.kt - SaveParcelData()
 * 3. node-red "Retrieve XYZ data for response" node
 *
 */

//SerializedName annotation tell the API the name of the property API only recognize the @SerializedName 's value
// val xxx -> xxx is the variable name we can access in AS
data class Parcel(
//    //@SerializedName("json key") val variable_name : type
    @SerializedName("barcode") val parcelCode: String?,
    @SerializedName("x") val parcelX: Int? = 0,
    @SerializedName("y") val parcelY: Int? = 0,
    @SerializedName("z") val parcelZ: Int? = 0,
    @SerializedName("length") val parcelLength: Int? = 0,
    @SerializedName("height") val parcelHeight: Int? = 0,
    @SerializedName("width") val parcelWidth: Int? = 0,

)



/*
TODO: This class is to test the RetrofitDemo - "Albumitems" class only, after testing should
 change all the "ParcelTesting" to "Parcel"
*/
data class ParcelTesting(
    /**
     * Testing only */
    @SerializedName("id")
    val id: Int,
    @SerializedName("title")
    val title: String,
    @SerializedName("userId")
    val userId: Int
)


