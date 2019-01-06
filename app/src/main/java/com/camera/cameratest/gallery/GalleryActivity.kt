package com.camera.cameratest.gallery

import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.StaggeredGridLayoutManager
import android.view.MenuItem
import android.view.View
import com.camera.cameratest.R
import com.camera.cameratest.camera2.Camera2Activity
import com.camera.cameratest.database.PhotoLibrary
import com.camera.cameratest.gallery.adapter.GalleryAdapter
import kotlinx.android.synthetic.main.activity_gallery.*
import org.litepal.LitePal

class GalleryActivity : AppCompatActivity() {

    private lateinit var adapter:GalleryAdapter
    private var photoLibarys:MutableList<PhotoLibrary> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT in 21..22){
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            window.statusBarColor = Color.TRANSPARENT
        }
        if (Build.VERSION.SDK_INT >= 23) {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            window.statusBarColor = Color.TRANSPARENT
        }
        setContentView(R.layout.activity_gallery)

        loadPhotoList()

        initView()
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when(item?.itemId){
            android.R.id.home -> {
                val intent = Intent(this@GalleryActivity,Camera2Activity::class.java)
                startActivity(intent)
                finish()
            }
        }
        return true
    }

    private fun initView(){
        setSupportActionBar(gallery_toolbar)
        if (supportActionBar != null){
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_fanhui)
        }
        recycler_photos.layoutManager = StaggeredGridLayoutManager(4 , StaggeredGridLayoutManager.VERTICAL)
        recycler_photos.itemAnimator = DefaultItemAnimator()
        recycler_photos.setHasFixedSize(true)

        adapter = GalleryAdapter(this@GalleryActivity,photoLibarys)

        recycler_photos.adapter = adapter
    }

    private fun loadPhotoList(){
        photoLibarys = LitePal.findAll(PhotoLibrary::class.java)
    }

    override fun onBackPressed() {
        val intent = Intent(this@GalleryActivity,Camera2Activity::class.java)
        startActivity(intent)
        super.onBackPressed()
    }
}
