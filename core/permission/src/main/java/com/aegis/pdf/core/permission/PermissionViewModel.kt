package com.aegis.pdf.core.permission  
  
import androidx.lifecycle.ViewModel  
import com.aegis.pdf.core.permission.PermissionHandler  
import dagger.hilt.android.lifecycle.HiltViewModel  
import kotlinx.coroutines.flow.MutableStateFlow  
import kotlinx.coroutines.flow.asStateFlow  
import javax.inject.Inject  
  
@HiltViewModel  
class PermissionViewModel @Inject constructor(  
    private val permissionHandler: PermissionHandler  
) : ViewModel() {  
  
    private val _permissionState = MutableStateFlow<PermissionState>(PermissionState.Checking)  
    val permissionState = _permissionState.asStateFlow()  
  
    fun checkStoragePermissions() {  
        if (permissionHandler.isStoragePermissionGranted()) {  
            _permissionState.value = PermissionState.Granted  
        } else {  
            _permissionState.value = PermissionState.Denied(  
                requiredPermissions = permissionHandler.getRequiredStoragePermissions()  
            )  
        }  
    }  
  
    fun onPermissionsResult(allGranted: Boolean) {  
        _permissionState.value = if (allGranted) {  
            PermissionState.Granted  
        } else {  
            PermissionState.PermanentlyDenied // Is state par user ko custom dialog dikhayenge  
        }  
    }  
}  
  
sealed interface PermissionState {  
    object Checking : PermissionState  
    object Granted : PermissionState  
    data class Denied(val requiredPermissions: List<String>) : PermissionState  
    object PermanentlyDenied : PermissionState  
}