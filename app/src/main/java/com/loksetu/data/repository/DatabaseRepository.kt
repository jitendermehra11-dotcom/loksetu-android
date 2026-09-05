package com.loksetu.data.repository

import com.loksetu.data.model.LokSetuListing
import com.loksetu.data.model.UserRole
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class DatabaseRepository {

    // लाइव डेटाबेस का स्टेट सिम्युलेटर
    private val _listings = MutableStateFlow<List<LokSetuListing>>(emptyList())
    val listings: Flow<List<LokSetuListing>> = _listings

    // नई लिस्टिंग जोड़ने का फ़ंक्शन (किसान, वर्कर या ड्राइवर)
    fun addListing(newListing: LokSetuListing) {
        val currentList = _listings.value.toMutableList()
        currentList.add(0, newListing) // सबसे नई एंट्री सबसे ऊपर
        _listings.value = currentList
    }

    // कैटेगरी के हिसाब से फिल्टर करने का फ़ंक्शन
    fun getListingsByCategory(role: UserRole): List<LokSetuListing> {
        return _listings.value.filter { it.category == role }
    }
}
