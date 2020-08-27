package com.dicoding.tourismapp.core.data.source.remote

import android.util.Log
import com.dicoding.tourismapp.core.data.source.remote.network.ApiResponse
import com.dicoding.tourismapp.core.data.source.remote.network.ApiService
import com.dicoding.tourismapp.core.data.source.remote.response.TourismResponse
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject

class RemoteDataSource private constructor(private val apiService: ApiService) {
    companion object {
        @Volatile
        private var instance: RemoteDataSource? = null

        fun getInstance(service: ApiService): RemoteDataSource =
            instance ?: synchronized(this) {
                instance ?: RemoteDataSource(service)
            }
    }

    fun getAllTourism(): Flowable<ApiResponse<List<TourismResponse>>> {
        val resultData = PublishSubject.create<ApiResponse<List<TourismResponse>>>()

        //get data from remote api
        val disposable = apiService.getList()
            .subscribeOn(Schedulers.computation())
            .observeOn(AndroidSchedulers.mainThread())
            .take(1)
            .subscribe({
                val dataArray = it.places
                resultData.onNext(if (dataArray.isNotEmpty()) ApiResponse.Success(dataArray) else ApiResponse.Empty)
            }, {
                resultData.onNext(ApiResponse.Error(it.message.toString()))
                Log.e("RemoteDataResource", it.toString())
            })
        return resultData.toFlowable(BackpressureStrategy.BUFFER)
    }
}

