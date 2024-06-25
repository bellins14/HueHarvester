package com.example.hueharvester

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet

class LineGraphFragment : Fragment() {

    private lateinit var lineChart: LineChart

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_line_graph, container, false)
        lineChart = view.findViewById(R.id.line_chart)

        // Configura il grafico
        lineChart.description.isEnabled = false
        lineChart.setTouchEnabled(true)
        lineChart.isDragEnabled = true
        lineChart.setScaleEnabled(true)
        lineChart.setPinchZoom(true)

        return view
    }

    fun updateGraph(redData: List<Entry>, greenData: List<Entry>, blueData: List<Entry>) {
        val redDataSet = LineDataSet(redData, "Red")
        redDataSet.color = Color.RED

        val greenDataSet = LineDataSet(greenData, "Green")
        greenDataSet.color = Color.GREEN

        val blueDataSet = LineDataSet(blueData, "Blue")
        blueDataSet.color = Color.BLUE

        val lineData = LineData(redDataSet, greenDataSet, blueDataSet)
        lineChart.data = lineData
        lineChart.invalidate()  // Refresh the chart
    }
}


