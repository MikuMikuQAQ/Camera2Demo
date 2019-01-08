package com.camera.cameratest.firstrun.adapter

import android.content.Context
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter

class FragmentAdapter : FragmentPagerAdapter {

    private lateinit var context: Context
    private var fragments:MutableList<Fragment> = ArrayList()

    constructor(fm: FragmentManager?, context: Context, fragments: MutableList<Fragment>) : super(fm) {
        this.context = context
        this.fragments = fragments
    }

    override fun getItem(p0: Int): Fragment {
        return fragments[p0]
    }

    override fun getCount(): Int {
        return fragments.size
    }
}