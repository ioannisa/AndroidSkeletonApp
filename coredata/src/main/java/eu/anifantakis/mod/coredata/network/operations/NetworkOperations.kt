package eu.anifantakis.mod.coredata.network.operations

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import okhttp3.Headers
import retrofit2.Response
import timber.log.Timber

abstract class NetworkOperations () {
    private suspend fun <API, CACHED, DOMAIN> performNetworkOperation(
        cacheFetch: (suspend () -> Flow<CACHED>)? = null,
        apiRequest: suspend () -> Response<API>,
        cacheUpdate: (suspend (API) -> Unit)? = null,
        cacheRefetch: (suspend () -> Flow<CACHED>)? = null,

        apiToDomain: ((API) -> DOMAIN)? = null,
        cacheToDomain: ((CACHED) -> DOMAIN)? = null,

        onFetchFailed: (Throwable) -> Unit = { }
    ): Flow<NetworkResult<DOMAIN>> = flow {
        emit(NetworkResult.Loading())
        Timber.d("BaseRepository - (Loading)")

        var localDataCache: CACHED? = null
        if (cacheFetch != null) {
            localDataCache = cacheFetch().firstOrNull()
            localDataCache?.let {
                emitDomainData(headers = null, it, cacheToDomain)
            }
        }

        try {
            val response = apiRequest()
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    cacheUpdate?.invoke(body)

                    // if we use cache handle domain via cache
                    if (cacheRefetch != null) {
                        val cachedResult = cacheRefetch().firstOrNull()
                        cachedResult?.let {
                            emitDomainData(headers = null, it, cacheToDomain)
                        }
                    }
                    // if we don't use cache handle domain via network (api)
                    else {
                        response.body()?.let {
                            emitDomainData(response.headers(), it, apiToDomain)
                        }
                    }
                } else {
                    emit(NetworkResult.Error<DOMAIN>("BaseRepository - No data found", null))
                }
            } else {
                emit(NetworkResult.Error<DOMAIN>(message = response.message(), headers = response.headers()))
                Timber.e("BaseRepository - Network Fetch (Fail) - ${response.message()}")
            }
        } catch (e: Exception) {
            onFetchFailed(e)
            emit(NetworkResult.Error(e.localizedMessage ?: "Unknown error occurred", null))
            Timber.e("BaseRepository - Network Fetch (Fail) - Unknown Error")
        }
    }

    protected suspend fun <API, CACHED, DOMAIN> netopCachedDomain(
        cacheFetch: (suspend () -> Flow<CACHED>),
        apiRequest: suspend () -> Response<API>,
        cacheUpdate: (suspend (API) -> Unit),
        cacheRefetch: (suspend () -> Flow<CACHED>),
        cacheToDomain: ((CACHED) -> DOMAIN)? = null,
        onFetchFailed: (Throwable) -> Unit = { }
    ): Flow<NetworkResult<DOMAIN>> =
        performNetworkOperation(
            cacheFetch = cacheFetch,
            apiRequest = apiRequest,
            cacheUpdate = cacheUpdate,
            cacheRefetch = cacheRefetch,
            cacheToDomain = cacheToDomain,
            onFetchFailed = onFetchFailed
        )


    protected suspend fun <API, DOMAIN> netopDomain(
        apiRequest: suspend () -> Response<API>,
        apiToDomain: ((API) -> DOMAIN),
        onFetchFailed: (Throwable) -> Unit = { }
    ): Flow<NetworkResult<DOMAIN>> =
        performNetworkOperation<API, API, DOMAIN>(
            apiRequest = apiRequest,
            apiToDomain = apiToDomain,
            onFetchFailed = onFetchFailed
        )

    protected suspend fun <API> netop(
        apiRequest: suspend () -> Response<API>,
        onFetchFailed: (Throwable) -> Unit = { }
    ): Flow<NetworkResult<API>> =
        performNetworkOperation<API, API, API>(
            apiRequest = apiRequest,
            onFetchFailed = onFetchFailed
        )


    private suspend fun <R, D> FlowCollector<NetworkResult<D>>.emitDomainData(headers: Headers? = null, data: R, domainMapper: ((R) -> D)?) {
        val emitData: NetworkResult<D> = if (domainMapper != null) {
            NetworkResult.Success(domainMapper(data), headers)
        } else {
            NetworkResult.Success(data as D, headers)
        }
        emit(emitData)
        Timber.d("BaseRepository - Data Emitted")
    }
}

