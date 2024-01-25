package com.example.democompose.manager

import com.example.democompose.utils.EncryptedData
import javax.inject.Inject

interface CredentialManager {
    fun setApiKey(apiKey: String)
    fun getApiKey(): String
}

class CredentialManagerImpl @Inject constructor(private val encryptedData: EncryptedData): CredentialManager {
    private val apiKeyPref = "API_KEY"

    override fun setApiKey(apiKey: String) {
        encryptedData.encryptSharedPreference(apiKeyPref, apiKey)
    }

    override fun getApiKey(): String {
        return encryptedData.decryptSharedPreference(key = apiKeyPref) ?: ""
    }
}