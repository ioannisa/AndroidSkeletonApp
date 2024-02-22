package eu.anifantakis.mod.coredata.network.operations

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import okhttp3.Headers
import retrofit2.Response
import timber.log.Timber
import java.io.IOException

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
                    emit(NetworkResult.Error<DOMAIN>(exception = null, message = "BaseRepository - No data found", headers =  null))
                }
            } else {
                emit(NetworkResult.Error<DOMAIN>(exception = null, message = response.message(), headers = response.headers()))
                Timber.e("BaseRepository - Network Fetch (Fail) - ${response.message()}")
            }
        } catch (e: Exception) {
            onFetchFailed(e)
            emit(NetworkResult.Error(exception = e, message = e.message ?: "", headers = null))
            Timber.e("BaseRepository - Network Fetch (Fail) - Unknown Error")
        }
    }

    /**
     * Performs a network operation with caching and domain mapping. This function fetches data from a local cache before attempting a network request. If the cache is empty or the caller wishes to refresh data, it proceeds with the network call, updates the cache with the new data, and optionally refetches it from the cache to ensure consistency. The data is then mapped from the cache or API to a domain-specific model before being emitted.
     *
     * @param cacheFetch A suspend function to fetch cached data.
     * @param apiRequest A suspend function to perform the API request.
     * @param cacheUpdate A suspend function to update the cache with new data from the API.
     * @param cacheRefetch A suspend function to refetch data from the cache after updating it.
     * @param cacheToDomain A function to map data from the cache to a domain-specific model.
     * @param onFetchFailed A lambda function that is invoked if the network request fails.
     * @return Flow<NetworkResult<DOMAIN>> A flow that emits loading, success, and error states of the network operation with domain-specific data.
     */
    protected suspend fun <API, CACHED, DOMAIN> netopCachedDomain(
        cacheFetch: (suspend () -> Flow<CACHED>),
        apiRequest: suspend () -> Response<API>,
        cacheUpdate: (suspend (API) -> Unit),
        cacheRefetch: (suspend () -> Flow<CACHED>),
        cacheToDomain: ((CACHED) -> DOMAIN),
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

    /**
     * Performs a network operation without caching but with domain mapping. This function directly performs a network request and maps the API response to a domain-specific model before emitting the result. It is suitable for cases where caching is not required or applicable.
     *
     * @param apiRequest A suspend function to perform the API request.
     * @param apiToDomain A function to map data from the API to a domain-specific model.
     * @param onFetchFailed A lambda function that is invoked if the network request fails.
     * @return Flow<NetworkResult<DOMAIN>> A flow that emits loading, success, and error states of the network operation with domain-specific data.
     */
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

    /**
     * Performs a basic network operation without caching or domain mapping. This function is the most straightforward, directly performing a network request and emitting the raw API response. It is suitable for cases where the raw API response is required without any modifications or mapping.
     *
     * @param apiRequest A suspend function to perform the API request.
     * @param onFetchFailed A lambda function that is invoked if the network request fails.
     * @return Flow<NetworkResult<API>> A flow that emits loading, success, and error states of the raw network operation.
     */
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

