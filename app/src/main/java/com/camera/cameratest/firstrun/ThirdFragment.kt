package com.camera.cameratest.firstrun

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.camera.cameratest.MainActivity
import com.camera.cameratest.R
import com.camera.cameratest.camera2.Camera2Activity
import kotlinx.android.synthetic.main.fragment_firstrun.view.*

class ThirdFragment : Fragment() {

    companion object {
        fun newInstance(): ThirdFragment {
            return ThirdFragment()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.fragment_firstrun, container, false)
        return v
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView(view)
    }

    private fun initView(v: View) {
        v.first_text.text = "快开始吧"
        v.start_run.visibility = View.VISIBLE
        v.start_run.setOnClickListener() {
            val intent = Intent(activity, Camera2Activity::class.java)
            startActivity(intent)
            MainActivity.status = false
            activity?.finish()
        }
    }
}