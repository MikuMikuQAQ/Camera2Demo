package com.camera.cameratest.util

import android.content.Context
import android.graphics.Bitmap
import jp.co.cyberagent.android.gpuimage.GPUImage
import jp.co.cyberagent.android.gpuimage.filter.*

class FilterUtil {
    companion object {

        const val IMAGE_GRAY = 0x01     //黑白
        const val IMAGE_TOON = 0x02     //卡通
        const val IMAGE_SKETCH = 0x03   //素描
        const val IMAGE_SPHERE = 0x04   //水晶球
        const val IMAGE_BULGE = 0x05    //鱼眼
        const val IMAGE_SWIRL = 0x06    //旋转
        const val IMAGE_EMBOSS = 0x07   //浮雕

        fun imageFilter(context: Context?,bitmap: Bitmap,kinds:Int?):Bitmap?{
            //e("imageFilter",kinds.toString())
            val gpuImage = GPUImage(context)
            when(kinds){
                IMAGE_GRAY -> gpuImage.setFilter(GPUImageGrayscaleFilter())
                IMAGE_TOON -> gpuImage.setFilter(GPUImageToonFilter())
                IMAGE_SKETCH -> gpuImage.setFilter(GPUImageSketchFilter())
                IMAGE_SPHERE -> gpuImage.setFilter(GPUImageGlassSphereFilter())
                IMAGE_BULGE -> gpuImage.setFilter(GPUImageBulgeDistortionFilter())
                IMAGE_SWIRL -> gpuImage.setFilter(GPUImageSwirlFilter())
                IMAGE_EMBOSS -> gpuImage.setFilter(GPUImageEmbossFilter())
            }
            gpuImage.setImage(bitmap)
            return gpuImage.bitmapWithFilterApplied
        }
    }
}