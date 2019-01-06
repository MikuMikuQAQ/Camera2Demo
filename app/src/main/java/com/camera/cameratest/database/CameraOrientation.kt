package com.camera.cameratest.database

import org.litepal.crud.LitePalSupport

data class CameraOrientation(
        val orientation: Int?
) : LitePalSupport()