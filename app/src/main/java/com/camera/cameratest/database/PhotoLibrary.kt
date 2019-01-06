package com.camera.cameratest.database

import org.litepal.crud.LitePalSupport

data class PhotoLibrary(
    val path:String?
) : LitePalSupport()