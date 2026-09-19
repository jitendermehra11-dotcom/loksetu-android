package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.ContactHistoryEntity
import com.example.data.model.ProviderEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProviderDao {

    @Query("SELECT * FROM providers ORDER BY isAvailableNow DESC, rating DESC")
    fun getAllProviders(): Flow<List<ProviderEntity>>

    @Query("SELECT * FROM providers WHERE category = :category ORDER BY isAvailableNow DESC, rating DESC")
    fun getProvidersByCategory(category: String): Flow<List<ProviderEntity>>

    @Query("""
        SELECT * FROM providers 
        WHERE name LIKE '%' || :query || '%' 
           OR speciality LIKE '%' || :query || '%' 
           OR subCategory LIKE '%' || :query || '%' 
           OR locationName LIKE '%' || :query || '%'
        ORDER BY isAvailableNow DESC, rating DESC
    """)
    fun searchProviders(query: String): Flow<List<ProviderEntity>>

    @Query("SELECT * FROM providers WHERE id = :id LIMIT 1")
    fun getProviderById(id: Long): Flow<ProviderEntity?>

    @Query("SELECT COUNT(*) FROM providers")
    suspend fun getProviderCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProvider(provider: ProviderEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProviders(providers: List<ProviderEntity>)

    @Update
    suspend fun updateProvider(provider: ProviderEntity)

    @Delete
    suspend fun deleteProvider(provider: ProviderEntity)

    @Query("UPDATE providers SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun updateFavorite(id: Long, isFavorite: Boolean)

    // Contact history
    @Query("SELECT * FROM contact_history ORDER BY timestamp DESC")
    fun getContactHistory(): Flow<List<ContactHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContactHistory(entry: ContactHistoryEntity): Long

    @Query("DELETE FROM contact_history")
    suspend fun clearContactHistory()
}
