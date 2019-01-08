package com.camera.cameratest.camera2

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.FragmentTransaction
import android.view.Window
import android.view.WindowManager
import com.camera.cameratest.R
import com.camera.cameratest.util.LoadSystemPhotos

class Camera2Activity : AppCompatActivity() {

    lateinit var transaction:FragmentTransaction

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN)
        setContentView(R.layout.activity_camera)
        LoadSystemPhotos.loadDatabase(this)
        transaction = supportFragmentManager.beginTransaction()
        transaction.add(R.id.camera_view,Camera2Fragment.newInstance())
        transaction.commit()
    }

    fun restartFragment(){
        transaction = supportFragmentManager.beginTransaction()
        transaction.add(R.id.camera_view,Camera2Fragment.newInstance())
        transaction.commit()
    }
}
