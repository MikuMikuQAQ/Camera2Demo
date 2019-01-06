package com.camera.cameratest.camera2

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.*
import android.hardware.camera2.*
import android.os.Bundle
import android.support.v4.app.Fragment
import android.media.ImageReader
import android.os.Handler
import android.os.HandlerThread
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.StaggeredGridLayoutManager
import android.util.Log.e
import android.util.Size
import android.util.SparseIntArray
import android.view.*
import com.camera.cameratest.R
import com.camera.cameratest.camera2.adapter.FilterAdapter
import com.camera.cameratest.util.ImageSaver
import com.camera.cameratest.database.CameraOrientation
import com.camera.cameratest.database.FilterLibrary
import com.camera.cameratest.gallery.GalleryActivity
import com.camera.cameratest.preview.PreviewActivity
import kotlinx.android.synthetic.main.fragment_camera.*
import org.litepal.LitePal
import java.io.File
import java.lang.Long.signum
import java.util.*
import java.util.concurrent.Semaphore
import kotlin.collections.ArrayList

class Camera2Fragment : Fragment(), View.OnClickListener {

    companion object {
        var fitlerNum = 0x00

        const val STATE_PREVIEW = 0
        const val STATE_WAITING_LOCK = 1
        const val STATE_WAITING_PRECAPTURE = 2
        const val STATE_WAITING_NON_PRECAPTURE = 3
        const val STATE_PICTURE_TAKEN = 4

        fun newInstance(): Camera2Fragment {
            return Camera2Fragment()
        }

        class CompareSize : Comparator<Size> {
            override fun compare(o1: Size?, o2: Size?): Int = signum(o1?.width?.toLong()!! * o1.height - o2?.width?.toLong()!! * o2.height)
        }
    }

    private lateinit var cameraManager: CameraManager
    private lateinit var camera: CameraDevice
    private lateinit var cameraId: String
    private lateinit var characteristics: CameraCharacteristics
    private lateinit var backgroundThread: HandlerThread
    private lateinit var backgroundHandler: Handler
    private lateinit var captureRequestBuilder: CaptureRequest.Builder
    private lateinit var captureRequest: CaptureRequest
    private lateinit var imageReader: ImageReader
    private lateinit var file: File
    private lateinit var captureSession: CameraCaptureSession
    private lateinit var Orientation: CameraOrientation
    private lateinit var previewSize:Size
    private lateinit var adapter:FilterAdapter

    private var foucsStatus: Int = STATE_PREVIEW
    private var cameraOrientation: Int = 0
    private var orientations = SparseIntArray()
    private var isOpenFlash = false
    private var flashSupport = false
    private var isAutoFlash = false
    private var sensorOrientation = 0
    private var filterLibrarys:MutableList<FilterLibrary> = ArrayList()

    private val MAX_PREVIEW_WIDTH = 1920
    private val MAX_PREVIEW_HEIGHT = 1080
    private val cameraOpenLock = Semaphore(1)

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.camera_tripper -> {
                lockFoucs()
                e("camera_tripper", "lockFoucs()")
            }
            R.id.camera_scale -> {
                Orientation = LitePal.findFirst(CameraOrientation::class.java)
                if (Orientation.orientation == 1) {
                    LitePal.deleteAll(CameraOrientation::class.java)
                    val orientation = CameraOrientation(0)
                    orientation.save()
                } else if (Orientation.orientation == 0) {
                    LitePal.deleteAll(CameraOrientation::class.java)
                    val orientation = CameraOrientation(1)
                    orientation.save()
                }
                closeCamera()
                stopBackgroundThread()
                cameraManager == null
                val camera2Activity = activity as Camera2Activity
                camera2Activity.restartFragment()
            }
            R.id.camera_gallery -> {
                val intent = Intent(activity, GalleryActivity::class.java)
                activity?.startActivity(intent)
                activity?.finish()
            }
            R.id.camera_flash_off -> {
                Orientation = LitePal.findFirst(CameraOrientation::class.java)
                if (Orientation.orientation == 0) {
                    isAutoFlash = true
                    isOpenFlash = true
                    camera_flash_off.visibility = View.GONE
                    camera_flash_auto.visibility = View.VISIBLE
                    camera.createCaptureSession(Arrays.asList(Surface(camera_texture.surfaceTexture), imageReader.surface),
                            object : CameraCaptureSession.StateCallback() {
                                override fun onConfigureFailed(session: CameraCaptureSession) {
                                }

                                override fun onConfigured(session: CameraCaptureSession) {
                                    captureSession = session
                                    setBackFlash(captureRequestBuilder)
                                    captureSession.setRepeatingRequest(captureRequestBuilder.build(), captureCallback, backgroundHandler)
                                }

                            }, null)
                } else {
                        isAutoFlash = false
                        isOpenFlash = true
                        camera_flash_off.visibility = View.GONE
                        camera_flash_on.visibility = View.VISIBLE
                }
            }
            R.id.camera_flash_auto -> {
                isAutoFlash = false
                isOpenFlash = true
                camera_flash_auto.visibility = View.GONE
                camera_flash_on.visibility = View.VISIBLE
                camera.createCaptureSession(Arrays.asList(Surface(camera_texture.surfaceTexture), imageReader.surface),
                        object : CameraCaptureSession.StateCallback() {
                            override fun onConfigureFailed(session: CameraCaptureSession) {
                            }

                            override fun onConfigured(session: CameraCaptureSession) {
                                captureSession = session
                                setBackFlash(captureRequestBuilder)
                                captureSession.setRepeatingRequest(captureRequestBuilder.build(), captureCallback, backgroundHandler)
                            }

                        }, null)
            }
            R.id.camera_flash_on -> {
                Orientation = LitePal.findFirst(CameraOrientation::class.java)
                if (Orientation.orientation == 1) {
                    isOpenFlash = false
                    isAutoFlash = false
                    camera_flash_on.visibility = View.GONE
                    camera_flash_off.visibility = View.VISIBLE
                    camera.createCaptureSession(Arrays.asList(Surface(camera_texture.surfaceTexture), imageReader.surface),
                            object : CameraCaptureSession.StateCallback() {
                                override fun onConfigureFailed(session: CameraCaptureSession) {
                                }

                                override fun onConfigured(session: CameraCaptureSession) {
                                    captureSession = session
                                    setBackFlash(captureRequestBuilder)
                                    captureSession.setRepeatingRequest(captureRequestBuilder.build(), captureCallback, backgroundHandler)
                                }

                            }, null)
                } else {
                    isOpenFlash = false
                    isAutoFlash = false
                    camera_flash_on.visibility = View.GONE
                    camera_flash_off.visibility = View.VISIBLE
                }
            }
            R.id.camera_filter ->{
                if (filter_list.visibility == View.GONE){
                    filter_list.visibility = View.VISIBLE
                }else{
                    filter_list.visibility = View.GONE
                }
            }
        }
    }

    private val callback = object : CameraDevice.StateCallback() {
        override fun onOpened(camera: CameraDevice) {
            cameraOpenLock.release()
            //e("onOpened","callback")
            this@Camera2Fragment.camera = camera
        }

        override fun onDisconnected(camera: CameraDevice) {
            cameraOpenLock.release()
            camera.close()
        }

        override fun onError(camera: CameraDevice, error: Int) {
            onDisconnected(camera)
            activity?.finish()
        }
    }

    private val textureListener = object : TextureView.SurfaceTextureListener {
        override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture?, width: Int, height: Int) {

        }

        override fun onSurfaceTextureUpdated(surface: SurfaceTexture?) = Unit

        override fun onSurfaceTextureDestroyed(surface: SurfaceTexture?): Boolean = true

        override fun onSurfaceTextureAvailable(surface: SurfaceTexture?, width: Int, height: Int) {
            cameraOpenLock.release()
            openCamera(width,height)
        }
    }

    private val captureCallback = object : CameraCaptureSession.CaptureCallback() {

        private fun process(result: CaptureResult) {
            when (foucsStatus) {
                STATE_PREVIEW -> {
                }
                STATE_WAITING_LOCK -> {
                    /*val afStatus = result.get(CaptureResult.CONTROL_AF_STATE)
                    if (afStatus == null){
                        captureImage()
                    } else if (CaptureResult.CONTROL_AF_STATE_FOCUSED_LOCKED == afStatus || CaptureResult.CONTROL_AF_STATE_NOT_FOCUSED_LOCKED == afStatus){
                        val aeStatus = result.get(CaptureResult.CONTROL_AE_STATE)
                        if (aeStatus == null || aeStatus == CaptureResult.CONTROL_AE_STATE_CONVERGED) {
                            foucsStatus = STATE_PICTURE_TAKEN
                            captureImage()
                        }else{
                            advanceCaptureImage()
                        }
                        e("aeStatus",aeStatus.toString())
                    }
                    e("afStatus",afStatus.toString())*/
                    foucsStatus = STATE_PICTURE_TAKEN
                    captureImage()
                }
                STATE_WAITING_PRECAPTURE -> {
                    val aeStatus = result.get(CaptureResult.CONTROL_AE_STATE)
                    if (aeStatus == null || aeStatus == CaptureResult.CONTROL_AE_STATE_PRECAPTURE || aeStatus == CaptureRequest.CONTROL_AE_STATE_FLASH_REQUIRED) {
                        foucsStatus = STATE_WAITING_NON_PRECAPTURE
                    }
                }
                STATE_WAITING_NON_PRECAPTURE -> {
                    val aeStatus = result.get(CaptureResult.CONTROL_AE_STATE)
                    if (aeStatus == null || aeStatus != CaptureResult.CONTROL_AE_STATE_PRECAPTURE) {
                        foucsStatus = STATE_PICTURE_TAKEN
                        captureImage()
                    }
                }
            }
        }

        override fun onCaptureCompleted(session: CameraCaptureSession, request: CaptureRequest, result: TotalCaptureResult) {
            super.onCaptureCompleted(session, request, result)
            process(result)
        }

        override fun onCaptureProgressed(session: CameraCaptureSession, request: CaptureRequest, partialResult: CaptureResult) {
            super.onCaptureProgressed(session, request, partialResult)
            process(partialResult)
        }
    }

    private val onImageAvailableListener = ImageReader.OnImageAvailableListener {
        backgroundHandler.post(ImageSaver(it.acquireNextImage(), file))
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_camera, container, false)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getFilterList()
        initView(view)
    }

    override fun onResume() {
        super.onResume()
        file = File(activity?.getExternalFilesDir(null), "pic.jpg")
        getCamera()
        startBackgroundThread()
        if (camera_texture.isAvailable) {
            getUsesPermission()
        } else {
            camera_texture.surfaceTextureListener = textureListener
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            1 -> {
                if (grantResults.isNotEmpty()) {
                    for (results in grantResults) {
                        if (results != PackageManager.PERMISSION_GRANTED) {
                            activity!!.finish()
                        } else {
                            openCamera(camera_texture.width,camera_texture.height)
                        }
                    }
                }
            }
            else -> {
            }
        }
    }

    override fun onPause() {
        closeCamera()
        stopBackgroundThread()
        super.onPause()
    }

    private fun initView(v: View?) {
        camera_tripper.setOnClickListener(this)
        camera_scale.setOnClickListener(this)
        camera_gallery.setOnClickListener(this)
        camera_flash_off.setOnClickListener(this)
        camera_flash_on.setOnClickListener(this)
        camera_flash_auto.setOnClickListener(this)
        camera_filter.setOnClickListener(this)

        filter_list.layoutManager = StaggeredGridLayoutManager(3 , StaggeredGridLayoutManager.VERTICAL)
        filter_list.itemAnimator = DefaultItemAnimator()
        filter_list.setHasFixedSize(true)

        adapter = FilterAdapter(context!!,filterLibrarys)
        filter_list.adapter = adapter
    }

    private fun getFilterList(){
        filterLibrarys = LitePal.findAll(FilterLibrary::class.java)
    }

    private fun getCamera() {
        var frontId: String? = null
        var backId: String? = null

        var frontCharacteristics: CameraCharacteristics? = null
        var frontCameraOrientation: Int? = null

        var backCharacteristics: CameraCharacteristics? = null
        var backCameraOrientation: Int? = null

        Orientation = LitePal.findFirst(CameraOrientation::class.java)
        e("Orientation.orientation", Orientation.orientation.toString())
        val activity = activity
        cameraManager = activity?.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        for (i in cameraManager.cameraIdList) {
            val characteristics = cameraManager.getCameraCharacteristics(i)
            val orientation = characteristics.get(CameraCharacteristics.LENS_FACING)
            if (orientation == CameraCharacteristics.LENS_FACING_FRONT) {
                frontId = i
                frontCharacteristics = characteristics
                frontCameraOrientation = characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION)
            } else {
                backId = i
                backCharacteristics = characteristics
                backCameraOrientation = characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION)
            }
        }
        if (Orientation.orientation == 0) {
            cameraId = backId!!
            this@Camera2Fragment.characteristics = backCharacteristics!!
            cameraOrientation = backCameraOrientation!!
            flashSupport = backCharacteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
        } else {
            cameraId = frontId!!
            this@Camera2Fragment.characteristics = frontCharacteristics!!
            cameraOrientation = frontCameraOrientation!!
        }
    }

    private fun setCameraOutputs(width: Int, height: Int) {
        e("setCameraOutputs", "run")
        val map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
        val size: Size = Collections.max(Arrays.asList(*map.getOutputSizes(ImageFormat.JPEG)), CompareSize())
        imageReader = ImageReader.newInstance(size.width, size.height, ImageFormat.JPEG, 2)
        imageReader.setOnImageAvailableListener(onImageAvailableListener, backgroundHandler)

        val displayRotation = activity?.windowManager?.defaultDisplay?.rotation

        sensorOrientation = characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION)
        val swappedDimensions = areDimensionsSwapped(displayRotation!!)

        val displaySize = Point()
        activity?.windowManager?.defaultDisplay?.getSize(displaySize)
        val rotatedPreviewWidth = if (swappedDimensions) height else width
        val rotatedPreviewHeight = if (swappedDimensions) width else height
        var maxPreviewWidth = if (swappedDimensions) displaySize.y else displaySize.x
        var maxPreviewHeight = if (swappedDimensions) displaySize.x else displaySize.y

        if (maxPreviewWidth > MAX_PREVIEW_WIDTH) maxPreviewWidth = MAX_PREVIEW_WIDTH
        if (maxPreviewHeight > MAX_PREVIEW_HEIGHT) maxPreviewHeight = MAX_PREVIEW_HEIGHT

        previewSize = chooseOptimalSize(map.getOutputSizes(SurfaceTexture::class.java),
                rotatedPreviewWidth, rotatedPreviewHeight,
                maxPreviewWidth, maxPreviewHeight,
                size)

        if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            camera_texture.setAspectRatio(previewSize.width, previewSize.height)
        } else {
            camera_texture.setAspectRatio(previewSize.height, previewSize.width)
        }
    }

    @SuppressLint("MissingPermission")
    private fun openCamera(width: Int, height: Int) {
        cameraManager.openCamera(cameraId, callback, backgroundHandler)
        setCameraOutputs(width,height)
        configureTransform(width,height)
        createPreview()
    }

    fun closeCamera() {
        cameraOpenLock.acquire()
        when (true) {
            null != camera -> {
                camera.close()
            }
            null != imageReader -> {
                imageReader.close()
            }
        }
    }

    private fun createPreview() {
        captureRequestBuilder = camera.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
        captureRequestBuilder.addTarget(Surface(camera_texture.surfaceTexture))
        captureRequest = captureRequestBuilder.build()
        camera.createCaptureSession(Arrays.asList(Surface(camera_texture.surfaceTexture), imageReader.surface),
                object : CameraCaptureSession.StateCallback() {
                    override fun onConfigureFailed(session: CameraCaptureSession) {
                    }

                    override fun onConfigured(session: CameraCaptureSession) {
                        captureSession = session
                        setBackFlash(captureRequestBuilder)
                        captureSession.setRepeatingRequest(captureRequestBuilder.build(), captureCallback, backgroundHandler)
                    }

                }, null)
    }

    private fun getUsesPermission() {
        var permissionList: MutableList<String> = ArrayList()
        if (ContextCompat.checkSelfPermission(context!!, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
        if (ContextCompat.checkSelfPermission(context!!, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.CAMERA)
        }
        if (permissionList.isNotEmpty()) {
            val permissions = permissionList.toTypedArray()
            ActivityCompat.requestPermissions(activity!!, permissions, 1)
        } else {
            openCamera(camera_texture.width,camera_texture.height)
        }
    }

    private fun startBackgroundThread() {
        backgroundThread = HandlerThread("CameraBackground")
        backgroundThread.start()
        backgroundHandler = Handler(backgroundThread.looper)
    }

    fun stopBackgroundThread() {
        backgroundThread.quitSafely()
        try {
            backgroundThread.join()
        } catch (e: InterruptedException) {

        }
    }

    private fun lockFoucs() {
        captureRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, CameraMetadata.CONTROL_AF_TRIGGER_START)
        foucsStatus = STATE_WAITING_LOCK
        captureSession.capture(captureRequestBuilder.build(), captureCallback, backgroundHandler)
    }

    private fun unLockFoucs() {
        captureRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, CameraMetadata.CONTROL_AF_TRIGGER_CANCEL)
        setBackFlash(captureRequestBuilder)
        captureSession.capture(captureRequestBuilder.build(), captureCallback, backgroundHandler)
        foucsStatus = STATE_PREVIEW
        captureSession.setRepeatingRequest(captureRequest, captureCallback, backgroundHandler)
        activity?.runOnUiThread(Runnable {
            if (front_flash.visibility == View.VISIBLE){
                front_flash.visibility == View.GONE
            }
        })
        val intent = Intent(activity, PreviewActivity::class.java)
        intent.putExtra("package", Camera2Activity::class.java.toString())
        intent.putExtra("file", activity?.getExternalFilesDir(null).toString() + "/pic.jpg")
        intent.putExtra("kinds", fitlerNum.toString())
        activity?.startActivity(intent)

        activity?.finish()
    }

    private fun captureImage() {
        val activity = activity
        val captureRequest = camera.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE)
        captureRequest.addTarget(imageReader.surface)
        captureRequest.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE)
        setBackFlash(captureRequest)
        setFrontFlash()
        val rotation = activity?.windowManager?.defaultDisplay?.rotation!!
        captureRequest.set(CaptureRequest.JPEG_ORIENTATION, getRotation(rotation))

        val captureCallback = object : CameraCaptureSession.CaptureCallback() {
            override fun onCaptureCompleted(session: CameraCaptureSession, request: CaptureRequest, result: TotalCaptureResult) {
                super.onCaptureCompleted(session, request, result)
                unLockFoucs()
                e("captureCallback", file.toString())
            }
        }
        captureSession.stopRepeating()
        captureSession.abortCaptures()
        captureSession.capture(captureRequest.build(), captureCallback, null)
    }

    private fun setBackFlash(requestBuilder: CaptureRequest.Builder) {
        Orientation = LitePal.findFirst(CameraOrientation::class.java)
        if (Orientation.orientation == 0) {
            if (flashSupport) {
                if (isOpenFlash) {
                    if (isAutoFlash) {
                        requestBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH)
                    } else {
                        requestBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON_ALWAYS_FLASH)
                    }
                }
            }
        }
    }

    private fun setFrontFlash() {
        if (Orientation.orientation == 1) {
            if (isOpenFlash){
                activity?.runOnUiThread(Runnable {
                    front_flash.visibility = View.VISIBLE
                    val lp = activity?.window?.attributes
                    lp?.screenBrightness = 1.0f
                    activity?.window?.attributes = lp
                })
                //Settings.System.putInt(activity?.contentResolver,Settings.System.SCREEN_BRIGHTNESS_MODE,255)
            }
        }
    }

    /*private fun advanceCaptureImage(){
        captureRequestBuilder.set(CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER, CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER_START)
        foucsStatus = STATE_WAITING_PRECAPTURE
        captureSession.capture(captureRequestBuilder.build(),captureCallback,backgroundHandler)
    }*/

    private fun getRotation(rotation: Int): Int {
        val sensorOrientation = characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION)
        orientations.append(Surface.ROTATION_0, 90)
        orientations.append(Surface.ROTATION_90, 0)
        orientations.append(Surface.ROTATION_180, 270)
        orientations.append(Surface.ROTATION_270, 180)
        return (orientations.get(rotation) + sensorOrientation + 270) % 360
    }

    private fun configureTransform(viewWidth: Int, viewHeight: Int) {
        val rotation = activity?.windowManager?.defaultDisplay?.rotation
        val matrix = Matrix()
        val viewRect = RectF(0f, 0f, viewWidth.toFloat(), viewHeight.toFloat())
        val bufferRect = RectF(0f, 0f, previewSize.height.toFloat(), previewSize.width.toFloat())
        val centerX = viewRect.centerX()
        val centerY = viewRect.centerY()

        if (Surface.ROTATION_90 == rotation || Surface.ROTATION_270 == rotation) {
            bufferRect.offset(centerX - bufferRect.centerX(), centerY - bufferRect.centerY())
            val scale = Math.max(
                    viewHeight.toFloat() / previewSize.height,
                    viewWidth.toFloat() / previewSize.width)
            with(matrix) {
                setRectToRect(viewRect, bufferRect, Matrix.ScaleToFit.FILL)
                postScale(scale, scale, centerX, centerY)
                postRotate((90 * (rotation - 2)).toFloat(), centerX, centerY)
            }
        } else if (Surface.ROTATION_180 == rotation) {
            matrix.postRotate(180f, centerX, centerY)
        }
        camera_texture.setTransform(matrix)
    }

    private fun areDimensionsSwapped(displayRotation: Int): Boolean {
        var swappedDimensions = false
        when (displayRotation) {
            Surface.ROTATION_0, Surface.ROTATION_180 -> {
                if (sensorOrientation == 90 || sensorOrientation == 270) {
                    swappedDimensions = true
                }
            }
            Surface.ROTATION_90, Surface.ROTATION_270 -> {
                if (sensorOrientation == 0 || sensorOrientation == 180) {
                    swappedDimensions = true
                }
            }
            else -> {
                //Log.e(TAG, "Display rotation is invalid: $displayRotation")
            }
        }
        return swappedDimensions
    }

    private fun chooseOptimalSize(choices: Array<Size>, textureViewWidth: Int, textureViewHeight: Int, maxWidth: Int, maxHeight: Int, aspectRatio: Size): Size {

        val bigEnough = ArrayList<Size>()
        val notBigEnough = ArrayList<Size>()
        val w = aspectRatio.width
        val h = aspectRatio.height
        for (option in choices) {
            if (option.width <= maxWidth && option.height <= maxHeight &&
                    option.height == option.width * h / w) {
                if (option.width >= textureViewWidth && option.height >= textureViewHeight) {
                    bigEnough.add(option)
                } else {
                    notBigEnough.add(option)
                }
            }
        }

        if (bigEnough.size > 0) {
            return Collections.min(bigEnough, CompareSizesByArea())
        } else if (notBigEnough.size > 0) {
            return Collections.max(notBigEnough, CompareSizesByArea())
        } else {
            //Log.e(TAG, "Couldn't find any suitable preview size")
            return choices[0]
        }
    }

    class CompareSizesByArea : Comparator<Size> {

        override fun compare(lhs: Size, rhs: Size) =
                signum(lhs.width.toLong() * lhs.height - rhs.width.toLong() * rhs.height)

    }

}
