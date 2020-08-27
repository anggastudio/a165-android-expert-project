package com.dicoding.tourismapp.core.data

import android.annotation.SuppressLint
import com.dicoding.tourismapp.core.data.source.remote.network.ApiResponse
import com.dicoding.tourismapp.core.utils.AppExecutors
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject

abstract class NetworkBoundResource<ResultType, RequestType>(private val mExecutors: AppExecutors) {

    private val result = PublishSubject.create<Resource<ResultType>>()
    private val mCompositeDisposable = CompositeDisposable()

    init {
        @Suppress("LeakingThis")
        val dbSource = loadFromDB()
        val db = dbSource
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .take(1)
            .subscribe {
                dbSource.unsubscribeOn(Schedulers.io())
                if (shouldFetch(it)) {
                    fetchFromNetwork()
                } else {
                    result.onNext(Resource.Success(it))
                }
            }
        mCompositeDisposable.add(db)
    }

    protected open fun onFetchFailed() {}

    protected abstract fun loadFromDB(): Flowable<ResultType>

    protected abstract fun shouldFetch(data: ResultType?): Boolean

    protected abstract fun createCall(): Flowable<ApiResponse<RequestType>>

    protected abstract fun saveCallResult(data: RequestType)

    private fun fetchFromNetwork() {

        val apiResponse = createCall()
        result.onNext(Resource.Loading(null))
        val response = apiResponse
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .take(1)
            .doOnComplete { mCompositeDisposable.dispose() }
            .subscribe { response ->
                when (response) {
                    is ApiResponse.Success -> successApiHandler(response)
                    is ApiResponse.Empty -> emptyApiHandler()
                    is ApiResponse.Error -> errorApiHandler(response)
                }
            }
        mCompositeDisposable.add(response)
    }

    fun asFlowable(): Flowable<Resource<ResultType>> =
        result.toFlowable(BackpressureStrategy.BUFFER)

    private fun errorApiHandler(response: ApiResponse.Error) {
        onFetchFailed()
        result.onNext(Resource.Error(response.errorMessage, null))
    }

    @SuppressLint("CheckResult")
    private fun emptyApiHandler() {
        val dbSource = loadFromDB()
        dbSource.subscribeOn(Schedulers.computation())
            .observeOn(AndroidSchedulers.mainThread())
            .take(1)
            .subscribe {
                dbSource.unsubscribeOn(Schedulers.io())
                result.onNext(Resource.Success(it))
            }
    }

    @SuppressLint("CheckResult")
    private fun successApiHandler(response: ApiResponse.Success<RequestType>) {
        saveCallResult(response.data)
        val dbSource = loadFromDB()
        dbSource.subscribeOn(Schedulers.computation())
            .observeOn(AndroidSchedulers.mainThread())
            .take(1)
            .subscribe {
                dbSource.unsubscribeOn(Schedulers.io())
                result.onNext(Resource.Success(it))
            }
    }


}