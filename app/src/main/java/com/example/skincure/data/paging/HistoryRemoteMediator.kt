package com.example.skincure.data.paging

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import com.example.skincure.data.local.AppDatabase
import com.example.skincure.data.local.ResultRemoteKeys
import com.example.skincure.data.remote.response.HistoriesItem
import com.example.skincure.data.remote.retrofit.ApiService
import kotlin.collections.firstOrNull
import kotlin.collections.isNotEmpty

@OptIn(ExperimentalPagingApi::class)
class HistoryRemoteMediator(
    private val database: AppDatabase,
    private val apiService: ApiService,
) : RemoteMediator<Int, HistoriesItem>() {

    private companion object {
        const val INITIAL_PAGE_INDEX = 1
    }

    override suspend fun initialize(): InitializeAction {
        return InitializeAction.LAUNCH_INITIAL_REFRESH
    }

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, HistoriesItem>,
    ): MediatorResult {
        val page = when (loadType) {
            LoadType.REFRESH -> {
                val remoteKeys = getRemoteKeyClosestToCurrentPosition(state)
                remoteKeys?.nextKey?.minus(1) ?: INITIAL_PAGE_INDEX
            }

            LoadType.PREPEND -> {
                val remoteKeys = getRemoteKeyForFirstItem(state)
                val prevKey = remoteKeys?.prevKey
                    ?: return MediatorResult.Success(endOfPaginationReached = remoteKeys != null)
                prevKey
            }

            LoadType.APPEND -> {
                val remoteKeys = getRemoteKeyForLastItem(state)
                val nextKey = remoteKeys?.nextKey
                    ?: return MediatorResult.Success(endOfPaginationReached = remoteKeys != null)
                nextKey
            }
        }

        try {
            val responseData = apiService.getPredictHistory(page.toString(), state.config.pageSize)
            val history = responseData.histories
            val endOfPaginationReached = history.isEmpty()

            database.withTransaction {
                if (loadType == LoadType.REFRESH) {
                    database.remoteKeysDao().deleteRemoteKeys()
                    database.historyDao().deleteStory()
                }
                val prevKey = if (page == 1) null else page - 1
                val nextKey = if (endOfPaginationReached) null else page + 1
                val keys = history.map {
                    ResultRemoteKeys(id = it.id.toString(), prevKey = prevKey, nextKey = nextKey)
                }
                database.remoteKeysDao().insertAll(keys)
                database.historyDao().insert(history)
            }
            return MediatorResult.Success(endOfPaginationReached = endOfPaginationReached)
        } catch (exception: Exception) {
            return MediatorResult.Error(exception)
        }
    }

    private suspend fun getRemoteKeyForLastItem(state: PagingState<Int,  HistoriesItem>): ResultRemoteKeys? {
        return state.pages.lastOrNull { it.data.isNotEmpty() }?.data?.lastOrNull()?.let { data ->
            database.remoteKeysDao().getRemoteKeysId(data.id.toString())
        }
    }

    private suspend fun getRemoteKeyForFirstItem(state: PagingState<Int,  HistoriesItem>): ResultRemoteKeys? {
        return state.pages.firstOrNull { it.data.isNotEmpty() }?.data?.firstOrNull()?.let { data ->
            database.remoteKeysDao().getRemoteKeysId(data.id.toString())
        }
    }

    private suspend fun getRemoteKeyClosestToCurrentPosition(state: PagingState<Int, HistoriesItem>): ResultRemoteKeys? {
        return state.anchorPosition?.let { position ->
            state.closestItemToPosition(position)?.id?.let { id ->
                database.remoteKeysDao().getRemoteKeysId(id.toString())
            }
        }
    }

}