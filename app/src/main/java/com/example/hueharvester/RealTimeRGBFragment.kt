package com.example.hueharvester

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment

class RealTimeRGBFragment : Fragment() {

    private lateinit var rgbTextView: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_rgb_values, container, false)
        rgbTextView = view.findViewById(R.id.rgb_text_view)

        // TODO: Initialize UI components here

        return view
    }

    @SuppressLint("SetTextI18n")
    fun updateRGBValues(r: Int, g: Int, b: Int) {
        rgbTextView.text = "R: $r, G: $g, B: $b"
    }
}
