package eu.anifantakis.mod.coredata

sealed class RepositoryResponse<T>(
    val data: T? = null,
    val message: String? = null
) {
    class Success<T>(data: T) : RepositoryResponse<T>(data)
    class Error<T>(message: String, data: T? = null) : RepositoryResponse<T>(data, message)
}