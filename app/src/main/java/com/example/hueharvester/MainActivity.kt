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
import com.google.android.material.tabs.TabLayout
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.opencv.android.OpenCVLoader
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.imgproc.Imgproc
import java.lang.NullPointerException


class MainActivity : AppCompatActivity() {
    private lateinit var viewPager: ViewPager
    private lateinit var tabLayout: TabLayout
    private lateinit var realTimeRGBFragment: RealTimeRGBFragment
    private lateinit var lineGraphFragment: LineGraphFragment
    private lateinit var cameraPreview: SurfaceView
    private var camera: Camera? = null

    private var isSurfaceCreated = false
    private var previousSurfaceWidth = 0
    private var previousSurfaceHeight = 0

    private var savedPreviewSize: Camera.Size? = null
    private var savedPreviewFpsRange: IntArray? = null

    private val applicationScope = lifecycleScope
    private lateinit var database: AppDatabase
    private lateinit var repository: ColorDataRepository
    private var creationDataID: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate")

        setContentView(R.layout.activity_main)

        // Initialize database
        database = AppDatabase.getDatabase(this)
        repository = ColorDataRepository(database.colorDataDao())

        savedInstanceState?.let{
            Log.d("DatabaseRoom", "Last data ID from BUNDLE: ${it.getInt("creationDataID")}")
        } ?: run {
            applicationScope.launch {
                val lastData = repository.getLastInsertedData()
                lastData?.let{
                    creationDataID = it.id
                    Log.d("DatabaseRoom", "Last data ID from ROOM: $creationDataID")
                }
            }
        }

        // Initialize OpenCV
        if (!OpenCVLoader.initDebug())
            Log.e(TAG, "Unable to load OpenCV!")
        else
            Log.d(TAG, "OpenCV loaded Successfully!")

        cameraPreview = findViewById(R.id.camera_preview)
        viewPager = findViewById(R.id.view_pager)
        tabLayout = findViewById(R.id.tab_layout)

        setupViewPager(viewPager)

        tabLayout.setupWithViewPager(viewPager, true)

        viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
                //Log.d(TAG, "onPageScrolled")
            }

            override fun onPageSelected(position: Int) {
                //Log.d(TAG, "onPageSelected")
                if (position == 0) {
                    Log.d(TAG, "RealTimeRGBFragment selected")
                } else if (position == 1) {
                    Log.d(TAG, "LineGraphFragment selected")
                }
            }

            override fun onPageScrollStateChanged(state: Int) {
                //Log.d(TAG, "onPageScrollStateChanged")
            }
        })

        val adapter = viewPager.adapter as ViewPagerAdapter
        realTimeRGBFragment = adapter.getFragment(0) as RealTimeRGBFragment
        lineGraphFragment = adapter.getFragment(1) as LineGraphFragment


        if (checkCameraPermission()) {
            Log.d(TAG, "Camera permission granted")
            setupCameraPreview()
        } else {
            requestCameraPermission() // TODO: Handle permission request result (se do ok app rimane bloccata)
        }

        applicationScope.launch{
            while (true) {
                /*if(camera != null){*/
                    val lastData = repository.getLastInsertedData()
                    // Delete data older than 5 minutes before the last data acquisition
                    lastData?.let {
                        val startId = it.id - (5 * 1350) // 1350 = 22.5 samples/sec * 60 sec
                        repository.deleteOldData(startId)
                        Log.d("DatabaseRoom", "Deleted too old data => id < $startId")
                    }
                /*}*/
                delay(60000)
            }
        }
    }


    private fun setupViewPager(viewPager: ViewPager) {
        val adapter = ViewPagerAdapter(supportFragmentManager)
        realTimeRGBFragment = RealTimeRGBFragment()
        lineGraphFragment = LineGraphFragment()
        adapter.addFragment(realTimeRGBFragment, getString(R.string.RGB_values))
        adapter.addFragment(lineGraphFragment, getString(R.string.line_graph))
        viewPager.adapter = adapter
    }

    private fun setupCameraPreview() {
        val holder: SurfaceHolder = cameraPreview.holder
        holder.addCallback(object : SurfaceHolder.Callback {
            override fun surfaceCreated(holder: SurfaceHolder) {
                if (!isSurfaceCreated) {
                    Log.d(TAG, "Surface created")
                    isSurfaceCreated = true
                    previousSurfaceWidth = cameraPreview.width
                    previousSurfaceHeight = cameraPreview.height
                    initializeCameraAsync(holder)
                }
            }

            override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
                if (camera != null && (width != previousSurfaceWidth || height != previousSurfaceHeight)) {
                    Log.d(TAG, "Surface changed")
                    camera?.stopPreview()
                    previousSurfaceWidth = width
                    previousSurfaceHeight = height
                    initializeCameraAsync(holder)
                }
            }

            override fun surfaceDestroyed(holder: SurfaceHolder) {
                if(isSurfaceCreated){
                    releaseCamera()
                    isSurfaceCreated = false
                    Log.d(TAG, "Surface destroyed")
                }
            }
        })
    }

    private fun initializeCameraAsync(holder: SurfaceHolder) {
        Thread {
            try {
                if (camera == null) {
                    camera = Camera.open()
                }
                adjustCameraOrientation()
                val params = camera?.parameters

                savedPreviewSize?.let {
                    params?.setPreviewSize(it.width, it.height)
                } ?: run {
                    val supportedPreviewSizes = params?.supportedPreviewSizes
                    val minPreviewSize = supportedPreviewSizes?.minByOrNull { it.width * it.height }
                    minPreviewSize?.let {
                        params.setPreviewSize(it.width, it.height)
                        savedPreviewSize = it
                    }
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

                camera?.parameters = params
                camera?.setPreviewDisplay(holder)
                camera?.startPreview()

                camera?.setPreviewCallback { data, camera ->
                    val previewSize = camera.parameters.previewSize
                    val (avgRed, avgGreen, avgBlue) = calculateAverageColor(
                        data,
                        previewSize.width,
                        previewSize.height
                    )

                    if (avgRed != -1 && avgGreen != -1 && avgBlue != -1){
                        runOnUiThread {
                            if (realTimeRGBFragment.view != null) {
                                realTimeRGBFragment.updateRGBValues(avgRed, avgGreen, avgBlue)
                            }
                            //Log.d(TAG, "Preview callback")
                        }
                        manageNewData(avgRed, avgGreen, avgBlue)
                    }

                }
                Log.d(TAG, "Camera setup successful")
            } catch (e: Exception) {
                releaseCamera()
                Log.e(TAG, "Camera setup failed", e)
            }
        }.start()
    }

    private fun manageNewData(avgRed: Int, avgGreen: Int, avgBlue: Int) = applicationScope.launch{
        val currentTime = System.currentTimeMillis()

        // Save color data to database
        val colorData = ColorData(
            timestamp = currentTime,
            red = avgRed,
            green = avgGreen,
            blue = avgBlue
        )
        repository.insert(colorData)

        // Update line graph with the last 5 minutes of collected data
        val lastFiveMinData = repository.getDataAfter(
            colorData.id - (5 * 1350) // 1350 = 22.5 samples/sec * 60 sec
        )
        runOnUiThread{
            if (lineGraphFragment.view != null) {
                lineGraphFragment.updateGraph(lastFiveMinData, creationDataID)
                //Log.d("DatabaseRoom", "startId: $creationDataID")
            }
        }
    }

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

    private fun releaseCamera() {
        camera?.setPreviewCallback(null)
        camera?.stopPreview()
        camera?.release()
        camera = null
        Log.d(TAG, "Camera released")
    }

    private fun checkCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestCameraPermission() {
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), CAMERA_REQUEST_CODE)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                setupCameraPreview()
            } else {
                Toast.makeText(this, getString(R.string.camera_permission_required), Toast.LENGTH_SHORT).show()
            }
        }
    }

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
            Log.e(TAG, "Error calculating average color")
            return Triple(-1, -1, -1)
        }
    }

    override fun onPause() {
        super.onPause()
        Log.d(TAG, "onPause")
        releaseCamera()
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume")
        if (checkCameraPermission()) {
            setupCameraPreview()
        }
    }

    override fun onStop() {
        super.onStop()
        Log.d(TAG, "onStop")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy")
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        Log.d(TAG, "onSaveInstanceState")

        outState.putInt("creationDataID", creationDataID)
        supportFragmentManager.putFragment(outState, "RealTimeRGBFragment", realTimeRGBFragment)
        supportFragmentManager.putFragment(outState, "LineGraphFragment", lineGraphFragment)

        savedPreviewSize?.let{
            outState.putInt("previewWidth", it.width)
            outState.putInt("previewHeight", it.height)
        }
        savedPreviewFpsRange?.let{
            outState.putIntArray("previewFpsRange", it)
        }
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        Log.d(TAG, "onRestoreInstanceState")
        super.onRestoreInstanceState(savedInstanceState)

        creationDataID = savedInstanceState.getInt("creationDataID")
        realTimeRGBFragment = supportFragmentManager.getFragment(savedInstanceState, "RealTimeRGBFragment") as RealTimeRGBFragment
        lineGraphFragment = supportFragmentManager.getFragment(savedInstanceState, "LineGraphFragment") as LineGraphFragment

        savedPreviewSize = camera?.Size(savedInstanceState.getInt("previewWidth"), savedInstanceState.getInt("previewHeight"))
        savedPreviewFpsRange = savedInstanceState.getIntArray("previewFpsRange")
    }

    companion object {
        private const val CAMERA_REQUEST_CODE = 100
        private const val TAG = "MainActivity"
    }
}