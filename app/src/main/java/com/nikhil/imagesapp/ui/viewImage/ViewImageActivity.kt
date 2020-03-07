package com.nikhil.imagesapp.ui.viewImage

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.View
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.request.transition.Transition
import com.davemorrissey.labs.subscaleview.ImageSource
import com.nikhil.imagesapp.R
import com.nikhil.imagesapp.ui.base.BaseActivity
import kotlinx.android.synthetic.main.activity_view_image.*
import timber.log.Timber

class ViewImageActivity : BaseActivity() {

    companion object {

        private val TAG = ViewImageActivity::class.java.name
        private val IMAGE_URL ="image_url"

        fun launchActivity(startingActivity: Context, imageUrl: String) {
            val intent = Intent(startingActivity, ViewImageActivity::class.java)
            intent.putExtra(IMAGE_URL, imageUrl)
            startingActivity.startActivity(intent)
        }
    }

    private var imageUrl: String? = null

    @SuppressLint("CheckResult")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_image)

        imageUrl = intent.getStringExtra(IMAGE_URL) ?: ""

        Glide.with(this)
            .load(imageUrl)
            .apply(
                RequestOptions.fitCenterTransform().override(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
                    .priority(Priority.HIGH))
            .listener(object: RequestListener<Drawable> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable>?,
                    isFirstResource: Boolean
                ): Boolean {
                    return false
                }

                override fun onResourceReady(
                    resource: Drawable?,
                    model: Any?,
                    target: Target<Drawable>?,
                    dataSource: DataSource?,
                    isFirstResource: Boolean
                ): Boolean {
                    runOnUiThread {
                        image_progress.visibility = View.GONE
                        image.setImage(ImageSource.bitmap((resource as BitmapDrawable).bitmap))
                    }
                    return true
                }

            }).submit()
    }

}