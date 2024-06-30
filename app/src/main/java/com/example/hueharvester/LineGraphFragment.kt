@file:Suppress("DEPRECATION")

package com.example.hueharvester

import android.graphics.Color
import android.os.Bundle
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

/**
 * A simple [Fragment] subclass.
 * This fragment is used to display the RGB values of the colors harvested in real time.
 * It is composed of three [LineChart]s, one for each color channel (red, green, blue).
 * The [updateGraph] method is used to update the data displayed in the graphs.
 * The [drawGraph] method is used to draw the graphs.
 * The [onSaveInstanceState] and [onViewStateRestored] methods are used to save and restore the state of the fragment.
 * The [entryToBundle] and [bundleToEntry] methods are used to convert [Entry] objects to [Bundle] objects and vice versa.
 * @see Fragment
 * @see LineChart
 * @see Entry
 * @see LineDataSet
 * @see LineData
 */
class LineGraphFragment : Fragment() {

    private lateinit var lineChart: LineChart
    private var redData: List<Entry> = emptyList()
    private var greenData: List<Entry> = emptyList()
    private var blueData: List<Entry> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        //Log.d(TAG, "$TAG onCreateView")

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

    /**
     * Updates the data displayed in the graphs.
     * @param data The list of [ColorData] objects to be displayed in the graphs
     * @param creationDataID The ID of the first [ColorData] object in the list
     */
    fun updateGraph(data: List<ColorData>, creationDataID: Int) {
        redData = data.map { Entry((it.id-creationDataID) / 1350f, it.red.toFloat()) }
        greenData = data.map { Entry((it.id-creationDataID) / 1350f, it.green.toFloat()) }
        blueData = data.map { Entry((it.id-creationDataID) / 1350f, it.blue.toFloat()) }

        drawGraph()
    }

    /**
     * Draws the graphs.
     */
    private fun drawGraph() {

        if(redData.isNotEmpty() && greenData.isNotEmpty() && blueData.isNotEmpty()) {
            val redDataSet = LineDataSet(redData, "Red").apply {
                color = Color.RED
                axisDependency = YAxis.AxisDependency.LEFT
                setDrawCircles(false)
            }
            val greenDataSet = LineDataSet(greenData, "Green").apply {
                color = Color.GREEN
                axisDependency = YAxis.AxisDependency.LEFT
                setDrawCircles(false)
            }
            val blueDataSet = LineDataSet(blueData, "Blue").apply {
                color = Color.BLUE
                axisDependency = YAxis.AxisDependency.LEFT
                setDrawCircles(false)
            }
            LineData(redDataSet, greenDataSet, blueDataSet).apply {
                lineChart.data = this
                lineChart.moveViewToX(this.entryCount.toFloat())
            }
            //Log.d(TAG, "$TAG updateGraph")
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        //Log.d(TAG, "$TAG onSaveInstanceState")

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
            //Log.i(TAG, "$TAG onViewStateRestored")
        }

    }

    /**
     * Converts an [Entry] object to a [Bundle] object.
     * @param entry The [Entry] object to be converted
     * @return The [Bundle] object obtained from the [Entry] object
     */
    private fun entryToBundle(entry: Entry): Bundle {
        return Bundle().apply {
            putFloat("x", entry.x)
            putFloat("y", entry.y)
        }
    }

    /**
     * Converts a [Bundle] object to an [Entry] object.
     * @param bundle The [Bundle] object to be converted
     * @return The [Entry] object obtained from the [Bundle] object
     */
    private fun bundleToEntry(bundle: Bundle): Entry {
        return Entry(bundle.getFloat("x"), bundle.getFloat("y"))
    }

    companion object {
        private const val TAG = "LineGraphFragment"
    }
}
