package com.nikhil.imagesapp.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.nikhil.imagesapp.data.ImagesRepository
import com.nikhil.imagesapp.data.remote.DataWrapper
import com.nikhil.imagesapp.models.Image
import io.reactivex.disposables.CompositeDisposable
import timber.log.Timber
import javax.inject.Inject

class HomeViewModel@Inject constructor(private val mImagesRepository: ImagesRepository): ViewModel() {

    private val mCompositeDisposable = CompositeDisposable()
    private val _allImagesObserver = MutableLiveData<DataWrapper<List<Image>>>()

    fun getAlImages() {
        _allImagesObserver.value = DataWrapper(isLoading = true)
        mCompositeDisposable.add(mImagesRepository.getAllImages()
            .subscribe ({
                _allImagesObserver.value = DataWrapper(response = it)
            }, {
                Timber.e(it)
                _allImagesObserver.value = DataWrapper(error = it)
            })
        )
    }

    override fun onCleared() {
        super.onCleared()
        mCompositeDisposable.clear()
    }

    val referEarnDataObserver: LiveData<DataWrapper<List<Image>>> = _allImagesObserver

}