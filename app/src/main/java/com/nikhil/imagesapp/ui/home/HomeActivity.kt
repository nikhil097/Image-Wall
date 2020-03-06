package com.nikhil.imagesapp.ui.home

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.nikhil.imagesapp.R
import com.nikhil.imagesapp.inject.ViewModelFactory
import com.nikhil.imagesapp.ui.base.BaseActivity
import kotlinx.android.synthetic.main.activity_home.*
import javax.inject.Inject

class HomeActivity : BaseActivity() {

    companion object {

        private val TAG = HomeActivity::class.java.name

        fun launchActivity(startingActivity: Context) {
            val intent = Intent(startingActivity, HomeActivity::class.java)
            startingActivity.startActivity(intent)
        }
    }

    @Inject
    lateinit var mViewModelFactory: ViewModelFactory
    private lateinit var mViewModel: HomeViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        isDisplayHomeAsUpEnabled = true
        setActivityTitle(getString(R.string.home))

        mViewModel = ViewModelProvider(this, mViewModelFactory).get(HomeViewModel::class.java)
        setupObservers()
        mViewModel.getAlImages();
    }

    private fun setupObservers() {



    }

    private fun inflateData() {
        fab_upload_image.setOnClickListener {

        }
    }


}
