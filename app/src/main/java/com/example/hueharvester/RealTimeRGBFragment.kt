package com.example.hueharvester

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.hueharvester.databinding.FragmentRgbValuesBinding

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

    private var _binding: FragmentRgbValuesBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRgbValuesBinding.inflate(inflater, container, false)

        binding.redTextView.apply { text = getString(R.string.red_txt, NA) }
        binding.greenTextView.apply { text = getString(R.string.green_txt, NA) }
        binding.blueTextView.apply { text = getString(R.string.blue_txt, NA) }

        //Log.d(TAG, "$TAG onCreateView")
        return binding.root
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
        val textColor = ContextCompat.getColor(requireContext(), R.color.white)
        binding.redTextView.apply {
            text = getString(R.string.red_txt, r.toString())
            setTextColor(textColor)
            updateDrawableColor(this, Color.rgb(r, 0, 0))
        }

        binding.greenTextView.apply {
            text = getString(R.string.green_txt, g.toString())
            setTextColor(textColor)
            updateDrawableColor(this, Color.rgb(0, g, 0))
        }
        binding.blueTextView.apply {
            text = getString(R.string.blue_txt, b.toString())
            setTextColor(textColor)
            updateDrawableColor(this, Color.rgb(0, 0, b))

        }
        binding.averageColorTextView.apply {
            text = getString(R.string.avg_color_txt)
            setTextColor(textColor)
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val NA = "N/A"
        private const val TAG = "RealTimeRGBFragment"
    }
}
