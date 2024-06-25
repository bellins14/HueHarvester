@file:Suppress("DEPRECATION")

package com.example.hueharvester

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment

class RealTimeRGBFragment : Fragment() {

    private lateinit var redTextView: TextView
    private lateinit var greenTextView: TextView
    private lateinit var blueTextView: TextView
    private lateinit var averageColorTextView: TextView


    var isViewCreated = false
        private set

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true  // Mantieni il fragment durante il cambio di configurazione
        Log.d(TAG, "RGB Fagment onCreate")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_rgb_values, container, false)

        redTextView = view.findViewById(R.id.red_text_view)
        greenTextView = view.findViewById(R.id.green_text_view)
        blueTextView = view.findViewById(R.id.blue_text_view)
        averageColorTextView = view.findViewById(R.id.average_color_text_view)

        isViewCreated = true
        Log.d(TAG, "RGB Fagment onCreateView = $isViewCreated")
        return view
    }

    @SuppressLint("SetTextI18n")
    fun updateRGBValues(r: Int, g: Int, b: Int) {
        if (isViewCreated) {
            redTextView.apply {
                text = "R = $r"
                setBackgroundColor(Color.rgb(r, 0, 0))
            }
            greenTextView.apply {
                text = "G = $g"
                setBackgroundColor(Color.rgb(0, g, 0))
            }
            blueTextView.apply {
                text = "B = $b"
                setBackgroundColor(Color.rgb(0, 0, b))
            }
            averageColorTextView.apply {
                text = "Average Color"
                setBackgroundColor(Color.rgb(r, g, b))
            }
        }
    }

    companion object {
        private const val TAG = "RealTimeRGBFragment"
    }

    override fun onDetach() {
        super.onDetach()
        isViewCreated = false
        Log.d(TAG, "RGB Fagment onDetach")
    }
}
