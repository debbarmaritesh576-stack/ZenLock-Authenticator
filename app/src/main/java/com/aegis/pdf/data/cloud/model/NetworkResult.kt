package com.aegis.pdf.data.cloud.model  
  
/**  
 * A sealed class to represent the state of any Network/Cloud operation.  
 */  
sealed class NetworkResult<out T> {  
      
    // Initial state before any action  
    object Idle : NetworkResult<Nothing>()  
  
    // Loading state (show shimmers/progress)  
    object Loading : NetworkResult<Nothing>()  
  
    // Data fetched successfully  
    data class Success<out T>(val data: T) : NetworkResult<T>()  
  
    // Error state with message and optional status code  
    data class Error(  
        val message: String,   
        val code: Int? = null,  
        val exception: Throwable? = null  
    ) : NetworkResult<Nothing>()  
}