package com.example.data.repository

import com.example.data.local.InitialData
import com.example.data.local.ProviderDao
import com.example.data.model.ContactHistoryEntity
import com.example.data.model.ProviderEntity
import kotlinx.coroutines.flow.Flow

class LokSetuRepository(private val dao: ProviderDao) {

    val allProviders: Flow<List<ProviderEntity>> = dao.getAllProviders()
    val contactHistory: Flow<List<ContactHistoryEntity>> = dao.getContactHistory()

    fun getProvidersByCategory(category: String): Flow<List<ProviderEntity>> {
        return dao.getProvidersByCategory(category)
    }

    fun searchProviders(query: String): Flow<List<ProviderEntity>> {
        return dao.searchProviders(query)
    }

    suspend fun checkAndSeedInitialData(userLat: Double = 28.6139, userLng: Double = 77.2090) {
        val count = dao.getProviderCount()
        if (count < 12) {
            val seed = InitialData.getSeedProviders(userLat, userLng)
            dao.insertProviders(seed)
        }
    }

    suspend fun resetWithNewCenter(centerLat: Double, centerLng: Double) {
        val seed = InitialData.getSeedProviders(centerLat, centerLng)
        dao.insertProviders(seed)
    }

    suspend fun addProvider(provider: ProviderEntity): Long {
        return dao.insertProvider(provider)
    }

    suspend fun updateProvider(provider: ProviderEntity) {
        dao.updateProvider(provider)
    }

    suspend fun deleteProvider(provider: ProviderEntity) {
        dao.deleteProvider(provider)
    }

    suspend fun toggleFavorite(id: Long, isFavorite: Boolean) {
        dao.updateFavorite(id, isFavorite)
    }

    suspend fun logContactAction(
        provider: ProviderEntity,
        actionType: String,
        notes: String = ""
    ) {
        dao.insertContactHistory(
            ContactHistoryEntity(
                providerId = provider.id,
                providerName = provider.name,
                providerCategory = provider.category,
                providerPhone = provider.phone,
                actionType = actionType,
                notes = notes,
                timestamp = System.currentTimeMillis()
            )
        )
    }

    suspend fun clearHistory() {
        dao.clearContactHistory()
    }
}
