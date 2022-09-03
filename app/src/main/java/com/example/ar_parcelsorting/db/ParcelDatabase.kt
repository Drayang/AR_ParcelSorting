package com.example.ar_parcelsorting.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase


@Database(entities = [ParcelDB::class], version = 1, exportSchema = false)
abstract class ParcelDatabase : RoomDatabase() {
    /* One Dao interface need one abstract function*/
    abstract fun parcelDao(): ParcelDao

    // THis is a very common database code pattern, can just copy paste without understanding
    companion object {
        @Volatile
        private var INSTANCE: ParcelDatabase? = null
        fun getInstance(context: Context): ParcelDatabase {
            synchronized(this) {
                var instance = INSTANCE
                if (instance == null) {
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        ParcelDatabase::class.java,
                        "parcel_data_database"
                    ).build()
                }
                return instance
            }
        }
    }
}
