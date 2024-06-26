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
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet

class LineGraphFragment : Fragment() {

    private lateinit var lineChart: LineChart
    private var rDataList: MutableList<Entry> = ArrayList()
    private var gDataList: MutableList<Entry> = ArrayList()
    private var bDataList: MutableList<Entry> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true  // Mantieni il fragment durante il cambio di configurazione
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
        lineChart.setPinchZoom(true)
        //lineChart.setDrawGridBackground(true)
        //lineChart.xAxis.setGranularity(1f)

        updateGraph(rDataList, gDataList, bDataList, false)

        Log.d(TAG, "LineGraphFragment onCreateView")
        return view
    }

    fun updateGraph(redData: List<Entry>, greenData: List<Entry>, blueData: List<Entry>, saveData: Boolean) {
        val redDataSet = LineDataSet(redData, "Red")
        redDataSet.color = Color.RED

        val greenDataSet = LineDataSet(greenData, "Green")
        greenDataSet.color = Color.GREEN

        val blueDataSet = LineDataSet(blueData, "Blue")
        blueDataSet.color = Color.BLUE

        if (saveData) {
            rDataList = redData.toMutableList()
            gDataList = greenData.toMutableList()
            bDataList = blueData.toMutableList()
        }

        val lineData = LineData(redDataSet, greenDataSet, blueDataSet)
        lineChart.data = lineData
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


