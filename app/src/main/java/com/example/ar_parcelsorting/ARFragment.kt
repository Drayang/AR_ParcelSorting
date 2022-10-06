package com.example.ar_parcelsorting

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isGone
import com.example.ar_parcelsorting.LoginFragment.Companion.TAG
import com.example.ar_parcelsorting.data.Model
import com.example.ar_parcelsorting.databinding.FragmentARBinding
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.ar.core.*
import io.github.sceneview.ar.ArSceneView
import io.github.sceneview.ar.arcore.ArSession
import io.github.sceneview.ar.arcore.position
import io.github.sceneview.ar.node.ArModelNode
import io.github.sceneview.math.Position
import io.github.sceneview.math.Rotation


class ARFragment : Fragment() {

    private lateinit var binding : FragmentARBinding

    private lateinit var sceneView: ArSceneView
    private lateinit var loadingView: View

    private lateinit var btnDisplay: ExtendedFloatingActionButton

    /** Augmented image*/
    private var aruco_0_Detected = false
    private var aruco_2_Detected = false
    private var aruco_3_Detected = false
    private var aruco_4_Detected = false
    private var all_marker_Detected = false

    private lateinit var augmentedImagePose : Pose
    private lateinit var augmentedImageAnchor : Anchor

    /** Pose store translation and rotation info of the augmented image
     *  Anchor.position store exactly the same thing of Pose*/
    private var markerPoseList = arrayListOf<Pose>()
    private var markerAnchorList = arrayListOf<Anchor>()
    private var augmentedImageList = arrayListOf<AugmentedImage>()

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

        binding = FragmentARBinding.inflate(inflater,container,false)

        //Initialise the view
        sceneView = binding.sceneView
        loadingView = binding.loadingView

        //Initialise the image database for Augmented Image
        sceneView.configureSession(this::initialiseSceneViewSession)

        // Track the augmented image - which are the aruco marker in our case
        sceneView.onAugmentedImageUpdate += this::onAugmentedImageTrackingUpdate

        btnDisplay = binding.btnDisplay

        retrievePrevFragmentInfo()

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
//                arFrame.camera.pose
//                Log.i("MyTag","Enter onArFrame")
//                Log.i("MyTag","Children are ${sceneView.children}")
//                Log.i("MyTag","Children size are ${sceneView.children.size}")
//                Log.i("MyTag","Added children name is ${children[children.size-1].name}")
//                Log.i("MyTag","Added children scale is ${children[children.size-1].scale}")
//                Log.i("MyTag","The hit test is ${arFrame.camera.pose}")
//                children[children.size-1].isVisible = true

            }
        }

        return binding.root // Inflate the layout for this fragment
    }

    /** Retrieve information from previous fragment*/
    private fun retrievePrevFragmentInfo(){
        // Retrieved the scanned parcelCode from previous fragment (after scanning the parcel code)
        parcelCode = requireArguments().getString("parcelCode")!!
        Log.i(TAG,"Manage to receive the parcelCode ${parcelCode}")
        // Retrieve the parcel position and convert to map the AR frame coordinate
        parcelPosition = convertCoordFrame() //convert the parcel's position to be relative to Ar Camera Frame Coordinate
        Log.i(TAG,"The parcelPosition is $parcelPosition")
        parcelOrientation = requireArguments().getString("parcelOrientation")?.toInt()!!
        Log.i(TAG,"The orientaton is $parcelOrientation")

        parcelLength = requireArguments().getString("parcelLength")?.toFloat()?.div(100)!!
        parcelWidth = requireArguments().getString("parcelWidth")?.toFloat()?.div(100)!!
        Log.i(TAG,"The length and witdth is $parcelLength and $parcelWidth")

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
        val tempPosition= (markerPoseList[0].position + parcelPosition)
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
            ) { // after successfully loading the model then....
                sceneView.planeRenderer.isVisible = false
                isLoading = false
                name = pattern.find(model.fileLocation)?.groupValues?.get(1)
            }
            position = newParcelPosition
            anchor = markerAnchorList[0]
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
     * Note: only 1 marker is used now!!*/
    private fun onAugmentedImageTrackingUpdate(augmentedImage :AugmentedImage ) {
        // If all marker have been detected, stop scanning augmented image to save CPU usage
        if (aruco_0_Detected && !all_marker_Detected){
            all_marker_Detected = true
            isLoading = false
            btnDisplay.isGone = true // Remove the button
            Toast.makeText(activity, "All markers has been scanned.", Toast.LENGTH_LONG).show()
            Log.i(
                "My", "The augmentedImagePosition list is $markerPoseList"
            )
            Log.i(
                "My", "The augmentedImageAnchorlist is ${markerAnchorList[0].pose}"
            )
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
                Toast.makeText(activity, "Marker ${augmentedImage.index} detected", Toast.LENGTH_LONG).show()
                markerPoseList.add(augmentedImagePose)
                markerAnchorList.add(augmentedImageAnchor)
                augmentedImageList.add(augmentedImage)
            }
            if (!aruco_2_Detected && augmentedImage.name == "aruco2.png") {
                aruco_2_Detected = true
                Toast.makeText(activity, "Marker ${augmentedImage.index} detected", Toast.LENGTH_LONG).show()
                markerPoseList.add(augmentedImagePose)
                markerAnchorList.add(augmentedImageAnchor)
                augmentedImageList.add(augmentedImage)
            }
            if (!aruco_3_Detected && augmentedImage.name == "aruco3.png") {
                aruco_3_Detected = true
                Toast.makeText(activity, "Marker ${augmentedImage.index} detected", Toast.LENGTH_LONG).show()
                markerPoseList.add(augmentedImagePose)
                markerAnchorList.add(augmentedImageAnchor)
                augmentedImageList.add(augmentedImage)
            }
            if (!aruco_4_Detected && augmentedImage.name == "aruco4.png") {
                aruco_4_Detected = true
                Toast.makeText(activity, "Marker ${augmentedImage.index} detected", Toast.LENGTH_LONG).show()
                markerPoseList.add(augmentedImagePose)
                markerAnchorList.add(augmentedImageAnchor)
                augmentedImageList.add(augmentedImage)
            }
        }
    }

    private fun findModelIndex(parcelCode: String?): Int {
        var modelIndex = 0
        // To find the index of model in the list with the corresponding code
        for (i in models.indices) {
            if (models[i].parcelCode == parcelCode ){
                Log.i("My","${models[i]}")
                modelIndex = i
            }
        }
        return modelIndex
    }


}