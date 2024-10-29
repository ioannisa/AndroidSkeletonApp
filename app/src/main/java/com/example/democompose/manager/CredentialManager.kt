package com.example.democompose.manager

import eu.anifantakis.lib.securepersist.PersistManager
import javax.inject.Inject

interface CredentialManager {
    fun setApiKey(apiKey: String)
    fun getApiKey(): String
}

class CredentialManagerImpl @Inject constructor(private val encryptedData: PersistManager): CredentialManager {
    private val apiKeyPref = "API_KEY"

    override fun setApiKey(apiKey: String) {
        encryptedData.sharedPrefs.put(apiKeyPref, apiKey)
    }

    override fun getApiKey(): String {
        return encryptedData.sharedPrefs.get(key = apiKeyPref, "") ?: ""
    }
}