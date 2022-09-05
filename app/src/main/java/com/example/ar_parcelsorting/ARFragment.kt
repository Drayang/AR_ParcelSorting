package com.example.ar_parcelsorting

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isGone
import androidx.core.view.isVisible
import com.example.ar_parcelsorting.databinding.FragmentARBinding
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import io.github.sceneview.ar.ArSceneView
import io.github.sceneview.ar.node.ArModelNode
import io.github.sceneview.ar.node.CursorNode
import io.github.sceneview.ar.node.PlacementMode
import io.github.sceneview.math.Position
import io.github.sceneview.utils.doOnApplyWindowInsets


class ARFragment : Fragment() {

    private lateinit var binding : FragmentARBinding

    private lateinit var sceneView: ArSceneView
    private lateinit var loadingView: View

    private lateinit var cursorNode: CursorNode  // The cursor
//    private lateinit var modelNode: ArModelNode // The 3D model

    private lateinit var btnAnchorModel: ExtendedFloatingActionButton
    private lateinit var btnNewModel: ExtendedFloatingActionButton

    data class Model(
        val fileLocation: String,
        val scaleUnits: Float? = null,
        val placementMode: PlacementMode = PlacementMode.BEST_AVAILABLE,
        val parcelCode: String = "SPXMY0001"
    )

    val models = listOf(
        Model("model/parcelSPXMY0001.glb",
            placementMode = PlacementMode.BEST_AVAILABLE.apply {
                keepRotation = true
            },
            scaleUnits = 0.5f,
            parcelCode = "SPXMY0001"

        ),
        Model("model/parcelSPXMY0055.glb",
            placementMode = PlacementMode.BEST_AVAILABLE.apply {
                keepRotation = true
            },
            scaleUnits = 0.5f,
            parcelCode = "SPXMY0055"
        ),
        Model("model/parcelSPXMY0060.glb",
            placementMode = PlacementMode.BEST_AVAILABLE.apply {
                keepRotation = true
            },
            scaleUnits = 0.5f,
            parcelCode = "SPXMY0060"
        ),
//        Model(
//            "https://storage.googleapis.com/ar-answers-in-search-models/static/Tiger/model.glb",
//            placementMode = PlacementMode.BEST_AVAILABLE.apply {
//                keepRotation = true
//            },
//            // Display the Tiger with a size of 3 m long
//            scaleUnits = 0.5f
//        ),
//        Model(
//            "https://sceneview.github.io/assets/models/DamagedHelmet.glb",
//            placementMode = PlacementMode.INSTANT,
//            scaleUnits = 0.5f
//        ),
//        Model(
//            "https://storage.googleapis.com/ar-answers-in-search-models/static/GiantPanda/model.glb",
//            placementMode = PlacementMode.PLANE_HORIZONTAL,
//            // Display the Tiger with a size of 1.5 m height
//            scaleUnits = 1.5f
//        ),
//        Model(
//            "https://sceneview.github.io/assets/models/Spoons.glb",
//            placementMode = PlacementMode.PLANE_HORIZONTAL_AND_VERTICAL,
//            // Keep original model size
//            scaleUnits = null
//        ),
//        Model(
//            "https://sceneview.github.io/assets/models/Halloween.glb",
//            placementMode = PlacementMode.PLANE_HORIZONTAL,
//            scaleUnits = 2.5f
//        ),
    )
    /**---------------------------------------------------------------------------------------------*/

    // ARModelNode
    var modelIndex = 0
    var modelNode: ArModelNode? = null

    var isLoading = false
        set(value) {
            field = value
            loadingView.isGone = !value
        }

//    var model = models[0] // Intialise with first model first



    /**---------------------------------------------------------------------------------------------*/
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentARBinding.inflate(inflater,container,false)

        //Intialise the view
        sceneView = binding.sceneView
        loadingView = binding.loadingView

        // Receive the parcelCode received after scanning the parcel code
        val parcelCode = requireArguments().getString("parcelCode")
        // TODO: remove after finish project
        Log.i("My","Manage to receive the parcelCode ${parcelCode}")

        // To find the index of model in the list with the corresponding code
        modelIndex = findModelIndex(parcelCode)

        // Button to add new model from the modelist
        btnNewModel = binding.btnNewModel.apply {
            // Add system bar margins
            val bottomMargin = (layoutParams as ViewGroup.MarginLayoutParams).bottomMargin
            doOnApplyWindowInsets { systemBarsInsets ->
                (layoutParams as ViewGroup.MarginLayoutParams).bottomMargin =
                    systemBarsInsets.bottom + bottomMargin
            }
            setOnClickListener { newModelNode() }
        }

        // Button to anchor the model
        btnAnchorModel = binding.btnAnchorModel.apply{
            setOnClickListener { placeModelNode() }
        }

        newModelNode()


        // Inflate the layout for this fragment
        return binding.root
    }

    fun placeModelNode() {
        modelNode?.anchor()
        btnAnchorModel.isVisible = false
        sceneView.planeRenderer.isVisible = false //the many many small dots de
    }

    fun newModelNode() {
        isLoading = true
        modelNode?.takeIf { //to check first time  call or not
            !it.isAnchored }?.let { //if the model node not anchor yet,
            sceneView.removeChild(it)
            it.destroy()
        }
        val model = models[modelIndex]
        Log.i("My","The fie is ${model.fileLocation}")
        modelIndex = (modelIndex + 1) % models.size
        // API doc. https://sceneview.github.io/api/sceneview-android/arsceneview/arsceneview/io.github.sceneview.ar.node/-ar-model-node/-ar-model-node.html
        modelNode = ArModelNode(
            placementMode = model.placementMode,
            hitPosition = Position(0.0f, 0.0f, -2.0f),
            followHitPosition = true,
            instantAnchor = false,
        ).apply {
            loadModelAsync( //try to load the model... This is function of ArModelNOde
                context = requireContext(),
                lifecycle = lifecycle,
                glbFileLocation = model.fileLocation,
                autoAnimate = true,
//                autoscale = true,
                scaleToUnits = model.scaleUnits,
                // Place the model origin at the bottom center
                centerOrigin = Position(y = -1.0f)
            ) { // after successfully loading the model then....
                sceneView.planeRenderer.isVisible = true
                isLoading = false
                Log.i("My","The fie is ${model.fileLocation}")
            }
            onPoseChanged = { node, _ -> //ARModelNode 's   onXXXX e.g onError, onLoaded...
                btnAnchorModel.isGone = node.isAnchored || !node.isTracking
            }
        }
        sceneView.addChild(modelNode!!) //recode the ARmodelNode to our sceneview
        // Select the model node by default (the model node is also selected on tap)
        sceneView.selectedNode = modelNode
    }

    private fun findModelIndex(parcelCode: String?): Int {
        var modelIndex = 0
        // To find the index of model in the list with the corresponding code
        for (i in models.indices) {
            if (models[i].parcelCode == parcelCode ){
                android.util.Log.i("My","${models[i]}")
                modelIndex = i
            }
        }
        return modelIndex
    }


}