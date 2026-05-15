package com.aegis.pdf.ui.search  
  
import androidx.lifecycle.ViewModel  
import androidx.lifecycle.viewModelScope  
import com.aegis.pdf.data.repository.SearchRepository  
import dagger.hilt.android.lifecycle.HiltViewModel  
import kotlinx.coroutines.FlowPreview  
import kotlinx.coroutines.flow.*  
import javax.inject.Inject  
  
@HiltViewModel  
class SearchViewModel @Inject constructor(  
    private val repository: SearchRepository  
) : ViewModel() {  
  
    private val _searchQuery = MutableStateFlow("")  
    val searchQuery = _searchQuery.asStateFlow()  
  
    @OptIn(FlowPreview::class)  
    val searchResults = _searchQuery  
        .debounce(300) // User ke rukne ka wait karo (Efficiency)  
        .filter { it.length >= 2 } // Kam se kam 2 characters par search karo  
        .flatMapLatest { query ->  
            repository.searchAllFiles(query)  
        }  
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())  
  
    fun onQueryChange(query: String) {  
        _searchQuery.value = query  
    }  
}