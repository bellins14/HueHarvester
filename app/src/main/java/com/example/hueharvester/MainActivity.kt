@file:Suppress("DEPRECATION")

package com.example.hueharvester

import android.content.pm.PackageManager
import android.hardware.Camera
import android.Manifest
import android.content.res.Configuration
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

    override fun onCreate(savedInstanceState: Bundle?) {
        //enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        cameraPreview = findViewById(R.id.camera_preview)
        viewPager = findViewById(R.id.view_pager)
        tabLayout = findViewById(R.id.tab_layout)

        setupViewPager(viewPager)
        tabLayout.setupWithViewPager(viewPager, true)

        viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}

            override fun onPageSelected(position: Int) {
                if (position == 0) {
                    val adapter = viewPager.adapter as ViewPagerAdapter
                    realTimeRGBFragment = adapter.getFragment(position) as RealTimeRGBFragment
                }
            }

            override fun onPageScrollStateChanged(state: Int) {}
        })

        // Ottieni il fragment dall'adapter
        val adapter = viewPager.adapter as ViewPagerAdapter
        realTimeRGBFragment = adapter.getFragment(0) as RealTimeRGBFragment

        if (checkCameraPermission()) {
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
                Log.d("MainActivity", "Surface created")
                initializeCamera(holder)
            }

            override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
                Log.d("MainActivity", "Surface changed")
                releaseCamera()
                initializeCamera(holder)
            }

            override fun surfaceDestroyed(holder: SurfaceHolder) {
                Log.d("MainActivity", "Surface destroyed")
                releaseCamera()
            }
        })
    }

    private fun initializeCamera(holder: SurfaceHolder) {
        try {
            camera = Camera.open()
            adjustCameraOrientation()
            val params = camera?.parameters
            val supportedPreviewSizes = params?.supportedPreviewSizes
            val minPreviewSize = supportedPreviewSizes?.minByOrNull { it.width * it.height }

            minPreviewSize?.let { // TODO: cambia in implementazione normale
                params.setPreviewSize(it.width, it.height)
            }

            // TODO: salva preverences  per diminuire tempo caricamento senza che vengano calcolate ogni volta (a meno che dati app cancellati)
            val supportedPreviewFpsRanges = params?.supportedPreviewFpsRange
            val minPreviewFpsRange = supportedPreviewFpsRanges?.minByOrNull { it[Camera.Parameters.PREVIEW_FPS_MIN_INDEX] }

            minPreviewFpsRange?.let {
                params.setPreviewFpsRange(it[Camera.Parameters.PREVIEW_FPS_MIN_INDEX], it[Camera.Parameters.PREVIEW_FPS_MAX_INDEX])
            }

            camera?.parameters = params
            camera?.setPreviewDisplay(holder)
            camera?.startPreview()

            camera?.setPreviewCallback { data, camera ->
                // TODO: Calculate average color values here
                val previewSize = camera.parameters.previewSize
                val (avgRed, avgGreen, avgBlue) = calculateAverageColor(data, previewSize.width, previewSize.height)
                runOnUiThread {
                    currentAvgRed = avgRed
                    currentAvgGreen = avgGreen
                    currentAvgBlue = avgBlue
                    if (this::realTimeRGBFragment.isInitialized && realTimeRGBFragment.isViewCreated) {
                        realTimeRGBFragment.updateRGBValues(avgRed, avgGreen, avgBlue)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("MainActivity", "Camera setup failed", e)
            releaseCamera()
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
        releaseCamera()
    }

    override fun onResume() {
        super.onResume()
        if (checkCameraPermission()) {
            setupCameraPreview()
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE ||
            newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            releaseCamera()
            setupCameraPreview()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        // Salva lo stato necessario, come ad esempio il colore corrente
        outState.putInt("avgRed", currentAvgRed)
        outState.putInt("avgGreen", currentAvgGreen)
        outState.putInt("avgBlue", currentAvgBlue)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        // Ripristina lo stato salvato
        val avgRed = savedInstanceState.getInt("avgRed")
        val avgGreen = savedInstanceState.getInt("avgGreen")
        val avgBlue = savedInstanceState.getInt("avgBlue")
        realTimeRGBFragment.updateRGBValues(avgRed, avgGreen, avgBlue)
    }

    private fun calculateAverageColor(data: ByteArray, width: Int, height: Int): Triple<Int, Int, Int> {
        var redSum = 0L
        var greenSum = 0L
        var blueSum = 0L
        val pixelCount = width * height

        for (i in 0 until pixelCount step 4) {
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
    }
}