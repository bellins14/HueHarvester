@file:Suppress("DEPRECATION")

package com.example.hueharvester

import android.Manifest
import android.content.pm.PackageManager
import android.content.res.Configuration
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
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout

class MainActivity : AppCompatActivity() {

    private lateinit var viewPager: ViewPager
    private lateinit var tabLayout: TabLayout
    private lateinit var realTimeRGBFragment: RealTimeRGBFragment
    private lateinit var cameraPreview: SurfaceView
    private var camera: Camera? = null

    private var currentAvgRed: Int = 0
    private var currentAvgGreen: Int = 0
    private var currentAvgBlue: Int = 0

    private var isSurfaceCreated = false
    private var previousSurfaceWidth = 0
    private var previousSurfaceHeight = 0

    private var isCameraReleased = true
    private var savedPreviewSize: Camera.Size? = null
    private var savedPreviewFpsRange: IntArray? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate")
        setContentView(R.layout.activity_main)

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
                Log.d(TAG, "onPageSelected")
                if (position == 0) {
                    val adapter = viewPager.adapter as ViewPagerAdapter
                    realTimeRGBFragment = adapter.getFragment(position) as RealTimeRGBFragment
                    Log.d(TAG, "RealTimeRGBFragment selected")
                }
            }

            override fun onPageScrollStateChanged(state: Int) {
                //Log.d(TAG, "onPageScrollStateChanged")
            }
        })

        val adapter = viewPager.adapter as ViewPagerAdapter
        realTimeRGBFragment = adapter.getFragment(0) as RealTimeRGBFragment

        if (checkCameraPermission()) {
            Log.d(TAG, "Camera permission granted")
            setupCameraPreview()
        } else {
            requestCameraPermission()
        }
    }

    private fun setupViewPager(viewPager: ViewPager) {
        val adapter = ViewPagerAdapter(supportFragmentManager)
        realTimeRGBFragment = RealTimeRGBFragment()
        adapter.addFragment(realTimeRGBFragment, getString(R.string.RGB_values))
        adapter.addFragment(LineGraphFragment(), getString(R.string.line_graph))
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
                } /*else {
                    camera?.startPreview()
                }*/
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
                    val minPreviewFpsRange = supportedPreviewFpsRanges?.minByOrNull { it[Camera.Parameters.PREVIEW_FPS_MIN_INDEX] }
                    minPreviewFpsRange?.let {
                        params.setPreviewFpsRange(it[Camera.Parameters.PREVIEW_FPS_MIN_INDEX], it[Camera.Parameters.PREVIEW_FPS_MAX_INDEX])
                        savedPreviewFpsRange = it
                    }
                }

                camera?.parameters = params
                camera?.setPreviewDisplay(holder)
                camera?.startPreview()

                camera?.setPreviewCallback { data, camera ->
                    val previewSize = camera.parameters.previewSize
                    val (avgRed, avgGreen, avgBlue) = calculateAverageColor(data, previewSize.width, previewSize.height)
                    runOnUiThread {
                        currentAvgRed = avgRed
                        currentAvgGreen = avgGreen
                        currentAvgBlue = avgBlue
                        realTimeRGBFragment.updateRGBValues(avgRed, avgGreen, avgBlue)

                        //Log.d(TAG, "Preview callback")
                    }
                }
                Log.d(TAG, "Camera setup successful")
            } catch (e: Exception) {
                releaseCamera()
                Log.e(TAG, "Camera setup failed", e)
            }
        }.start()
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

    /*override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE ||
            newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            Log.d(TAG, "onConfigurationChanged")
            releaseCamera()
            setupCameraPreview()
        }
    }*/

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.putInt("avgRed", currentAvgRed)
        outState.putInt("avgGreen", currentAvgGreen)
        outState.putInt("avgBlue", currentAvgBlue)
        Log.d(TAG, "onSaveInstanceState")

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
        currentAvgRed = savedInstanceState.getInt("avgRed")
        currentAvgGreen = savedInstanceState.getInt("avgGreen")
        currentAvgBlue = savedInstanceState.getInt("avgBlue")
        savedPreviewSize = camera?.Size(savedInstanceState.getInt("previewWidth"), savedInstanceState.getInt("previewHeight"))
        savedPreviewFpsRange = savedInstanceState.getIntArray("previewFpsRange")
        //realTimeRGBFragment.updateRGBValues(currentAvgRed, currentAvgGreen, currentAvgBlue)
        if(realTimeRGBFragment.isViewCreated) Log.d(TAG, "RGBFragment restored: $currentAvgRed, $currentAvgGreen, $currentAvgBlue")

    }

    private fun calculateAverageColor(data: ByteArray, width: Int, height: Int): Triple<Int, Int, Int> {
        var redSum = 0L
        var greenSum = 0L
        var blueSum = 0L
        val pixelCount = width * height

        for (i in data.indices step 4) {
            val red = data[i].toInt() and 0xFF
            val green = data[i + 1].toInt() and 0xFF
            val blue = data[i + 2].toInt() and 0xFF

            redSum += red
            greenSum += green
            blueSum += blue
        }

        val avgRed = (redSum / pixelCount).toInt()
        val avgGreen = (greenSum / pixelCount).toInt()
        val avgBlue = (blueSum / pixelCount).toInt()

        return Triple(avgRed, avgGreen, avgBlue)
    }

    companion object {
        private const val CAMERA_REQUEST_CODE = 100
        private const val TAG = "MainActivity"
    }
}
