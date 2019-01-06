package com.camera.cameratest.database

import org.litepal.crud.LitePalSupport

data class FilterLibrary(
        val filterNum:Int,
        val filterName:String
):LitePalSupport() {
}