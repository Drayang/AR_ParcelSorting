package com.example.ar_parcelsorting.data

import io.github.sceneview.ar.node.PlacementMode

data class Model(
    val fileLocation: String,
    val scaleUnits: Float? = null,
    val placementMode: PlacementMode = PlacementMode.BEST_AVAILABLE.apply{keepRotation=true},
    val parcelCode: String = "SPXMY0011"
)
