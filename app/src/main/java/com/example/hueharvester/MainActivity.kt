@file:Suppress("DEPRECATION")

package com.example.hueharvester

import android.Manifest
import android.content.pm.PackageManager
import android.hardware.Camera
import android.os.Bundle
import android.util.Log
import android.view.Surface
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.viewpager.widget.ViewPager
import com.example.hueharvester.database.ColorData
import com.example.hueharvester.database.ColorDataViewModel
import com.example.hueharvester.database.ColorDataViewModelFactory
import com.example.hueharvester.databinding.ActivityMainBinding
import org.opencv.android.OpenCVLoader
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.imgproc.Imgproc
import java.lang.NullPointerException

/** Main activity of the application
 *  It manages the camera preview and the data collection
 */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var realTimeRGBFragment: RealTimeRGBFragment
    private lateinit var lineGraphFragment: LineGraphFragment
    private var camera: Camera? = null

    private var isCameraReleased = true

    private var isSurfaceCreated = false
    private var savedPreviewSize: IntArray? = null
    private var savedPreviewFpsRange: IntArray? = null

    private var isIdSaved = false
    private var creationDataID: Int = 0
    private var lastDataID: Int = 0

    private val viewModel: ColorDataViewModel by viewModels {
        ColorDataViewModelFactory((application as ColorDataApplication).repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //Log.d(MAIN, "onCreate")

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize OpenCV
        if (!OpenCVLoader.initDebug())
            Log.e(MAIN, "Unable to load OpenCV!")
        else
            Log.d(MAIN, "OpenCV loaded Successfully!")

        setupViewPager(binding.viewPager)
        binding.tabLayout.setupWithViewPager(binding.viewPager, true)

        val adapter = binding.viewPager.adapter as ViewPagerAdapter
        realTimeRGBFragment = adapter.getFragment(0) as RealTimeRGBFragment
        lineGraphFragment = adapter.getFragment(1) as LineGraphFragment

        observeData(savedInstanceState)

        setupCameraPreview()
    }

    /** Set up the ViewPager with the two fragments
     *  @param viewPager the ViewPager to set up
     *
     *  @see ViewPagerAdapter
     *  @see RealTimeRGBFragment
     *  @see LineGraphFragment
     *  @see ColorDataViewModel
     */
    private fun setupViewPager(viewPager: ViewPager) {
        val adapter = ViewPagerAdapter(supportFragmentManager)
        realTimeRGBFragment = RealTimeRGBFragment()
        lineGraphFragment = LineGraphFragment()
        adapter.addFragment(realTimeRGBFragment, getString(R.string.RGB_values))
        adapter.addFragment(lineGraphFragment, getString(R.string.line_graph))
        viewPager.adapter = adapter
    }

    /** Observe the data collection from the database
     *  @param savedInstanceState the Bundle to save the data IDs
     *
     *  @see ColorDataViewModel
     *  @see ColorData
     *  @see LineGraphFragment
     *  @see RealTimeRGBFragment
     *
     *  @throws NoSuchElementException if there is no data in the database
     */
    private fun observeData(savedInstanceState: Bundle?) {
        viewModel.allColorData.observe(this) { dataList ->
            if (!isIdSaved) {
                // Set the data collection starting point
                savedInstanceState?.let {
                    //Log.d(DBR, "Creation data ID from BUNDLE: ${it.getInt("creationDataID")}")
                    //Log.d(DBR, "Last data ID from BUNDLE: ${it.getInt("lastDataID")}")
                } ?: try {
                    run {
                        dataList.last().let {
                            creationDataID = it.id
                            lastDataID = it.id
                            //Log.d(DBR, "Creation data ID from ROOM: $creationDataID")
                            //Log.d(DBR, "Last data ID from ROOM: $lastDataID")
                        }
                    }
                } catch (e: NoSuchElementException) {
                    //Log.e(DBR, "No data in the database", e)
                    Log.i(DBR, "No data in the database")
                    //Log.d(DBR, "Creation data ID: $creationDataID")
                    //Log.d(DBR, "Last data ID: $lastDataID")
                }
                isIdSaved = true
            }
            if (dataList.isNotEmpty()){
                dataList.last().apply {
                    runOnUiThread {
                        if (lineGraphFragment.view != null) {
                            lineGraphFragment.updateGraph(dataList, creationDataID, SPM.toFloat())
                            //Log.d(DBR, "startId: $creationDataID")
                        }
                        if (realTimeRGBFragment.view != null) {
                            realTimeRGBFragment.updateRGBValues(
                                this.red,
                                this.green,
                                this.blue
                            )
                        }
                    }
                    if (this.id - dataList.first().id > MAX_TIME ) { // doesn't clean up if there are less than MAX_TIME data
                        // Clean up old data
                        viewModel.deleteOldData(this.id - MAX_TIME)
                        //Log.d(DBR, "Last ID = ${this.id} : deleted too old data => id < ${this.id - MAX_TIME}")
                    }
                }
            }
        }
    }

    /** Set up the camera preview on the [SurfaceView]
     *  It manages the camera permission request and the camera initialization
     *  @see SurfaceHolder
     *  @see Camera
     */
    private fun setupCameraPreview() {
        //Log.v(MAIN, "Setting up camera preview")
        val holder: SurfaceHolder = binding.cameraPreview.holder
        holder.addCallback(object : SurfaceHolder.Callback {
            override fun surfaceCreated(holder: SurfaceHolder) {
                //Log.i(MAIN, "Surface created")
                if (!checkCameraPermission()) {
                    requestCameraPermission()
                } else {
                    //Log.v(MAIN, "surfaceCreated calls initializeCameraAsync")
                    initializeCamera(holder)
                    isSurfaceCreated = true
                }
            }
            override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
                if (isSurfaceCreated) { // possible calling of surfaceChanged before surfaceCreated
                    //Log.i(MAIN, "Surface changed from [${savedPreviewSize?.get(0)} x ${savedPreviewSize?.get(1)}] to [$width x $height]")
                    if (camera != null &&
                        (width != savedPreviewSize?.get(0) || height != savedPreviewSize?.get(1))
                    ) {
                        //Log.v(MAIN, "surfaceChanged calls initializeCameraAsync")
                        camera?.stopPreview()
                        initializeCamera(holder)
                    }
                }
            }
            override fun surfaceDestroyed(holder: SurfaceHolder) {
                //Log.d(MAIN, "Surface destroyed")
                releaseCamera()
                isSurfaceCreated = false
            }
        })
    }

    /** Initialize the camera
     *  It sets the camera preview size and the preview frame rate
     *  @param holder the [SurfaceHolder] to set the camera preview
     *
     *  @see Camera
     *  @see Camera.Parameters
     *  @see SurfaceHolder
     */
    private fun initializeCamera(holder: SurfaceHolder) {
        try {
            if (isCameraReleased) {
                camera = Camera.open()
                isCameraReleased = false
                //Log.i(MAIN, "Camera opened")
            }

            adjustCameraOrientation()

            val params = camera?.parameters

            savedPreviewSize?.let {
                //Log.v(MAIN, "Restoring preview size: ${it[0]} x ${it[1]}")
                params?.setPreviewSize(it[0], it[1])
            } ?: run {
                val supportedPreviewSizes = params?.supportedPreviewSizes
                val minPreviewSize = supportedPreviewSizes?.minByOrNull { it.width * it.height }
                minPreviewSize?.let {
                    savedPreviewSize = intArrayOf(it.width, it.height)
                    params.setPreviewSize(it.width, it.height)
                }
                //Log.i(MAIN,"Preview size set to: ${savedPreviewSize?.get(0)} x ${savedPreviewSize?.get(1)}")
            }
            savedPreviewFpsRange?.let {
                params?.setPreviewFpsRange(it[0], it[1])
            } ?: run {
                val supportedPreviewFpsRanges = params?.supportedPreviewFpsRange
                val minPreviewFpsRange =
                    supportedPreviewFpsRanges?.minByOrNull { it[Camera.Parameters.PREVIEW_FPS_MIN_INDEX] }
                minPreviewFpsRange?.let {
                    params.setPreviewFpsRange(
                        it[Camera.Parameters.PREVIEW_FPS_MIN_INDEX],
                        it[Camera.Parameters.PREVIEW_FPS_MAX_INDEX]
                    )
                    savedPreviewFpsRange = it
                }
            }

            camera?.apply {
                parameters = params

                setPreviewCallback { data, cam ->
                    val previewSize = cam.parameters.previewSize
                    val (avgRed, avgGreen, avgBlue) = calculateAverageColor(
                        data,
                        previewSize.width,
                        previewSize.height
                    )

                    manageNewData(avgRed, avgGreen, avgBlue)
                }

                setPreviewDisplay(holder)
                startPreview()
                //Log.v(MAIN, "Camera preview started")
            }
            Log.d(MAIN, "Camera setup successful")
        } catch (e: Exception) {
            releaseCamera()
            Log.e(MAIN, "Camera setup failed", e)
        }
    }

    /** Manage the new data collected from the camera
     *  It saves the color data to the database
     *  @param avgRed the average red value
     *  @param avgGreen the average green value
     *  @param avgBlue the average blue value
     *
     *  @see ColorData
     *  @see ColorDataViewModel
     */
    private fun manageNewData(avgRed: Int, avgGreen: Int, avgBlue: Int) {
        ColorData(
            id = ++lastDataID,
            timestamp = System.currentTimeMillis(),
            red = avgRed,
            green = avgGreen,
            blue = avgBlue
        ).let {
            viewModel.insert(it)
        }
    }

    /** Adjust the camera orientation based on the device rotation
     * It uses the [Camera.CameraInfo] to get the camera orientation
     * and the [Surface] to get the device rotation value
     *  @see Camera.CameraInfo
     *  @see Surface
     * */
    private fun adjustCameraOrientation() {
        val rotation = windowManager.defaultDisplay.rotation
        var degrees = 0
        when (rotation) {
            Surface.ROTATION_0 -> degrees = 0
            Surface.ROTATION_90 -> degrees = 90
            Surface.ROTATION_180 -> degrees = 180
            Surface.ROTATION_270 -> degrees = 270
        }
        val info = Camera.CameraInfo()
        Camera.getCameraInfo(Camera.CameraInfo.CAMERA_FACING_BACK, info)
        val result = (info.orientation - degrees + 360) % 360
        camera?.setDisplayOrientation(result)
    }

    /** Release the camera resources
     *  It stops the camera preview and releases the camera
     *  @see Camera
     */
    private fun releaseCamera() {
        if (!isCameraReleased) {
            //Log.v(MAIN, "Releasing camera")
            camera?.apply {
                stopPreview()
                setPreviewCallback(null)
                release()
                camera = null
                isCameraReleased = true
                //Log.d(MAIN, "Camera released")
            }
        }
    }

    /** Checks if the camera permission is granted
     *  @return true if the camera permission is granted, false otherwise
     */
    private fun checkCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    /** Requests the camera permission
     *  It requests the camera permission to the user via [requestPermissions]
     */
    private fun requestCameraPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.CAMERA),
            CAMERA_REQUEST_CODE
        )
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode == CAMERA_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                Toast.makeText(this, getString(R.string.camera_permission_granted), Toast.LENGTH_SHORT).show()
                //Log.i(MAIN, "Camera permission granted")
                //Log.v(MAIN, "requestCameraPermission calls initializeCameraAsync")
                initializeCamera(binding.cameraPreview.holder)
            } else {
                Toast.makeText(this, getString(R.string.camera_permission_required), Toast.LENGTH_SHORT).show()
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    /** Calculate the average color from the YUV data
     *  It converts the YUV data to RGB and calculates the average RGB values
     *  @param data the YUV data
     *  @param width the width of the frame
     *  @param height the height of the frame
     *  @return a [Triple] containing the average red, green and blue values
     *
     *  @see Mat
     *  @see Imgproc
     */
    private fun calculateAverageColor(data: ByteArray, width: Int, height: Int): Triple<Int, Int, Int> {
        // 1. Creation of a Mat for YUV data
        val yuv = Mat(height + height/2, width, CvType.CV_8UC1)
        yuv.put(0, 0, data)

        // 2. Creation of an empty Mat for RGB data
        val rgb = Mat()

        // 3. Conversion from YUV to RGB
        Imgproc.cvtColor(yuv, rgb, Imgproc.COLOR_YUV2RGB_NV21, 3)

        // 4. Initialization of color sums
        var redSum = 0L
        var greenSum = 0L
        var blueSum = 0L
        val pixelCount = width * height

        try {
            // 5. Calculation of the sum of RGB values
            var rgbArray: DoubleArray
            for (y in 0 until height) {
                for (x in 0 until width) {
                    rgbArray = rgb.get(y, x)
                    redSum += rgbArray[0].toInt()
                    greenSum += rgbArray[1].toInt()
                    blueSum += rgbArray[2].toInt()
                }
            }

            // 6. Calculation of average values
            val avgRed = (redSum / pixelCount).toInt()
            val avgGreen = (greenSum / pixelCount).toInt()
            val avgBlue = (blueSum / pixelCount).toInt()

            // 7. Release Mats to free memory
            yuv.release()
            rgb.release()

            // 8. Return average values as a Triple
            return Triple(avgRed, avgGreen, avgBlue)
        } catch (e: NullPointerException) {
            //Log.e(MAIN, "Error calculating average color")
            //Log.i(MAIN, "Returning default values due to Error")
            return Triple(0, 0, 0)
        }
    }

    override fun onPause() {
        //Log.d(MAIN, "onPause")
        super.onPause()
        releaseCamera()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        //Log.d(MAIN, "onSaveInstanceState")

        outState.putInt("creationDataID", creationDataID)
        outState.putInt("lastDataID", lastDataID)

        savedPreviewSize?.let{
            outState.putInt("previewWidth", it[0])
            outState.putInt("previewHeight", it[1])
        }
        savedPreviewFpsRange?.let{
            outState.putIntArray("previewFpsRange", it)
        }

        try {
            supportFragmentManager.putFragment(outState, "RealTimeRGBFragment", realTimeRGBFragment)
            supportFragmentManager.putFragment(outState, "LineGraphFragment", lineGraphFragment)
        } catch (e: IllegalStateException){
            Log.e(MAIN, "Error saving fragments", e)
        }
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        //Log.d(MAIN, "onRestoreInstanceState")

        creationDataID = savedInstanceState.getInt("creationDataID")
        lastDataID = savedInstanceState.getInt("lastDataID")

        savedPreviewSize = intArrayOf(
            savedInstanceState.getInt("previewWidth"),
            savedInstanceState.getInt("previewHeight")
        )
        savedPreviewFpsRange = savedInstanceState.getIntArray("previewFpsRange")

        realTimeRGBFragment = supportFragmentManager.getFragment(savedInstanceState, "RealTimeRGBFragment") as RealTimeRGBFragment
        lineGraphFragment = supportFragmentManager.getFragment(savedInstanceState, "LineGraphFragment") as LineGraphFragment
    }

    companion object {
        private const val SPM = 1350 // 22.5 samples/sec * 60 sec
        private const val MAX_TIME = SPM * 5 // change here to set the max time for data collection
        private const val CAMERA_REQUEST_CODE = 100
        private const val MAIN = "MainActivity"
        private const val DBR = "DatabaseRoom"
    }
}
