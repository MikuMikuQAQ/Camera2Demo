package com.camera.cameratest

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.support.v4.app.ActivityCompat
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.util.Log.e
import android.view.View
import android.widget.Toast
import com.bumptech.glide.Glide
import com.camera.cameratest.camera2.Camera2Activity
import com.camera.cameratest.util.HttpUtil
import com.camera.cameratest.util.LoadSystemPhotos
import com.camera.cameratest.database.CameraOrientation
import com.camera.cameratest.database.FilterLibrary
import com.camera.cameratest.firstrun.FirstFragment
import com.camera.cameratest.firstrun.SecondFragment
import com.camera.cameratest.firstrun.ThirdFragment
import com.camera.cameratest.firstrun.adapter.FragmentAdapter
import kotlinx.android.synthetic.main.activity_main.*
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import org.litepal.LitePal
import org.litepal.tablemanager.Connector
import java.io.IOException
import java.lang.Thread.sleep

class MainActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var adapter:FragmentAdapter

    private var fragments:MutableList<Fragment> = ArrayList()

    companion object {
        const val ADDRESS_AD = "https://cn.bing.com/sa/simg/hpb/NorthMale_EN-US8782628354_1920x1080.jpg"
        const val COUNT_TIME = 1
        var status = true
        var i = 7
    }

    private val callback = object : Callback{
        override fun onFailure(call: Call, e: IOException) {
            e("onFailure",e.toString())
        }

        override fun onResponse(call: Call, response: Response) {
            e("onResponse","start")
            runOnUiThread(Runnable {
                    Glide.with(this@MainActivity).load("https://cn.bing.com/sa/simg/hpb/NorthMale_EN-US8782628354_1920x1080.jpg").into(main_ad)
            })
        }
    }

    override fun onClick(v: View?) {
        when(v?.id){
            R.id.textView2 -> {
                val intent = Intent(this,Camera2Activity::class.java)
                startActivity(intent)
                status = false
                finish()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= 21) {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            window.statusBarColor = Color.TRANSPARENT
        }
        setContentView(R.layout.activity_main)

        initView()

    }

    override fun onResume() {
        super.onResume()
        firstRun()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            1 -> {
                if (grantResults.isNotEmpty()){
                    for (results in grantResults){
                        if (results != PackageManager.PERMISSION_GRANTED){
                            finish()
                        }else{
                            //e("RequestPermissions","playAD()")
                            playAD()
                        }
                    }
                }
            }
            else -> {
            }
        }
    }

    private fun initView(){
        textView2.setOnClickListener(this)
    }

    private fun firstRun(){
        val shared = getSharedPreferences("FirstRun",0)
        val firstRun = shared.getBoolean("First",true)
        if (firstRun){
            //Toast.makeText(this,"第一次",Toast.LENGTH_LONG).show()
            usesPermission()
            Thread(Runnable {
                writeDB()
            }).start()
            fragments.add(FirstFragment.newInstance())
            fragments.add(SecondFragment.newInstance())
            fragments.add(ThirdFragment.newInstance())
            adapter = FragmentAdapter(supportFragmentManager,this,fragments)
            first_run.adapter = adapter
            first_run.visibility = View.VISIBLE
        }else{
            //Toast.makeText(this,"no",Toast.LENGTH_LONG).show()
            status = true
            i = 5
            getUsesPermission()
        }
    }

    private var handler = object : Handler(){
        override fun handleMessage(msg: Message?) {
            super.handleMessage(msg)
            when(msg?.what){
                COUNT_TIME -> {
                    countdown_view.text = msg.arg1.toString()
                }
            }
        }
    }

    private var thread = Thread(Runnable {
        while (status){
            var msg = Message.obtain()
            msg.what = COUNT_TIME
            msg.arg1 = i
            handler.sendMessage(msg)
            i--
            if (i == 0) {
                val intent = Intent(this,Camera2Activity::class.java)
                startActivity(intent)
                status = false
                finish()
            }
            sleep(1000)
        }
    })

    private fun getUsesPermission() {
        var permissionList: MutableList<String> = ArrayList()
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.CAMERA)
        }
        if (permissionList.isNotEmpty()) {
            var permissions = permissionList.toTypedArray()
            ActivityCompat.requestPermissions(this, permissions, 1)
        }else{
            playAD()
        }
    }

    private fun usesPermission() {
        var permissionList: MutableList<String> = ArrayList()
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.CAMERA)
        }
        if (permissionList.isNotEmpty()) {
            var permissions = permissionList.toTypedArray()
            ActivityCompat.requestPermissions(this, permissions, 1)
        }else{
            val shared = getSharedPreferences("FirstRun",0)
            shared.edit().putBoolean("First",false).apply()
        }
    }

    private fun playAD(){
        loadAD()
        if(!thread.isAlive) thread.start()
    }

    private fun loadAD(){
        HttpUtil.sendOkHttpRequest(ADDRESS_AD,callback)
    }

    private fun addFilter(){
        FilterLibrary(0x00,"无滤镜").save()
        FilterLibrary(0x01,"黑白").save()
        FilterLibrary(0x02,"卡通").save()
        FilterLibrary(0x03,"素描").save()
        FilterLibrary(0x04,"水晶球").save()
        FilterLibrary(0x05,"鱼眼").save()
        FilterLibrary(0x06,"旋转").save()
        FilterLibrary(0x07,"浮雕").save()
    }

    private fun writeDB(){
        Connector.getDatabase()
        LitePal.deleteAll(CameraOrientation::class.java)
        LitePal.deleteAll(FilterLibrary::class.java)
        CameraOrientation(1).save()
        addFilter()
    }
}
