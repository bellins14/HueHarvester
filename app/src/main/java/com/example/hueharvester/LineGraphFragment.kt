@file:Suppress("DEPRECATION")

package com.example.hueharvester

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet

class LineGraphFragment : Fragment() {

    private lateinit var lineChart: LineChart
    var creationTimeMillis: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true  // Mantieni il fragment durante il cambio di configurazione
        creationTimeMillis = System.currentTimeMillis()
        Log.d(TAG, "LineGraphFragment onCreate")
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

    fun updateGraph(redData: List<Entry>, greenData: List<Entry>, blueData: List<Entry>) {
        //Collections.sort(redData, EntryXComparator())
        val redDataSet = LineDataSet(redData, "Red")
        redDataSet.color = Color.RED
        redDataSet.axisDependency = YAxis.AxisDependency.LEFT
        redDataSet.setDrawCircles(false)
        //redDataSet.lineWidth = 2f

        //Collections.sort(greenData, EntryXComparator())
        val greenDataSet = LineDataSet(greenData, "Green")
        greenDataSet.color = Color.GREEN
        greenDataSet.axisDependency = YAxis.AxisDependency.LEFT
        greenDataSet.setDrawCircles(false)
        //greenDataSet.lineWidth = 2f

        //Collections.sort(blueData, EntryXComparator())
        val blueDataSet = LineDataSet(blueData, "Blue")
        blueDataSet.color = Color.BLUE
        blueDataSet.axisDependency = YAxis.AxisDependency.LEFT
        blueDataSet.setDrawCircles(false)
        //blueDataSet.lineWidth = 2f

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


