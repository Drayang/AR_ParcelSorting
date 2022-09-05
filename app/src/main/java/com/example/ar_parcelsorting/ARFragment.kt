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
        val placementMode: PlacementMode = PlacementMode.BEST_AVAILABLE
    )

    val models = listOf(
        Model("model/parcelSPXMY0072.glb",
            placementMode = PlacementMode.BEST_AVAILABLE.apply {
                keepRotation = true
            },
            scaleUnits = 0.5f
        ),
//        Model("models/parcelSPXMY0060.glb",
//            placementMode = PlacementMode.PLANE_HORIZONTAL.apply {
//                keepRotation = true
//            },
//            scaleUnits = 0.5f),
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


    // Receive the parcelCode received after scanning the parcel code
//    val parcelCode = requireArguments().getString("parcelCode")

    /**---------------------------------------------------------------------------------------------*/
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentARBinding.inflate(inflater,container,false)


        sceneView = binding.sceneView
        loadingView = binding.loadingView
//        sceneView = findViewById(R.id.sceneView)
//        loadingView = findViewById(R.id.loadingView)

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

//        newModelButton = findViewById<ExtendedFloatingActionButton>(R.id.newModelButton).apply {
//            // Add system bar margins
//            val bottomMargin = (layoutParams as ViewGroup.MarginLayoutParams).bottomMargin
//            doOnApplyWindowInsets { systemBarsInsets ->
//                (layoutParams as ViewGroup.MarginLayoutParams).bottomMargin =
//                    systemBarsInsets.bottom + bottomMargin
//            }
//            setOnClickListener { newModelNode() }
//        }
//        placeModelButton = findViewById<ExtendedFloatingActionButton>(R.id.placeModelButton).apply {
//            setOnClickListener { placeModelNode() }
//        }

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


}