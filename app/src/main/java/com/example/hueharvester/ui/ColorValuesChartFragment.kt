package com.example.hueharvester.ui

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.hueharvester.R
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet

class ColorValuesChartFragment : Fragment() {

    private lateinit var lineChart: LineChart

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_color_values_chart, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        lineChart = view.findViewById(R.id.line_chart)

        setupChart()
    }

    private fun setupChart() {
        // Configure your chart here
        lineChart.apply {
            description.isEnabled = false
            setTouchEnabled(true)
            setPinchZoom(true)
            xAxis.position = XAxis.XAxisPosition.BOTTOM
            axisRight.isEnabled = false
        }

        // Example data
        val entries = listOf(
            Entry(0f, 0f),
            Entry(1f, 1f),
            Entry(2f, 2f)
        )

        val dataSet = LineDataSet(entries, "RGB Values").apply {
            color = Color.RED
            setCircleColor(Color.RED)
        }

        val lineData = LineData(dataSet)
        lineChart.data = lineData
        lineChart.invalidate() // refresh
    }
}
