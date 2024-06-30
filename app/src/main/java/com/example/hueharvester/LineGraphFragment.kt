@file:Suppress("DEPRECATION")

package com.example.hueharvester

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.hueharvester.database.ColorData
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet

// TODO: commenta bene e documenta
class LineGraphFragment : Fragment() {
    private lateinit var lineChart: LineChart
    private var redData: List<Entry> = emptyList()
    private var greenData: List<Entry> = emptyList()
    private var blueData: List<Entry> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "$TAG onCreate")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d(TAG, "$TAG onCreateView")

        val view = inflater.inflate(R.layout.fragment_line_graph, container, false)

        lineChart = view.findViewById(R.id.line_chart)

        lineChart.apply {
            setNoDataText(getString(R.string.no_data_text))
            setTouchEnabled(true)
            setScaleEnabled(true)
            setPinchZoom(false)
            description.isEnabled = false
            isDragEnabled = false
            isHighlightPerTapEnabled = false
            xAxis.isEnabled = true
            xAxis.setAvoidFirstLastClipping(true)
            xAxis.position = XAxis.XAxisPosition.BOTTOM
        }

        drawGraph()

        return view
    }

    fun updateGraph(data: List<ColorData>, creationDataID: Int) {
        redData = data.map { Entry((it.id-creationDataID) / 1350f, it.red.toFloat()) }
        greenData = data.map { Entry((it.id-creationDataID) / 1350f, it.green.toFloat()) }
        blueData = data.map { Entry((it.id-creationDataID) / 1350f, it.blue.toFloat()) }

        drawGraph()
    }

    private fun drawGraph() {

        if(redData.isNotEmpty() && greenData.isNotEmpty() && blueData.isNotEmpty()) {
            val redDataSet = LineDataSet(redData, "Red").apply {
                color = Color.RED
                axisDependency = YAxis.AxisDependency.LEFT
                setDrawCircles(false)
                //lineWidth = 2f
            }
            val greenDataSet = LineDataSet(greenData, "Green").apply {
                color = Color.GREEN
                axisDependency = YAxis.AxisDependency.LEFT
                setDrawCircles(false)
                //lineWidth = 2f
            }
            val blueDataSet = LineDataSet(blueData, "Blue").apply {
                color = Color.BLUE
                axisDependency = YAxis.AxisDependency.LEFT
                setDrawCircles(false)
                //lineWidth = 2f
            }
            LineData(redDataSet, greenDataSet, blueDataSet).apply {
                lineChart.data = this
                lineChart.moveViewToX(this.entryCount.toFloat())
            }
            //Log.d(TAG, "$TAG updateGraph")
        }
    }

    override fun onDetach() {
        super.onDetach()
        Log.d(TAG, "$TAG onDetach")
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        Log.d(TAG, "$TAG onSaveInstanceState")

        val redDataBundles = redData.map { entryToBundle(it) }
        val greenDataBundles = greenData.map { entryToBundle(it) }
        val blueDataBundles = blueData.map { entryToBundle(it) }

        outState.putParcelableArrayList("redData", ArrayList(redDataBundles))
        outState.putParcelableArrayList("greenData", ArrayList(greenDataBundles))
        outState.putParcelableArrayList("blueData", ArrayList(blueDataBundles))
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)

        savedInstanceState?.let{ bundle ->
            redData = bundle.getParcelableArrayList<Bundle>("redData")?.map { bundleToEntry(it) } ?: emptyList()
            greenData = bundle.getParcelableArrayList<Bundle>("greenData")?.map { bundleToEntry(it) } ?: emptyList()
            blueData = bundle.getParcelableArrayList<Bundle>("blueData")?.map { bundleToEntry(it) } ?: emptyList()

            Log.i(TAG, "$TAG onViewStateRestored")
        }

    }

    private fun entryToBundle(entry: Entry): Bundle {

        return Bundle().apply {
            putFloat("x", entry.x)
            putFloat("y", entry.y)
        }
    }

    private fun bundleToEntry(bundle: Bundle): Entry {
        return Entry(bundle.getFloat("x"), bundle.getFloat("y"))
    }

    companion object {
        private const val TAG = "LineGraphFragment"
    }
}