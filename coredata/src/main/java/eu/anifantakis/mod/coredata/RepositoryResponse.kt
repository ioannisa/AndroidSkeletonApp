package eu.anifantakis.mod.coredata

sealed class RepositoryResponse<T>(
    open val data: T? = null,
    open val message: String? = null
) {
    data class Success<T>(override val data: T) : RepositoryResponse<T>(data)
    data class Error<T>(override val message: String, override val data: T? = null) : RepositoryResponse<T>(data, message)
}

sealed interface Result<out D, out E: Error> {
    data class Success<out D>(val data: D) : Result<D, Nothing>
    data class Error<out E: eu.anifantakis.mod.coredata.Error>(val error: E) : Result<Nothing, E>
}

/**
 * Map a Result<T, E> to Result<R, E>
 *
 *     where T is what we got from the network
 *     and R is what we want to map it to
 */
inline fun <T, E: Error, R> Result<T, E>.map(map: (T) -> R): Result<R, E> {
    return when (this) {
        is Result.Success -> Result.Success(map(this.data))
        is Result.Error -> Result.Error(this.error)
    }
}

/**
 * EmptyDataResult is a typealias for Result<Unit, E>, where E is an Error
 */
typealias EmptyDataResult<E> = Result<Unit, E>

/**
 * If we have a result, but don't care about it, we can map it to EmptyDataResult
 */
fun <T, E: Error> Result<T, E>.asEmptyDataResult(): EmptyDataResult<E> {
    return map { Unit }
}

interface Error

sealed interface DataError: Error {
    enum class Network: DataError {
        REQUEST_TIMEOUT,
        UNAUTHORISED,
        CONFLICT,
        TOO_MANY_REQUESTS,
        NO_INTERNET,
        PAYLOAD_TOO_LARGE,
        SERVER_ERROR,
        SERIALIZATION,
        UNKNOWN
    }

    enum class Local: DataError {
        DISK_FULL
    }
}