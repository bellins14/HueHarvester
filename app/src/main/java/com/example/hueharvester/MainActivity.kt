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
import com.github.mikephil.charting.data.Entry
import com.google.android.material.tabs.TabLayout
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.opencv.android.OpenCVLoader
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.imgproc.Imgproc


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

    private var isCameraReleased = true
    private var savedPreviewSize: Camera.Size? = null
    private var savedPreviewFpsRange: IntArray? = null

    private val applicationScope = lifecycleScope
    private lateinit var database: AppDatabase
    private lateinit var repository: ColorDataRepository
    //private var creationTime: Float = 0f

    private var redData: MutableList<Entry> = ArrayList()
    private var greenData: MutableList<Entry> = ArrayList()
    private var blueData: MutableList<Entry> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate")

        setContentView(R.layout.activity_main)

        // Initialize database
        database = AppDatabase.getDatabase(this, applicationScope)
        repository = ColorDataRepository(database.colorDataDao())

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
            requestCameraPermission()
        }

        applicationScope.launch {
            /*val startTime = lineGraphFragment.creationTimeMillis - (5 * 60 * 1000) // 5 minutes ago
            lineGraphFragment.initializeGraph(repository.getDataFromLastFiveMinutes(startTime))*/

            while (true) {
                val lastData = repository.getLastInsertedData()
                lastData?.let { repository.deleteOldData(it.timestamp - (5 * 60 * 1000) ) }
                Log.d(TAG, "Deleted old data")
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
                if (isCameraReleased) {
                    camera = Camera.open()
                    isCameraReleased = false
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
                    //var timestamp: Long? = null
                    runOnUiThread {
                        if (realTimeRGBFragment.view != null) {
                            realTimeRGBFragment.updateRGBValues(avgRed, avgGreen, avgBlue)
                        }
                        /*if (lineGraphFragment.view != null) {
                            //timestamp = addDataPoint(avgRed, avgGreen, avgBlue)

                        }*/
                        //Log.d(TAG, "Preview callback")
                    }

                    applicationScope.launch {
                        val colorData = addDataPoint(avgRed, avgGreen, avgBlue)
                        repository.insert(colorData)
                        val lastColors = repository.getDataAfter(colorData.timestamp - (5 * 60 * 1000))
                        runOnUiThread{
                            if (lineGraphFragment.view != null) {
                                lineGraphFragment.initializeGraph(lastColors)
                            }
                        }
                    }
                    // Save color data to database
                    /*timestamp?.let {
                        applicationScope.launch {
                            val colorData = ColorData(
                                timestamp = it,
                                red = avgRed,
                                green = avgGreen,
                                blue = avgBlue
                            )
                            repository.insert(colorData)
                        }
                    }*/
                }
                Log.d(TAG, "Camera setup successful")
            } catch (e: Exception) {
                releaseCamera()
                Log.e(TAG, "Camera setup failed", e)
            }
        }.start()
    }

    private fun addDataPoint(avgRed: Int, avgGreen: Int, avgBlue: Int) : ColorData {
        return ColorData(
            timestamp = System.currentTimeMillis(),
            red = avgRed,
            green = avgGreen,
            blue = avgBlue
        )
    }

    /*private fun addDataPoint(avgRed: Int, avgGreen: Int, avgBlue: Int) : Long {
        val timestamp = System.currentTimeMillis()
        val chartCreationTime = lineGraphFragment.creationTimeMillis
        val currentTime = (timestamp - chartCreationTime) / 1000f / 60f

        val redEntry = Entry(currentTime, avgRed.toFloat())
        redData.add(redEntry)
        val greenEntry = Entry(currentTime, avgGreen.toFloat())
        greenData.add(greenEntry)
        val blueEntry = Entry(currentTime, avgBlue.toFloat())
        blueData.add(blueEntry)

        lineGraphFragment.updateGraph(redData, greenData, blueData)

        return timestamp
    }*/

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
        if (!isCameraReleased){
            camera?.setPreviewCallback(null)
            camera?.stopPreview()
            camera?.release()
            camera = null
            isCameraReleased = true
            Log.d(TAG, "Camera released")
        }
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
                Toast.makeText(this, "Camera permission is required", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun calculateAverageColor(data: ByteArray, width: Int, height: Int): Triple<Int, Int, Int> {
        val yuv = Mat(height + height / 2, width, CvType.CV_8UC1)
        yuv.put(0, 0, data)

        val rgb = Mat()
        Imgproc.cvtColor(yuv, rgb, Imgproc.COLOR_YUV2RGB_NV21, 3)

        var redSum = 0L
        var greenSum = 0L
        var blueSum = 0L
        val pixelCount = width * height

        for (y in 0 until height) {
            for (x in 0 until width) {
                val pixel = rgb.get(y, x)
                redSum += pixel[0].toInt()
                greenSum += pixel[1].toInt()
                blueSum += pixel[2].toInt()
            }
        }

        val avgRed = (redSum / pixelCount).toInt()
        val avgGreen = (greenSum / pixelCount).toInt()
        val avgBlue = (blueSum / pixelCount).toInt()

        return Triple(avgRed, avgGreen, avgBlue)
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
        //releaseCamera()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        Log.d(TAG, "onSaveInstanceState")

        supportFragmentManager.putFragment(outState, "RealTimeRGBFragment", realTimeRGBFragment)
        supportFragmentManager.putFragment(outState, "LineGraphFragment", lineGraphFragment)

        /*val rEntryArray = redData.map { entry -> floatArrayOf(entry.x, entry.y) }.toTypedArray()
        outState.putSerializable("redData", rEntryArray)
        val gEntryArray = greenData.map { entry -> floatArrayOf(entry.x, entry.y) }.toTypedArray()
        outState.putSerializable("greenData", gEntryArray)
        val bEntryArray = blueData.map { entry -> floatArrayOf(entry.x, entry.y) }.toTypedArray()
        outState.putSerializable("blueData", bEntryArray)*/

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

        realTimeRGBFragment = supportFragmentManager.getFragment(savedInstanceState, "RealTimeRGBFragment") as RealTimeRGBFragment
        lineGraphFragment = supportFragmentManager.getFragment(savedInstanceState, "LineGraphFragment") as LineGraphFragment

        /*val savedRedEntries = savedInstanceState.getSerializable("redData") as? Array<FloatArray>
        savedRedEntries?.let {
            redData = it.map { e -> Entry(e[0], e[1]) }.toMutableList()
        }
        val savedGreenEntries = savedInstanceState.getSerializable("greenData") as? Array<FloatArray>
        savedGreenEntries?.let {
            greenData = it.map { e -> Entry(e[0], e[1]) }.toMutableList()
        }
        val savedBlueEntries = savedInstanceState.getSerializable("blueData") as? Array<FloatArray>
        savedBlueEntries?.let {
            blueData = it.map { e -> Entry(e[0], e[1]) }.toMutableList()
        }*/

        savedPreviewSize = camera?.Size(savedInstanceState.getInt("previewWidth"), savedInstanceState.getInt("previewHeight"))
        savedPreviewFpsRange = savedInstanceState.getIntArray("previewFpsRange")
    }

    companion object {
        private const val CAMERA_REQUEST_CODE = 100
        private const val TAG = "MainActivity"
    }
}