package com.nikhil.imagesapp.ui.base

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import androidx.appcompat.widget.Toolbar
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import com.afollestad.materialdialogs.MaterialDialog
import com.nikhil.imagesapp.R
import com.nikhil.imagesapp.extensions.whenNotNull
import dagger.android.support.DaggerAppCompatActivity

@SuppressLint("Registered")
open class BaseActivity : DaggerAppCompatActivity(), BaseErrorInterface {

    var toolbar: Toolbar? = null

    private var progressDialog: MaterialDialog? = null

    //If back button is displayed in action bar, return false
    var isDisplayHomeAsUpEnabled: Boolean
        get() = false
        set(value) {
            if (supportActionBar != null)
                supportActionBar!!.setDisplayHomeAsUpEnabled(value)
        }

    override fun setContentView(layoutResID: Int) {
        super.setContentView(layoutResID)

        injectViews()

        //Displaying the back button in the action bar
        if (isDisplayHomeAsUpEnabled) {
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        }
    }

    protected fun injectViews() {
        toolbar = findViewById(R.id.toolbar)
        setupToolbar()
    }

    fun setContentViewWithoutInject(layoutResId: Int) {
        super.setContentView(layoutResId)
    }

    protected fun setupToolbar() {
        whenNotNull(toolbar) {
            setSupportActionBar(it)
        }
    }

    fun setActivityTitle(title: Int) {
        whenNotNull(supportActionBar) {
            it.setTitle(title)
        }
    }

    fun setActivityTitle(title: String) {
        whenNotNull(supportActionBar) {
            it.title = title
        }
    }

    private fun showProgressDialog() {
        if (!isFinishing) {
            if (progressDialog == null) {
                initMaterialDialog()
            } else if (!progressDialog!!.isShowing) {
                progressDialog!!.show()
            }
        }
    }

    private fun initMaterialDialog() {
        progressDialog = MaterialDialog.Builder(this)
                .content(R.string.please_wait)
                .cancelable(false)
                .progress(true, 0)
                .show()
    }

    private fun hideProgressDialog() {
        whenNotNull(progressDialog) {
            progressDialog!!.dismiss()
        }
    }

    override fun onPause() {
        super.onPause()
        hideProgressDialog()
    }

    open fun showLoading(active: Boolean) {
        if(active) showProgressDialog() else hideProgressDialog()
    }

    open fun showError(throwable: Throwable) {
        showError(throwable, findViewById(android.R.id.content))
    }

}
