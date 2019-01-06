package com.camera.cameratest.util

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import android.widget.Toast
import com.camera.cameratest.util.ShareUtil.ScanApp.Companion.MOBILE_QQ
import com.camera.cameratest.util.ShareUtil.ScanApp.Companion.SINA
import com.camera.cameratest.util.ShareUtil.ScanApp.Companion.WECHAT

class ShareUtil {

    class ScanApp {

        companion object {
            const val WECHAT = "com.tencent.mm"
            const val MOBILE_QQ = "com.tencent.mobileqq"
            const val QZONE = "com.qzone"
            const val SINA = "com.sina.weibo"

            fun isInstallApp(context: Context?,appName:String):Boolean{
                val manager = context?.packageManager
                val info: MutableList<PackageInfo>? = manager?.getInstalledPackages(0)
                if (info != null && info.size > 0){
                    for (i in 1..info.size){
                        val name = info[i-1].packageName
                        if (appName == name){
                            return true
                        }
                    }
                }
                return false
            }
        }

    }

    companion object {

        fun shareQQ(context: Context?,bitmap: Bitmap?){
            if (ScanApp.isInstallApp(context,MOBILE_QQ)){
                try {
                    val uri = Uri.parse(MediaStore.Images.Media.insertImage(context?.contentResolver, bitmap, null, null))
                    val intent = Intent()
                    intent.action = Intent.ACTION_SEND
                    intent.putExtra(Intent.EXTRA_STREAM, uri)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    intent.type = "image/*"
                    val name = ComponentName("com.tencent.mobileqq", "com.tencent.mobileqq.activity.JumpActivity")
                    intent.component = name
                    context?.startActivity(Intent.createChooser(intent, "Share"))
                }catch (e:Exception){

                }
            }else{
                Toast.makeText(context,"未找到QQ",Toast.LENGTH_SHORT).show()
            }
        }

        fun shareWechat(context: Context?,bitmap: Bitmap?){
            if (ScanApp.isInstallApp(context,WECHAT)){
                try {
                    val uri = Uri.parse(MediaStore.Images.Media.insertImage(context?.contentResolver, bitmap, null, null))
                    val intent = Intent()
                    intent.action = Intent.ACTION_SEND
                    intent.putExtra(Intent.EXTRA_STREAM, uri)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    intent.type = "image/*"
                    val name = ComponentName("com.tencent.mm","com.tencent.mm.ui.tools.ShareImgUI")
                    intent.component = name
                    context?.startActivity(Intent.createChooser(intent, "Share"))
                }catch (e:Exception){

                }
            }else{
                Toast.makeText(context,"未找到微信",Toast.LENGTH_SHORT).show()
            }
        }

        fun shareSina(context: Context?,bitmap: Bitmap?){
            if (ScanApp.isInstallApp(context,SINA)){
                try {
                    val uri = Uri.parse(MediaStore.Images.Media.insertImage(context?.contentResolver, bitmap, null, null))
                    val intent = Intent()
                    intent.action = Intent.ACTION_SEND
                    intent.putExtra(Intent.EXTRA_STREAM, uri)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    intent.type = "image/*"
                    val manager = context?.packageManager
                    val info:MutableList<ResolveInfo>? = manager?.queryIntentActivities(intent,PackageManager.MATCH_DEFAULT_ONLY)
                    var resolveInfo:ResolveInfo? = null
                    for (i in info!!){
                        val name = i.activityInfo.applicationInfo.packageName
                        if ("com.sina.weibo" == name){
                            resolveInfo = i
                            break
                        }
                    }
                    intent.setClassName(SINA, resolveInfo?.activityInfo?.name)
                    context.startActivity(intent)
                }catch (e:Exception){

                }
            }else{
                Toast.makeText(context,"未找到新浪微博",Toast.LENGTH_SHORT).show()
            }
        }
    }

}