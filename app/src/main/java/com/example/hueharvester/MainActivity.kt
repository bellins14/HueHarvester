@file:Suppress("DEPRECATION")

package com.example.hueharvester

import android.content.pm.PackageManager
import android.hardware.Camera
import android.Manifest
import android.os.Bundle
import android.view.SurfaceView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout


class MainActivity : AppCompatActivity() {
    private lateinit var cameraPreview: SurfaceView
    private lateinit var camera: Camera
    private lateinit var tabLayout: TabLayout
    private lateinit var viewPager: ViewPager
    private val cameraRequestCode = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        cameraPreview = findViewById(R.id.camera_preview)
        viewPager = findViewById(R.id.viewPager)
        tabLayout = findViewById(R.id.tabLayout)

        setupViewPager(viewPager)
        tabLayout.setupWithViewPager(viewPager, true)

        if (checkCameraPermission()) {
            setupCamera()
        } else {
            requestCameraPermission()
        }
    }

    private fun setupViewPager(viewPager: ViewPager) {
        val adapter = ViewPagerAdapter(supportFragmentManager)
        adapter.addFragment(RealTimeRGBFragment(), "RGB Values")
        adapter.addFragment(LineGraphFragment(), "Line Graph")
        viewPager.adapter = adapter
    }

    private fun setupCamera() {
        camera = Camera.open()
        val params = camera.parameters

        // Get the supported preview sizes
        val supportedPreviewSizes = params.supportedPreviewSizes
        val minPreviewSize = supportedPreviewSizes.minByOrNull { it.width * it.height }

        // Get the supported preview FPS ranges
        val supportedPreviewFpsRanges = params.supportedPreviewFpsRange
        val minPreviewFpsRange = supportedPreviewFpsRanges.minByOrNull { it[Camera.Parameters.PREVIEW_FPS_MIN_INDEX] }

        // Set the minimum preview size and frame rate
        minPreviewSize?.let {
            params.setPreviewSize(it.width, it.height)
        }

        minPreviewFpsRange?.let {
            params.setPreviewFpsRange(it[Camera.Parameters.PREVIEW_FPS_MIN_INDEX], it[Camera.Parameters.PREVIEW_FPS_MAX_INDEX])
        }

        camera.parameters = params
        camera.setPreviewDisplay(cameraPreview.holder)
        camera.startPreview()

        /*camera.setPreviewCallbackWithBuffer { data, camera ->
            // TODO: Calculate average color values here
        }*/
    }

    private fun checkCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestCameraPermission() {
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), cameraRequestCode)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == cameraRequestCode) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                setupCamera()
            } else {
                Toast.makeText(this, "Camera permission is required", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onPause() {
        super.onPause()
        camera.stopPreview()
        camera.release()
    }

    override fun onResume() {
        super.onResume()
        if (checkCameraPermission()) {
            setupCamera()
        }
    }
}
