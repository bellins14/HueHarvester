@file:Suppress("DEPRECATION")

package com.example.hueharvester

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import kotlinx.coroutines.launch


class LineGraphFragment : Fragment() {
    private lateinit var lineChart: LineChart
    var creationTimeMillis: Long = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true  // Mantieni il fragment durante il cambio di configurazione
        creationTimeMillis = System.currentTimeMillis()
        Log.d(TAG, "LineGraphFragment onCreate, creation time: $creationTimeMillis")
    }

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
        lineChart.setPinchZoom(false)
        lineChart.xAxis.isEnabled = true
        lineChart.xAxis.setAvoidFirstLastClipping(true)
        lineChart.xAxis.position = XAxis.XAxisPosition.BOTTOM

        Log.d(TAG, "LineGraphFragment onCreateView")
        return view
    }

    fun initializeGraph(data: List<ColorData>) {
        val redData = data.map { Entry((it.timestamp - creationTimeMillis) / 1000f / 60f, it.red.toFloat()) }
        val greenData = data.map { Entry((it.timestamp - creationTimeMillis) / 1000f / 60f, it.green.toFloat()) }
        val blueData = data.map { Entry((it.timestamp - creationTimeMillis) / 1000f / 60f, it.blue.toFloat()) }
        updateGraph(redData, greenData, blueData)

    }

    fun updateGraph(redData: List<Entry>, greenData: List<Entry>, blueData: List<Entry>) {

        val redDataSet = LineDataSet(redData, "Red").apply {
            color = Color.RED
            axisDependency = YAxis.AxisDependency.LEFT
            setDrawCircles(false)
            lineWidth = 2f
        }
        val greenDataSet = LineDataSet(greenData, "Green").apply {
            color = Color.GREEN
            axisDependency = YAxis.AxisDependency.LEFT
            setDrawCircles(false)
            lineWidth = 2f
        }
        val blueDataSet = LineDataSet(blueData, "Blue").apply {
            color = Color.BLUE
            axisDependency = YAxis.AxisDependency.LEFT
            setDrawCircles(false)
            lineWidth = 2f
        }

        val lineData = LineData(redDataSet, greenDataSet, blueDataSet)
        lineChart.data = lineData
        lineChart.moveViewToX(lineData.entryCount.toFloat())
        lineChart.invalidate()  // Refresh the chart

        //Log.d(TAG, "LineGraphFragment updateGraph")
    }

    override fun onDetach() {
        super.onDetach()
        Log.d(TAG, "LineGraphFragment onDetach")
    }

    companion object {
        private const val TAG = "LineGraphFragment"
    }
}