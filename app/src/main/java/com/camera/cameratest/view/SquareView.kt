package com.camera.cameratest.view

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.RelativeLayout

class SquareView : RelativeLayout {

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        setMeasuredDimension(View.getDefaultSize(0,widthMeasureSpec),View.getDefaultSize(0,heightMeasureSpec))
        val size = measuredWidth
        super.onMeasure(MeasureSpec.makeMeasureSpec(size,MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(size,MeasureSpec.EXACTLY))
    }
}