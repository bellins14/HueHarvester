package com.example.hueharvester.ui

import android.hardware.Camera
import android.os.Bundle
import android.view.TextureView
import android.view.View
import android.widget.Button
import androidx.fragment.app.Fragment
import com.example.hueharvester.R

class CameraPreviewFragment : Fragment() {

    private lateinit var textureView: TextureView
    private lateinit var startButton: Button
    private var camera: Camera? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        textureView = view.findViewById(R.id.texture_view)
        startButton = view.findViewById(R.id.start_button)

        startButton.setOnClickListener {
            startCameraPreview()
        }
    }

    private fun startCameraPreview() {
        camera = Camera.open()
        camera?.setPreviewTexture(textureView.surfaceTexture)
        camera?.startPreview()
        startButton.visibility = View.GONE
    }
}
