package com.example.hueharvester

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment

// TODO: commenta bene e documenta
class RealTimeRGBFragment : Fragment() {

    private lateinit var redTextView: TextView
    private lateinit var greenTextView: TextView
    private lateinit var blueTextView: TextView
    private lateinit var averageColorTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "ReaTimeRGBFragment onCreate")
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

        Log.d(TAG, "ReaTimeRGBFragment onCreateView")
        return view
    }

    fun updateRGBValues(r: Int, g: Int, b: Int) {
        redTextView.apply {
            text = getString(R.string.red_txt, r.toString())
            setBackgroundColor(Color.rgb(r, 0, 0))
            setTextColor(Color.WHITE)
        }
        greenTextView.apply {
            text = getString(R.string.green_txt, g.toString())
            setBackgroundColor(Color.rgb(0, g, 0))
            setTextColor(Color.WHITE)
        }
        blueTextView.apply {
            text = getString(R.string.blue_txt, b.toString())
            setBackgroundColor(Color.rgb(0, 0, b))
            setTextColor(Color.WHITE)

        }
        averageColorTextView.apply {
            text = getString(R.string.avg_color_txt)
            setBackgroundColor(Color.rgb(r, g, b))
            setTextColor(Color.WHITE)
        }
    }

    override fun onDetach() {
        super.onDetach()
        Log.d(TAG, "ReaTimeRGBFragment onDetach")
    }

    companion object {
        private const val TAG = "RealTimeRGBFragment"
    }
}
