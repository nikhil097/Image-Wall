package com.nikhil.imagesapp.ui.camera

import android.Manifest
import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.hardware.display.DisplayManager
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.DisplayMetrics
import android.util.Log
import android.util.Size
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import android.webkit.MimeTypeMap
import android.widget.ImageButton
import android.widget.Toast
import androidx.camera.camera2.Camera2Config
import androidx.camera.core.*
import androidx.camera.core.impl.CameraCaptureMetaData
import androidx.camera.core.impl.PreviewConfig
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.net.toFile
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.afollestad.materialdialogs.MaterialDialog
import com.nikhil.imagesapp.BuildConfig
import com.nikhil.imagesapp.R
import com.nikhil.imagesapp.ui.base.BaseActivity
import com.nikhil.imagesapp.ui.home.HomeActivity
import com.nikhil.imagesapp.utils.startCropActivity
import com.tbruyelle.rxpermissions2.RxPermissions
import com.yalantis.ucrop.UCrop
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.activity_camera.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

class CameraActivity : BaseActivity(), View.OnTouchListener, ScaleGestureDetector.OnScaleGestureListener {

    companion object {

        private val TAG = CameraActivity::class.java.name

        fun launchActivity(startingActivity: Activity) {
            val intent = Intent(startingActivity, CameraActivity::class.java)
            startingActivity.startActivityForResult(intent, HomeActivity.REQUEST_CODE_OPEN_CAMERA)
        }

        private const val FILENAME = "yyyy-MM-dd-HH-mm-ss-SSS"
        private const val PHOTO_EXTENSION = ".jpg"
        private const val RATIO_4_3_VALUE = 4.0 / 3.0
        private const val RATIO_16_9_VALUE = 16.0 / 9.0

        private fun createFile(baseFolder: File, format: String, extension: String) =
            File(
                baseFolder, SimpleDateFormat(format, Locale.US)
                    .format(System.currentTimeMillis()) + extension
            )
    }

    private var rxPermissions: RxPermissions? = null
    private var compositeDisposable = CompositeDisposable()
    val KEY_EVENT_ACTION = "key_event_action"
    val KEY_EVENT_EXTRA = "key_event_extra"
    val ANIMATION_FAST_MILLIS = 50L
    val ANIMATION_SLOW_MILLIS = 100L

    private lateinit var container: ConstraintLayout
    private lateinit var viewFinder: PreviewView
    private lateinit var broadcastManager: LocalBroadcastManager
    private lateinit var cameraExecutor: ExecutorService

    private var displayId: Int = -1
    private var lensFacing: Int = CameraSelector.LENS_FACING_BACK
    private var preview: Preview? = null
    private var imageCapture: ImageCapture? = null
    private var camera: Camera? = null
    private lateinit var scaleDetector: ScaleGestureDetector
    private var lastScaleFactor = 0f
    private var flashMode: Int = ImageCapture.FLASH_MODE_ON

    private val displayManager by lazy {
        getSystemService(Context.DISPLAY_SERVICE) as DisplayManager
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)
        rxPermissions = RxPermissions(this)
        askPermissionsAndStartCamera()
    }

    private fun askPermissionsAndStartCamera() {
        compositeDisposable.clear()
        compositeDisposable.add(
            rxPermissions!!
                .request(Manifest.permission.CAMERA)
                .subscribe { granted ->
                    if (granted) {
                        initCameraView()
                    } else {
                        MaterialDialog.Builder(this)
                            .title(R.string.title_settings_dialog)
                            .content(R.string.rationale_ask_again)
                            .positiveText(R.string.allow)
                            .negativeText(R.string.deny)
                            .onPositive { dialog, which ->
                                Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                    data =
                                        Uri.fromParts("package", BuildConfig.APPLICATION_ID, null)
                                    startActivity(this)
                                }
                            }
                            .onNegative { dialog, which ->
                                dialog.dismiss()
                                finish()
                            }.show()
                    }
                }
        )
    }

    private val volumeDownReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.getIntExtra(KEY_EVENT_EXTRA, KeyEvent.KEYCODE_UNKNOWN)) {
                KeyEvent.KEYCODE_VOLUME_DOWN -> {
                    val shutter = container
                        .findViewById<ImageButton>(R.id.camera_capture_button)
                    simulateClick(shutter)
                }
            }
        }
    }

    fun simulateClick(imageButton: ImageButton, delay: Long = ANIMATION_FAST_MILLIS) {
        imageButton.run {
            performClick()
            isPressed = true
            invalidate()
            postDelayed({
                invalidate()
                isPressed = false
            }, delay)
        }
    }

    private val displayListener = object : DisplayManager.DisplayListener {
        override fun onDisplayAdded(displayId: Int) = Unit
        override fun onDisplayRemoved(displayId: Int) = Unit
        override fun onDisplayChanged(displayId: Int) = layout_camera_container?.let { view ->
            if (displayId == this@CameraActivity.displayId) {
                imageCapture?.targetRotation = view.display.rotation
            }
        } ?: Unit
    }

    private fun initCameraView() {
        container = layout_camera_container as ConstraintLayout
        viewFinder = container.findViewById(R.id.view_finder)
        scaleDetector = ScaleGestureDetector(this, this);
        viewFinder.setOnTouchListener(this)
        cameraExecutor = Executors.newSingleThreadExecutor()
        broadcastManager = LocalBroadcastManager.getInstance(layout_camera_container.context)
        val filter = IntentFilter().apply { addAction(KEY_EVENT_ACTION) }
        broadcastManager.registerReceiver(volumeDownReceiver, filter)

        displayManager.registerDisplayListener(displayListener, null)

        viewFinder.post {
            displayId = viewFinder.display.displayId
            updateCameraUi()
            bindCameraUseCases()
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        updateCameraUi()
    }

    private fun bindCameraUseCases() {
        val metrics = DisplayMetrics().also { viewFinder.display.getRealMetrics(it) }
        val screenAspectRatio = aspectRatio(metrics.widthPixels, metrics.heightPixels)
        val rotation = viewFinder.display.rotation

        val cameraSelector = CameraSelector.Builder().requireLensFacing(lensFacing).build()
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this@CameraActivity)
        cameraProviderFuture.addListener(Runnable {
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
            preview = Preview.Builder()
                .setTargetAspectRatio(screenAspectRatio)
                .setTargetRotation(rotation)
                .build()

            preview?.setSurfaceProvider(viewFinder.previewSurfaceProvider)
            imageCapture = ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .setTargetAspectRatio(screenAspectRatio)
                .setTargetRotation(rotation).
                    setFlashMode(flashMode)
                .build()

            cameraProvider.unbindAll()

            try {
                camera = cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageCapture
                )
            } catch (exc: Exception) {
            }
        }, ContextCompat.getMainExecutor(this@CameraActivity))
    }

    private fun aspectRatio(width: Int, height: Int): Int {
        val previewRatio = max(width, height).toDouble() / min(width, height)
        if (abs(previewRatio - RATIO_4_3_VALUE) <= abs(previewRatio - RATIO_16_9_VALUE)) {
            return AspectRatio.RATIO_4_3
        }
        return AspectRatio.RATIO_16_9
    }

    private fun updateCameraUi() {
        container.findViewById<ConstraintLayout>(R.id.camera_ui_container)?.let {
            container.removeView(it)
        }
        val controls = View.inflate(this@CameraActivity, R.layout.camera_ui_container, container)
        when (flashMode) {
            ImageCapture.FLASH_MODE_ON -> {
                controls.findViewById<ImageButton>(R.id.camera_flash_button).setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_flash_on))
            }
            ImageCapture.FLASH_MODE_OFF -> {
                controls.findViewById<ImageButton>(R.id.camera_flash_button).setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_flash_off))
            }
        }
        controls.findViewById<ImageButton>(R.id.camera_capture_button).setOnClickListener {
            imageCapture?.let { imageCapture ->

                val photoFile = createFile(cacheDir, FILENAME, PHOTO_EXTENSION)
                val metadata = ImageCapture.Metadata().apply {
                    isReversedHorizontal = lensFacing == CameraSelector.LENS_FACING_FRONT
                }
                val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile)
                    .setMetadata(metadata)
                    .build()
                imageCapture.takePicture(
                    outputOptions, cameraExecutor, object : ImageCapture.OnImageSavedCallback {
                        override fun onError(exc: ImageCaptureException) {
                            //on Error
                        }

                        override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                            startCropActivity(photoFile, this@CameraActivity)
                        }
                    })

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

                    container.postDelayed({
                        container.foreground = ColorDrawable(Color.WHITE)
                        container.postDelayed(
                            { container.foreground = null }, ANIMATION_FAST_MILLIS
                        )
                    }, ANIMATION_SLOW_MILLIS)
                }
            }
        }

        controls.findViewById<ImageButton>(R.id.camera_switch_button).setOnClickListener {
            lensFacing = if (CameraSelector.LENS_FACING_FRONT == lensFacing) {
                CameraSelector.LENS_FACING_BACK
            } else {
                CameraSelector.LENS_FACING_FRONT
            }
            bindCameraUseCases()
        }

       if (CameraSelector.LENS_FACING_BACK == lensFacing) {
           controls.findViewById<ImageButton>(R.id.camera_flash_button).setOnClickListener {
               flashMode = if (flashMode == ImageCapture.FLASH_MODE_OFF) ImageCapture.FLASH_MODE_ON else ImageCapture.FLASH_MODE_OFF
               updateCameraUi()
               bindCameraUseCases()
           }
       } else {
           controls.findViewById<ImageButton>(R.id.camera_flash_button).visibility = View.GONE
       }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)

        if(resultCode == RESULT_OK && requestCode == UCrop.REQUEST_CROP) {
            if (intent!=null) {
                val resultUri = UCrop.getOutput(intent)
                val resultantIntent = Intent()
                resultantIntent.putExtra(HomeActivity.IMAGE_URI_DATA, resultUri.toString())
                setResult(Activity.RESULT_OK, resultantIntent)
            } else {
                Toast.makeText(this@CameraActivity, R.string.error_msg_retrieve_selected_image, Toast.LENGTH_SHORT).show()
            }
            finish()
        } else if (resultCode == UCrop.RESULT_ERROR) {
            Toast.makeText(this@CameraActivity, R.string.error_msg_retrieve_selected_image, Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    override fun onTouch(view: View?, event: MotionEvent?): Boolean {
        scaleDetector.onTouchEvent(event)
        return true
    }

    override fun onScaleBegin(detector: ScaleGestureDetector?): Boolean {
        return true
    }

    override fun onScaleEnd(detector: ScaleGestureDetector?) {
    }

    override fun onScale(detector: ScaleGestureDetector?): Boolean {
        val zoomRatio: Float? = camera!!.cameraInfo.zoomState.value!!.zoomRatio
        val minZoomRatio: Float? = camera!!.cameraInfo.zoomState.value!!.minZoomRatio
        val maxZoomRatio: Float? = camera!!.cameraInfo.zoomState.value!!.maxZoomRatio
        val scaleFactor = scaleDetector.getScaleFactor()
        if (lastScaleFactor == 0f || (Math.signum(scaleFactor) == Math.signum(lastScaleFactor))) {
            camera!!.cameraControl.setZoomRatio(Math.max(minZoomRatio!!, Math.min(zoomRatio!! * scaleFactor, maxZoomRatio!!)))
            lastScaleFactor = scaleFactor
        } else {
            lastScaleFactor = 0f
        }
        return true
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
        broadcastManager.unregisterReceiver(volumeDownReceiver)
        displayManager.unregisterDisplayListener(displayListener)
    }

}