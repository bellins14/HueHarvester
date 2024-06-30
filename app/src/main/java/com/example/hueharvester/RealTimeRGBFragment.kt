package com.example.hueharvester

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
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

        redTextView = view.findViewById<TextView?>(R.id.red_text_view).apply { text = getString(R.string.red_txt, NA) }
        greenTextView = view.findViewById<TextView?>(R.id.green_text_view).apply { text = getString(R.string.green_txt, NA) }
        blueTextView = view.findViewById<TextView?>(R.id.blue_text_view).apply { text = getString(R.string.blue_txt, NA) }
        averageColorTextView = view.findViewById(R.id.average_color_text_view)

        Log.d(TAG, "ReaTimeRGBFragment onCreateView")
        return view
    }

    fun updateRGBValues(r: Int, g: Int, b: Int) {
        //lateinit var bkg: ShapeDrawable
        redTextView.apply {
            text = getString(R.string.red_txt, r.toString())
            setTextColor(Color.WHITE)
            updateDrawableColor(this, Color.rgb(r, 0, 0))
        }

        greenTextView.apply {
            text = getString(R.string.green_txt, g.toString())
            setTextColor(Color.WHITE)
            updateDrawableColor(this, Color.rgb(0, g, 0))
        }
        blueTextView.apply {
            text = getString(R.string.blue_txt, b.toString())
            setTextColor(Color.WHITE)
            updateDrawableColor(this, Color.rgb(0, 0, b))

        }
        averageColorTextView.apply {
            text = getString(R.string.avg_color_txt)
            setTextColor(Color.WHITE)
            updateDrawableColor(this, Color.rgb(r, g, b))
        }
    }

    private fun updateDrawableColor(view: TextView, color: Int) {
        val background = view.background as GradientDrawable
        background.setColor(color)
    }

    override fun onDetach() {
        super.onDetach()
        Log.d(TAG, "ReaTimeRGBFragment onDetach")
    }

    companion object {
        private const val NA = "N/A"
        private const val TAG = "RealTimeRGBFragment"
    }
}
