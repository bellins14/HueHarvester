package com.example.hueharvester.ui

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.example.hueharvester.R
import com.github.mikephil.charting.charts.LineChart

class ColorValuesFragment : Fragment() {

    private lateinit var viewPager: ViewPager2
    private lateinit var chart: LineChart

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewPager = view.findViewById(R.id.view_pager)
        chart = view.findViewById(R.id.line_chart)

        setupViewPager()
    }

    private fun setupViewPager() {
        val adapter = ColorPagerAdapter(this)
        viewPager.adapter = adapter
    }

    inner class ColorPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
        override fun getItemCount(): Int = 2

        override fun createFragment(position: Int): Fragment {
            return when (position) {
                0 -> ColorValuesTextFragment()
                1 -> ColorValuesChartFragment()
                else -> throw IllegalStateException("Unexpected position: $position")
            }
        }
    }
}
