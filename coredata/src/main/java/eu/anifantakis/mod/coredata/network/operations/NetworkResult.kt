package eu.anifantakis.mod.coredata.network.operations

import androidx.datastore.dataStore
import okhttp3.Headers

sealed class NetworkResult<T>(
    val data: T? = null,
    val headers: Headers? = null,
    val message: String? = null,
    val exception: Exception? = null
) {
    class Success<T>(data: T, headers: Headers? = null) : NetworkResult<T>(data, headers)
    class Error<T>(exception: Exception? = null, message: String, headers: Headers? = null, data: T? = null) : NetworkResult<T>(data, headers, message, exception)
    class Loading<T> : NetworkResult<T>()
}