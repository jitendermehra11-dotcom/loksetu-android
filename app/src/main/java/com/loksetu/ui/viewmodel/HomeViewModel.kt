package com.loksetu.ui.viewmodel

import com.loksetu.data.model.LokSetuListing
import com.loksetu.data.model.UserRole
import com.loksetu.data.repository.DatabaseRepository
import kotlinx.coroutines.flow.Flow

class HomeViewModel(private val repository: DatabaseRepository = DatabaseRepository()) {

    // लाइव लिस्टिंग डेटा Flow
    val allListings: Flow<List<LokSetuListing>> = repository.listings

    // नई एंट्री (किसान, मजदूर, ड्राइवर) जोड़ने का फंक्शन
    fun addNewListing(listing: LokSetuListing) {
        repository.addListing(listing)
    }

    // कैटेगरी के हिसाब से फ़िल्टर करने का फ़ंक्शन
    fun filterByCategory(role: UserRole): List<LokSetuListing> {
        return repository.getListingsByCategory(role)
    }
}
