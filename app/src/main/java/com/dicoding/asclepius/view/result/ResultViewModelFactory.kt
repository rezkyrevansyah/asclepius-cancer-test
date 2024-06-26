package com.dicoding.asclepius.view.result

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class ResultViewModelFactory(private val application: Application) :
    ViewModelProvider.NewInstanceFactory() {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ResultViewModel::class.java)) {
            return ResultViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown viewModel: " + modelClass.name)
    }

    companion object {
        @Volatile
        private var instance: ResultViewModelFactory? = null

        fun getInstance(application: Application): ResultViewModelFactory =
            instance ?: synchronized(this) {
                instance ?: createFactory(application)
            }.also { instance = it }

        private fun createFactory(application: Application): ResultViewModelFactory {
            return ResultViewModelFactory(application)
        }
    }
}
