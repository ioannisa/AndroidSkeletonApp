package com.example.democompose.manager

import eu.anifantakis.mod.coredata.persist.PersistManager
import javax.inject.Inject

interface CredentialManager {
    fun setApiKey(apiKey: String)
    fun getApiKey(): String
}

class CredentialManagerImpl @Inject constructor(private val encryptedData: PersistManager): CredentialManager {
    private val apiKeyPref = "API_KEY"

    override fun setApiKey(apiKey: String) {
        encryptedData.encryptSharedPreference(apiKeyPref, apiKey)
    }

    override fun getApiKey(): String {
        return encryptedData.decryptSharedPreference(key = apiKeyPref, "") ?: ""
    }
}