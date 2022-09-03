package com.example.ar_parcelsorting.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName


/** Duplicate of Parcel data class, specifically made for data storing using ROOM jetpack
 *  Can check the stored data under ........
 * */
@Entity(tableName = "parcel_data_table")
data class ParcelDB(

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "parcel_id")
    var id: Int?,

    @ColumnInfo(name = "parcel_code") var parcelCode: String?,
    @ColumnInfo(name = "parcel_X") var parcelX: Int?,
    @ColumnInfo(name = "parcel_Y") var parcelY: Int?,
    @ColumnInfo(name = "parcel_Z") var parcelZ: Int?,
    @ColumnInfo(name = "parcel_Length") var parcelLength: Int?,
    @ColumnInfo(name = "parcel_Height") var parcelHeight: Int?,
    @ColumnInfo(name = "parcel_Width") var parcelWidth: Int?,

    )
