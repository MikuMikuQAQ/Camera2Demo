package com.camera.cameratest.util

import android.content.Context
import android.database.Cursor
import android.provider.MediaStore
import com.camera.cameratest.database.PhotoLibrary
import org.litepal.LitePal
import java.lang.Exception

class LoadSystemPhotos {

    companion object {
        fun loadDatabase(context: Context?){
            Thread(Runnable {
                LitePal.deleteAll(PhotoLibrary::class.java)
                val resolver = context?.contentResolver
                var cursor:Cursor? = null
                try {
                    cursor = resolver?.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, null,
                            MediaStore.Images.Media.MIME_TYPE + "=? or "
                                    + MediaStore.Images.Media.MIME_TYPE + "=?",
                            arrayOf("image/jpeg", "image/png"),MediaStore.Images.Media.DATE_MODIFIED)
                    if (cursor != null && cursor.moveToFirst()){
                        do{
                            val photoLibary = PhotoLibrary(
                                    cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA))
                            )
                            //e("cursor",cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA)))
                            photoLibary.save()
                        }while (cursor.moveToNext())
                    }
                }catch (e:Exception){

                }finally {
                    if (cursor != null){
                        cursor.close()
                    }
                }
            }).start()
        }
    }

}