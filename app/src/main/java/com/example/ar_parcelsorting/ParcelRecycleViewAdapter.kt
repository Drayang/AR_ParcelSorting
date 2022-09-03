package com.example.ar_parcelsorting

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.ar_parcelsorting.db.ParcelDB

class ParcelRecycleViewAdapter(
    private val clickListener: (ParcelDB) ->Unit
    ):RecyclerView.Adapter<ParcelViewHolder>() {

    private val parcelList = ArrayList<ParcelDB>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ParcelViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        //then use the LayoutInflater to input the layout xml file
        val listItem = layoutInflater.inflate(R.layout.list_parcel, parent, false)
        return ParcelViewHolder(listItem)
    }

    override fun onBindViewHolder(holder: ParcelViewHolder, position: Int) {
        val parcel = parcelList[position]
        /* In initial example fruit is the string so can directly put*/
        // holder.myTextView.text = fruit

        /* THen we use data class "Fruit" to display the data*/
        //holder.myTextView.text = fruit.name

        /* But actually for best practice, the assign name to TextView.text and any other function
        * should be done in the ViewHolder class
        */
        holder.bind(parcel,clickListener)
    }

    override fun getItemCount(): Int {
        return parcelList.size
    }


    fun setList(parcels:List<ParcelDB>){
        parcelList.clear()
        parcelList.addAll(parcels)
    }

}


class ParcelViewHolder(private val view: View) : RecyclerView.ViewHolder(view){
    @SuppressLint("SetTextI18n")
    fun bind(parcel:ParcelDB,
             clickListener: (ParcelDB) ->Unit ) //this is a function lambda expression, return nothing(Unit)
    {
        val tvParcelCode = view.findViewById<TextView>(R.id.tvParcelCode)
        val tvParcelSize = view.findViewById<TextView>(R.id.tvParcelSize)
        val tvParcelPos = view.findViewById<TextView>(R.id.tvParcelPos)
        tvParcelCode.text = parcel.parcelCode
        tvParcelSize.text = "Size: [${parcel.parcelLength.toString()}," +
                "${parcel.parcelWidth.toString()}," +
                "${parcel.parcelHeight}] "
        tvParcelPos.text = "Position: [X:${parcel.parcelX.toString()} " +
                "Y:${parcel.parcelY.toString()} " +
                "Z:${parcel.parcelZ.toString()}] "

        view.setOnClickListener{
            clickListener(parcel)
        }

    }
}