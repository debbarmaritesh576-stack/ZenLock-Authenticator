package com.aegis.pdf.ui.search  
  
import androidx.lifecycle.ViewModel  
import androidx.lifecycle.viewModelScope  
import com.aegis.pdf.data.repository.SearchRepository  
import com.aegis.pdf.data.repository.SearchResult  
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
    val searchResults: StateFlow<List<SearchResult>> = _searchQuery  
        .debounce(400) // 0.4 second wait karega typing rukne ka  
        .distinctUntilChanged() // Same query dobara nahi chalayega  
        .flatMapLatest { query ->  
            repository.searchUniversal(query)  
        }  
        .stateIn(  
            scope = viewModelScope,  
            started = SharingStarted.WhileSubscribed(5000),  
            initialValue = emptyList()  
        )  
  
    fun onQueryChange(newQuery: String) {  
        _searchQuery.value = newQuery  
    }  
}