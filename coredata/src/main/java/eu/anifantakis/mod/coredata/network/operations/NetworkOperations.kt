package eu.anifantakis.mod.coredata.network.operations

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import retrofit2.Response
import timber.log.Timber

abstract class NetworkOperations () {
    /**
     * Performs a network operation with optional caching and data processing.
     * This function abstracts the common pattern of fetching data from a network,
     * optionally caching it, processing the response, and handling errors.
     *
     * The function is generic and can be used with various types of data.
     * It leverages Kotlin's coroutines and Flow for asynchronous operations and
     * reactive data handling.
     *
     * @param T The type of the network response.
     * @param R The type of the local cache data.
     * @param D The type of the final domain-specific data after mapping.
     * @param apiRequest A suspend lambda function for making the network request. It should return a Response<T>.
     * @param cacheFetch An optional suspend lambda function to fetch data from the local cache. Returns a Flow<R>.
     * @param processResponse An optional suspend lambda function to process the network response. Transforms T to R.
     * @param cacheUpdate An optional suspend lambda function to update the local cache with new data from the network. Consumes T.
     * @param cacheRefetch An optional suspend lambda function to refetch data from the cache after updating. Returns a Flow<R>.
     * @param domainMapper An optional lambda function to map the cached/processed data to the domain-specific type. Transforms R to D.
     * @param onFetchFailed An optional lambda function to handle exceptions that may occur during the network request or processing.
     *
     * @return A Flow of NetworkResult<D>, which represents the asynchronous stream of data states (Loading, Success, Error).
     */
    protected fun <T, R, D> performNetworkOperation(
        apiRequest: suspend () -> Response<T>,          // Lambda for making the API request.
        cacheFetch: (suspend () -> Flow<R>)? = null,    // Lambda for fetching data from cache (optional).
        processResponse: (suspend (T) -> R)? = null,    // Lambda for processing the network response (optional).
        cacheUpdate: (suspend (T) -> Unit)? = null,
        cacheRefetch: (suspend () -> Flow<R>)? = null,
        domainMapper: ((R) -> D)? = null,
        onFetchFailed: (Throwable) -> Unit = { }
    ): Flow<NetworkResult<D>> = flow {
        emit(NetworkResult.Loading())
        Timber.d("BaseRepository - (Loading)")

        var localDataCache: R? = null
        if (cacheFetch != null) {
            localDataCache = cacheFetch().firstOrNull()
            localDataCache?.let {
                emitData(it, domainMapper)
            }
        }

        try {
            val response = apiRequest()
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    cacheUpdate?.invoke(body)

                    val processedResponse = processResponse?.invoke(body) ?: body as R
                    val finalData = if (cacheRefetch != null) {
                        cacheRefetch().firstOrNull() ?: processedResponse
                    } else {
                        processedResponse
                    }

                    emitData(finalData, domainMapper)
                } else {
                    emit(NetworkResult.Error<D>("BaseRepository - No data found", null))
                }
            } else {
                emit(NetworkResult.Error<D>(response.message()))
                Timber.e("BaseRepository - Network Fetch (Fail) - ${response.message()}")
            }
        } catch (e: Exception) {
            onFetchFailed(e)
            emit(NetworkResult.Error(e.localizedMessage ?: "Unknown error occurred", null))
            Timber.e("BaseRepository - Network Fetch (Fail) - Unknown Error")
        }
    }

    suspend fun <R, D> FlowCollector<NetworkResult<D>>.emitData(data: R, domainMapper: ((R) -> D)?) {
        val emitData: NetworkResult<D> = if (domainMapper != null) {
            NetworkResult.Success(domainMapper(data))
        } else {
            NetworkResult.Success(data as D)
        }
        emit(emitData)
        Timber.d("BaseRepository - Data Emitted")
    }
}