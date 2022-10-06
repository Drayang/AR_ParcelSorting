package com.example.ar_parcelsorting

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isGone
import androidx.navigation.findNavController
import com.example.ar_parcelsorting.LoginFragment.Companion.markerScannedTracking
import com.example.ar_parcelsorting.data.Model
import com.example.ar_parcelsorting.databinding.FragmentTrackingBinding
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.ar.core.*
import io.github.sceneview.ar.ArSceneView
import io.github.sceneview.ar.arcore.ArSession
import io.github.sceneview.ar.arcore.position
import io.github.sceneview.ar.node.ArModelNode
import io.github.sceneview.math.Position
import io.github.sceneview.math.Rotation


class TrackingFragment : Fragment() {

    private lateinit var binding : FragmentTrackingBinding

    private lateinit var sceneView: ArSceneView
    private lateinit var loadingView: View

    private lateinit var btnDisplay: ExtendedFloatingActionButton
    private lateinit var btnBack: ExtendedFloatingActionButton
    /** Augmented image*/
    private var aruco_0_Detected = false
    private var all_marker_Detected = false

    // Data receive from the CodeScannerFragment - parcelCode and parcelX,Y,Z
    private lateinit var parcelCode: String
    private lateinit var parcelPosition: Position //Parcel's position based on sorting algorithm
    private lateinit var newParcelPosition: Position // Position after adding the origin pose of the augmented image
    private var parcelOrientation = 0 // Parcel's orientation based on sorting algorithm
    private lateinit var newParcelRotation:Rotation //Rotation that need to be make so that the parcel's orientation match with sorting algorithm
    private var parcelLength = 0.0f
    private var parcelWidth = 0.0f


    /** Regex to obtain the name of the model [No needed anymore]*/
    private val pattern = Regex("model/(.+).glb")
    private val matchResult = pattern.find("model/parcelSPXMY0011.glb") //example only
    private val name = matchResult?.groupValues?.get(1) //example only


    /** Define the model list - display parcel as ArModelNode */
    private val models = listOf(
        Model(
            "model/parcelSPXMY0011.glb",
            parcelCode = "SPXMY0011"
        ),
        Model("model/parcelSPXMY0017.glb",
            parcelCode = "SPXMY0017"
        ),
        Model("model/parcelSPXMY0027.glb",
            parcelCode = "SPXMY0027"
        ),
        Model("model/parcelSPXMY0033.glb",
            parcelCode = "SPXMY0033"
        ),
        Model("model/parcelSPXMY0044.glb",
            parcelCode = "SPXMY0044"
        ),
        Model("model/parcelSPXMY0050.glb",
            parcelCode = "SPXMY0050"
        ),
        Model("model/parcelSPXMY0087.glb",
            parcelCode = "SPXMY0087"
        ),
        Model("model/parcelSPXMY0091.glb",
            parcelCode = "SPXMY0091"
        ),
    )

    // ARModelNode
    private var modelIndex = 0
    private var modelNode: ArModelNode? = null
    private var modelAnchored = false

    private var isLoading = false
        set(value) {
            field = value
            loadingView.isGone = !value
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentTrackingBinding.inflate(inflater,container,false)

        //Initialise the view
        sceneView = binding.sceneView
        loadingView = binding.loadingView

        // Set up the button
        btnDisplay = binding.btnDisplay
        btnBack = binding.btnBack.apply{
            setOnClickListener{
                it.findNavController().navigate(R.id.action_trackingFragment_to_parcelListFragment)
            }
        }

        Log.i("My","the markerScannedTracking is = $markerScannedTracking")

        if (!markerScannedTracking) { // First time scan the marker
            //Initialise the image database for Augmented Image
            sceneView.configureSession(this::initialiseSceneViewSession)

            // Track the augmented image - which are the aruco marker in our case
            sceneView.onAugmentedImageUpdate += this::onAugmentedImageTrackingUpdate
        }
        else{ // Already scanned marker before, trying to display other model only
            btnBack.isGone = false
            isLoading = false
            btnDisplay.isGone = true // Remove the button
            aruco_0_Detected = true
            all_marker_Detected = true
            modelAnchored = false

        }

        retrievePrevFragmentInfo() // Retrieve the information

        // To find the index of model in the list with the corresponding code
        modelIndex = findModelIndex(parcelCode)

        sceneView.apply{
            onArFrame= {
                /** Load the model into the space if all marker has been scanned*/
                if (all_marker_Detected && !modelAnchored){
                    modelAnchored = true
                    newParcelPosition = getModelPosition(parcelPosition) // find the new parcel position
                    newParcelRotation = getModelRotation(parcelOrientation)
                    Log.i("My","Enter Load model ")
                    loadModelNode()
                }
            }
        }

        // Inflate the layout for this fragment
        return binding.root
    }

    /** Retrieve information from previous fragment*/
    private fun retrievePrevFragmentInfo(){
        parcelCode = requireArguments().getString("parcelCode")!! // Retrieved the scanned parcelCode
        parcelPosition = convertCoordFrame() //convert the parcel's position to be relative to Ar Camera Frame Coordinate
        parcelOrientation = requireArguments().getString("parcelOrientation")?.toInt()!!
        parcelLength = requireArguments().getString("parcelLength")?.toFloat()?.div(100)!!
        parcelWidth = requireArguments().getString("parcelWidth")?.toFloat()?.div(100)!!
    }

    /**
     * The frame coordinate used in sorting algorithm is different to the AR coordinate
     * Sorting algorithm coordinate -> parcelX (container length),parcelY(container width),parcelZ (container height)
     * AR coordinate -> ArModelNode coordinate
     * The conversion is :
     * parcelX => Position(x)  [Notice this is valid for Pose, Position,HitPosition, Anchor.position and etc]
     * parcelZ => Position(y)
     * parcelY => Position(-z)
     *
     * For coding convenience, we convert the sorting algo. coord. to map with Ar coord.
     * !!!!! Therefore, use AR coordinate to think and work on the coding.
     * */
    private fun convertCoordFrame(): Position {
        //divide by 100 to convert cm to m
        val posX = requireArguments().getString("parcelX")?.toFloat()?.div(100)
        val posY = requireArguments().getString("parcelY")?.toFloat()?.div(100)
        val posZ = requireArguments().getString("parcelZ")?.toFloat()?.div(100)


        Log.i("My","ConvertCoordFrame")
        return Position(posX!!, posZ!!,-posY!!)
    }

    /** To find the correct parcel position in AR frame after considering the "restricted bounding area"
     *  Restricted bounding area: Scan four marker (that form a rectangular bounding area) using Augmented image and get their Pose or Anchor
     *  The data are stored in markerPoseList and markerAnchorList respectively.
     *  Add the parcelPosition with the first marker's position to get the newParcelPosition
     *
     *  Can think the first marker as the origin of our sorting algorithm system.
     * */
    private fun getModelPosition(parcelPosition: Position): Position{
        val tempPosition= (augmentedImagePose.position + parcelPosition)
        // adjust the origin of the renderable model to the corner of the image
        tempPosition.x = tempPosition.x - (augmentedImageList[0].extentX.div(2))
        tempPosition.z = tempPosition.z + (augmentedImageList[0].extentZ.div(2))
        return tempPosition
    }

    /** Rotate the parcel model based on the sorting algorithm result*/
    private fun getModelRotation(parcelOrientation: Int):Rotation{

        /**
         * The rotation axis is based on SceneView camera frame.
         * The rotation is in degree
         * */
        return when(parcelOrientation) {
            1 -> Rotation(x=0.0f,y=0.0f,z=0.0f)
            2 -> Rotation(x=0.0f,y=90.0f,z=0.0f)
            3 -> Rotation(x=0.0f,y=0.0f,z=90.0f)
            4 -> Rotation(x=0.0f,y=90.0f,z=-90.0f)
            5 -> Rotation(x=0.0f,y=-90.0f,z=90.0f)
            6 -> Rotation(x=90.0f,y=180.0f,z=0.0f)
            else -> Rotation(x=0.0f,y=0.0f,z=0.0f)
        }
    }

    /** Load the Ar model of the parcel in the space based on the sorting algorithm resukt*/
    private fun loadModelNode() {
        val model = models[modelIndex]
        modelIndex = (modelIndex + 1) % models.size
        modelNode = ArModelNode(
            placementMode = model.placementMode,
            hitPosition = Position(0.0f, 0.0f, 0.0f),
            followHitPosition = false,
            instantAnchor = false,
        ).apply {
            loadModelAsync( //load the model based on the information
                context = requireContext(),
                lifecycle = lifecycle,
                glbFileLocation = model.fileLocation,
                autoAnimate = true,
                scaleToUnits = null,
                centerOrigin = Position(x = -parcelLength,z= parcelWidth) // Place the model origin at the front left bottom corner
            ) {
                sceneView.planeRenderer.isVisible = false
                isLoading = false
                name = pattern.find(model.fileLocation)?.groupValues?.get(1)
            }
            position = newParcelPosition
            anchor = augmentedImageAnchor
            // rotation = newParcelRotation // TODO: dont use first
        }
        sceneView.addChild(modelNode!!) //recode the ARmodelNode to our sceneview
        sceneView.selectedNode = modelNode
    }

    /** Set up the augmented image dabtabse*/
    private fun initialiseSceneViewSession(session: ArSession, config: Config) {

        val imageDatabase = this.requireActivity().assets.open("imgdb/aruco_marker.imgdb").use {
            AugmentedImageDatabase.deserialize(session, it)
        }
        config.augmentedImageDatabase = imageDatabase
        session.configure(config)
    }

    /** Detect the desired images (i.e. markers in our case) and define the consequence operation
     * Note: only 1 marker is used!!*/
    private fun onAugmentedImageTrackingUpdate(augmentedImage :AugmentedImage ) {
        // If all marker have been detected, stop scanning augmented image to save CPU usage
        if (aruco_0_Detected && !all_marker_Detected){
            all_marker_Detected = true
            isLoading = false
            btnDisplay.isGone = true // Remove the display button
            btnBack.isGone = false // Show the back button
            markerScannedTracking = true // Indicate marker has been scanned and we no need to scan again
            Toast.makeText(activity, "Container Match.", Toast.LENGTH_LONG).show()
            // To stop viewing the square bounding box
            sceneView.instructions.augmentedImageInfoEnabled = false
            return
        }

        /** Record the scanned marker and stop duplicate scanning on same marker*/
        if (augmentedImage.trackingState == TrackingState.TRACKING
            && augmentedImage.trackingMethod == AugmentedImage.TrackingMethod.FULL_TRACKING) {

            // Get the pose
            augmentedImagePose = augmentedImage.centerPose
            //Create anchor
            augmentedImageAnchor = augmentedImage.createAnchor(augmentedImagePose)

            if (!aruco_0_Detected && augmentedImage.name == "aruco0.png") {
                aruco_0_Detected = true

                augmentedImageList.add(augmentedImage)
            }
        }
    }

    private fun findModelIndex(parcelCode: String?): Int {
        var modelIndex = 0
        // To find the index of model in the list with the corresponding code
        for (i in models.indices) {
            if (models[i].parcelCode == parcelCode ){
                Log.i("My","The found model is ${models[i]}")
                modelIndex = i
            }
        }
        return modelIndex
    }

    companion object{
        /** Initialise in companion object to store the information*/
        lateinit var augmentedImagePose : Pose
        lateinit var augmentedImageAnchor : Anchor
        var augmentedImageList = arrayListOf<AugmentedImage>()
    }

}