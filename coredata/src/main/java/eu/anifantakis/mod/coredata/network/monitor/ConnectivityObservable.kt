package eu.anifantakis.mod.coredata.network.monitor

import kotlinx.coroutines.flow.Flow

interface ConnectivityObservable {
    fun observe(): Flow<Status>

    enum class Status {
        Available,
        Unavailable
    }
}