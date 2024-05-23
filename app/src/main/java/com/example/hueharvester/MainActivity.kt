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


class MainActivity : AppCompatActivity() {

    private lateinit var viewPager: ViewPager
    private lateinit var cameraPreview: SurfaceView
    private lateinit var camera: Camera
    private val cameraRequestCode = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        cameraPreview = findViewById(R.id.camera_preview)
        viewPager = findViewById(R.id.viewPager)

        setupViewPager(viewPager)

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
        params.setPreviewSize(640, 480)  // TODO: Example size, adjust as needed
        params.setPreviewFpsRange(15000, 30000)  // TODO: Example range, adjust as needed
        camera.parameters = params
        camera.setPreviewDisplay(cameraPreview.holder)
        camera.startPreview()

        camera.setPreviewCallback { data, camera ->
            // TODO: Calculate average color values here
        }
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
        camera.release()
    }

    override fun onResume() {
        super.onResume()
        if (checkCameraPermission()) {
            setupCamera()
        }
    }
}
