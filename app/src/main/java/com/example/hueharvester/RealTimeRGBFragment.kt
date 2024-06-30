package com.example.hueharvester

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment

/**
 * A simple [Fragment] subclass.
 * This fragment is used to display the real-time RGB values of the camera preview.
 * It is composed of three [TextView]s, one for each color channel (red, green, blue),
 * and one for the average color.
 * The background of each [TextView] is colored with the corresponding color.
 * The RGB values are updated by calling the [updateRGBValues] method.
 * The RGB values are displayed in the format "R: r, G: g, B: b".
 * The average color is displayed in the format "Average color: (r, g, b)".
 * The RGB values are updated by calling the [updateRGBValues] method.
 *
 * @see Fragment
 * @see TextView
 * @see GradientDrawable
 * @see Color
 */
class RealTimeRGBFragment : Fragment() {

    private lateinit var redTextView: TextView
    private lateinit var greenTextView: TextView
    private lateinit var blueTextView: TextView
    private lateinit var averageColorTextView: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_rgb_values, container, false)

        redTextView = view.findViewById<TextView?>(R.id.red_text_view).apply { text = getString(R.string.red_txt, NA) }
        greenTextView = view.findViewById<TextView?>(R.id.green_text_view).apply { text = getString(R.string.green_txt, NA) }
        blueTextView = view.findViewById<TextView?>(R.id.blue_text_view).apply { text = getString(R.string.blue_txt, NA) }
        averageColorTextView = view.findViewById(R.id.average_color_text_view)

        //Log.d(TAG, "$TAG onCreateView")
        return view
    }

    /**
     * Updates the RGB values displayed in the fragment.
     * The RGB values are displayed in the format "R: r, G: g, B: b".
     * The average color is displayed in the format "Average color: (r, g, b)".
     * The background of each [TextView] is colored with the corresponding color.
     * @param r the red value
     * @param g the green value
     * @param b the blue value
     */
    fun updateRGBValues(r: Int, g: Int, b: Int) {
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

    /**
     * Updates the color of the background of the [TextView] with the given color.
     * @param view the [TextView] whose background color will be updated
     * @param color the color to set as background
     */
    private fun updateDrawableColor(view: TextView, color: Int) {
        val background = view.background as GradientDrawable
        background.setColor(color)
    }

    companion object {
        private const val NA = "N/A"
        private const val TAG = "RealTimeRGBFragment"
    }
}
