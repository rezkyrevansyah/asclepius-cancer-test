package com.dicoding.asclepius.data.repository

import android.app.Application
import androidx.lifecycle.LiveData
import com.dicoding.asclepius.data.local.entity.History
import com.dicoding.asclepius.data.local.room.HistoryDao
import com.dicoding.asclepius.data.local.room.HistoryDatabase
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class HistoryRepository(private val application: Application) {

    private val histoDao: HistoryDao
    private val executor: ExecutorService = Executors.newSingleThreadExecutor()

    init {
        histoDao = HistoryDatabase.getDatabase(application).hisDao()
    }

    fun getAllHistory(): LiveData<List<History>> = histoDao.getAllHistory()

    fun insert(history: History) {
        executor.execute { histoDao.insert(history) }
    }

    fun delete(history: History) {
        executor.execute { histoDao.delete(history) }
    }

}