package com.example.hueharvester

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

        // TODO: Initialize and configure the line chart

        return view

    }

    fun updateGraph(data: List<Entry>) {
        val dataSet = LineDataSet(data, "RGB Averages")
        val lineData = LineData(dataSet)
        lineChart.data = lineData
        lineChart.invalidate() // refresh
    }
}

