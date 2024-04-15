package eu.anifantakis.mod.coredata

sealed class RepositoryResponse<T>(
    open val data: T? = null,
    open val message: String? = null
) {
    data class Success<T>(override val data: T) : RepositoryResponse<T>(data)
    data class Error<T>(override val message: String, override val data: T? = null) : RepositoryResponse<T>(data, message)
}