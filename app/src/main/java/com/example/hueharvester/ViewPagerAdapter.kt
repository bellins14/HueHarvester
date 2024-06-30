@file:Suppress("DEPRECATION")

package com.example.hueharvester

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter

/** Adapter for the ViewPager in the MainActivity
 *  @see FragmentStatePagerAdapter
 *  */
class ViewPagerAdapter(manager: FragmentManager) : FragmentStatePagerAdapter(manager) {

    private val fragmentList: MutableList<Fragment> = ArrayList()
    private val fragmentTitleList: MutableList<String> = ArrayList()

    override fun getItem(position: Int): Fragment {
        return fragmentList[position]
    }


    override fun getCount(): Int {
        return fragmentList.size
    }

    /** Adds a fragment to [fragmentList] and the fragment title to [fragmentTitleList] */
    fun addFragment(fragment: Fragment, title: String) {
        fragmentList.add(fragment)
        fragmentTitleList.add(title)
    }


    override fun getPageTitle(position: Int): CharSequence {
        return fragmentTitleList[position]
    }


    /** Returns the fragment at the given position in [fragmentList]
     * @param position The position of the fragment requested
     * @return [Fragment] */
    fun getFragment(position: Int): Fragment {
        return fragmentList[position]
    }
}