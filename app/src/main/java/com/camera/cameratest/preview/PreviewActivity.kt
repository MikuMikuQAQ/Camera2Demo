package com.camera.cameratest.preview

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Matrix
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.camera.cameratest.R
import com.camera.cameratest.util.ShareUtil
import com.camera.cameratest.camera2.Camera2Activity
import com.camera.cameratest.gallery.GalleryActivity
import com.camera.cameratest.util.FilterUtil
import kotlinx.android.synthetic.main.activity_preview.*
import java.io.File

class PreviewActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var formIntent: Intent
    private lateinit var comeForm:String
    private lateinit var file:String
    private lateinit var bitmap: Bitmap
    private var kinds:Int = 0x00
    var bitmapOption = BitmapFactory.Options()

    override fun onClick(v: View?) {
        when(v?.id){
            R.id.share_qq -> {
                ShareUtil.shareQQ(this@PreviewActivity, bitmap)
            }
            R.id.share_sina -> {
                ShareUtil.shareSina(this@PreviewActivity,bitmap)
            }
            R.id.share_wechat -> {
                ShareUtil.shareWechat(this@PreviewActivity,bitmap)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= 21) {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            window.statusBarColor = Color.TRANSPARENT
        }
        setContentView(R.layout.activity_preview)
        formIntent = intent
        file = formIntent.getStringExtra("file")
        comeForm = formIntent.getStringExtra("package")
        bitmapOption.inSampleSize = 2

        initView()
        showImage(file)
    }

    override fun onBackPressed() {
        when(comeForm){
            Camera2Activity::class.java.toString() -> {
                val intent = Intent(this@PreviewActivity,Camera2Activity::class.java)
                startActivity(intent)
            }
            GalleryActivity::class.java.toString() -> {
            }
        }
        super.onBackPressed()
    }

    private fun initView(){
        share_qq.setOnClickListener(this)
        share_sina.setOnClickListener(this)
        share_wechat.setOnClickListener(this)
    }

    private fun showImage(fileString:String?){
        val option = RequestOptions().diskCacheStrategy(DiskCacheStrategy.NONE).skipMemoryCache(true)
        if (fileString != null){
            val file = File(fileString)
            if (comeForm == Camera2Activity::class.java.toString()){
                kinds = formIntent.getStringExtra("kinds").toInt()
                val bitmap = BitmapFactory.decodeFile(fileString,bitmapOption)//bitmapRotation(BitmapFactory.decodeFile(fileString,bitmapOption))//BitmapFactory.decodeFile(fileString,bitmapOption)
                this.bitmap = FilterUtil.imageFilter(this,bitmap, kinds)!!
                Glide.with(this@PreviewActivity).load(this.bitmap).apply(option).into(photo)
            }
            else
                Glide.with(this@PreviewActivity).load(
                        BitmapFactory.decodeFile(fileString, bitmapOption)).apply(option).into(photo)
        }
    }

    private fun bitmapRotation(bitmap: Bitmap):Bitmap{
        val m = Matrix()
        m.setRotate(90.0f,bitmap.width.toFloat(),bitmap.height.toFloat())
        val b = Bitmap.createBitmap(bitmap,0,0,bitmap.width,bitmap.height,m,true)
        return b
    }
}
