package eu.anifantakis.mod.coredata.network.operations

import okhttp3.Headers

sealed class NetworkResult<T>(
    val data: T? = null,
    val headers: Headers? = null,
    val message: String? = null
) {
    class Success<T>(data: T, headers: Headers? = null) : NetworkResult<T>(data, headers)
    class Error<T>(val exception: Exception? = null, message: String, headers: Headers? = null, data: T? = null) : NetworkResult<T>(data, headers, message)
    class Loading<T> : NetworkResult<T>()
}