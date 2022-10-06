package com.example.ar_parcelsorting.service

import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class ParcelServiceBuilder {
    companion object {
        /** For testing purpose only*/
//         val BASE_URL: String = "https://jsonplaceholder.typicode.com"

        /** To access localhost on my machine when using Android Emulator */
//        val BASE_URL = "http://10.0.2.2:1880"

        /** To access localhost on my machine when using Android Device -iponfig command: IPV4 address */
//        val BASE_URL = "http://10.156.50.255:1880/"
//        val BASE_URL = "http://192.168.0.194:1880/"
        val BASE_URL = "http://10.156.50.255:1880/"


        /*
        * Retrofit library created on top of the OKHttp library. Retrofit uses classes of the
        * OKHttp to perform network operation
        * HttpLoggingInterceptor is a feature belong to okhttp library which shows lot of network
        * operations that happened in our application. Useful to find what happen behind and use to
        * debug any issue
        * */
        val interceptor = HttpLoggingInterceptor().apply {
            /*
            * This body level look(?) request and response lines and their respective header and body
            * of the network operation. We can decide to look ".BODY" or ".HEADER"
            * Basically we can see what our HTTP request have done since it shows us Header and http status
            */
            this.level = HttpLoggingInterceptor.Level.BODY
        }
        //Create OkHttpClient instance
        val client = OkHttpClient.Builder().apply {
            this.addInterceptor(interceptor)
                .connectTimeout(30, TimeUnit.SECONDS) //time allow for the retrofit instance to connect to the server (default 10sec)
                .readTimeout(20, TimeUnit.SECONDS) //max. time gap between arrival of two data packet when waiting for the server's response
                .writeTimeout(25, TimeUnit.SECONDS)// max. time gap to send data to the server
        }.build()
        // To get retrofit instance class
        fun getRetrofitInstance(): Retrofit {
            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create(GsonBuilder().create())) //convert json to kotlin using GsonConverterFactory
                .build()
        }

    }
}