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
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.viewpager.widget.ViewPager
import com.example.hueharvester.database.AppDatabase
import com.example.hueharvester.database.ColorData
import com.example.hueharvester.database.ColorDataRepository
import com.google.android.material.tabs.TabLayout
import kotlinx.coroutines.launch
import org.opencv.android.OpenCVLoader
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.imgproc.Imgproc
import java.lang.NullPointerException

// TODO: disattiva tutti Log in tutti i file
class MainActivity : AppCompatActivity() {
    private lateinit var viewPager: ViewPager
    private lateinit var tabLayout: TabLayout
    private lateinit var realTimeRGBFragment: RealTimeRGBFragment
    private lateinit var lineGraphFragment: LineGraphFragment
    private lateinit var cameraPreview: SurfaceView
    private var camera: Camera? = null

    private var savedPreviewSize: IntArray? = null
    private var savedPreviewFpsRange: IntArray? = null

    //private val applicationScope = lifecycleScope
    private lateinit var database: AppDatabase
    private lateinit var repository: ColorDataRepository
    private var creationDataID: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(MAIN, "onCreate")

        setContentView(R.layout.activity_main)

        // TODO: crea funzione per inizializzazione
        // Initialize database
        database = AppDatabase.getDatabase(this)
        repository = ColorDataRepository(database.colorDataDao())
        // Set the data collection starting point
        savedInstanceState?.let{
            Log.d(DBR, "Last data ID from BUNDLE: ${it.getInt("creationDataID")}")
        } ?: run {
            lifecycleScope.launch {
                repository.getLastInsertedData()?.let{
                    creationDataID = it.id
                    Log.d(DBR, "Last data ID from ROOM: $creationDataID")
                }
            }
        }

        // Initialize OpenCV
        if (!OpenCVLoader.initDebug())
            Log.e(MAIN, "Unable to load OpenCV!")
        else
            Log.d(MAIN, "OpenCV loaded Successfully!")

        cameraPreview = findViewById(R.id.camera_preview)
        viewPager = findViewById(R.id.view_pager)
        tabLayout = findViewById(R.id.tab_layout)

        setupViewPager(viewPager)
        tabLayout.setupWithViewPager(viewPager, true)

        // TODO: usa .let{}
        val adapter = viewPager.adapter as ViewPagerAdapter
        realTimeRGBFragment = adapter.getFragment(0) as RealTimeRGBFragment
        lineGraphFragment = adapter.getFragment(1) as LineGraphFragment

        setupCameraPreview()
    }

    // Set up the ViewPager with the fragments
    private fun setupViewPager(viewPager: ViewPager) {
        val adapter = ViewPagerAdapter(supportFragmentManager)
        realTimeRGBFragment = RealTimeRGBFragment()
        lineGraphFragment = LineGraphFragment()
        adapter.addFragment(realTimeRGBFragment, getString(R.string.RGB_values))
        adapter.addFragment(lineGraphFragment, getString(R.string.line_graph))
        viewPager.adapter = adapter
    }

    // Set up the camera preview on the SurfaceView
    private fun setupCameraPreview() {
        Log.v(MAIN, "Setting up camera preview")
        val holder: SurfaceHolder = cameraPreview.holder

        holder.addCallback(object : SurfaceHolder.Callback {
            override fun surfaceCreated(holder: SurfaceHolder) {
                //Log.i(MAIN, "Surface created")
                if (!checkCameraPermission()) {
                    requestCameraPermission()
                } else {
                    //Log.v(MAIN, "surfaceCreated calls initializeCameraAsync")
                    initializeCameraAsync(holder)
                }
            }
            override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
                //Log.i(MAIN, "Surface changed from [${savedPreviewSize?.get(0)} x ${savedPreviewSize?.get(1)}] to [$width x $height]")
                if (camera != null && (width != savedPreviewSize?.get(0) || height != savedPreviewSize?.get(1))) {
                    //Log.v(MAIN, "surfaceChanged calls initializeCameraAsync")
                    camera?.stopPreview()
                    initializeCameraAsync(holder)
                }
            }
            override fun surfaceDestroyed(holder: SurfaceHolder) {
                releaseCamera()
                Log.d(MAIN, "Surface destroyed")
            }
        })
    }

    // TODO: capire se usare thread o no (guarda documentazione + lezione)
    // Initialize the camera asynchronously
    private fun initializeCameraAsync(holder: SurfaceHolder) {
        //Thread {
            try {
                if (camera == null) {
                    camera = Camera.open()
                    Log.v(MAIN, "Camera opened")
                }
                adjustCameraOrientation()
                val params = camera?.parameters

                savedPreviewSize?.let {
                    Log.i(MAIN, "Restoring preview size: ${it[0]} x ${it[1]}")
                    params?.setPreviewSize(it[0], it[1])
                } ?: run {
                    val supportedPreviewSizes = params?.supportedPreviewSizes
                    val minPreviewSize = supportedPreviewSizes?.minByOrNull { it.width * it.height }
                    minPreviewSize?.let {
                        savedPreviewSize = intArrayOf(it.width, it.height)
                        params.setPreviewSize(it.width, it.height)
                    }
                    Log.i(MAIN, "Preview size set to: ${savedPreviewSize?.get(0)} x ${savedPreviewSize?.get(1)}")
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

                    setPreviewCallback { data, camera ->
                        val previewSize = camera.parameters.previewSize
                        val (avgRed, avgGreen, avgBlue) = calculateAverageColor(
                            data,
                            previewSize.width,
                            previewSize.height
                        )

                        if (avgRed > -1 && avgGreen > -1 && avgBlue > -1){
                            if (camera != null) {
                                runOnUiThread {
                                    if (realTimeRGBFragment.view != null) {
                                        realTimeRGBFragment.updateRGBValues(avgRed, avgGreen, avgBlue)
                                    }
                                    //Log.d(MAIN, "Preview callback")
                                }
                                manageNewData(avgRed, avgGreen, avgBlue)
                            }
                        }

                    }

                    setPreviewDisplay(holder)
                    startPreview()
                    Log.v(MAIN, "Camera preview started")
                }
                Log.d(MAIN, "Camera setup successful")
            } catch (e: Exception) {
                releaseCamera()
                Log.e(MAIN, "Camera setup failed", e)
            }
        //}.start()
    }

    // Manage new data collected from the callback
    private fun manageNewData(avgRed: Int, avgGreen: Int, avgBlue: Int) = lifecycleScope.launch{
        // Save color data to database
        ColorData(
            timestamp = System.currentTimeMillis(),
            red = avgRed,
            green = avgGreen,
            blue = avgBlue
        ).let {
            // Save data to database
            repository.insert(it)
        }

        repository.getLastInsertedData()?.let{
            // Clean up old data
            val startId = it.id - MAX_TIME
            repository.deleteOldData(startId)
            //Log.d(DBR, "Last ID = ${it.id} : deleted too old data => id < $startId")

            // Update line graph with the last 5 minutes of collected data
            repository.getDataAfter(
                startId
            ).let{
                runOnUiThread {
                    if (lineGraphFragment.view != null) {
                        lineGraphFragment.updateGraph(it, creationDataID)
                        //Log.d(DBR, "startId: $creationDataID")
                    }
                }
            }
        }
    }

    // TODO: controlla logiche ogni funzione qua dentro
    // Adjust camera orientation based on device rotation
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

    // Manage camera release
    private fun releaseCamera() {
        camera?.apply {
            setPreviewCallback(null)
            stopPreview()
            release()
            camera = null
            Log.d(MAIN, "Camera released")
        }
    }

    /** @return true if the camera permission is granted, false otherwise */
    private fun checkCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    /** Requests the camera permission using [requestPermissions]*/
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
                Log.v(MAIN, "requestCameraPermission calls initializeCameraAsync")
                initializeCameraAsync(cameraPreview.holder)
            } else {
                Toast.makeText(this, getString(R.string.camera_permission_required), Toast.LENGTH_SHORT).show()
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    // TODO: scrivi documentazione fatta bene con /** ... */
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
            for (y in 0 until height) {
                for (x in 0 until width) {
                    val pixel = rgb.get(y, x)
                    redSum += pixel[0].toInt()
                    greenSum += pixel[1].toInt()
                    blueSum += pixel[2].toInt()
                }
            }

            // 6. Calculation of average values
            val avgRed = (redSum / pixelCount).toInt()
            val avgGreen = (greenSum / pixelCount).toInt()
            val avgBlue = (blueSum / pixelCount).toInt()

            // 7. Return average values as a Triple
            return Triple(avgRed, avgGreen, avgBlue)
        } catch (e: NullPointerException) {
            Log.e(MAIN, "Error calculating average color")
            return Triple(-1, -1, -1)
        }
    }

    override fun onPause() {
        Log.d(MAIN, "onPause")
        super.onPause()
        releaseCamera()
    }

    // TODO: elimina
    override fun onResume() {
        super.onResume()
        Log.d(MAIN, "onResume")
    }

    // TODO: elimina
    override fun onStop() {
        Log.d(MAIN, "onStop")
        super.onStop()
    }

    // TODO: elimina
    override fun onDestroy() {
        Log.d(MAIN, "onDestroy")
        super.onDestroy()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        Log.d(MAIN, "onSaveInstanceState")
        super.onSaveInstanceState(outState)

        outState.putInt("creationDataID", creationDataID)

        savedPreviewSize?.let{
            outState.putInt("previewWidth", it[0])
            outState.putInt("previewHeight", it[1])
        }
        savedPreviewFpsRange?.let{
            outState.putIntArray("previewFpsRange", it)
        }

        // TODO: controlla corretta implementazione salvataggio fragment
        try {
            supportFragmentManager.putFragment(outState, "RealTimeRGBFragment", realTimeRGBFragment)
            supportFragmentManager.putFragment(outState, "LineGraphFragment", lineGraphFragment)
        } catch (e: IllegalStateException){
            Log.e(MAIN, "Error saving fragments", e)
        }
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        Log.d(MAIN, "onRestoreInstanceState")
        super.onRestoreInstanceState(savedInstanceState)

        creationDataID = savedInstanceState.getInt("creationDataID")

        savedPreviewSize = intArrayOf(
            savedInstanceState.getInt("previewWidth"),
            savedInstanceState.getInt("previewHeight")
        )
        savedPreviewFpsRange = savedInstanceState.getIntArray("previewFpsRange")

        // TODO: controlla corretta implementazione ripristino fragment
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
