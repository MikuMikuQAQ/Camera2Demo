package com.camera.cameratest.firstrun

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.camera.cameratest.R
import kotlinx.android.synthetic.main.fragment_firstrun.view.*

class SecondFragment : Fragment() {

    companion object {
        fun newInstance():SecondFragment{
            return SecondFragment()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.fragment_firstrun,container,false)
        return v
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView(view)
    }

    private fun initView(v: View){
        v.first_text.text = "还带滤镜功能哦"
    }
}