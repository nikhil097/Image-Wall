package com.nikhil.imagesapp.ui.home

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.net.toFile
import androidx.core.net.toUri
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.afollestad.materialdialogs.MaterialDialog
import com.nikhil.imagesapp.BuildConfig
import com.nikhil.imagesapp.R
import com.nikhil.imagesapp.inject.ViewModelFactory
import com.nikhil.imagesapp.models.Image
import com.nikhil.imagesapp.ui.base.BaseActivity
import com.nikhil.imagesapp.ui.camera.CameraActivity
import com.nikhil.imagesapp.ui.home.imageSourceOptions.ChooseImageSourceBottomSheet
import com.nikhil.imagesapp.ui.viewImage.ViewImageActivity
import com.nikhil.imagesapp.utils.startCropActivity
import com.tbruyelle.rxpermissions2.RxPermissions
import com.yalantis.ucrop.UCrop
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.activity_home.*
import timber.log.Timber
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import javax.inject.Inject
import kotlin.math.min


class HomeActivity : BaseActivity(), ChooseImageSourceBottomSheet.Callbacks, ImagesAdapter.Callbacks {

    companion object {

        private val TAG = HomeActivity::class.java.name
        val REQUEST_CODE_OPEN_CAMERA = 1235
        val IMAGE_URI_DATA = "image_uri_data"

        fun launchActivity(startingActivity: Context) {
            val intent = Intent(startingActivity, HomeActivity::class.java)
            startingActivity.startActivity(intent)
        }
    }

    @Inject
    lateinit var mViewModelFactory: ViewModelFactory
    private lateinit var mViewModel: HomeViewModel
    private var rxPermissions: RxPermissions? = null
    private var compositeDisposable = CompositeDisposable()
    var mImages: MutableList<Image> = arrayListOf()
    private val REQUEST_CODE_OPEN_GALLERY = 1234

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        setActivityTitle(getString(R.string.home))

        mViewModel = ViewModelProvider(this, mViewModelFactory).get(HomeViewModel::class.java)
        rxPermissions = RxPermissions(this)
        setupObservers()
        mViewModel.getAllImages()

        fab_upload_image.setOnClickListener {
            ChooseImageSourceBottomSheet.showDialog(this, supportFragmentManager)
        }
    }

    private fun setupObservers() {

        mViewModel.allImagesObserver.observe(this, Observer { data ->

            data.run {

                showLoading(isLoading)

                response?.let {
                    mImages.clear()
                    mImages.addAll(it)
                    inflateData()
                }

                error?.let {
                    showError(it)
                }
            }
        })

        mViewModel.uploadImageObserver.observe(this, Observer { data ->

            data.run {

                super.showLoading(isLoading)
                response?.let {
                    mViewModel.getAllImages()
                }

                error?.let {
                    showError(it)
                }
            }
        })
    }

    override fun showLoading(active: Boolean) {
        if (active) {
            layout_shimmer.visibility = View.VISIBLE
            layout_shimmer.startShimmer()
        } else {
            layout_shimmer.visibility = View.GONE
            layout_shimmer.stopShimmer()
        }
    }

    private fun inflateData() {
        val adapter = ImagesAdapter(mImages)
        adapter.setCalbacks(this)
        recyclerView_images.layoutManager = GridLayoutManager(this, 2)
        recyclerView_images.adapter = adapter
    }

    private fun openGallery() {
        compositeDisposable.clear()
        compositeDisposable.add(
            rxPermissions!!
                .request(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .subscribe { granted ->
                    if(granted) {
                        pickImageFromGallery()
                    } else {
                        MaterialDialog.Builder(this)
                            .title(R.string.title_settings_dialog)
                            .content(R.string.rationale_ask_again)
                            .positiveText(R.string.allow)
                            .negativeText(R.string.deny)
                            .onPositive { dialog, which ->
                                Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                    data = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null)
                                    startActivity(this)
                                }
                            }
                            .onNegative { dialog, which ->
                                dialog.dismiss()
                            }.show()
                    }
                }
        )
    }

    private fun pickImageFromGallery() {
        val i = Intent(
            Intent.ACTION_PICK,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        )
        startActivityForResult(i, REQUEST_CODE_OPEN_GALLERY)
    }

    private fun getFileName(uri: Uri): String? {
        var result: String? = null
        if (uri.scheme.equals("content")) {
            val cursor = contentResolver.query(uri, null, null, null, null)
            cursor.use { cursor ->
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME))
                }
            }
        }
        if (result == null) {
            result = uri.path
            val cut = result?.lastIndexOf ('/')
            if (cut != -1) {
                result = result?.substring(cut!! + 1)
            }
        }
        return result
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_CODE_OPEN_GALLERY) {
                if (intent != null) {
                    val uri = intent.data
                    startCropActivity(getFile(uri!!), this)
                }
            }
        }

        if(resultCode == RESULT_OK && requestCode == UCrop.REQUEST_CROP) {
            if (intent!=null) {
                val resultUri = UCrop.getOutput(intent)
                mViewModel.uploadImage(File(resultUri!!.path!!))
            }

        } else if (resultCode == UCrop.RESULT_ERROR) {
            val cropError = UCrop.getError(intent!!)
            Toast.makeText(this@HomeActivity, R.string.error_msg_retrieve_selected_image, Toast.LENGTH_SHORT).show()
        }

        if(resultCode == RESULT_OK && requestCode == REQUEST_CODE_OPEN_CAMERA) {
            if (intent!=null) {
                val resultFile: File = intent.getSerializableExtra(IMAGE_URI_DATA) as File
                mViewModel.uploadImage(resultFile)
            }
        }
    }

    private fun getFile(uri: Uri): File {
        val file = File(cacheDir, getFileName(uri!!)!!)
        val maxBufferSize = 1 * 1024 * 1024

        try {
            val inputStream = contentResolver.openInputStream(uri)
            val bytesAvailable = inputStream!!.available()
            val bufferSize = min(bytesAvailable, maxBufferSize)
            val buffers = ByteArray(bufferSize)

            val outputStream = FileOutputStream(file)
            while (true) {
                val readFile = inputStream.read(buffers)
                if (readFile != -1) outputStream.write(buffers, 0, readFile) else break
            }
            inputStream.close()
            outputStream.close()

        } catch (e : FileNotFoundException) {

        }

     return file
    }

    override fun onCameraSelected() {
        CameraActivity.launchActivity(this@HomeActivity)
    }

    override fun onGallerySelected() {
        openGallery()
    }

    override fun onItemClick(imageUrl: String) {
        ViewImageActivity.launchActivity(this@HomeActivity, imageUrl)
    }

}
