package com.example.hueharvester.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.hueharvester.R

class ColorValuesTextFragment : Fragment() {

    private lateinit var redTextView: TextView
    private lateinit var greenTextView: TextView
    private lateinit var blueTextView: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_color_values_text, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        redTextView = view.findViewById(R.id.red_value)
        greenTextView = view.findViewById(R.id.green_value)
        blueTextView = view.findViewById(R.id.blue_value)

        // For demonstration, set some example values
        updateColorValues(250, 350, 475)
    }

    fun updateColorValues(red: Int, green: Int, blue: Int) {
        redTextView.text = "R: $red"
        greenTextView.text = "G: $green"
        blueTextView.text = "B: $blue"
    }
}
